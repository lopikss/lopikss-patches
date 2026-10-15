package app.lopikss.patches.xphero

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.string
import com.android.tools.smali.dexlib2.AccessFlags

/**
 * The Google Play Billing 8.3.0 result handler in original XP Hero 26.2.0.
 * Narrow fingerprint: mismatching library versions should fail visibly.
 */
object BillingResultFingerprint : Fingerprint(
    definingClass = "Lcom/android/billingclient/api/ProxyBillingActivity;",
    name = "onActivityResult",
    returnType = "V",
    accessFlags = listOf(AccessFlags.PROTECTED),
    parameters = listOf("I", "I", "Landroid/content/Intent;"),
    filters = listOf(
        string("Activity finished with resultCode "),
        string("INTENT_SOURCE"),
        methodCall(
            definingClass = "Lcom/android/billingclient/api/ProxyBillingActivity;",
            name = "sendBroadcast",
        ),
    ),
)
