package com.noshorts.mobile

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.net.Uri
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class ShortsBlockerService : AccessibilityService() {

    private var lastTriggerTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != "com.google.android.youtube") return

        val rootNode: AccessibilityNodeInfo = rootInActiveWindow ?: return

        // Detect YouTube Mobile App's Shorts Player View IDs
        val isShortOpen = hasShortsNode(rootNode, "com.google.android.youtube:id/reel_watch_fragment_root") ||
                          hasShortsNode(rootNode, "com.google.android.youtube:id/reel_recycler")

        if (isShortOpen) {
            val now = System.currentTimeMillis()
            if (now - lastTriggerTime > 600) { // Debounce 600ms
                lastTriggerTime = now
                // Force redirect directly to YouTube Mobile App Home Screen
            val homeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube://")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(homeIntent)
            }
        }
    }

    private fun hasShortsNode(node: AccessibilityNodeInfo, viewId: String): Boolean {
        val matches = node.findAccessibilityNodeInfosByViewId(viewId)
        return matches != null && matches.isNotEmpty()
    }

    override fun onInterrupt() {}
}
