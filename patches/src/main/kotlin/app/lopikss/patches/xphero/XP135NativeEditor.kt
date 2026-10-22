package app.lopikss.patches.xphero

import java.security.MessageDigest

/**
 * XP Hero 26.2.0 ARM64 IL2CPP native edits.
 *
 * Independent stackable patches touch this library:
 * - zero-coin trait rerolls (user verified in v1.3.2)
 * - Heroes' Tomb refill price fallback (v1.3.5, experimental)
 * - free Return daily reset (v1.3.7, experimental)
 * - hero star upgrades (v1.3.8, experimental)
 * - zero-piece hero unlock confirmation (v1.3.9, experimental)
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
    // CostumeManager.TryGradeUp: the unmodified HasEnough(cost) check accepts
    // zero (inventory balance >= zero). The prior v1.3.8-1.3.10 changes
    // incorrectly bypassed that check, while leaving the next B.LT guard in
    // place; the zero-cost operation then SKIPPED TryConsume and its normal
    // transaction/notification path. The user's upgrade persisted but the
    // upgrade UI stayed locked until restarting the game.
    //
    // Keep the original HasEnough result branch at VA 0x02847EA0; NOP ONLY
    // the B.LT skip of the zero-quantity consumer at VA 0x02847EA8. Thus the
    // real TryConsume(0) call at 0x02847EBC runs and continues through the
    // ordinary grade-up notifications. This does not deduct shards.
    private const val STAR_UPGRADE_OFFSET = 0x2843EA0
    private val ORIGINAL_STAR_UPGRADE = byteArrayOf(0xA0.toByte(), 0x1A, 0x00, 0x36)
    private val LEGACY_MOV_STAR_UPGRADE = byteArrayOf(0x20, 0x00, 0x80.toByte(), 0x52) // v1.3.10
    private val LEGACY_NOP_STAR_UPGRADE = byteArrayOf(0x1F, 0x20, 0x03, 0xD5.toByte()) // v1.3.8-9
    private val STAR_UPGRADE_SPEND_CMP = byteArrayOf(0xDF.toByte(), 0x06, 0x00, 0xF1.toByte())
    private const val STAR_ZERO_SPEND_GUARD_OFFSET = STAR_UPGRADE_OFFSET + 8
    private val ORIGINAL_STAR_ZERO_SPEND_GUARD = byteArrayOf(0x2B, 0x01, 0x00, 0x54)
    private val FREE_STAR_ZERO_SPEND_GUARD = byteArrayOf(0x1F, 0x20, 0x03, 0xD5.toByte()) // NOP
    // CostumeController.TryGradeUpCostume calls TryConsumeCostumePiece.
    // The original <= 0 guard at VA 0x02852E64 diverts zero-amount spend
    // away from its inventory transaction and change notifications. The old
    // v1.3.9-v1.3.11 shortcut forced success without those notifications.
    // New B.LT condition rejects only negative amounts and passes 0 into
    // the lower consumer at VA 0x02854ED4. Disassembly confirms that
    // lower consumer accepts zero (balance - 0 >= 0) and emits the normal
    // update/notification, without deducting a shard.
    private const val HERO_UNLOCK_CONSUME_OFFSET = 0x2852e64 - 0x4000
    private val ORIGINAL_HERO_UNLOCK = byteArrayOf(0x4D, 0x09, 0x00, 0x54)
    private val LEGACY_ZERO_HERO_UNLOCK = byteArrayOf(0x0D, 0x09, 0x00, 0x54) // v1.3.9-1.3.11: skip notification path
    // B.LE -> B.LT: allow zero amount through ordinary consume/notification
    // code, but still reject invalid negative amounts.
    private val ZERO_HERO_UNLOCK = byteArrayOf(0x4B, 0x09, 0x00, 0x54)
    private val HERO_UNLOCK_COMPARE = byteArrayOf(0x7F, 0x02, 0x00, 0xF1.toByte())
    private val HERO_UNLOCK_SUCCESS = byteArrayOf(0x20, 0x00, 0x80.toByte(), 0x52)


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

    // HomeReturnPortalPassController daily-reset handler (VA 0x024C4C48).
    // Before v1.3.11, the new free-ticket ResetDailyTickets call only ran
    // when a PAID pass existed AND was active. For accounts without a pass,
    // these two branches skipped free ticket delivery entirely. Redirect
    // only the null/inactive-pass exits to the EXISTING ResetDailyTickets
    // invocation at VA 0x024C4CF0. Active paid-pass handling stays intact.
    // Original "cbz x0, 0x24C4D64" -> "cbz x0, 0x24C4CF0".
    private const val RETURN_NO_PASS_OFFSET = 0x24C4CD0 - 0x4000
    private val ORIGINAL_RETURN_NO_PASS = byteArrayOf(0xA0.toByte(), 0x04, 0x00, 0xB4.toByte())
    private val FREE_RETURN_NO_PASS = byteArrayOf(0x00, 0x01, 0x00, 0xB4.toByte())
    // Original "tbz w0,#0, 0x24C4D64" -> "tbz w0,#0, 0x24C4CF0".
    private const val RETURN_INACTIVE_PASS_OFFSET = 0x24C4CDC - 0x4000
    private val ORIGINAL_RETURN_INACTIVE_PASS = byteArrayOf(0x40, 0x04, 0x00, 0x36)
    private val FREE_RETURN_INACTIVE_PASS = byteArrayOf(0xA0.toByte(), 0x00, 0x00, 0x36)

    // GetFreeTicketCount (VA 0x024C237C) is used by the actual field Return
    // action. Daily resets cannot be relied on to give the user tickets NOW.
    // Return a configured virtual free-ticket allowance immediately instead
    // of waiting for the daily-reset callback. No paid item 7001 is touched.
    // At the free-ticket consumption call, allow the Return completion path
    // when the underlying item 7002 inventory is still zero.
    // These do NOT grant 5000 persisted items or change paid purchase offers.
    private const val INSTANT_RETURN_COUNT_OFFSET = 0x24C237C - 0x4000
    private val ORIGINAL_RETURN_COUNT_ENTRY = byteArrayOf(
        0xFE.toByte(), 0x0F, 0x1E, 0xF8.toByte(),
        0xF4.toByte(), 0x4F, 0x01, 0xA9.toByte())
    private val INSTANT_RETURN_COUNT_RET = byteArrayOf(0xC0.toByte(), 0x03, 0x5F, 0xD6.toByte())
    private const val RETURN_FREE_CONSUME_GUARD_OFFSET = 0x24C6458 - 0x4000
    private val ORIGINAL_RETURN_FREE_CONSUME_GUARD = byteArrayOf(
        0xE0.toByte(), 0x06, 0x00, 0x36)
    private val INSTANT_RETURN_FREE_CONSUME_GUARD = byteArrayOf(
        0x1F, 0x20, 0x03, 0xD5.toByte()) // ARM64 NOP


    private fun instruction(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 255) or
            ((bytes[offset + 1].toInt() and 255) shl 8) or
            ((bytes[offset + 2].toInt() and 255) shl 16) or
            ((bytes[offset + 3].toInt() and 255) shl 24)

    private fun replaceInstruction(bytes: ByteArray, offset: Int, code: Int) {
        for (i in 0..3) bytes[offset + i] = (code ushr (i * 8)).toByte()
    }

    private fun knownInstantReturnCount(bytes: ByteArray): Boolean {
        val word = instruction(bytes, INSTANT_RETURN_COUNT_OFFSET)
        return (word and 0xFFE0001F.toInt()) == 0x52800000 &&
            ((word ushr 5) and 0xFFFF) in 10..5000 &&
            equalAt(bytes, INSTANT_RETURN_COUNT_OFFSET + 4, INSTANT_RETURN_COUNT_RET)
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
        require(equalAt(input, STAR_UPGRADE_OFFSET + 4, STAR_UPGRADE_SPEND_CMP)) {
            "The zero-shard spending comparison differs; refusing hero upgrade edit"
        }
        require(equalAt(input, HERO_UNLOCK_CONSUME_OFFSET - 4, HERO_UNLOCK_COMPARE) &&
                equalAt(input, HERO_UNLOCK_CONSUME_OFFSET + 0x120, HERO_UNLOCK_SUCCESS)) {
            "Different XP Hero zero-piece unlock function; refusing patch"
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
        require(equalAt(input, INSTANT_RETURN_COUNT_OFFSET, ORIGINAL_RETURN_COUNT_ENTRY) ||
            knownInstantReturnCount(input)) {
            "Unexpected XP Hero free Return count reader"
        }
        ORIGINAL_RETURN_COUNT_ENTRY.copyInto(normalized, INSTANT_RETURN_COUNT_OFFSET)
        require(equalAt(input, RETURN_FREE_CONSUME_GUARD_OFFSET, ORIGINAL_RETURN_FREE_CONSUME_GUARD) ||
            equalAt(input, RETURN_FREE_CONSUME_GUARD_OFFSET, INSTANT_RETURN_FREE_CONSUME_GUARD)) {
            "Unexpected XP Hero free Return consume result branch"
        }
        ORIGINAL_RETURN_FREE_CONSUME_GUARD.copyInto(normalized, RETURN_FREE_CONSUME_GUARD_OFFSET)
        for ((offset, original, changed) in listOf(
            Triple(RETURN_NO_PASS_OFFSET, ORIGINAL_RETURN_NO_PASS, FREE_RETURN_NO_PASS),
            Triple(RETURN_INACTIVE_PASS_OFFSET, ORIGINAL_RETURN_INACTIVE_PASS, FREE_RETURN_INACTIVE_PASS)
        )) {
            require(equalAt(input, offset, original) || equalAt(input, offset, changed)) {
                "Unexpected Return daily-reset pass gate"
            }
            original.copyInto(normalized, offset)
        }

        for ((offset, original, changed) in listOf(
            Triple(TRAIT_OFFSET, ORIGINAL_TRAIT, FREE_TRAIT),
            Triple(TOMB_OFFSET, ORIGINAL_TOMB, FREE_TOMB)
        )) {
            require(equalAt(input, offset, original) || equalAt(input, offset, changed)) {
                "Unknown XP Hero native instructions"
            }
            original.copyInto(normalized, offset)
        }
        // Both the old shortcut to success and the newer B.LT fall-through
        // must be accepted and restored for the exact original SHA check.
        require(equalAt(input, HERO_UNLOCK_CONSUME_OFFSET, ORIGINAL_HERO_UNLOCK) ||
            equalAt(input, HERO_UNLOCK_CONSUME_OFFSET, LEGACY_ZERO_HERO_UNLOCK) ||
            equalAt(input, HERO_UNLOCK_CONSUME_OFFSET, ZERO_HERO_UNLOCK)) {
            "Unknown XP Hero zero-piece consume instruction"
        }
        ORIGINAL_HERO_UNLOCK.copyInto(normalized, HERO_UNLOCK_CONSUME_OFFSET)

        // Normalize all previous released star-upgrade byte patterns, plus
        // the new zero-cost Spend guard, before the original-library SHA check.
        require(equalAt(input, STAR_UPGRADE_OFFSET, ORIGINAL_STAR_UPGRADE) ||
            equalAt(input, STAR_UPGRADE_OFFSET, LEGACY_MOV_STAR_UPGRADE) ||
            equalAt(input, STAR_UPGRADE_OFFSET, LEGACY_NOP_STAR_UPGRADE)) {
            "Unknown XP Hero star upgrade approval branch"
        }
        require(equalAt(input, STAR_ZERO_SPEND_GUARD_OFFSET, ORIGINAL_STAR_ZERO_SPEND_GUARD) ||
            equalAt(input, STAR_ZERO_SPEND_GUARD_OFFSET, FREE_STAR_ZERO_SPEND_GUARD)) {
            "Unknown XP Hero star upgrade spend branch"
        }
        ORIGINAL_STAR_UPGRADE.copyInto(normalized, STAR_UPGRADE_OFFSET)
        ORIGINAL_STAR_ZERO_SPEND_GUARD.copyInto(normalized, STAR_ZERO_SPEND_GUARD_OFFSET)
        val sha = MessageDigest.getInstance("SHA-256").digest(normalized)
            .joinToString("") { "%02x".format(it) }
        require(sha == ORIGINAL_SHA) { "Unexpected XP Hero ARM64 code; refusing patch" }
    }

    /**
     * Let the normal hero-unlock purchase flow treat a zero-piece
     * consumption as success, without changing positive piece spending.
     */
    fun allowZeroPieceHeroUnlock(input: ByteArray): ByteArray {
        verify(input)
        return input.copyOf().also {
            ZERO_HERO_UNLOCK.copyInto(it, HERO_UNLOCK_CONSUME_OFFSET)
        }
    }

    /** Version-locked zero-shard star upgrade fix, compatible with native patches. */
    fun freeHeroStarUpgrades(input: ByteArray): ByteArray {
        verify(input)
        return input.copyOf().also {
            // Character detail upgrades call CostumeController.TryGradeUpCostume,
            // not the static grade-up routine patched in v1.3.8-1.3.11.
            // Restore static routine to original and fix the shared real
            // costume-piece consumption helper instead.
            ORIGINAL_STAR_UPGRADE.copyInto(it, STAR_UPGRADE_OFFSET)
            ORIGINAL_STAR_ZERO_SPEND_GUARD.copyInto(it, STAR_ZERO_SPEND_GUARD_OFFSET)
            ZERO_HERO_UNLOCK.copyInto(it, HERO_UNLOCK_CONSUME_OFFSET)
        }
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
            FREE_RETURN_NO_PASS.copyInto(it, RETURN_NO_PASS_OFFSET)
            FREE_RETURN_INACTIVE_PASS.copyInto(it, RETURN_INACTIVE_PASS_OFFSET)
            replaceInstruction(it, INSTANT_RETURN_COUNT_OFFSET,
                0x52800000 or (freeReturnDaily shl 5)) // MOV W0,#N
            INSTANT_RETURN_COUNT_RET.copyInto(it, INSTANT_RETURN_COUNT_OFFSET + 4)
            INSTANT_RETURN_FREE_CONSUME_GUARD.copyInto(it, RETURN_FREE_CONSUME_GUARD_OFFSET)
        }
    }
}
