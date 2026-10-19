package com.jarvis.assistant

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class JarvisAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No background tracking or logging of user text
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    fun performBack(): Boolean {
        return performGlobalAction(GLOBAL_ACTION_BACK)
    }

    fun performScrollDown(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        val success = scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD) ?: false
        scrollableNode?.recycle()
        root.recycle()
        return success
    }

    fun performScrollUp(): Boolean {
        val root = rootInActiveWindow ?: return false
        val scrollableNode = findScrollableNode(root)
        val success = scrollableNode?.performAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD) ?: false
        scrollableNode?.recycle()
        root.recycle()
        return success
    }

    private fun findScrollableNode(root: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val deque = ArrayDeque<AccessibilityNodeInfo>()
        deque.add(root)

        while (deque.isNotEmpty()) {
            val current = deque.removeFirst()
            if (current.isScrollable) {
                return current
            }
            for (i in 0 until current.childCount) {
                val child = current.getChild(i)
                if (child != null) {
                    deque.add(child)
                }
            }
        }
        return null
    }

    companion object {
        var instance: JarvisAccessibilityService? = null
            private set

        fun isRunning(): Boolean = instance != null
    }
}
