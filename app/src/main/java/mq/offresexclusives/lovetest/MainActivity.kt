package com.example.lovetest

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.security.MessageDigest
import kotlin.math.abs

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoveTestTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    LoveTestApp()
                }
            }
        }
    }
}

private sealed class Screen {
    data object Home : Screen()
    data object Result : Screen()
}

@Composable
private fun LoveTestApp() {
    var screen by rememberSaveable { mutableStateOf<Screen>(Screen.Home) }
    var yourName by rememberSaveable { mutableStateOf("") }
    var crushName by rememberSaveable { mutableStateOf("") }
    var score by rememberSaveable { mutableIntStateOf(0) }

    when (screen) {
        Screen.Home -> HomeScreen(
            yourName = yourName,
            crushName = crushName,
            onYourNameChange = { yourName = it },
            onCrushNameChange = { crushName = it },
            onTest = {
                score = computeLoveScore(yourName, crushName)
                screen = Screen.Result
            }
        )

        Screen.Result -> ResultScreen(
            yourName = yourName,
            crushName = crushName,
            score = score,
            onBack = { screen = Screen.Home },
            onTryAgain = {
                yourName = ""
                crushName = ""
                screen = Screen.Home
            }
        )
    }
}

@Composable
private fun HomeScreen(
    yourName: String,
    crushName: String,
    onYourNameChange: (String) -> Unit,
    onCrushNameChange: (String) -> Unit,
    onTest: () -> Unit
) {
    val focus = LocalFocusManager.current
    val canTest = yourName.trim().isNotEmpty() && crushName.trim().isNotEmpty()

    val bg = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.25f),
            MaterialTheme.colorScheme.background
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Spacer(Modifier.height(16.dp))

                Icon(
                    imageVector = Icons.Rounded.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(10.dp))

                Text(
                    text = "Love Test",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Enter two names and get a fun compatibility score ✨",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )

                Spacer(Modifier.height(22.dp))

                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        OutlinedTextField(
                            value = yourName,
                            onValueChange = onYourNameChange,
                            label = { Text("Your name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = crushName,
                            onValueChange = onCrushNameChange,
                            label = { Text("Crush name") },
                            leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focus.clearFocus()
                                if (canTest) onTest()
                            }),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = {
                                focus.clearFocus()
                                onTest()
                            },
                            enabled = canTest,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                        ) {
                            Text("Test compatibility", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Text(
                text = "Tip: The same names will always give the same score.",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun ResultScreen(
    yourName: String,
    crushName: String,
    score: Int,
    onBack: () -> Unit,
    onTryAgain: () -> Unit
) {
    val context = LocalContext.current
    val bg = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.20f),
            MaterialTheme.colorScheme.background
        )
    )

    val progress = remember { Animatable(0f) }
    LaunchedEffect(score) {
        progress.snapTo(0f)
        progress.animateTo(score / 100f, animationSpec = tween(900))
    }

    val (title, subtitle) = remember(score) { scoreMessage(score) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                TextButton(onClick = onBack) { Text("Back") }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$yourName  +  $crushName",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(22.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress.value },
                                strokeWidth = 14.dp,
                                strokeCap = StrokeCap.Round,
                                modifier = Modifier.size(190.dp)
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "$score%",
                                    fontSize = 40.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = "compatibility",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(14.dp))

                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = subtitle,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.70f)
                        )

                        Spacer(Modifier.height(18.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onTryAgain,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text("Try again")
                            }

                            Button(
                                onClick = {
                                    val text = "Love Test: $yourName + $crushName = $score% 💗"
                                    val share = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, text)
                                    }
                                    context.startActivity(Intent.createChooser(share, "Share result"))
                                },
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                            ) {
                                Text("Share")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
        }
    }
}

/** Deterministic score from names (0..100). */
private fun computeLoveScore(a: String, b: String): Int {
    val n1 = a.trim().lowercase()
    val n2 = b.trim().lowercase()
    val input = "love_test_v1|$n1|$n2"
    val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())

    // Use first 4 bytes to create an int
    var num = 0
    for (i in 0 until 4) {
        num = (num shl 8) or (bytes[i].toInt() and 0xFF)
    }
    return abs(num) % 101
}

private fun scoreMessage(score: Int): Pair<String, String> = when (score) {
    in 0..19 -> "Hmm… low vibes 😅" to "Still, friendships and good energy can surprise you!"
    in 20..39 -> "Not bad 👀" to "Could work if you both put effort into it."
    in 40..59 -> "Pretty good ✨" to "You two might actually match more than you think."
    in 60..79 -> "Strong match 💖" to "Nice! That’s a solid compatibility score."
    else -> "Top tier 🔥💘" to "Okay wow—this is giving soulmate energy (for fun 😄)."
}

/* ---------- Simple Theme (Material 3) ---------- */

@Composable
private fun LoveTestTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = androidx.compose.ui.graphics.Color(0xFF7C4DFF),
            secondary = androidx.compose.ui.graphics.Color(0xFFFF4081),
            tertiary = androidx.compose.ui.graphics.Color(0xFF00BFA5)
        ),
        typography = Typography(),
        content = content
    )
}
