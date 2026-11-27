package app.lopikss.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val XP_HERO = Compatibility(
        name = "XP Hero",
        packageName = "io.supercent.weaponrpg",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xF5A623,
        targets = listOf(
            AppTarget(version = "27.0.0", isExperimental = true),
            AppTarget(version = "26.2.0", isExperimental = true)
        )
    )
    /** Only XP Hero 27.0.0: trait roll tables are version-pinned. */
    val XP_HERO_27 = Compatibility(
        name = "XP Hero",
        packageName = "io.supercent.weaponrpg",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xF5A623,
        targets = listOf(AppTarget(version = "27.0.0", isExperimental = true))
    )

    /** 27.2.0 is enabled only on patches with byte-identical verified assets. */
    val XP_HERO_UNCHANGED_272 = Compatibility(
        name = "XP Hero",
        packageName = "io.supercent.weaponrpg",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xF5A623,
        targets = listOf(
            AppTarget(version = "27.2.0", isExperimental = true),
            AppTarget(version = "27.0.0", isExperimental = true),
            AppTarget(version = "26.2.0", isExperimental = true)
        )
    )
    val XP_HERO_27_UNCHANGED_272 = Compatibility(
        name = "XP Hero",
        packageName = "io.supercent.weaponrpg",
        apkFileType = ApkFileType.XAPK,
        appIconColor = 0xF5A623,
        targets = listOf(
            AppTarget(version = "27.2.0", isExperimental = true),
            AppTarget(version = "27.0.0", isExperimental = true)
        )
    )
}
