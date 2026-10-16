package app.lopikss.patches.xphero

import java.security.MessageDigest

/**
 * Targeted, size-preserving edit to XP Hero 26.2.0's serialized TextAsset cost table.
 *
 * The raw Unity asset contains a JSON array as a payload. The game's serializer stores
 * the payload length in its binary header, so replacing digits with the same number
 * of ASCII zeroes avoids changing any Unity object offsets or length fields.
 *
 * Only the Gem (stackable item 3) cost rows associated with the upgrade and
 * enhancement material Exchange products are changed. Ads/free rewards and the
 * cash/IAP cost rows are deliberately left untouched.
 *
 * This is a client-side experiment, not evidence that reward redemption succeeds.
 */
internal object ExchangeCostTransformer {
    const val ASSET_PATH = "assets/bin/Data/c48bf6e962b374c2d829c84217571712"

    private const val ORIGINAL_SHA256 =
        "4e36ede85e3927f9f98ad3e3ae28a5e3362f1676da4c95e1ed33f6df263e26ac"

    // Matched via ShopProductData (category_id 6=EnhancementMat, 7=WeaponMat)
    // and ShopProductCostData in the unmodified XP Hero v26.2.0 APK.
    // Key = cost_group_id; value = expected gem price.
    private val gemCostByGroup = mapOf(
        1011 to 50, 1013 to 100, 1014 to 500,
        1021 to 50, 1023 to 100, 1024 to 500,
        1031 to 50, 1033 to 100, 1034 to 500,
        1041 to 50, 1043 to 100, 1044 to 500,
        1051 to 50, 1053 to 100, 1054 to 500,
        1061 to 50, 1063 to 100, 1064 to 500,
        1071 to 50, 1073 to 100, 1074 to 500,
        1081 to 100, 1083 to 200, 1084 to 1000,
    )

    // JSON field order is stable in this particular Unity TextAsset. Do not match
    // and overwrite every occurrence of a price such as "100" in unrelated products.
    private val costRow = Regex(
        """\{"cost_param":"(\d+)","cost_type":"(\d+)","cost_value":"(\d+)","group_id":"(\d+)","id":"(\d+)","max_purchase_count":"(-?\d+)","org_price_iap":"(\d+)","priority":"(\d+)"\}"""
    )
    private val costValue = Regex(""""cost_value":"\d+"""")

    fun transform(original: ByteArray): ByteArray {
        val actualHash = MessageDigest.getInstance("SHA-256")
            .digest(original)
            .joinToString("") { "%02x".format(it) }
        require(actualHash == ORIGINAL_SHA256) {
            "Unsupported XP Hero shop data (SHA-256: ${actualHash}); refusing to modify it."
        }

        val text = String(original, Charsets.ISO_8859_1)
        val changedGroups = mutableSetOf<Int>()
        val updated = costRow.replace(text) { match ->
            val costParam = match.groupValues[1].toInt()
            val costType = match.groupValues[2].toInt()
            val existingValue = match.groupValues[3].toInt()
            val group = match.groupValues[4].toInt()
            val expectedValue = gemCostByGroup[group]

            if (expectedValue == null || costParam != 3 || costType != 1) {
                match.value
            } else {
                check(existingValue == expectedValue) {
                    "Unexpected Exchange cost for group ${group}: ${existingValue} (expected ${expectedValue})"
                }
                check(changedGroups.add(group)) { "Duplicate Exchange cost group ${group}" }
                val replacementValue = "0".repeat(match.groupValues[3].length)
                match.value.replace(costValue, "\"cost_value\":\"${replacementValue}\"")
            }
        }

        check(changedGroups == gemCostByGroup.keys) {
            "Exchange cost table incomplete: expected ${gemCostByGroup.size}, found ${changedGroups.size}; " +
                "missing ${gemCostByGroup.keys - changedGroups}"
        }
        val output = updated.toByteArray(Charsets.ISO_8859_1)
        check(output.size == original.size) { "Unity asset length changed unexpectedly" }
        return output
    }
}
