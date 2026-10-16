package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch
import java.security.MessageDigest

/** XP Hero 26.2.0: isolated, version-pinned S-ticket cost experiment. */
private object XpHeroSTicketCost {
    const val CONFIG = "assets/bin/Data/22fc19c3e5dc14f5ca465a3e6cd62dc3"
    private const val EXPECTED_SHA256 = "c2f1df06c02d8723b569c532457eced4baee00f1c0bbb6a139bdcb3be801bfb1"
    private const val FROM = "\"key\":\"TRAIT_SUPERROLL_COST_AMOUNT\",\"value\":\"1\""
    private const val TO = "\"key\":\"TRAIT_SUPERROLL_COST_AMOUNT\",\"value\":\"0\""

    fun makeFree(original: ByteArray): ByteArray {
        require(original.size == 17_816) { "Unsupported XP Hero config file size" }
        val digest = MessageDigest.getInstance("SHA-256").digest(original)
            .joinToString("") { "%02x".format(it) }
        require(digest == EXPECTED_SHA256) { "Unsupported XP Hero 26.2.0 game config" }
        val source = String(original, Charsets.ISO_8859_1)
        val start = source.indexOf(FROM)
        check(start >= 0 && start == source.lastIndexOf(FROM)) {
            "Expected exactly one S-ticket cost of 1"
        }
        val updated = source.replace(FROM, TO).toByteArray(Charsets.ISO_8859_1)
        check(updated.size == original.size) { "S-ticket asset size changed" }
        return updated
    }
}

/**
 * User-tested trait rerolls use a different price path. This module targets
 * the ONE S-ticket spent by TraitSpecialTicketView for individual S-rolls.
 * A server-side purchase check, if present, may reject 0; test in game.
 */
@Suppress("unused")
val xpHeroFreeSTicketRolls = rawResourcePatch(
    name = "XP Hero 1.3: Free S-Ticket Rolls",
    description = "Experimental: make S-Ticket rolls cost 0 by changing TRAIT_SUPERROLL_COST_AMOUNT from 1 to 0. In-game test needed.",
    default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XpHeroSTicketCost.CONFIG)
        file.writeBytes(XpHeroSTicketCost.makeFree(file.readBytes()))
    }
}
