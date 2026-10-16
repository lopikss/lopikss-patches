package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch

/**
 * Zero-gem material offers in XP Hero 26.2.0's Exchange tab.
 * User-confirmed working on a device; other versions remain unsupported.
 * Does not modify paid IAPs, video ads or any other shop categories.
 */
@Suppress("unused")
val xpHeroExchangeMaterialsPatch = rawResourcePatch(
    name = "XP Hero: Zero-gem Exchange materials",
    description = "Set 24 gem-priced material exchanges to zero in XP Hero 26.2.0 (user-tested; IAPs and ad limits unaffected).",
    default = false,
) {
    compatibleWith(XP_HERO)

    execute {
        val file = get(ExchangeCostTransformer.ASSET_PATH)
        val updated = ExchangeCostTransformer.transform(file.readBytes())
        file.writeBytes(updated)
    }
}
