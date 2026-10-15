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
            AppTarget(version = "26.2.0", isExperimental = true)
        )
    )
}
