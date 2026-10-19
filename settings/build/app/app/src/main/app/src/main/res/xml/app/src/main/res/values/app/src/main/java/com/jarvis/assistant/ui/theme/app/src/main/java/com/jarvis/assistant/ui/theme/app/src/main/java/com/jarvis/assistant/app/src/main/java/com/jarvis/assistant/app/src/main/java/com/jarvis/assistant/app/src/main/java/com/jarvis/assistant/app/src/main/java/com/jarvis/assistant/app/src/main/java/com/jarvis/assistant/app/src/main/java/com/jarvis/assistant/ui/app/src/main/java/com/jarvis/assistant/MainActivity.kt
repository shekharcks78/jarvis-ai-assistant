package com.jarvis.assistant

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import com.jarvis.assistant.ui.JarvisScreen
import com.jarvis.assistant.ui.theme.JarvisTheme

class MainActivity : ComponentActivity() {

    private val viewModel: JarvisViewModel by viewModels()
    private lateinit var voiceManager: JarvisVoiceManager

    private val requestAudioPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startListeningInternal()
        } else {
            viewModel.addHistory("Microphone", "Permission denied. Audio recording is needed for voice recognition.", isError = true)
        }
    }

    private val requestCallPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Call permission granted.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        voiceManager = JarvisVoiceManager(
            context = this,
            onResult = { speechText ->
                handleSpokenCommand(speechText)
            },
            onError = { errorMsg ->
                viewModel.addHistory("Voice Input", errorMsg, isError = true)
            },
            onListeningStateChanged = { listening ->
                viewModel.setListeningState(listening)
            },
            onRmsChanged = { rms ->
                viewModel.updateRms(rms)
            }
        )

        setContent {
            JarvisTheme {
                JarvisScreen(
                    viewModel = viewModel,
                    onMicClick = {
                        toggleListening()
                    },
                    onOpenAccessibilitySettings = {
                        openAccessibilitySettings()
                    },
                    onConfirmCall = { contact ->
                        viewModel.setPendingCall(null)
                        makeCall(contact.phoneNumber)
                    },
                    onCancelCall = {
                        viewModel.setPendingCall(null)
                        voiceManager.speak("Call cancelled.")
                    }
                )
            }
        }
    }

    private fun toggleListening() {
        if (viewModel.isListening) {
            voiceManager.stopListening()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                startListeningInternal()
            } else {
                requestAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun startListeningInternal() {
        voiceManager.startListening()
    }

    private fun handleSpokenCommand(text: String) {
        val hasContactPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        val result = CommandProcessor.processCommand(this, text, hasContactPermission)

        when (result) {
            is CommandResult.SpokenResponse -> {
                viewModel.addHistory(text, result.response)
                voiceManager.speak(result.response)
            }
            is CommandResult.LaunchIntent -> {
                viewModel.addHistory(text, result.response)
                voiceManager.speak(result.response)
                try {
                    startActivity(result.intent)
                } catch (e: Exception) {
                    viewModel.addHistory("App Launch", "Unable to open requested application.", isError = true)
                }
            }
            is CommandResult.RequestCallConfirmation -> {
                viewModel.setPendingCall(result.contact)
                voiceManager.speak("Found ${result.contact.name}. Tap confirm to call.")
            }
            is CommandResult.Error -> {
                viewModel.addHistory(text, result.message, isError = true)
                voiceManager.speak(result.message)
            }
        }
    }

    private fun makeCall(phoneNumber: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
            startActivity(intent)
        } else {
            requestCallPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
            // Fallback to dialer so call is not lost
            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${Uri.encode(phoneNumber)}"))
            startActivity(dialIntent)
        }
    }

    private fun openAccessibilitySettings() {
        Toast.makeText(this, "Enable 'JARVIS AI' service under Downloaded Apps/Services", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceManager.destroy()
    }
}
