package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HangmanGameTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HangmanGame()
                }
            }
        }
    }
}

@Composable
fun HangmanGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6366F1),
            secondary = Color(0xFF10B981),
            tertiary = Color(0xFFF59E0B),
            background = Color(0xFFF8FAFC),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF1E293B),
            onSurface = Color(0xFF1E293B),
            error = Color(0xFFEF4444),
            onError = Color.White
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanGame() {
    // Game Data with safe fallback
    val words = remember {
        listOf(
            "ANDROID", "KOTLIN", "COMPOSE", "MOBILE", "DEVELOPMENT",
            "PROGRAMMING", "COMPUTER", "TECHNOLOGY", "SOFTWARE", "CODING",
            "JETPACK", "GOOGLE", "STUDIO", "APPLICATION", "FRAMEWORK",
            "DESIGN", "INTERFACE", "CREATIVE", "INNOVATION", "DIGITAL",
            "PHONE", "TABLET", "SCREEN", "BUTTON", "CAMERA"
        )
    }

    // Game State with safe initialization
    var currentWord by remember {
        mutableStateOf(
            try {
                words.randomOrNull() ?: "ANDROID"
            } catch (e: Exception) {
                "ANDROID"
            }
        )
    }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableStateOf(0) }
    var gameScore by remember { mutableStateOf(0) }
    var isAnimating by remember { mutableStateOf(false) }

    val maxWrongGuesses = 6
    val hapticFeedback = LocalHapticFeedback.current

    // Game Logic with safe checks
    val gameWon = try {
        currentWord.isNotEmpty() && currentWord.all { it in guessedLetters }
    } catch (e: Exception) {
        false
    }

    val gameLost = wrongGuesses >= maxWrongGuesses
    val gameOver = gameWon || gameLost

    // Safe game reset function
    fun resetGame() {
        try {
            currentWord = words.randomOrNull() ?: "ANDROID"
            guessedLetters = setOf()
            wrongGuesses = 0
            isAnimating = false
        } catch (e: Exception) {
            currentWord = "ANDROID"
            guessedLetters = setOf()
            wrongGuesses = 0
            isAnimating = false
        }
    }

    // Safe guess function with haptic feedback
    fun makeGuess(letter: Char) {
        if (letter in guessedLetters || gameOver || isAnimating) return

        try {
            isAnimating = true
            guessedLetters = guessedLetters + letter

            if (letter in currentWord) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                gameScore += 10
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                wrongGuesses++
            }
        } catch (e: Exception) {
            // Fallback in case of error
            guessedLetters = guessedLetters + letter
            if (letter !in currentWord) {
                wrongGuesses++
            }
        }
    }

    // Animation delay effect
    LaunchedEffect(isAnimating) {
        if (isAnimating) {
            delay(300)
            isAnimating = false
        }
    }

    // UI Layout with improved design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2),
                        Color(0xFF6366F1)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Header Section
            GameHeader(score = gameScore)

            // Hangman Drawing Card
            HangmanDrawingCard(wrongGuesses = wrongGuesses)

            // Progress Indicator
            GameProgressIndicator(
                wrongGuesses = wrongGuesses,
                maxWrongGuesses = maxWrongGuesses
            )

            // Word Display
            WordDisplayCard(
                currentWord = currentWord,
                guessedLetters = guessedLetters,
                isAnimating = isAnimating
            )

            // Game Status
            AnimatedVisibility(
                visible = gameOver,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                GameStatusCard(
                    gameWon = gameWon,
                    currentWord = currentWord,
                    score = gameScore
                )
            }

            // Alphabet Grid
            AlphabetGrid(
                guessedLetters = guessedLetters,
                currentWord = currentWord,
                gameOver = gameOver,
                isAnimating = isAnimating,
                onLetterClick = { makeGuess(it) }
            )

            // New Game Button
            NewGameButton(
                onClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    resetGame()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun GameHeader(score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎯 HANGMAN GAME 🎯",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF6366F1),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                color = Color(0xFF10B981).copy(alpha = 0.1f)
            ) {
                Text(
                    text = "Score: $score",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun HangmanDrawingCard(wrongGuesses: Int) {
    val scale by animateFloatAsState(
        targetValue = if (wrongGuesses > 0) 1f else 0.95f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "hangman_scale"
    )

    Card(
        modifier = Modifier
            .size(240.dp)
            .scale(scale)
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            drawColorfulHangman(wrongGuesses)
        }
    }
}

@Composable
fun GameProgressIndicator(wrongGuesses: Int, maxWrongGuesses: Int) {
    val progress by animateFloatAsState(
        targetValue = wrongGuesses.toFloat() / maxWrongGuesses,
        animationSpec = tween(durationMillis = 500),
        label = "progress_animation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = when {
                wrongGuesses <= 2 -> Color(0xFF10B981).copy(alpha = 0.1f)
                wrongGuesses <= 4 -> Color(0xFFF59E0B).copy(alpha = 0.1f)
                else -> Color(0xFFEF4444).copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wrong Guesses: $wrongGuesses / $maxWrongGuesses",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF10B981)
                    wrongGuesses <= 4 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF10B981)
                    wrongGuesses <= 4 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                },
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun WordDisplayCard(currentWord: String, guessedLetters: Set<Char>, isAnimating: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Guess the Word",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = try {
                    currentWord.mapIndexed { index, letter ->
                        if (letter in guessedLetters) letter else '_'
                    }.joinToString("  ")
                } catch (e: Exception) {
                    "_ _ _ _ _ _ _"
                },
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color(0xFF6366F1),
                modifier = Modifier.animateContentSize()
            )
        }
    }
}

@Composable
fun GameStatusCard(gameWon: Boolean, currentWord: String, score: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (gameWon)
                Color(0xFF10B981).copy(alpha = 0.1f)
            else
                Color(0xFFEF4444).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (gameWon) "🎉 EXCELLENT! 🎉" else "💀 GAME OVER 💀",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (!gameWon) {
                Text(
                    text = "The word was: $currentWord",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Surface(
                modifier = Modifier.clip(RoundedCornerShape(12.dp)),
                color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444)
            ) {
                Text(
                    text = "Final Score: $score",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AlphabetGrid(
    guessedLetters: Set<Char>,
    currentWord: String,
    gameOver: Boolean,
    isAnimating: Boolean,
    onLetterClick: (Char) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(220.dp)
    ) {
        items(('A'..'Z').toList()) { letter ->
            LetterButton(
                letter = letter,
                isGuessed = letter in guessedLetters,
                isCorrect = letter in currentWord,
                isEnabled = !gameOver && !isAnimating,
                onClick = { onLetterClick(letter) }
            )
        }
    }
}

@Composable
fun LetterButton(
    letter: Char,
    isGuessed: Boolean,
    isCorrect: Boolean,
    isEnabled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isGuessed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "letter_scale"
    )

    Button(
        onClick = onClick,
        enabled = isEnabled && !isGuessed,
        modifier = Modifier
            .aspectRatio(1f)
            .scale(scale)
            .shadow(
                elevation = if (isGuessed) 2.dp else 6.dp,
                shape = CircleShape
            ),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                !isGuessed -> Color(0xFF6366F1)
                isCorrect -> Color(0xFF10B981)
                else -> Color(0xFFEF4444)
            },
            disabledContainerColor = when {
                isCorrect -> Color(0xFF10B981).copy(alpha = 0.7f)
                isGuessed -> Color(0xFFEF4444).copy(alpha = 0.7f)
                else -> Color(0xFF6366F1).copy(alpha = 0.5f)
            }
        )
    ) {
        Text(
            text = letter.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF10B981)
        ),
        shape = RoundedCornerShape(30.dp)
    ) {
        Text(
            text = "🎮 NEW GAME 🎮",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

fun DrawScope.drawColorfulHangman(wrongGuesses: Int) {
    val strokeWidth = 8.dp.toPx()
    val gallowsColor = Color(0xFF8D6E63)

    try {
        // Base
        if (wrongGuesses >= 1) {
            drawLine(
                color = gallowsColor,
                start = Offset(50.dp.toPx(), size.height - 30.dp.toPx()),
                end = Offset(150.dp.toPx(), size.height - 30.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Pole
        if (wrongGuesses >= 2) {
            drawLine(
                color = gallowsColor,
                start = Offset(100.dp.toPx(), size.height - 30.dp.toPx()),
                end = Offset(100.dp.toPx(), 40.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Top beam
        if (wrongGuesses >= 3) {
            drawLine(
                color = gallowsColor,
                start = Offset(100.dp.toPx(), 40.dp.toPx()),
                end = Offset(150.dp.toPx(), 40.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // Noose
        if (wrongGuesses >= 4) {
            drawLine(
                color = Color(0xFF795548),
                start = Offset(150.dp.toPx(), 40.dp.toPx()),
                end = Offset(150.dp.toPx(), 70.dp.toPx()),
                strokeWidth = strokeWidth - 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Head
        if (wrongGuesses >= 5) {
            drawCircle(
                color = Color(0xFFFFE0B2),
                radius = 15.dp.toPx(),
                center = Offset(150.dp.toPx(), 85.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF2196F3),
                radius = 15.dp.toPx(),
                center = Offset(150.dp.toPx(), 85.dp.toPx()),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
            )

            // Eyes
            drawCircle(
                color = Color.Black,
                radius = 2.5.dp.toPx(),
                center = Offset(145.dp.toPx(), 82.dp.toPx())
            )
            drawCircle(
                color = Color.Black,
                radius = 2.5.dp.toPx(),
                center = Offset(155.dp.toPx(), 82.dp.toPx())
            )

            // Mouth
            drawLine(
                color = Color(0xFFEF4444),
                start = Offset(145.dp.toPx(), 90.dp.toPx()),
                end = Offset(155.dp.toPx(), 90.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Body and limbs
        if (wrongGuesses >= 6) {
            // Body
            drawLine(
                color = Color(0xFF2196F3),
                start = Offset(150.dp.toPx(), 100.dp.toPx()),
                end = Offset(150.dp.toPx(), 160.dp.toPx()),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )

            // Arms
            drawLine(
                color = Color(0xFFFF5722),
                start = Offset(150.dp.toPx(), 120.dp.toPx()),
                end = Offset(125.dp.toPx(), 140.dp.toPx()),
                strokeWidth = strokeWidth - 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFFFF5722),
                start = Offset(150.dp.toPx(), 120.dp.toPx()),
                end = Offset(175.dp.toPx(), 140.dp.toPx()),
                strokeWidth = strokeWidth - 2.dp.toPx(),
                cap = StrokeCap.Round
            )

            // Legs
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(150.dp.toPx(), 160.dp.toPx()),
                end = Offset(130.dp.toPx(), 185.dp.toPx()),
                strokeWidth = strokeWidth - 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0xFF4CAF50),
                start = Offset(150.dp.toPx(), 160.dp.toPx()),
                end = Offset(170.dp.toPx(), 185.dp.toPx()),
                strokeWidth = strokeWidth - 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    } catch (e: Exception) {
        // Fallback: draw a simple stick figure if there's any error
        drawCircle(
            color = Color.Black,
            radius = 10.dp.toPx(),
            center = Offset(size.width / 2, size.height / 3)
        )
    }
}