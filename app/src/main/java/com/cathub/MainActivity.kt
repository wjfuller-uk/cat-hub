package com.cathub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.cathub.data.model.*
import com.cathub.ui.cats.CatView
import com.cathub.ui.theme.CatHubTheme
import com.cathub.voice.VoiceService
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val audioGranted = permissions[Manifest.permission.RECORD_AUDIO] == true
        if (audioGranted) {
            startVoiceService()
        } else {
            Toast.makeText(this, "Microphone permission needed for voice", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatHubTheme {
                CatHubScreen(onStartVoice = { requestPermissionsAndStart() })
            }
        }
    }

    private fun requestPermissionsAndStart() {
        val perms = arrayOf(Manifest.permission.RECORD_AUDIO)
        val allGranted = perms.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (allGranted) {
            startVoiceService()
        } else {
            permissionLauncher.launch(perms)
        }
    }

    private fun startVoiceService() {
        val intent = Intent(this, VoiceService::class.java)
        startForegroundService(intent)
        Toast.makeText(this, "🐱 Cat Hub is listening!", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun CatHubScreen(onStartVoice: () -> Unit) {
    val context = LocalContext.current
    val currentHour = remember { LocalTime.now().hour }

    // Voice state
    var isListening by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var isConnected by remember { mutableStateOf(false) }
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var transcript by remember { mutableStateOf("") }
    var responseText by remember { mutableStateOf("") }
    var voiceActive by remember { mutableStateOf(false) }

    // Wire up voice callbacks
    LaunchedEffect(Unit) {
        VoiceService.onWakeWord = {
            isListening = true
            transcript = ""
            responseText = ""
        }
        VoiceService.onTranscript = { text, isFinal ->
            transcript = text
            if (isFinal) isListening = false
        }
        VoiceService.onResponseText = { text ->
            responseText = text
            isSpeaking = true
        }
        VoiceService.onSpeakingChanged = { speaking ->
            isSpeaking = speaking
            if (!speaking) {
                // Reset after speaking
                isListening = false
            }
        }
        VoiceService.onConnectionChanged = { connected, status ->
            isConnected = connected
            connectionStatus = status
        }
    }

    // Determine time of day
    val timeOfDay = remember(currentHour) {
        when {
            currentHour in 5..6 -> TimeOfDay.DAWN
            currentHour in 7..11 -> TimeOfDay.MORNING
            currentHour in 12..16 -> TimeOfDay.AFTERNOON
            currentHour in 17..19 -> TimeOfDay.EVENING
            currentHour in 20..21 -> TimeOfDay.DUSK
            else -> TimeOfDay.NIGHT
        }
    }

    val backgroundGradient = remember(timeOfDay) {
        when (timeOfDay) {
            TimeOfDay.DAWN -> Brush.verticalGradient(listOf(Color(0xFFFFB74D), Color(0xFFFFCC02), Color(0xFFFFF176)))
            TimeOfDay.MORNING -> Brush.verticalGradient(listOf(Color(0xFF81D4FA), Color(0xFFB3E5FC), Color(0xFFE1F5FE)))
            TimeOfDay.AFTERNOON -> Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF64B5F6), Color(0xFF90CAF9)))
            TimeOfDay.EVENING -> Brush.verticalGradient(listOf(Color(0xFFFF8A65), Color(0xFFFFAB91), Color(0xFFFFCC80)))
            TimeOfDay.DUSK -> Brush.verticalGradient(listOf(Color(0xFF7E57C2), Color(0xFF9575CD), Color(0xFFB39DDB)))
            TimeOfDay.NIGHT -> Brush.verticalGradient(listOf(Color(0xFF1A237E), Color(0xFF283593), Color(0xFF3949AB)))
        }
    }

    val worldState = WorldState(timeOfDay = timeOfDay, weather = Weather.SUNNY, season = Season.SPRING)

    // Determine cat states based on voice
    val willState = when {
        isSpeaking -> CatState.TALKING
        isListening -> CatState.LISTENING
        transcript.isNotEmpty() && !isSpeaking -> CatState.THINKING
        else -> CatStateRules.determineState(FullerFamily.WILL, worldState)
    }
    val lucyState = CatStateRules.determineState(FullerFamily.LUCY, worldState)
    val imogenState = CatStateRules.determineState(FullerFamily.IMOGEN, worldState)

    Box(modifier = Modifier.fillMaxSize().background(backgroundGradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header with connection status
            Text("🐱 Cat Hub", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(
                connectionStatus,
                fontSize = 12.sp,
                color = if (isConnected) Color(0xFF81C784) else Color(0xFFEF9A9A),
                modifier = Modifier.padding(bottom = 4.dp),
            )

            // Time
            Text(
                "${String.format("%02d", currentHour)}:${String.format("%02d", LocalTime.now().minute)}",
                fontSize = 48.sp, fontWeight = FontWeight.Light, color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 24.dp),
            )

            // Family cats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                CatView(member = FullerFamily.WILL, catState = willState)
                CatView(member = FullerFamily.LUCY, catState = lucyState)
                CatView(member = FullerFamily.IMOGEN, catState = imogenState)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Transcript bubble
            if (transcript.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.2f)),
                ) {
                    Text(
                        "🎤 $transcript",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Response bubble
            if (responseText.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.25f)),
                ) {
                    Text(
                        "🐱 $responseText",
                        modifier = Modifier.padding(16.dp),
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            // Voice button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable {
                        if (!VoiceService.isActive) {
                            onStartVoice()
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isListening) Color(0xFF4CAF50).copy(alpha = 0.3f)
                    else if (isSpeaking) Color(0xFFFF9800).copy(alpha = 0.3f)
                    else Color.White.copy(alpha = 0.15f)
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = when {
                            isListening -> "🎤 Listening..."
                            isSpeaking -> "🔊 Speaking..."
                            VoiceService.isActive -> "💤 Say \"Jarvis...\""
                            else -> "🎤 Tap to start"
                        },
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 16.sp,
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
