package org.telegram.messenger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CopyCodeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val text = intent.getStringExtra("text")
        AndroidUtilities.addToClipboard(text)
    }
}
