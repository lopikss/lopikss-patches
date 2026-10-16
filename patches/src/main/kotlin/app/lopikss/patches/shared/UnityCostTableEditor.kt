package app.lopikss.patches.shared

import java.security.MessageDigest

/**
 * Reusable guarded editor for one *specific* Unity embedded JSON cost-table format.
 *
 * This is not a universal game patch: each supported app/version still needs a
 * known asset path, verified SHA-256 and an explicit set of expected price rows.
 * No game assets or generated patch binaries are stored in this repository.
 */
internal object UnityCostTableEditor {
    data class PriceRule(val groupId: Int, val expected: Int, val replacement: Int = 0)

    private val row = Regex(
        """\{"cost_param":"(\d+)","cost_type":"(\d+)","cost_value":"(\d+)","group_id":"(\d+)","id":"(\d+)","max_purchase_count":"(-?\d+)","org_price_iap":"(\d+)","priority":"(\d+)"\}"""
    )
    private val costValueField = Regex(""""cost_value":"\d+"""")

    fun rewrite(
        original: ByteArray,
        expectedSha256: String,
        currencyParam: Int,
        costType: Int,
        rules: List<PriceRule>,
    ): ByteArray {
        require(rules.isNotEmpty()) { "At least one cost rule is required" }
        require(rules.all { it.groupId >= 0 && it.expected >= 0 && it.replacement >= 0 }) {
            "Group IDs and cost values must be nonnegative"
        }
        val byGroup = rules.associateBy { it.groupId }
        require(byGroup.size == rules.size) { "Duplicate cost group in patch configuration" }

        val actualSha256 = MessageDigest.getInstance("SHA-256")
            .digest(original)
            .joinToString("") { "%02x".format(it) }
        require(actualSha256.equals(expectedSha256, ignoreCase = true)) {
            "Unsupported game asset (SHA-256: $actualSha256); refusing to edit"
        }

        // ISO-8859-1 is a lossless byte-to-character mapping. The substitution is
        // the same number of ASCII bytes, preserving Unity's binary offsets.
        val text = String(original, Charsets.ISO_8859_1)
        val changed = mutableSetOf<Int>()
        val rewritten = row.replace(text) { match ->
            val param = match.groupValues[1].toInt()
            val type = match.groupValues[2].toInt()
            val priceString = match.groupValues[3]
            val group = match.groupValues[4].toInt()
            val rule = byGroup[group]
            if (rule == null || param != currencyParam || type != costType) {
                match.value
            } else {
                check(priceString.toInt() == rule.expected) {
                    "Unexpected price in group $group: $priceString (expected ${rule.expected})"
                }
                check(changed.add(group)) { "Duplicate cost row for group $group" }
                val newPrice = rule.replacement.toString()
                check(newPrice.length <= priceString.length) {
                    "Replacement price for group $group exceeds original field width"
                }
                val fixedWidthPrice = newPrice.padStart(priceString.length, '0')
                match.value.replace(costValueField, "\"cost_value\":\"$fixedWidthPrice\"")
            }
        }
        check(changed == byGroup.keys) {
            "Cost rules did not all match; missing groups: ${byGroup.keys - changed}"
        }
        val result = rewritten.toByteArray(Charsets.ISO_8859_1)
        check(result.size == original.size) { "Unity asset byte count changed" }
        return result
    }

    /**
     * Experimental, wider XP Hero cost-table rewrite.
     *
     * Rewrites positive local prices in rows matching the known ShopProductCostData
     * shape, across currency types. It excludes rows with an IAP original price.
     * It cannot alter prices in other assets or server-authoritative purchases.
     *
     * This deliberately requires all independently confirmed Exchange baseline
     * rows, plus at least one additional priced row, before writing anything.
     */
    fun rewriteAllNonIapStorePrices(
        original: ByteArray,
        expectedSha256: String,
        baselineCurrencyParam: Int,
        baselineCostType: Int,
        baselineRules: List<PriceRule>,
    ): ByteArray {
        require(baselineRules.isNotEmpty()) { "A known baseline is required" }
        val byGroup = baselineRules.associateBy { it.groupId }
        require(byGroup.size == baselineRules.size) { "Duplicate baseline group" }

        val actualSha256 = MessageDigest.getInstance("SHA-256")
            .digest(original)
            .joinToString("") { "%02x".format(it) }
        require(actualSha256.equals(expectedSha256, ignoreCase = true)) {
            "Unsupported game asset (SHA-256: $actualSha256); refusing to edit"
        }

        val text = String(original, Charsets.ISO_8859_1)
        val confirmedGroups = mutableSetOf<Int>()
        var extraModified = 0
        val rewritten = row.replace(text) { match ->
            val param = match.groupValues[1].toInt()
            val type = match.groupValues[2].toInt()
            val price = match.groupValues[3]
            val group = match.groupValues[4].toInt()
            val iapOriginalPrice = match.groupValues[7]
            val expectedBaseline = byGroup[group]

            if (expectedBaseline != null) {
                check(param == baselineCurrencyParam && type == baselineCostType) {
                    "Baseline group $group has a different currency or cost type"
                }
                check(price.toInt() == expectedBaseline.expected) {
                    "Unexpected baseline price in group $group"
                }
                check(iapOriginalPrice.all { it == '0' }) {
                    "Baseline group $group unexpectedly has an IAP price"
                }
                check(confirmedGroups.add(group)) {
                    "Duplicate baseline cost row for group $group"
                }
            }

            // Never modify entries associated with real-money IAPs, or prices
            // that were already zero. Fixed-width changes preserve asset size.
            if (iapOriginalPrice.any { it != '0' } || price.all { it == '0' }) {
                match.value
            } else {
                if (expectedBaseline == null) extraModified++
                val zero = "0".padStart(price.length, '0')
                match.value.replace(costValueField, "\"cost_value\":\"$zero\"")
            }
        }

        check(confirmedGroups == byGroup.keys) {
            "Missing verified baseline groups: ${byGroup.keys - confirmedGroups}"
        }
        check(extraModified > 0) {
            "No additional priced rows found in this asset; refusing a no-op expansion"
        }
        val result = rewritten.toByteArray(Charsets.ISO_8859_1)
        check(result.size == original.size) { "Unity asset byte count changed" }
        return result
    }
}
