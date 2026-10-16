package app.lopikss.patches.xphero

import app.lopikss.patches.shared.UnityCostTableEditor

/**
 * EXPERIMENTAL: this only expands the exact, already-verified Unity asset.
 * It does not promise to cover every shop screen or any server-side transaction.
 */
internal object AllShopCostTransformer {
    fun transform(original: ByteArray): ByteArray =
        UnityCostTableEditor.rewriteAllNonIapStorePrices(
            original = original,
            expectedSha256 = ExchangeCostTransformer.ORIGINAL_SHA256,
            baselineCurrencyParam = 3,
            baselineCostType = 1,
            baselineRules = ExchangeCostTransformer.priceRules,
        )
}
