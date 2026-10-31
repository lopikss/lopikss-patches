package app.lopikss.patches.xphero

import java.security.MessageDigest

/**
 * XP Hero 27.0.0: genuine enemy-drop EXP and the corresponding per-kill cap.
 * Both Unity resources are matched to the original game with exact SHA-256.
 * No level requirement, gold, item reward or non-EXP enemy is changed.
 */
internal object XP142ExpMultiplierData {
    const val ENEMY_EXP = "assets/bin/Data/b88519e7b85074d4eb6b23781e4d9458"
    const val KILL_LIMIT = "assets/bin/Data/36457843a12d047c09afa0c1e2da08a3"

    private const val ENEMY_SHA256 =
        "33eef481fce46f41ad31db596d4f600d1338b03d4724bcc8ec1a40abf4b9c0ea"
    private const val LIMIT_SHA256 =
        "f3fecdca205abe070b9b0d07ac72ab0fdd47a05d23d05ea1e8a2b6f6bd3a9820"

    private val enemyExp = Regex("\"DropAccountExpAmount\":\"([0-9]+)\"")
    private val singleKillLimit = Regex("\"SingleKillExp_Limit\":\"([0-9]+)\"")

    private fun sha(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes).joinToString("") { "%02x".format(it) }

    private fun le32(bytes: ByteArray, offset: Int): Int =
        (bytes[offset].toInt() and 255) or
            ((bytes[offset + 1].toInt() and 255) shl 8) or
            ((bytes[offset + 2].toInt() and 255) shl 16) or
            ((bytes[offset + 3].toInt() and 255) shl 24)

    private fun be32(bytes: ByteArray, offset: Int): Int =
        ((bytes[offset].toInt() and 255) shl 24) or
            ((bytes[offset + 1].toInt() and 255) shl 16) or
            ((bytes[offset + 2].toInt() and 255) shl 8) or
            (bytes[offset + 3].toInt() and 255)

    private fun putLE32(bytes: ByteArray, offset: Int, value: Int) {
        for (i in 0..3) bytes[offset + i] = (value ushr (8 * i)).toByte()
    }

    private fun putBE64(bytes: ByteArray, offset: Int, value: Long) {
        for (i in 0..7) bytes[offset + i] = (value ushr (8 * (7 - i))).toByte()
    }

    private class UnityTextAsset(bytes: ByteArray, name: String, expectedSha: String) {
        private val header: ByteArray = bytes
        private val textStart: Int
        val text: String

        init {
            require(sha(bytes) == expectedSha) {
                "Unsupported XP Hero 27.0.0 " + name + " asset"
            }
            require(bytes.size > 172 && be32(bytes, 8) == 22 && be32(bytes, 36) == 144) {
                "Unexpected Unity TextAsset header: " + name
            }
            val length = (0..7).fold(0L) { acc, i ->
                (acc shl 8) or (bytes[24 + i].toLong() and 255L)
            }
            require(length == bytes.size.toLong() && le32(bytes, 112) == bytes.size - 144) {
                "Invalid Unity file length: " + name
            }
            val nameLen = le32(bytes, 144)
            require(nameLen == name.length &&
                String(bytes, 148, nameLen, Charsets.ISO_8859_1) == name) {
                "Unexpected Unity TextAsset name: " + name
            }
            val lengthOffset = 148 + ((nameLen + 3) and -4)
            val textSize = le32(bytes, lengthOffset)
            textStart = lengthOffset + 4
            require(textSize > 0 && textStart + textSize <= bytes.size &&
                bytes.copyOfRange(textStart + textSize, bytes.size)
                    .all { it == 0.toByte() }) {
                "Malformed Unity TextAsset payload: " + name
            }
            text = String(bytes, textStart, textSize, Charsets.ISO_8859_1)
        }

        fun encode(replacement: String): ByteArray {
            val payload = replacement.toByteArray(Charsets.ISO_8859_1)
            val size = (textStart + payload.size + 3) and -4
            val out = ByteArray(size)
            header.copyInto(out, 0, 0, textStart)
            payload.copyInto(out, textStart)
            putLE32(out, textStart - 4, payload.size)
            putLE32(out, 112, size - 144)
            putBE64(out, 24, size.toLong())
            return out
        }
    }

    private fun scaled(original: String, factor: Int): String {
        val value = original.toLong()
        require(value >= 0 && value <= Long.MAX_VALUE / factor) {
            "XP reward overflow: " + value + " * " + factor
        }
        return (value * factor).toString()
    }

    fun multiplyEnemyExp(input: ByteArray, multiplier: Int): ByteArray {
        require(multiplier in 1..10_000) { "EXP multiplier must be 1–10000" }
        val asset = UnityTextAsset(input, "EnemyData_1084", ENEMY_SHA256)
        val rows = enemyExp.findAll(asset.text).toList()
        check(rows.size == 246 && rows.count { it.groupValues[1] != "0" } == 169) {
            "Unexpected enemy EXP reward table"
        }
        val text = enemyExp.replace(asset.text) { match ->
            val old = match.groupValues[1]
            match.value.replace(old, scaled(old, multiplier))
        }
        return asset.encode(text)
    }

    fun multiplyKillLimit(input: ByteArray, multiplier: Int): ByteArray {
        require(multiplier in 1..10_000) { "EXP multiplier must be 1–10000" }
        val asset = UnityTextAsset(input, "LevelUpData_Exp_116", LIMIT_SHA256)
        val rows = singleKillLimit.findAll(asset.text).toList()
        check(rows.size == 1500 && rows.all { it.groupValues[1].toLong() > 0L }) {
            "Unexpected per-kill EXP limit table"
        }
        val text = singleKillLimit.replace(asset.text) { match ->
            val old = match.groupValues[1]
            match.value.replace(old, scaled(old, multiplier))
        }
        return asset.encode(text)
    }
}
