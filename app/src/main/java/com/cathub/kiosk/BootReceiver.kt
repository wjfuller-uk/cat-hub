package com.cathub.kiosk

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cathub.MainActivity

/**
 * Boot receiver — auto-start Cat Hub when device boots.
 * Used for kiosk mode / always-on display.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(launchIntent)
        }
    }
}
