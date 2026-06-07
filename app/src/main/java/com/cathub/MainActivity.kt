package com.cathub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cathub.data.model.*
import com.cathub.ui.cats.CatView
import com.cathub.ui.theme.CatHubTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalTime

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CatHubTheme {
                CatHubScreen()
            }
        }
    }
}

@Composable
fun CatHubScreen() {
    // Current time for determining state
    val currentHour = remember { LocalTime.now().hour }

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

    // Background gradient based on time
    val backgroundGradient = remember(timeOfDay) {
        when (timeOfDay) {
            TimeOfDay.DAWN -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFB74D), // Orange
                    Color(0xFFFFCC02), // Yellow
                    Color(0xFFFFF176), // Light yellow
                )
            )
            TimeOfDay.MORNING -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF81D4FA), // Light blue
                    Color(0xFFB3E5FC), // Lighter blue
                    Color(0xFFE1F5FE), // Very light blue
                )
            )
            TimeOfDay.AFTERNOON -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF42A5F5), // Blue
                    Color(0xFF64B5F6), // Lighter blue
                    Color(0xFF90CAF9), // Light blue
                )
            )
            TimeOfDay.EVENING -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFF8A65), // Deep orange
                    Color(0xFFFFAB91), // Light orange
                    Color(0xFFFFCC80), // Amber
                )
            )
            TimeOfDay.DUSK -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7E57C2), // Purple
                    Color(0xFF9575CD), // Light purple
                    Color(0xFFB39DDB), // Very light purple
                )
            )
            TimeOfDay.NIGHT -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A237E), // Dark blue
                    Color(0xFF283593), // Medium dark blue
                    Color(0xFF3949AB), // Blue
                )
            )
        }
    }

    // World state
    val worldState = remember(timeOfDay) {
        WorldState(
            timeOfDay = timeOfDay,
            weather = Weather.SUNNY, // TODO: Fetch from API
            season = Season.SPRING,  // TODO: Calculate from date
        )
    }

    // Family members with determined states
    val familyMembers = remember(worldState) {
        FullerFamily.ALL.map { member ->
            val catState = CatStateRules.determineState(member, worldState)
            member to catState
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Header
            Text(
                text = "🐱 Cat Hub",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
            )

            // Time display
            Text(
                text = "${String.format("%02d", currentHour)}:${String.format("%02d", LocalTime.now().minute)}",
                fontSize = 48.sp,
                fontWeight = FontWeight.Light,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(bottom = 32.dp),
            )

            // Family cats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                familyMembers.forEach { (member, catState) ->
                    CatView(
                        member = member,
                        catState = catState,
                        onTap = {
                            // TODO: Show detail card
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Schedule preview (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                ),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                ) {
                    Text(
                        text = "📅 Today",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Will: Team standup (14:00), Pick up Imogen (15:30)",
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Text(
                        text = "Imogen: Maths (09:00), Art (11:00), Swimming (14:00)",
                        color = Color.White.copy(alpha = 0.8f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice indicator (placeholder)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.15f),
                ),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "🎤",
                        fontSize = 24.sp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Say \"Jarvis...\" or tap to talk",
                        color = Color.White.copy(alpha = 0.7f),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
