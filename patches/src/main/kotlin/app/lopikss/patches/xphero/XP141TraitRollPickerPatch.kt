package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO_27
import app.morphe.patcher.patch.rawResourcePatch
import app.morphe.patcher.patch.stringOption

/**
 * XP Hero 27.0.0 S-ticket outcome picker.
 *
 * Configure from Morphe's PATCH SETTINGS before patching. The chosen outcome
 * applies to subsequent S-ticket rolls; it does not directly edit existing
 * traits, add tickets, or provide an in-game menu. Change the choice by
 * changing patch settings and creating a new patched APK.
 */
@Suppress("unused")
val xp141ChooseSTicketTrait = rawResourcePatch(
    name = "XP Hero | Choose S-Ticket Trait",
    description = "Choose S/SS/SSS rarity, stat and set icon in patch settings. S-ticket roll weights favor exactly that combination. Coin-roll rarity/synergy odds unchanged; same-rarity stats are shared. Combine with Free S-Ticket Rolls.",
    default = false,
) {
    compatibleWith(XP_HERO_27)

    val desiredGrade = stringOption(
        key = "chosenTraitGrade",
        default = "8",
        values = mapOf(
            "6" to "S",
            "7" to "SS",
            "8" to "SSS",
        ),
        title = "S-ticket rarity",
        description = "Every S-ticket roll selects this rarity. SSS is the highest.",
        required = true,
        validator = { it != null && it in setOf("6", "7", "8") },
    )

    val desiredStat = stringOption(
        key = "chosenTraitStat",
        default = "1",
        values = mapOf(
            "1" to "Total Attack Power",
            "20" to "Melee Weapon Damage",
            "19" to "Ranged Weapon Damage",
            "2" to "Critical Damage",
            "9" to "Critical Chance",
            "10" to "Attack Speed",
            "5" to "Max HP",
            "18" to "Healing Reduction on Hit",
            "21" to "Status Effect Resistance",
        ),
        title = "Trait stat",
        description = "Choose the exact bonus you want from the next S-ticket roll.",
        required = true,
        validator = { it?.toIntOrNull() in XP141TraitRollPickerData.validStats },
    )

    val desiredSet = stringOption(
        key = "chosenTraitSynergy",
        default = "1",
        values = mapOf(
            "1" to "Warrior's Fist (orange attack)",
            "2" to "Vampire Fang (red)",
            "3" to "Dragon's Claw (blue critical damage)",
            "4" to "Ogre Heart (green HP)",
            "5" to "Sage's Hourglass (purple cooldown)",
        ),
        title = "Trait synergy icon",
        description = "Choose the set icon to build matching trait synergy.",
        required = true,
        validator = { it != null && it in setOf("1", "2", "3", "4", "5") },
    )

    execute {
        val grade = (desiredGrade.value ?: "8").toInt()
        val stat = (desiredStat.value ?: "1").toInt()
        val synergy = (desiredSet.value ?: "1").toInt()

        val rollAsset = get(XP141TraitRollPickerData.ROLLS)
        val optionAsset = get(XP141TraitRollPickerData.OPTIONS)
        val synergyAsset = get(XP141TraitRollPickerData.SYNERGIES)

        // All three files must pass original-XAPK SHA-256 and table validation
        // BEFORE writing any edited resource.
        val editedRolls = XP141TraitRollPickerData.forceTicketRarity(
            rollAsset.readBytes(), grade
        )
        val editedOptions = XP141TraitRollPickerData.forceTicketStat(
            optionAsset.readBytes(), grade, stat
        )
        val editedSynergies = XP141TraitRollPickerData.forceTicketSynergy(
            synergyAsset.readBytes(), synergy
        )
        rollAsset.writeBytes(editedRolls)
        optionAsset.writeBytes(editedOptions)
        synergyAsset.writeBytes(editedSynergies)
    }
}
