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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ModernHangmanTheme {
                HangmanGame()
            }
        }
    }
}

@Composable
fun ModernHangmanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6366F1), // Indigo
            secondary = Color(0xFF06B6D4), // Cyan
            tertiary = Color(0xFF8B5CF6), // Purple
            background = Color(0xFFF8FAFC), // Slate 50
            surface = Color.White,
            surfaceVariant = Color(0xFFF1F5F9), // Slate 100
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color(0xFF0F172A), // Slate 900
            onSurface = Color(0xFF1E293B), // Slate 800
            error = Color(0xFFEF4444), // Red 500
            onError = Color.White
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanGame() {
    // Game Data
    val words = listOf(
        "ANDROID", "KOTLIN", "COMPOSE", "MOBILE", "DEVELOPMENT",
        "PROGRAMMING", "COMPUTER", "TECHNOLOGY", "SOFTWARE", "CODING",
        "JETPACK", "GOOGLE", "STUDIO", "APPLICATION", "FRAMEWORK",
        "DESIGN", "INTERFACE", "CREATIVE", "INNOVATION", "DIGITAL"
    )

    // Game State
    var currentWord by remember { mutableStateOf(words.random()) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableStateOf(0) }
    val maxWrongGuesses = 6

    // Game Logic
    val gameWon = currentWord.all { it in guessedLetters }
    val gameLost = wrongGuesses >= maxWrongGuesses
    val gameOver = gameWon || gameLost

    fun resetGame() {
        currentWord = words.random()
        guessedLetters = setOf()
        wrongGuesses = 0
    }

    fun makeGuess(letter: Char) {
        if (letter !in guessedLetters && !gameOver) {
            guessedLetters = guessedLetters + letter
            if (letter !in currentWord) {
                wrongGuesses++
            }
        }
    }

    // Modern gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6366F1).copy(alpha = 0.1f),
                        Color(0xFF8B5CF6).copy(alpha = 0.05f),
                        Color(0xFFF8FAFC)
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            ModernGameHeader()

            // Game Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Hangman Drawing Card
                ModernHangmanDrawingCard(wrongGuesses = wrongGuesses)

                // Progress Indicator
                ModernGameProgressIndicator(
                    wrongGuesses = wrongGuesses,
                    maxWrongGuesses = maxWrongGuesses
                )

                // Word Display
                ModernWordDisplayCard(
                    currentWord = currentWord,
                    guessedLetters = guessedLetters
                )

                // Game Status
                AnimatedVisibility(
                    visible = gameOver,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    ModernGameStatusCard(
                        gameWon = gameWon,
                        currentWord = currentWord
                    )
                }
            }

            // Bottom Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Alphabet Grid
                ModernAlphabetGrid(
                    guessedLetters = guessedLetters,
                    currentWord = currentWord,
                    gameOver = gameOver,
                    onLetterClick = { makeGuess(it) }
                )

                // New Game Button
                ModernNewGameButton(onClick = { resetGame() })
            }
        }
    }
}

@Composable
fun ModernGameHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.05f),
                            Color(0xFF8B5CF6).copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Text(
                text = "🎯 HANGMAN",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF6366F1),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }
}

@Composable
fun ModernHangmanDrawingCard(wrongGuesses: Int) {
    Card(
        modifier = Modifier
            .size(240.dp)
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.1f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFF8FAFC),
                            Color(0xFFF1F5F9)
                        )
                    )
                )
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                drawModernHangman(wrongGuesses)
            }
        }
    }
}

@Composable
fun ModernGameProgressIndicator(wrongGuesses: Int, maxWrongGuesses: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Wrong Guesses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = "$wrongGuesses / $maxWrongGuesses",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        wrongGuesses <= 2 -> Color(0xFF10B981)
                        wrongGuesses <= 4 -> Color(0xFFF59E0B)
                        else -> Color(0xFFEF4444)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { wrongGuesses.toFloat() / maxWrongGuesses },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF10B981)
                    wrongGuesses <= 4 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                },
                trackColor = Color(0xFFE2E8F0)
            )
        }
    }
}

@Composable
fun ModernWordDisplayCard(currentWord: String, guessedLetters: Set<Char>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Text(
                text = currentWord.map { letter ->
                    if (letter in guessedLetters) letter else '_'
                }.joinToString("  "),
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                color = Color(0xFF6366F1),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp)
            )
        }
    }
}

@Composable
fun ModernGameStatusCard(gameWon: Boolean, currentWord: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = if (gameWon) listOf(
                            Color(0xFF10B981).copy(alpha = 0.1f),
                            Color(0xFF10B981).copy(alpha = 0.05f)
                        ) else listOf(
                            Color(0xFFEF4444).copy(alpha = 0.1f),
                            Color(0xFFEF4444).copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (gameWon) "🎉 VICTORY! 🎉" else "💀 GAME OVER 💀",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444),
                    textAlign = TextAlign.Center
                )
                if (!gameWon) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "The word was: $currentWord",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernAlphabetGrid(
    guessedLetters: Set<Char>,
    currentWord: String,
    gameOver: Boolean,
    onLetterClick: (Char) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.height(220.dp)
    ) {
        items(('A'..'Z').toList()) { letter ->
            val isGuessed = letter in guessedLetters
            val isCorrect = letter in currentWord

            Button(
                onClick = { onLetterClick(letter) },
                enabled = !isGuessed && !gameOver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !isGuessed -> Color(0xFF6366F1)
                        isCorrect -> Color(0xFF10B981)
                        else -> Color(0xFFEF4444)
                    },
                    disabledContainerColor = when {
                        isCorrect -> Color(0xFF10B981).copy(alpha = 0.8f)
                        isGuessed -> Color(0xFFEF4444).copy(alpha = 0.8f)
                        else -> Color(0xFF94A3B8)
                    }
                ),
                modifier = Modifier
                    .aspectRatio(1f)
                    .shadow(
                        elevation = if (!isGuessed && !gameOver) 6.dp else 2.dp,
                        shape = CircleShape
                    ),
                shape = CircleShape
            ) {
                Text(
                    text = letter.toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun ModernNewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(12.dp, RoundedCornerShape(32.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF06B6D4)
        ),
        shape = RoundedCornerShape(32.dp)
    ) {
        Text(
            text = "🎮 NEW GAME",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

fun DrawScope.drawModernHangman(wrongGuesses: Int) {
    val strokeWidth = 8.dp.toPx()

    // Modern gallows with gradient effect
    val gallowsColor = Color(0xFF64748B)

    // Base
    if (wrongGuesses >= 1) {
        drawLine(
            color = gallowsColor,
            start = Offset(40.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(140.dp.toPx(), size.height - 20.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Pole
    if (wrongGuesses >= 2) {
        drawLine(
            color = gallowsColor,
            start = Offset(90.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(90.dp.toPx(), 30.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Top beam
    if (wrongGuesses >= 3) {
        drawLine(
            color = gallowsColor,
            start = Offset(90.dp.toPx(), 30.dp.toPx()),
            end = Offset(140.dp.toPx(), 30.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Noose
    if (wrongGuesses >= 4) {
        drawLine(
            color = Color(0xFF475569),
            start = Offset(140.dp.toPx(), 30.dp.toPx()),
            end = Offset(140.dp.toPx(), 55.dp.toPx()),
            strokeWidth = strokeWidth - 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Modern hangman figure
    val figureStroke = 6.dp.toPx()

    // Head with modern styling
    if (wrongGuesses >= 5) {
        // Head outline
        drawCircle(
            color = Color(0xFF6366F1),
            radius = 15.dp.toPx(),
            center = Offset(140.dp.toPx(), 70.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = figureStroke)
        )

        // Eyes
        drawCircle(
            color = Color(0xFF6366F1),
            radius = 3.dp.toPx(),
            center = Offset(135.dp.toPx(), 67.dp.toPx())
        )
        drawCircle(
            color = Color(0xFF6366F1),
            radius = 3.dp.toPx(),
            center = Offset(145.dp.toPx(), 67.dp.toPx())
        )

        // Sad mouth
        drawLine(
            color = Color(0xFFEF4444),
            start = Offset(135.dp.toPx(), 76.dp.toPx()),
            end = Offset(145.dp.toPx(), 76.dp.toPx()),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Body and limbs with modern colors
    if (wrongGuesses >= 6) {
        // Body
        drawLine(
            color = Color(0xFF6366F1),
            start = Offset(140.dp.toPx(), 85.dp.toPx()),
            end = Offset(140.dp.toPx(), 135.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )

        // Left arm
        drawLine(
            color = Color(0xFF8B5CF6),
            start = Offset(140.dp.toPx(), 105.dp.toPx()),
            end = Offset(120.dp.toPx(), 120.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )

        // Right arm
        drawLine(
            color = Color(0xFF8B5CF6),
            start = Offset(140.dp.toPx(), 105.dp.toPx()),
            end = Offset(160.dp.toPx(), 120.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )

        // Left leg
        drawLine(
            color = Color(0xFF06B6D4),
            start = Offset(140.dp.toPx(), 135.dp.toPx()),
            end = Offset(125.dp.toPx(), 155.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )

        // Right leg
        drawLine(
            color = Color(0xFF06B6D4),
            start = Offset(140.dp.toPx(), 135.dp.toPx()),
            end = Offset(155.dp.toPx(), 155.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
    }
}