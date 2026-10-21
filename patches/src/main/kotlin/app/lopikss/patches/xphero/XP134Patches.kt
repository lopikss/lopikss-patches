package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.intOption

/** Experimental free gem ticket recharges and up to 400 daily refills for the
 * PvP, Boss Raid, Heroes' Tomb, and Challenge Tower settings in XP Hero 26.2.0. */
@Suppress("unused")
val xpHeroFreeTickets400 = rawResourcePatch(
    name = "XP Hero | Free Tickets & Return",
    description = "Configurable free Return tickets at daily reset, daily refill limits and zero-gem refills; never changes paid IAP.",
    default = false,
) {
    compatibleWith(XP_HERO)

    val dailyRefills = intOption(
        key = "dailyTicketRefills",
        default = 400,
        title = "Daily ticket refill limit",
        description = "Set the configured daily cap for PvP, Boss Raid, Heroes' Tomb and Challenge Tower. Range: 1–5000.",
        required = true,
        validator = { it != null && it in 1..5000 },
    )
    val returnDailyTickets = intOption(
        key = "returnDailyTickets",
        default = 5000,
        title = "Free Return tickets per day",
        description = "At the daily reset, allocate this many free Return tickets (not paid-pass tickets). The €5.99 offer remains unchanged.",
        required = true,
        validator = { it != null && it in 10..5000 },
    )
    val freeReturns = intOption(
        key = "freeReturnAllowance",
        default = 5000,
        title = "Initial free Return allowance",
        description = "Experimental: increase the game's initial free-return setting from 3. May be limited by online checks.",
        required = true,
        validator = { it != null && it in 3..5000 },
    )

    execute {
        val remote = get(XP134TicketEditor.REMOTE)
        val pvp = get(XP134TicketEditor.PVP)
        val native = get(XP135NativeEditor.ARM64)
        // Check all three assets before writing any file.
        val (newRemote, newPvp) = XP134TicketEditor.freeTicketRecharge(
            remote.readBytes(), pvp.readBytes(),
            dailyRefills.value ?: 400, returnDailyTickets.value ?: 5000,
            freeReturns.value ?: 5000,
        )
        val newNative = XP135NativeEditor.freeHeroesTombRefills(
            native.readBytes(), returnDailyTickets.value ?: 5000)
        remote.writeBytes(newRemote)
        pvp.writeBytes(newPvp)
        native.writeBytes(newNative)
    }
}
