package com.jarvis.assistant

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings

sealed class CommandResult {
    data class SpokenResponse(val response: String) : CommandResult()
    data class RequestCallConfirmation(val contact: ContactMatch) : CommandResult()
    data class LaunchIntent(val intent: Intent, val response: String) : CommandResult()
    data class Error(val message: String) : CommandResult()
}

object CommandProcessor {

    fun processCommand(context: Context, rawText: String, hasContactPermission: Boolean): CommandResult {
        val query = rawText.lowercase().trim()

        return when {
            // YouTube
            query.contains("open youtube") || query.contains("youtube chalao") || query.contains("youtube open") -> {
                val intent = context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
                    ?: Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com"))
                CommandResult.LaunchIntent(intent, "Opening YouTube, Sir.")
            }

            // Settings
            query.contains("open settings") || query.contains("setting kholo") || query.contains("open setting") -> {
                val intent = Intent(Settings.ACTION_SETTINGS)
                CommandResult.LaunchIntent(intent, "Opening System Settings.")
            }

            // Calculator
            query.contains("open calculator") || query.contains("calculator kholo") -> {
                val launchIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calculator")
                    ?: Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_CALCULATOR)
                CommandResult.LaunchIntent(launchIntent, "Launching Calculator.")
            }

            // Show Contacts
            query.contains("show contacts") || query.contains("show my contacts") || query.contains("contacts dikhao") -> {
                val intent = Intent(Intent.ACTION_VIEW, ContactsContract.Contacts.CONTENT_URI)
                CommandResult.LaunchIntent(intent, "Displaying your contacts.")
            }

            // Play Music
            query.contains("play music") || query.contains("gana bajao") || query.contains("music chalao") -> {
                val intent = Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER)
                CommandResult.LaunchIntent(intent, "Opening Music Player.")
            }

            // Phone Calling (Safe confirmation pattern)
            query.startsWith("call ") || query.startsWith("phone lagao ") || query.startsWith("call karo ") -> {
                val targetName = query.replace("call ", "")
                    .replace("phone lagao ", "")
                    .replace("call karo ", "")
                    .replace("ko", "")
                    .trim()

                if (targetName.isEmpty()) {
                    CommandResult.Error("Please specify a contact name to call.")
                } else if (!hasContactPermission) {
                    CommandResult.Error("Contacts permission is required to search and call.")
                } else {
                    val match = ContactHelper.searchContact(context, targetName)
                    if (match != null) {
                        CommandResult.RequestCallConfirmation(match)
                    } else {
                        CommandResult.Error("I could not find '$targetName' in your contacts.")
                    }
                }
            }

            // Navigation via Accessibility Service
            query == "go back" || query == "back jao" -> {
                val service = JarvisAccessibilityService.instance
                if (service != null) {
                    val handled = service.performBack()
                    if (handled) CommandResult.SpokenResponse("Going back.")
                    else CommandResult.Error("Failed to perform back action.")
                } else {
                    CommandResult.Error("Please enable JARVIS Accessibility Service in Settings to use navigation commands.")
                }
            }

            query == "scroll down" || query == "niche scroll karo" -> {
                val service = JarvisAccessibilityService.instance
                if (service != null) {
                    val handled = service.performScrollDown()
                    if (handled) CommandResult.SpokenResponse("Scrolling down.")
                    else CommandResult.Error("Unable to scroll down on this screen.")
                } else {
                    CommandResult.Error("JARVIS Accessibility Service is disabled.")
                }
            }

            query == "scroll up" || query == "upar scroll karo" -> {
                val service = JarvisAccessibilityService.instance
                if (service != null) {
                    val handled = service.performScrollUp()
                    if (handled) CommandResult.SpokenResponse("Scrolling up.")
                    else CommandResult.Error("Unable to scroll up on this screen.")
                } else {
                    CommandResult.Error("JARVIS Accessibility Service is disabled.")
                }
            }

            // Hello / Introduction
            query.contains("hello jarvis") || query.contains("who are you") || query.contains("kaun ho") -> {
                CommandResult.SpokenResponse("I am JARVIS, your personal offline voice assistant. How may I help you today?")
            }

            else -> {
                CommandResult.Error("Command '$rawText' is not supported. Try 'Open YouTube', 'Open Settings', or 'Call [Name]'.")
            }
        }
    }
}
