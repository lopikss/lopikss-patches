# Roadmap

| Milestone | State |
| --- | --- |
| Create independent Morphe repository | ✅ Done |
| Read-only APK/XAPK inspector and tests | ✅ Done |
| Identify XP Hero 26.2.0 Billing 8.3.0 markers | ✅ Done |
| XP Hero billing activity result tracer | ✅ Source complete |
| Morphe Gradle compilation | ✅ Confirmed on GitHub Actions |
| Python CI validation | ✅ Added; check Actions for results |
| Physical device test of the tracer | ⏳ Pending |
| Add more version-pinned diagnostics | 🔜 Future |

## Device test

1. Back up XP Hero saves; do not wipe the only copy.
2. Use the experimental patch on a controlled installation.
3. Capture the activity result via `adb logcat -s LopikssBilling:I '*:S'`.
4. Report **whether a Logcat line appears** and the **Android result integer**, not personal receipt details.
5. Compare normal, unmodified billing UI behavior with a patched installation.

### Boundaries

This project does not provide free paid items, fake purchase receipts, or server-side verification bypasses. Compiler success is not proof of runtime compatibility.
