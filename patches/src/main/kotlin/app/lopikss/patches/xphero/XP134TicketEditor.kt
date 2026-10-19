package app.lopikss.patches.xphero

import java.security.MessageDigest

/** Version-pinned, stackable XP Hero 26.2.0 Unity TextAsset ticket editor. */
internal object XP134TicketEditor {
    const val REMOTE = "assets/bin/Data/22fc19c3e5dc14f5ca465a3e6cd62dc3"
    const val PVP = "assets/bin/Data/8f4cc030fdc024de3acdae95bb99e004"
    private const val REMOTE_SHA = "c2f1df06c02d8723b569c532457eced4baee00f1c0bbb6a139bdcb3be801bfb1"
    private const val PVP_SHA = "a06150f47dfb4d441a10570466b0e43892c45c0136517f4fdf31880eb4d2d9ee"

    private val originals = linkedMapOf(
        "TRAIT_SUPERROLL_COST_AMOUNT" to "1",
        "PVP_TICKET_CHARGE_COST" to "10,20,30,50,100,100,100,100,100,100",
        "BR_TICKET_CHARGE_COST" to "100,200,300",
        "HEROTOMB_TICKET_CHARGE_COST" to "300",
        "BR_TICKET_MAX_BUY_LIMIT" to "3",
        "HEROTOMB_TICKET_MAX_BUY_LIMIT" to "3",
        "PVP_TICKET_RV_COUNT_LIMIT" to "10",
        "CHALLENGE_TOWER_DAY_RV_COUNT" to "5",
        "RV_TICKET_MAX_COUNT" to "5",
        "RV_SKIP_ONLY_TICKET_MAX_COUNT" to "10",
        "CHALLENGE_TOWER_RV_MAXCOUNT" to "5",
        "BOSS_RAID_TICKET_PACK_DAILY_PURCHASE_LIMIT" to "1",
        "HERO_TOMB_TICKET_PACK_DAILY_PURCHASE_LIMIT" to "5",
    )

    private fun sha(data: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(data).joinToString("") { "%02x".format(it) }

    private fun le32(data: ByteArray, pos: Int): Int =
        (data[pos].toInt() and 255) or ((data[pos + 1].toInt() and 255) shl 8) or
            ((data[pos + 2].toInt() and 255) shl 16) or ((data[pos + 3].toInt() and 255) shl 24)
    private fun be32(data: ByteArray, pos: Int): Int =
        ((data[pos].toInt() and 255) shl 24) or ((data[pos + 1].toInt() and 255) shl 16) or
            ((data[pos + 2].toInt() and 255) shl 8) or (data[pos + 3].toInt() and 255)
    private fun putLE32(out: ByteArray, pos: Int, value: Int) {
        for (n in 0..3) out[pos + n] = (value ushr (n * 8)).toByte()
    }
    private fun putBE64(out: ByteArray, pos: Int, value: Long) {
        for (n in 0..7) out[pos + n] = (value ushr ((7 - n) * 8)).toByte()
    }

    private data class TextAsset(val original: ByteArray, val textStart: Int, val text: String) {
        fun encode(newText: String): ByteArray {
            val data = newText.toByteArray(Charsets.ISO_8859_1)
            val requiredLength = textStart + data.size
            val alignedLength = (requiredLength + 3) and -4
            val bytes = ByteArray(alignedLength)
            original.copyInto(bytes, 0, 0, textStart)
            data.copyInto(bytes, textStart)
            putLE32(bytes, textStart - 4, data.size)
            putLE32(bytes, 112, alignedLength - 144)
            putBE64(bytes, 24, alignedLength.toLong())
            return bytes
        }
    }
    private fun parse(bytes: ByteArray, name: String): TextAsset {
        require(bytes.size > 168 && be32(bytes, 8) == 22 && be32(bytes, 36) == 144) {
            "Unsupported Unity TextAsset format"
        }
        val headerFileSize = (0..7).fold(0L) { acc, n ->
            (acc shl 8) or (bytes[24 + n].toLong() and 255L)
        }
        check(headerFileSize == bytes.size.toLong() && le32(bytes, 112) == bytes.size - 144) {
            "Unexpected Unity file/object length"
        }
        val nameLength = le32(bytes, 144)
        check(nameLength in 1..100)
        val nameStart = 148
        val actualName = String(bytes, nameStart, nameLength, Charsets.ISO_8859_1)
        check(actualName == name) { "Unexpected Unity TextAsset name" }
        val textLengthPos = nameStart + ((nameLength + 3) and -4)
        val textSize = le32(bytes, textLengthPos)
        val textStart = textLengthPos + 4
        check(textSize > 0 && textStart + textSize <= bytes.size &&
            bytes.copyOfRange(textStart + textSize, bytes.size).all { it == 0.toByte() }) {
            "Malformed Unity string payload or padding"
        }
        return TextAsset(bytes, textStart,
            String(bytes, textStart, textSize, Charsets.ISO_8859_1))
    }

    private fun pricePattern(key: String): Regex = Regex(
        "\\\"key\\\":\\\"" + Regex.escape(key) + "\\\",\\\"value\\\":\\\"([^\\\"]*)\\\""
    )
    private fun value(text: String, key: String): String {
        val matches = pricePattern(key).findAll(text).toList()
        check(matches.size == 1) { "Unexpected key count for $key" }
        return matches.single().groupValues[1]
    }
    private fun put(text: String, key: String, newValue: String): String {
        value(text, key)
        return pricePattern(key).replace(text) {
            "\"key\":\"$key\",\"value\":\"$newValue\""
        }
    }
    private fun supported(key: String, current: String, original: String): Boolean {
        if (current == original) return true
        if (key == "TRAIT_SUPERROLL_COST_AMOUNT") return current == "0"
        if (key == "HEROTOMB_TICKET_CHARGE_COST") return current == "0"
        if (key == "PVP_TICKET_CHARGE_COST" || key == "BR_TICKET_CHARGE_COST")
            return current == List(400) { "0" }.joinToString(",")
        return current == "400"
    }

    private fun remote(input: ByteArray, sTicket: Boolean): ByteArray {
        val asset = parse(input, "RemoteConfig_329")
        var normalized = asset.text
        for ((key, original) in originals) {
            val current = value(asset.text, key)
            require(supported(key, current, original)) { "Unexpected current $key" }
            normalized = put(normalized, key, original)
        }
        require(sha(asset.encode(normalized)) == REMOTE_SHA) {
            "Unsupported XP Hero 26.2.0 RemoteConfig asset"
        }
        var modified = asset.text
        if (sTicket) {
            modified = put(modified, "TRAIT_SUPERROLL_COST_AMOUNT", "0")
        } else {
            val free400 = List(400) { "0" }.joinToString(",")
            for (key in listOf("PVP_TICKET_CHARGE_COST", "BR_TICKET_CHARGE_COST")) {
                modified = put(modified, key, free400)
            }
            modified = put(modified, "HEROTOMB_TICKET_CHARGE_COST", "0")
            for (key in originals.keys) {
                if (key !in listOf("TRAIT_SUPERROLL_COST_AMOUNT", "PVP_TICKET_CHARGE_COST",
                        "BR_TICKET_CHARGE_COST", "HEROTOMB_TICKET_CHARGE_COST")) {
                    modified = put(modified, key, "400")
                }
            }
        }
        return asset.encode(modified)
    }

    /** Apply the independent S-ticket-roll price change in either patch order. */
    fun sTicket(input: ByteArray): ByteArray = remote(input, true)

    /** Free all configured ticket refills and allow up to 400 per day. */
    fun freeTicketRecharge(remoteBytes: ByteArray, pvpBytes: ByteArray): Pair<ByteArray, ByteArray> {
        val changedRemote = remote(remoteBytes, false)
        val pvp = parse(pvpBytes, "ConfigData_45")
        val current = value(pvp.text, "PVP_TICKET_RV_COUNT_LIMIT")
        check(current == "5" || current == "400") { "Unexpected PvP ticket configuration" }
        val originalText = put(pvp.text, "PVP_TICKET_RV_COUNT_LIMIT", "5")
        require(sha(pvp.encode(originalText)) == PVP_SHA) { "Unexpected PvP ticket asset" }
        val changedPvp = pvp.encode(put(pvp.text, "PVP_TICKET_RV_COUNT_LIMIT", "400"))
        return changedRemote to changedPvp
    }
}
