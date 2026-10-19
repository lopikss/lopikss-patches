package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch

/** Experimental free gem ticket recharges and up to 400 daily refills for the
 * PvP, Boss Raid, Heroes' Tomb, and Challenge Tower settings in XP Hero 26.2.0. */
@Suppress("unused")
val xpHeroFreeTickets400 = rawResourcePatch(
    name = "XP Hero | Ticket Refills (400/day)",
    description = "Experimental: 400 refill limits confirmed in Heroes' Tomb, but some purchases still cost gems.",
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
