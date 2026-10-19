package app.lopikss.patches.xphero

import java.security.MessageDigest

/**
 * XP Hero 26.2.0 ARM64 IL2CPP native edits.
 *
 * Two independent patches touch this library:
 * - zero-coin trait rerolls (user verified in v1.3.2)
 * - Heroes' Tomb refill price fallback (v1.3.5, experimental)
 *
 * Normalize ONLY these exact known changes before checking the complete
 * original library SHA-256. This permits either patch order and prevents
 * writing into a different game build.
 */
internal object XP135NativeEditor {
    const val ARM64 = "lib/arm64-v8a/libil2cpp.so"
    private const val ORIGINAL_SHA =
        "88b591e6099c55b1147b865847a14651b108f9bd6837707371766a054c8df443"
    private const val EXPECTED_SIZE = 83_724_712

    // CalculateRollCost ARM64 method entry, user-tested free trait rerolls.
    private const val TRAIT_OFFSET = 0x2686684
    private val ORIGINAL_TRAIT = byteArrayOf(
        0xFE.toByte(), 0x0F, 0x1D, 0xF8.toByte(), 0xF6.toByte(), 0x57, 0x01, 0xA9.toByte()
    )
    private val FREE_TRAIT = byteArrayOf(
        0x00, 0x00, 0x80.toByte(), 0x52, 0xC0.toByte(), 0x03, 0x5F, 0xD6.toByte()
    )

    // HeroTombTicketController.GetChargeCost at VA 0x024CD2CC.
    // Original: cmp w8,#0; mov w9,#100; csel w0,w8,w9,gt
    // Patched:  cmp w8,#0; mov w9,#100; mov w0,w8
    // Returns the configured zero instead of substituting 100 gems.
    private const val TOMB_OFFSET = 0x24C92CC
    private val ORIGINAL_TOMB = byteArrayOf(0x00, 0xC1.toByte(), 0x89.toByte(), 0x1A)
    private val FREE_TOMB = byteArrayOf(0xE0.toByte(), 0x03, 0x08, 0x2A)
    private val TOMB_NEIGHBORS = byteArrayOf(
        0x1F, 0x01, 0x00, 0x71, // cmp w8,#0
        0x89.toByte(), 0x0C, 0x80.toByte(), 0x52 // mov w9,#100
    )

    private fun equalAt(bytes: ByteArray, offset: Int, pattern: ByteArray): Boolean =
        bytes.copyOfRange(offset, offset + pattern.size).contentEquals(pattern)

    private fun verify(input: ByteArray) {
        require(input.size == EXPECTED_SIZE) { "Unexpected XP Hero IL2CPP binary size" }
        require(equalAt(input, TOMB_OFFSET - 8, TOMB_NEIGHBORS)) {
            "Different Heroes' Tomb price calculation; refusing patch"
        }
        val normalized = input.copyOf()
        for ((offset, original, changed) in listOf(
            Triple(TRAIT_OFFSET, ORIGINAL_TRAIT, FREE_TRAIT),
            Triple(TOMB_OFFSET, ORIGINAL_TOMB, FREE_TOMB)
        )) {
            require(equalAt(input, offset, original) || equalAt(input, offset, changed)) {
                "Unknown XP Hero native instructions"
            }
            original.copyInto(normalized, offset)
        }
        val sha = MessageDigest.getInstance("SHA-256").digest(normalized)
            .joinToString("") { "%02x".format(it) }
        require(sha == ORIGINAL_SHA) { "Unexpected XP Hero ARM64 code; refusing patch" }
    }

    fun freeTraitRerolls(input: ByteArray): ByteArray {
        verify(input)
        return input.copyOf().also { FREE_TRAIT.copyInto(it, TRAIT_OFFSET) }
    }

    fun freeHeroesTombRefills(input: ByteArray): ByteArray {
        verify(input)
        return input.copyOf().also { FREE_TOMB.copyInto(it, TOMB_OFFSET) }
    }
}
