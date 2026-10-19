package com.jarvis.assistant.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jarvis.assistant.ContactMatch
import com.jarvis.assistant.JarvisViewModel
import com.jarvis.assistant.ui.theme.CyanGlow
import com.jarvis.assistant.ui.theme.DeepNavy
import com.jarvis.assistant.ui.theme.ElectricBlue
import com.jarvis.assistant.ui.theme.ElectricCyan
import com.jarvis.assistant.ui.theme.ErrorRed
import com.jarvis.assistant.ui.theme.NeonCardBg
import com.jarvis.assistant.ui.theme.SpaceBlack
import com.jarvis.assistant.ui.theme.TextMuted
import com.jarvis.assistant.ui.theme.TextPrimary

@Composable
fun JarvisScreen(
    viewModel: JarvisViewModel,
    onMicClick: () -> Unit,
    onOpenAccessibilitySettings: () -> Unit,
    onConfirmCall: (ContactMatch) -> Unit,
    onCancelCall: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SpaceBlack, DeepNavy, SpaceBlack)
                )
            )
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "J.A.R.V.I.S.",
                        color = ElectricCyan,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = if (viewModel.isListening) "SYSTEM ACTIVE • LISTENING" else "SYSTEM ONLINE • STANDBY",
                        color = if (viewModel.isListening) ElectricCyan else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                IconButton(
                    onClick = onOpenAccessibilitySettings,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(NeonCardBg)
                        .border(1.dp, CyanGlow, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Accessibility",
                        tint = ElectricCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // History Feed
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                reverseLayout = true,
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(viewModel.history) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .border(1.dp, if (item.isError) ErrorRed.copy(alpha = 0.5f) else CyanGlow, RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = NeonCardBg)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "YOU: ${item.userText}",
                                color = TextPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "JARVIS: ${item.responseText}",
                                color = if (item.isError) ErrorRed else ElectricCyan,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Glowing Mic Section & Voice Wave
            GlowingMicSection(
                isListening = viewModel.isListening,
                rmsDb = viewModel.currentRms,
                onMicClick = onMicClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Call Confirmation Dialog
        viewModel.pendingCallContact?.let { contact ->
            AlertDialog(
                onDismissRequest = onCancelCall,
                containerColor = NeonCardBg,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = ElectricCyan)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Confirm Phone Call", color = ElectricCyan, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text("Initiate a phone call to:", color = TextPrimary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(contact.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(contact.phoneNumber, color = ElectricBlue, fontSize = 14.sp)
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { onConfirmCall(contact) },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                    ) {
                        Text("Call", color = SpaceBlack, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = onCancelCall,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun GlowingMicSection(
    isListening: Boolean,
    rmsDb: Float,
    onMicClick: () -> Unit
) {
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(isListening) {
        if (isListening) {
            pulseScale.animateTo(
                targetValue = 1.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(800, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )
        } else {
            pulseScale.snapTo(1f)
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(140.dp)
    ) {
        if (isListening) {
            // Glowing outer ripple
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(pulseScale.value)
                    .clip(CircleShape)
                    .background(CyanGlow)
            )
        }

        // Main Mic Button
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(80.dp)
                .shadow(elevation = if (isListening) 20.dp else 6.dp, shape = CircleShape, spotColor = ElectricCyan)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = if (isListening) listOf(ElectricCyan, ElectricBlue) else listOf(NeonCardBg, DeepNavy)
                    )
                )
                .border(2.dp, ElectricCyan, CircleShape)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicOff,
                contentDescription = "Voice Input",
                tint = if (isListening) SpaceBlack else ElectricCyan,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}
