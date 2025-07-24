package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
        // Enable edge-to-edge display
        enableEdgeToEdge()
        setContent {
            HangmanGameTheme {
                HangmanGame()
            }
        }
    }
}

@Composable
fun HangmanGameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6200EA),
            secondary = Color(0xFF03DAC6),
            tertiary = Color(0xFFFF6B35),
            background = Color(0xFFF8F9FA),
            surface = Color.White,
            onPrimary = Color.White,
            onSecondary = Color.Black,
            onBackground = Color(0xFF1C1B1F),
            onSurface = Color(0xFF1C1B1F)
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

    // UI Layout with proper system UI padding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF667eea),
                        Color(0xFF764ba2)
                    )
                )
            )
            // Apply system UI padding to avoid notch/status bar overlap
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            GameHeader()

            // Game Content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                    guessedLetters = guessedLetters
                )

                // Game Status
                if (gameOver) {
                    GameStatusCard(
                        gameWon = gameWon,
                        currentWord = currentWord
                    )
                }
            }

            // Bottom Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Alphabet Grid
                AlphabetGrid(
                    guessedLetters = guessedLetters,
                    currentWord = currentWord,
                    gameOver = gameOver,
                    onLetterClick = { makeGuess(it) }
                )

                // New Game Button
                NewGameButton(onClick = { resetGame() })
            }
        }
    }
}

@Composable
fun GameHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "🎯 HANGMAN GAME 🎯",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF6200EA),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Composable
fun HangmanDrawingCard(wrongGuesses: Int) {
    Card(
        modifier = Modifier
            .size(220.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            drawColorfulHangman(wrongGuesses)
        }
    }
}

@Composable
fun GameProgressIndicator(wrongGuesses: Int, maxWrongGuesses: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = when {
                wrongGuesses <= 2 -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                wrongGuesses <= 4 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> Color(0xFFF44336).copy(alpha = 0.1f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wrong Guesses: $wrongGuesses / $maxWrongGuesses",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF4CAF50)
                    wrongGuesses <= 4 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                }
            )
            LinearProgressIndicator(
                progress = wrongGuesses.toFloat() / maxWrongGuesses,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF4CAF50)
                    wrongGuesses <= 4 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                trackColor = Color.Gray.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun WordDisplayCard(currentWord: String, guessedLetters: Set<Char>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = currentWord.map { letter ->
                if (letter in guessedLetters) letter else '_'
            }.joinToString("  "),
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color(0xFF6200EA),
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Composable
fun GameStatusCard(gameWon: Boolean, currentWord: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = if (gameWon)
                Color(0xFF4CAF50).copy(alpha = 0.1f)
            else
                Color(0xFFF44336).copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (gameWon) "🎉 CONGRATULATIONS! 🎉" else "💀 GAME OVER 💀",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameWon) Color(0xFF4CAF50) else Color(0xFFF44336),
                textAlign = TextAlign.Center
            )
            if (!gameWon) {
                Text(
                    text = "The word was: $currentWord",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(top = 8.dp)
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
    onLetterClick: (Char) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.height(200.dp)
    ) {
        items(('A'..'Z').toList()) { letter ->
            val isGuessed = letter in guessedLetters
            val isCorrect = letter in currentWord
            Button(
                onClick = { onLetterClick(letter) },
                enabled = !isGuessed && !gameOver,
                colors = ButtonDefaults.buttonColors(
                    containerColor = when {
                        !isGuessed -> Color(0xFF6200EA)
                        isCorrect -> Color(0xFF4CAF50)
                        else -> Color(0xFFF44336)
                    },
                    disabledContainerColor = when {
                        isCorrect -> Color(0xFF4CAF50).copy(alpha = 0.7f)
                        isGuessed -> Color(0xFFF44336).copy(alpha = 0.7f)
                        else -> Color(0xFF6200EA).copy(alpha = 0.5f)
                    }
                ),
                modifier = Modifier
                    .aspectRatio(1f)
                    .shadow(4.dp, CircleShape),
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
fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .shadow(8.dp, RoundedCornerShape(28.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF03DAC6)
        ),
        shape = RoundedCornerShape(28.dp)
    ) {
        Text(
            text = "🎮 NEW GAME 🎮",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
    }
}

fun DrawScope.drawColorfulHangman(wrongGuesses: Int) {
    val strokeWidth = 6.dp.toPx()
    // Gallows (Brown color)
    val gallowsColor = Color(0xFF8D6E63)

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
            color = Color(0xFF795548),
            start = Offset(140.dp.toPx(), 30.dp.toPx()),
            end = Offset(140.dp.toPx(), 55.dp.toPx()),
            strokeWidth = strokeWidth - 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Hangman figure (Colorful)
    val figureColor = Color(0xFF2196F3)

    // Head
    if (wrongGuesses >= 5) {
        drawCircle(
            color = Color(0xFFFFDBCB), // Skin color
            radius = 12.dp.toPx(),
            center = Offset(140.dp.toPx(), 70.dp.toPx())
        )
        drawCircle(
            color = figureColor,
            radius = 12.dp.toPx(),
            center = Offset(140.dp.toPx(), 70.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
        )
        // Eyes
        drawCircle(
            color = Color.Black,
            radius = 2.dp.toPx(),
            center = Offset(136.dp.toPx(), 67.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = 2.dp.toPx(),
            center = Offset(144.dp.toPx(), 67.dp.toPx())
        )
        // Mouth (sad face)
        drawLine(
            color = Color.Red,
            start = Offset(136.dp.toPx(), 75.dp.toPx()),
            end = Offset(144.dp.toPx(), 75.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Body and limbs
    if (wrongGuesses >= 6) {
        // Body
        drawLine(
            color = figureColor,
            start = Offset(140.dp.toPx(), 82.dp.toPx()),
            end = Offset(140.dp.toPx(), 130.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Left arm
        drawLine(
            color = Color(0xFFFF5722),
            start = Offset(140.dp.toPx(), 100.dp.toPx()),
            end = Offset(120.dp.toPx(), 115.dp.toPx()),
            strokeWidth = strokeWidth - 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Right arm
        drawLine(
            color = Color(0xFFFF5722),
            start = Offset(140.dp.toPx(), 100.dp.toPx()),
            end = Offset(160.dp.toPx(), 115.dp.toPx()),
            strokeWidth = strokeWidth - 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Left leg
        drawLine(
            color = Color(0xFF4CAF50),
            start = Offset(140.dp.toPx(), 130.dp.toPx()),
            end = Offset(125.dp.toPx(), 150.dp.toPx()),
            strokeWidth = strokeWidth - 1.dp.toPx(),
            cap = StrokeCap.Round
        )
        // Right leg
        drawLine(
            color = Color(0xFF4CAF50),
            start = Offset(140.dp.toPx(), 130.dp.toPx()),
            end = Offset(155.dp.toPx(), 150.dp.toPx()),
            strokeWidth = strokeWidth - 1.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}