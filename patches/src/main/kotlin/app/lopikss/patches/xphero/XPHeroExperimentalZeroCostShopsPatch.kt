package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch

/**
 * Opt-in experiment: zero all locally stored, non-IAP prices represented by
 * matching cost rows in the known XP Hero 26.2.0 asset.
 *
 * Mutually exclusive with the confirmed Exchange-only patch because both
 * modify the same SHA-guarded resource. Other assets and server checks remain.
 */
@Suppress("unused")
val xpHeroExperimentalZeroCostShopsPatch = rawResourcePatch(
    name = "XP Hero: Experimental zero-cost shops",
    description = "Experimental: zero more local shop prices across currencies in XP Hero 26.2.0. Do not enable with Exchange materials; IAPs excluded.",
    default = false,
) {
    compatibleWith(XP_HERO)

    execute {
        val file = get(ExchangeCostTransformer.ASSET_PATH)
        val updated = AllShopCostTransformer.transform(file.readBytes())
        file.writeBytes(updated)
    }
}
