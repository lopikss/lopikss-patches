package app.lopikss.patches.xphero

import app.lopikss.patches.shared.Constants.XP_HERO
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch

/**
 * Emits the Android Activity resultCode when the Play billing activity returns.
 * It never grants, emulates, suppresses, or approves a purchase.
 *
 * Logs no purchase data, receipts, product IDs or personal identifiers.
 * Activity resultCode is not Google BillingResult responseCode.
 */
@Suppress("unused")
val xpHeroBillingTracePatch = bytecodePatch(
    name = "XP Hero: Billing result trace",
    description = "Log Android billing activity results to Logcat (tag: LopikssBilling). Does not alter purchases.",
    default = false,
) {
    compatibleWith(XP_HERO)

    execute {
        BillingResultFingerprint.method.addInstructions(0, """
            const-string v0, "LopikssBilling"
            invoke-static {p2}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
            move-result-object v1
            invoke-static {v0, v1}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
        """.trimIndent())
    }
}
