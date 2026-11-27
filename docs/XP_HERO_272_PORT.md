# XP Hero 27.2.0 partial port

The supplied 27.2.0 APK is a third-party modified APK, not a verified clean original. The following assets have the same filenames and SHA-256 hashes as already supported original assets, so their existing exact-hash guards are retained unchanged:

- Free Hero Level Up: CostumeLevelData_31
- Free Hero Evolve: CostumeEvolutionData_11
- Free Rune Upgrades: RuneLevelCostData_5
- Free Trait Presets: TraitPresetSlotData_4
- Free Party Skill Enhancements: PartySkillLevelData_8
- Choose S-Ticket Trait: TraitRollData_6, TraitOptionData_14, TraitSynergyGroupData_4
- Massive EXP Multiplier: EnemyData_1084, LevelUpData_Exp_116, PlayerLevelData_76

The remaining eight patches are intentionally not advertised for 27.2.0: Free Shops, Free Tickets & Return, Free Trait Rerolls, Free S-Ticket Rolls, both Hero Upgrade variants, Highest-Rarity Tomb Runes, and Unlock All Heroes. Shop, RemoteConfig, hero-star and reward tables have changed; the ARM64 library has changed in size (89,001,608 bytes vs 90,037,632), and the supplied native library cannot be treated as an original baseline. Revalidate against a clean 27.2.0 XAPK before adding new hashes or native offsets.

This is static asset verification, not device testing. No 27.2.0 release has been verified. All new compatibility declarations are experimental.
