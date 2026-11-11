package app.lopikss.patches.xphero

import java.security.MessageDigest

/**
 * XP Hero 27.0.0 field-boss data editor.
 *
 * WIP toward a user-visible button. This is ONLY the data/backend portion.
 * Until the button is implemented and wired to a verified runtime respawn,
 * do NOT publish this as a functioning 1.4 boss-respawn feature.
 */
internal object XP140BossRespawnData {
    const val ASSET = "assets/bin/Data/15ec72bdb57c947ed8299d241da1104f"
    private const val ORIGINAL_SHA256 =
        "ef1bfc79a04e89e4597f21f876b9d346c8f6af4d3ab6a0bcea0a9c962308c96f"

    private val row = Regex(
        """\{"EnemyDataId":"(\d+)","IsPatrol":"(TRUE|FALSE)","RespawnCoolTime":"([0-9.]+)","RetreatRange":"([0-9.]+)","ShowRespawnCoolTime":"(TRUE|FALSE)","id":"(\d+)","key":"([^"]+)"\}"""
    )

    private fun digest(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes)
            .joinToString("") { "%02x".format(it) }

    /**
     * Zero only chapter field-boss timer values. This does not by itself
     * reset saved kill timestamps or create enemies on button press.
     */
    fun zeroChapterBossCooldowns(original: ByteArray): ByteArray {
        require(digest(original) == ORIGINAL_SHA256) {
            "Unsupported XP Hero 27.0.0 EnemySpawnGroupData asset"
        }
        val text = String(original, Charsets.ISO_8859_1)
        check(row.findAll(text).count() == 214) {
            "Unexpected XP Hero spawn group row count"
        }
        val bossKeys = mutableSetOf<String>()
        val modified = row.replace(text) { match ->
            val key = match.groupValues[7]
            if (!key.startsWith("CH") || !key.endsWith("_Boss")) {
                match.value
            } else {
                check(bossKeys.add(key)) { "Duplicate field boss " + key }
                val existing = match.groupValues[3]
                val find = "\"RespawnCoolTime\":\"" + existing + "\""
                val replacement = "\"RespawnCoolTime\":\"" +
                    "0".repeat(existing.length) + "\""
                match.value.replace(find, replacement)
            }
        }
        check(bossKeys.size == 20) {
            "Expected 20 chapter field bosses, found " + bossKeys.size
        }
        val result = modified.toByteArray(Charsets.ISO_8859_1)
        check(result.size == original.size) { "Enemy spawn data length changed" }
        return result
    }
}
