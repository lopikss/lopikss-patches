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

    // HomeReturnPortalPassController.ResetDailyTickets originally sets
    // Stackable_HomeReturnPortalTicket (paid, item 7001) to the advertised
    // 30-day pass quantity. The Return-use path accepts item 7002, the
    // independent Stackable_HomeReturnPortalFreeTicket. The fix directs this
    // daily reset to 7002, with a configurable quantity, without changing
    // or activating any paid in-app purchase.
    private const val RETURN_QUANTITY_OFFSET = 0x24C0DC0
    private const val RETURN_ITEM_OFFSET = 0x24C0DD8
    private val ORIGINAL_RETURN_QUANTITY = byteArrayOf(0x73, 0x42, 0x80.toByte(), 0xB9.toByte())
    private val ORIGINAL_RETURN_ITEM = byteArrayOf(0x20, 0x6B, 0x83.toByte(), 0x52)
    private val FREE_RETURN_ITEM = byteArrayOf(0x40, 0x6B, 0x83.toByte(), 0x52)

    private fun instruction(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 255) or
            ((bytes[offset + 1].toInt() and 255) shl 8) or
            ((bytes[offset + 2].toInt() and 255) shl 16) or
            ((bytes[offset + 3].toInt() and 255) shl 24)

    private fun replaceInstruction(bytes: ByteArray, offset: Int, code: Int) {
        for (i in 0..3) bytes[offset + i] = (code ushr (i * 8)).toByte()
    }

    private fun knownFreeReturnQuantity(bytes: ByteArray): Boolean {
        val code = instruction(bytes, RETURN_QUANTITY_OFFSET)
        // ARM64 MOVZ x19,#imm16, no shift; only accept 10..5000.
        return (code and 0xFFE0001F.toInt()) == 0xD2800013.toInt() &&
            ((code ushr 5) and 0xffff) in 10..5000
    }

    private fun equalAt(bytes: ByteArray, offset: Int, pattern: ByteArray): Boolean =
        bytes.copyOfRange(offset, offset + pattern.size).contentEquals(pattern)

    private fun verify(input: ByteArray) {
        require(input.size == EXPECTED_SIZE) { "Unexpected XP Hero IL2CPP binary size" }
        require(equalAt(input, TOMB_OFFSET - 8, TOMB_NEIGHBORS)) {
            "Different Heroes' Tomb price calculation; refusing patch"
        }
        val normalized = input.copyOf()
        require(equalAt(input, RETURN_QUANTITY_OFFSET, ORIGINAL_RETURN_QUANTITY) ||
            knownFreeReturnQuantity(input)) {
            "Unexpected Return ticket daily-reset quantity instruction"
        }
        require(equalAt(input, RETURN_ITEM_OFFSET, ORIGINAL_RETURN_ITEM) ||
            equalAt(input, RETURN_ITEM_OFFSET, FREE_RETURN_ITEM)) {
            "Unexpected Return ticket daily-reset item identifier"
        }
        ORIGINAL_RETURN_QUANTITY.copyInto(normalized, RETURN_QUANTITY_OFFSET)
        ORIGINAL_RETURN_ITEM.copyInto(normalized, RETURN_ITEM_OFFSET)
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

    fun freeHeroesTombRefills(input: ByteArray, freeReturnDaily: Int): ByteArray {
        require(freeReturnDaily in 10..5000) { "Invalid daily free Return ticket count" }
        verify(input)
        return input.copyOf().also {
            FREE_TOMB.copyInto(it, TOMB_OFFSET)
            // MOVZ x19,#freeReturnDaily — count passed to stackable setter.
            replaceInstruction(it, RETURN_QUANTITY_OFFSET,
                0xD2800013.toInt() or (freeReturnDaily shl 5))
            FREE_RETURN_ITEM.copyInto(it, RETURN_ITEM_OFFSET)
        }
    }
}
