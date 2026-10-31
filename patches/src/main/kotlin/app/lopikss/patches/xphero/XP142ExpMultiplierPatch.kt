package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO_27
import app.morphe.patcher.patch.intOption
import app.morphe.patcher.patch.rawResourcePatch

/** XP Hero 27.0.0 local EXP reward and single-kill cap boost. */
@Suppress("unused")
val xp142MassiveExpMultiplier = rawResourcePatch(
    name = "XP Hero | Massive EXP Multiplier",
    description = "Multiply monster-kill EXP and its per-kill cap, from 1x up to 100000x (default 10000x). Extremely large values are capped to avoid 64-bit overflow; other rewards stay unchanged.",
    default = false,
) {
    compatibleWith(XP_HERO_27)

    val multiplier = intOption(
        key = "monsterExpMultiplier",
        default = 10000,
        title = "Monster EXP multiplier",
        description = "1x–100000x, default 10000x. Very large rewards are capped for 64-bit safety; actual EXP may be limited by game/server checks.",
        required = true,
        validator = { it != null && it in 1..100000 },
    )

    execute {
        val factor = multiplier.value ?: 10000
        val rewards = get(XP142ExpMultiplierData.ENEMY_EXP)
        val limit = get(XP142ExpMultiplierData.KILL_LIMIT)

        // Validate and generate BOTH complete resources before any write.
        val increasedExp = XP142ExpMultiplierData.multiplyEnemyExp(
            rewards.readBytes(), factor
        )
        val increasedLimit = XP142ExpMultiplierData.multiplyKillLimit(
            limit.readBytes(), factor
        )
        rewards.writeBytes(increasedExp)
        limit.writeBytes(increasedLimit)
    }
}
