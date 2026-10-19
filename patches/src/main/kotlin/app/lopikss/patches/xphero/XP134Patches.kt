package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch

/** Experimental free gem ticket recharges and up to 400 daily refills for the
 * PvP, Boss Raid, Heroes' Tomb, and Challenge Tower settings in XP Hero 26.2.0. */
@Suppress("unused")
val xpHeroFreeTickets400 = rawResourcePatch(
    name = "XP Hero 1.3: Free Ticket Refills (400/day)",
    description = "Experimental: 0-gem ticket recharges plus 400 daily refills in PvP, Boss Raid, Heroes' Tomb and Challenge Tower.",
    default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val remote = get(XP134TicketEditor.REMOTE)
        val pvp = get(XP134TicketEditor.PVP)
        val (newRemote, newPvp) = XP134TicketEditor.freeTicketRecharge(remote.readBytes(), pvp.readBytes())
        remote.writeBytes(newRemote)
        pvp.writeBytes(newPvp)
    }
}
