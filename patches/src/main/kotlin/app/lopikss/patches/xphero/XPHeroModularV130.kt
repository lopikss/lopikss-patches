package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch
import java.security.MessageDigest

/** Experimental, version-pinned modules for XP Hero 26.2.0. */
private object XP130 {
    const val COST = "assets/bin/Data/c48bf6e962b374c2d829c84217571712"
    const val PRODUCTS = "assets/bin/Data/9724a45587522461cb852200a35b8362"
    const val HERO_LEVEL = "assets/bin/Data/00199bb5af76d48728e982d6a69b40eb"
    const val HERO_STAR = "assets/bin/Data/02ce5273e9c794059afe2ada0670d172"
    const val HERO_EVOLVE = "assets/bin/Data/c71e9b16a78284852b30650ca0ebbf2e"
    const val TICKET_CONFIG = "assets/bin/Data/8f4cc030fdc024de3acdae95bb99e004"

    private const val PRODUCT_HASH = "ff0afd7f54a04fe9af0cdcbf3c9c149d8e7d867c7743a6d7399f50166e24ec22"
    // Hash calculated after zeroing only verified non-IAP, per-item cost fields.
    // Allows multiple independent store modules to modify this one Unity asset.
    private const val NORMALIZED_COST_HASH = "56bb8ae552d1a7cf39518c905dcabeaa5c2fa868ac0df75be7b0014adf763b49"

    private val productPattern = Regex("""\{"category_id":"(\d+)","cost_group_id":"(\d+)",[^{}]*\}""")
    private val pricePattern = Regex("""\{"cost_param":"(\d+)","cost_type":"(\d+)","cost_value":"(\d+)","group_id":"(\d+)","id":"(\d+)","max_purchase_count":"(-?\d+)","org_price_iap":"(\d+)","priority":"(\d+)"\}""")

    private fun digest(data: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(data).joinToString("") { "%02x".format(it) }

    private fun zero(m: MatchResult): String {
        val old = m.groupValues[3]
        return m.value.replace("\"cost_value\":\"$old\"", "\"cost_value\":\"${"0".repeat(old.length)}\"")
    }

    fun store(costBytes: ByteArray, productBytes: ByteArray, categoryIds: Set<Int>,
              expectedRowCount: Int, other: Boolean = false): ByteArray {
        require(digest(productBytes) == PRODUCT_HASH) { "Unsupported XP Hero product asset" }
        val products = productPattern.findAll(String(productBytes, Charsets.ISO_8859_1)).toList()
        check(products.size == 777) { "Unexpected product table" }
        val selectedGroups = products.filter {
            val category = it.groupValues[1].toInt()
            if (other) category !in categoryIds else category in categoryIds
        }.map { it.groupValues[2].toInt() }.toSet()

        val original = String(costBytes, Charsets.ISO_8859_1)
        check(pricePattern.findAll(original).count() == 829) { "Unexpected shop price table" }
        val normalized = pricePattern.replace(original) {
            if (it.groupValues[2] == "1" && it.groupValues[7] == "0") zero(it) else it.value
        }
        require(digest(normalized.toByteArray(Charsets.ISO_8859_1)) == NORMALIZED_COST_HASH) {
            "Unexpected differences in game cost table"
        }
        var visited = 0
        val resultText = pricePattern.replace(original) {
            if (it.groupValues[4].toInt() in selectedGroups &&
                it.groupValues[2] == "1" && it.groupValues[7] == "0") {
                visited++
                zero(it)
            } else it.value
        }
        check(visited == expectedRowCount) { "Unexpected shop cost count $visited" }
        val result = resultText.toByteArray(Charsets.ISO_8859_1)
        check(result.size == costBytes.size) { "Shop resource size changed" }
        return result
    }

    fun hero(data: ByteArray, expectedHash: String, expectedRows: Int, vararg fields: String): ByteArray {
        require(digest(data) == expectedHash) { "Unexpected hero data asset" }
        var value = String(data, Charsets.ISO_8859_1)
        for (field in fields) {
            val expression = Regex("(\"" + Regex.escape(field) + "\":\")([0-9]+)(\")")
            val matches = expression.findAll(value).count()
            check(matches == expectedRows) { "Unexpected $field count: $matches" }
            value = expression.replace(value) {
                it.groupValues[1] + "0".repeat(it.groupValues[2].length) + it.groupValues[3]
            }
        }
        val result = value.toByteArray(Charsets.ISO_8859_1)
        check(result.size == data.size) { "Hero asset size changed" }
        return result
    }

    /**
     * Diagnostic fallback for hero star upgrades that display a zero cost
     * yet do not complete. Use INSTEAD of the zero-piece Hero Upgrade patch.
     * This intentionally costs ONE hero piece, so it is not a free upgrade.
     * It helps distinguish game logic rejecting zero from other validation.
     */
    fun heroUpgradeOnePiece(data: ByteArray): ByteArray {
        require(digest(data) == "818570c2ec087bab93583352fbe6e6ff9dd859b5f06cd14b9d1995842467df87") {
            "Unsupported hero star-upgrade asset"
        }
        val text = String(data, Charsets.ISO_8859_1)
        val price = Regex("(\\\"Cost1_Amount\\\":\\\")([0-9]+)(\\\")")
        check(price.findAll(text).count() == 144) { "Unexpected star upgrade row count" }
        val rewritten = price.replace(text) { match ->
            match.groupValues[1] + "0".repeat(match.groupValues[2].length - 1) +
                "1" + match.groupValues[3]
        }
        val bytes = rewritten.toByteArray(Charsets.ISO_8859_1)
        check(bytes.size == data.size) { "Asset length changed" }
        return bytes
    }
    /** Changes one identified PvP video-ticket daily limit from 5 to 5,000.
     *  The Unity TextAsset's 4-byte-aligned object-size and file-size headers
     *  must also be updated because the JSON string becomes three bytes longer.
     *  This is experimental: server-side caps or different ticket types may remain.
     */
    fun pvpAdTickets(original: ByteArray): ByteArray {
        require(digest(original) == "a06150f47dfb4d441a10570466b0e43892c45c0136517f4fdf31880eb4d2d9ee") {
            "Unsupported ticket configuration asset"
        }
        require(original.size == 2644) { "Unexpected ticket asset size" }
        val current = "\"key\":\"PVP_TICKET_RV_COUNT_LIMIT\",\"value\":\"5\""
        val next = "\"key\":\"PVP_TICKET_RV_COUNT_LIMIT\",\"value\":\"5000\""
        val source = String(original, Charsets.ISO_8859_1)
        check(source.indexOf(current) >= 0 && source.indexOf(current) == source.lastIndexOf(current)) {
            "Ticket limit missing or duplicated"
        }
        // 2 bytes trailing padding for original 2474-byte JSON; the new 2477
        // byte JSON needs 3 bytes of trailing padding.
        check(original.takeLast(2).all { it == 0.toByte() })
        val changed = source.substring(0, source.length - 2).replace(current, next) + "\u0000\u0000\u0000"
        val result = changed.toByteArray(Charsets.ISO_8859_1)
        check(result.size == 2648)
        // Unity SerializedFile v22: 64-bit file size, BE at offset 24.
        result[28] = 0; result[29] = 0; result[30] = 0x0A; result[31] = 0x58
        // Object byte size (little endian), and TextAsset payload length.
        result[112] = 0xC8.toByte(); result[113] = 0x09; result[114] = 0; result[115] = 0
        result[164] = 0xAD.toByte(); result[165] = 0x09; result[166] = 0; result[167] = 0
        return result
    }
}

// Non-IAP shop categories; the optional modules may be enabled together.
@Suppress("unused")
val xp130DailyGemShop = rawResourcePatch(
    name = "XP Hero 1.3: Daily Gem Shop", description = "Experimental: zero local Daily Gem Shop item prices.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(), setOf(15), 300))
    }
}

@Suppress("unused")
val xp130ChestShop = rawResourcePatch(
    name = "XP Hero 1.3: Chest Shops", description = "Experimental: zero locally stored non-IAP weapon and soul chest costs.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(), setOf(4, 8), 20))
    }
}

@Suppress("unused")
val xp130TokenShop = rawResourcePatch(
    name = "XP Hero 1.3: Token Shop", description = "Experimental: zero red-token shop prices.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(), setOf(34), 27))
    }
}

@Suppress("unused")
val xp130InvasionShop = rawResourcePatch(
    name = "XP Hero 1.3: Invasion Shop", description = "Experimental: zero local Invasion Shop coin prices.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(), setOf(9), 38))
    }
}

@Suppress("unused")
val xp130ExchangeShop = rawResourcePatch(
    name = "XP Hero 1.3: Exchange Shop", description = "Stackable version of the tested 24 zero-gem material exchanges.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(), setOf(6, 7), 24))
    }
}

@Suppress("unused")
val xp130OtherShops = rawResourcePatch(
    name = "XP Hero 1.3: Other Shops", description = "Experimental: other verified local non-IAP currency prices.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val costs = get(XP130.COST)
        costs.writeBytes(XP130.store(costs.readBytes(), get(XP130.PRODUCTS).readBytes(),
            setOf(15, 4, 8, 34, 9, 6, 7), 27, true))
    }
}

@Suppress("unused")
val xp130FreeHeroLevel = rawResourcePatch(
    name = "XP Hero 1.3: Free Hero Level Up", description = "Experimental: zero hero level-up resource costs.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XP130.HERO_LEVEL)
        file.writeBytes(XP130.hero(file.readBytes(),
            "8d0ca15a7aaf9971fdebb962199af037f7493ed14002ee2565e16dded89c2e0b", 126, "Cost_Amount_1"))
    }
}

@Suppress("unused")
val xp130FreeHeroUpgrade = rawResourcePatch(
    name = "XP Hero 1.3: Free Hero Upgrade", description = "Experimental: zero hero star-upgrade costs.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XP130.HERO_STAR)
        file.writeBytes(XP130.hero(file.readBytes(),
            "818570c2ec087bab93583352fbe6e6ff9dd859b5f06cd14b9d1995842467df87", 144, "Cost1_Amount"))
    }
}

@Suppress("unused")
val xp130FreeHeroEvolve = rawResourcePatch(
    name = "XP Hero 1.3: Free Hero Evolve", description = "Experimental: zero hero evolution materials and pieces.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XP130.HERO_EVOLVE)
        file.writeBytes(XP130.hero(file.readBytes(),
            "f66705315246f8b730793a6964a6d148deb9b86a119bb1d232b801bd7201ffb9", 92,
            "Cost_Amount1", "Cost_Amount2", "Cost_Piece"))
    }
}

@Suppress("unused")
val xp130PvpVideoTickets = rawResourcePatch(
    name = "XP Hero 1.3: 5000 PvP Video Tickets", description = "Experimental: PvP ticket video-use configuration 5 to 5000. Other tickets unaffected.", default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XP130.TICKET_CONFIG)
        file.writeBytes(XP130.pvpAdTickets(file.readBytes()))
    }
}

/**
 * One-piece hero upgrade test. DO NOT select together with the 0-cost
 * XP Hero 1.3: Free Hero Upgrade patch; both target the same guarded asset.
 */
@Suppress("unused")
val xp131OnePieceHeroUpgradeTest = rawResourcePatch(
    name = "XP Hero 1.3: Hero Upgrade (1-piece test)",
    description = "Diagnostic only: hero star upgrades cost one piece, not free. Select instead of Free Hero Upgrade.",
    default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val file = get(XP130.HERO_STAR)
        file.writeBytes(XP130.heroUpgradeOnePiece(file.readBytes()))
    }
}
