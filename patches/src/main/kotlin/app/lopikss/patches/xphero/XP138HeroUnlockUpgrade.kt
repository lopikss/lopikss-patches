package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.patch.rawResourcePatch
import java.security.MessageDigest

/**
 * Version-pinned XP Hero 26.2.0 hero data edits.
 * Both preserve Unity file length and verify the *original* asset SHA-256.
 */
private object XP138Heroes {
    const val HERO_DATA = "assets/bin/Data/ac161aa51303a4fce8138bf3242d0920"
    const val HERO_STAR = "assets/bin/Data/02ce5273e9c794059afe2ada0670d172"

    private const val HERO_DATA_SHA = "ff5d1c8e995e8169b643227d7e87a202910dc81c8e141d12ca0d8c0488018408"
    private const val HERO_STAR_SHA = "818570c2ec087bab93583352fbe6e6ff9dd859b5f06cd14b9d1995842467df87"

    private fun digest(input: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(input).joinToString("") { "%02x".format(it) }

    fun unlockBlasterAndHana(input: ByteArray): ByteArray {
        require(digest(input) == HERO_DATA_SHA) { "Unsupported XP Hero CostumeData" }
        val source = String(input, Charsets.ISO_8859_1)
        val heroRow = Regex("""\{[^{}]*"UnlockRequiredPieces":"([0-9]+)"[^{}]*"id":"(23|24)"[^{}]*\}""")
        val matches = heroRow.findAll(source).toList()
        check(matches.size == 2 && matches.map { it.groupValues[2] }.toSet() == setOf("23", "24")) {
            "Expected exactly the Blaster (23) and Hana (24) hero rows"
        }
        check(matches.all { it.groupValues[1] == "100" }) {
            "Unexpected unlock requirements for Blaster or Hana"
        }
        val edited = heroRow.replace(source) { match ->
            match.value.replace(
                "\"UnlockRequiredPieces\":\"100\"",
                "\"UnlockRequiredPieces\":\"000\"",
            )
        }.toByteArray(Charsets.ISO_8859_1)
        check(edited.size == input.size && !edited.contentEquals(input)) {
            "Hero asset unexpectedly changed"
        }
        return edited
    }

    fun freeStarUpgrades(input: ByteArray): ByteArray {
        require(digest(input) == HERO_STAR_SHA) { "Unsupported XP Hero CostumeStarGradeOptionData" }
        val text = String(input, Charsets.ISO_8859_1)
        val price = Regex("""("Cost1_Amount":")([0-9]+)(")""")
        check(price.findAll(text).count() == 144) {
            "Unexpected hero star upgrade data"
        }
        val edited = price.replace(text) { match ->
            match.groupValues[1] + "0".repeat(match.groupValues[2].length) + match.groupValues[3]
        }.toByteArray(Charsets.ISO_8859_1)
        check(edited.size == input.size) { "Hero upgrade asset changed length" }
        return edited
    }
}

/**
 * Blaster and Hana are hero IDs 23 and 24 with 100 shards required in
 * the original CostumeData. Set their unlock requirements to zero.
 * This does not change any paid IAP offers or other heroes.
 */
@Suppress("unused")
val xp138UnlockLastTwoHeroes = rawResourcePatch(
    name = "XP Hero | Unlock Blaster & Hana (FREE)",
    description = "Experimental: Blaster and Hana cost 0 pieces; allows zero-piece confirmation in native code.",
    default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val heroFile = get(XP138Heroes.HERO_DATA)
        val native = get(XP135NativeEditor.ARM64)
        // Validate the original asset and native game logic before changing
        // either file. This remains opt-in and version-specific.
        val newHeroData = XP138Heroes.unlockBlasterAndHana(heroFile.readBytes())
        val newNative = XP135NativeEditor.allowZeroPieceHeroUnlock(native.readBytes())
        heroFile.writeBytes(newHeroData)
        native.writeBytes(newNative)
    }
}

/**
 * This is the actual zero-shard alternative to the confirmed one-shard
 * fallback. Never select both upgrade modules, as both rewrite the same
 * hero star data and expect its original SHA-256.
 */
@Suppress("unused")
val xp138FreeHeroStarUpgrades = rawResourcePatch(
    name = "XP Hero | Free Hero Upgrade (0 Shards)",
    description = "Experimental: zero all star upgrade costs and bypass their ARM64 zero-cost rejection. Do not combine with 1 Shard fallback.",
    default = false,
) {
    compatibleWith(XP_HERO)
    execute {
        val starFile = get(XP138Heroes.HERO_STAR)
        val native = get(XP135NativeEditor.ARM64)
        // Validate every source before modifying either file.
        val modifiedStars = XP138Heroes.freeStarUpgrades(starFile.readBytes())
        val modifiedNative = XP135NativeEditor.freeHeroStarUpgrades(native.readBytes())
        starFile.writeBytes(modifiedStars)
        native.writeBytes(modifiedNative)
    }
}
