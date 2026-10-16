package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch

/**
 * Changes gem-cost material offers on the XP Hero 26.2.0 Exchange tab to zero.
 * Works on embedded static data, not Play Billing, not rewards or ad limits.
 * Runtime purchase behavior still requires on-device validation.
 */
@Suppress("unused")
val xpHeroExchangeMaterialsPatch = rawResourcePatch(
    name = "XP Hero: Zero-gem Exchange materials",
    description = "Experimentally set gem prices to 0 for upgrade/enhancement materials in the Exchange shop. Leaves paid IAPs and ad limits alone.",
    default = false,
) {
    compatibleWith(XP_HERO)

    execute {
        val file = get(ExchangeCostTransformer.ASSET_PATH)
        val updated = ExchangeCostTransformer.transform(file.readBytes())
        file.writeBytes(updated)
    }
}
