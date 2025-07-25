package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
    // Game Data with hints
    val wordsWithHints = mapOf(
        "ANDROID" to "Google's mobile operating system",
        "KOTLIN" to "Modern programming language for Android",
        "COMPOSE" to "Android's modern UI toolkit",
        "MOBILE" to "Portable electronic device",
        "DEVELOPMENT" to "Process of creating software",
        "PROGRAMMING" to "Writing computer code",
        "COMPUTER" to "Electronic device for processing data",
        "TECHNOLOGY" to "Application of scientific knowledge",
        "SOFTWARE" to "Computer programs and applications",
        "CODING" to "Writing instructions for computers",
        "JETPACK" to "Android development components",
        "GOOGLE" to "Tech company that created Android",
        "STUDIO" to "IDE for Android development",
        "APPLICATION" to "Software program for users",
        "FRAMEWORK" to "Structure for building software",
        "DESIGN" to "Planning the appearance and function",
        "INTERFACE" to "Point of interaction between systems",
        "CREATIVE" to "Using imagination and original ideas",
        "INNOVATION" to "Introduction of new ideas or methods",
        "DIGITAL" to "Relating to computer technology"
    )

    // Game State
    var currentWordEntry by remember { mutableStateOf(wordsWithHints.entries.random()) }
    var currentWord by remember { mutableStateOf(currentWordEntry.key) }
    var currentHint by remember { mutableStateOf(currentWordEntry.value) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableStateOf(0) }
    var hintShown by remember { mutableStateOf(false) }
    val maxWrongGuesses = 6

    // Game Logic
    val gameWon = currentWord.all { it in guessedLetters }
    val gameLost = wrongGuesses >= maxWrongGuesses
    val gameOver = gameWon || gameLost

    fun resetGame() {
        currentWordEntry = wordsWithHints.entries.random()
        currentWord = currentWordEntry.key
        currentHint = currentWordEntry.value
        guessedLetters = setOf()
        wrongGuesses = 0
        hintShown = false
    }

    fun makeGuess(letter: Char) {
        if (letter !in guessedLetters && !gameOver) {
            guessedLetters = guessedLetters + letter
            if (letter !in currentWord) {
                wrongGuesses++
            }
        }
    }

    fun showHint() {
        hintShown = true
    }

    // Background with image and overlay
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        // Background Image
        // Replace R.drawable.game_background with your actual image resource
        // If you don't have an image yet, comment out this Image composable
        Image(
            painter = painterResource(id = R.drawable.game_background), // Replace with your image
            contentDescription = "Game Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Semi-transparent overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.3f)
                        )
                    )
                )
        )

        // Game Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            ModernGameHeader()

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

            // Hint Section
            ModernHintSection(
                hint = currentHint,
                hintShown = hintShown,
                onShowHint = { showHint() },
                gameOver = gameOver
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

            // Alphabet Grid (Back again!)
            ModernAlphabetGrid(
                guessedLetters = guessedLetters,
                currentWord = currentWord,
                gameOver = gameOver,
                onLetterClick = { makeGuess(it) }
            )

            // New Game Button
            ModernNewGameButton(onClick = { resetGame() })

            // Add some bottom padding to ensure scrolling works properly
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun ModernGameHeader() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(24.dp))
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = "🎯 HANGMAN",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        )
    }
}

@Composable
fun ModernHangmanDrawingCard(wrongGuesses: Int) {
    Card(
        modifier = Modifier
            .size(220.dp)
            .border(2.dp, Color.White, RoundedCornerShape(32.dp))
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(32.dp),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(32.dp)
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

@Composable
fun ModernGameProgressIndicator(wrongGuesses: Int, maxWrongGuesses: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    color = Color.White
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
                trackColor = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun ModernWordDisplayCard(currentWord: String, guessedLetters: Set<Char>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(24.dp))
            .shadow(12.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Text(
            text = currentWord.map { letter ->
                if (letter in guessedLetters) letter else '_'
            }.joinToString("  "),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        )
    }
}

@Composable
fun ModernHintSection(
    hint: String,
    hintShown: Boolean,
    onShowHint: () -> Unit,
    gameOver: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Hint",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Button(
                    onClick = onShowHint,
                    enabled = !hintShown && !gameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        disabledContainerColor = Color(0xFF94A3B8)
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(48.dp)
                ) {
                    Text(
                        text = "!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }

            AnimatedVisibility(
                visible = hintShown,
                enter = slideInVertically() + fadeIn(),
                exit = slideOutVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.border(1.dp, Color.White, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = hint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ModernGameStatusCard(gameWon: Boolean, currentWord: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(24.dp))
            .shadow(16.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (gameWon) "🎉 VICTORY! 🎉" else "💀 GAME OVER 💀",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )
            if (!gameWon) {
                Text(
                    text = "The word was: $currentWord",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, Color.White, RoundedCornerShape(20.dp))
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .height(200.dp)
                .padding(16.dp)
        ) {
            items(('A'..'Z').toList()) { letter ->
                val isGuessed = letter in guessedLetters
                val isCorrect = letter in currentWord
                Button(
                    onClick = { onLetterClick(letter) },
                    enabled = !isGuessed && !gameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !isGuessed -> Color(0xFF667eea)
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
                            elevation = if (!isGuessed && !gameOver) 4.dp else 2.dp,
                            shape = CircleShape
                        ),
                    shape = CircleShape
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
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
            .height(60.dp)
            .border(2.dp, Color.White, RoundedCornerShape(30.dp))
            .shadow(12.dp, RoundedCornerShape(30.dp)),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF764ba2)
        ),
        shape = RoundedCornerShape(30.dp)
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
    // Modern gallows with white color for visibility
    val gallowsColor = Color.White

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
            color = Color.White,
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
            color = Color.White,
            radius = 15.dp.toPx(),
            center = Offset(140.dp.toPx(), 70.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = figureStroke)
        )
        // Eyes
        drawCircle(
            color = Color.White,
            radius = 3.dp.toPx(),
            center = Offset(135.dp.toPx(), 67.dp.toPx())
        )
        drawCircle(
            color = Color.White,
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

    // Body and limbs with white colors
    if (wrongGuesses >= 6) {
        // Body
        drawLine(
            color = Color.White,
            start = Offset(140.dp.toPx(), 85.dp.toPx()),
            end = Offset(140.dp.toPx(), 135.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
        // Left arm
        drawLine(
            color = Color.White,
            start = Offset(140.dp.toPx(), 105.dp.toPx()),
            end = Offset(120.dp.toPx(), 120.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
        // Right arm
        drawLine(
            color = Color.White,
            start = Offset(140.dp.toPx(), 105.dp.toPx()),
            end = Offset(160.dp.toPx(), 120.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
        // Left leg
        drawLine(
            color = Color.White,
            start = Offset(140.dp.toPx(), 135.dp.toPx()),
            end = Offset(125.dp.toPx(), 155.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
        // Right leg
        drawLine(
            color = Color.White,
            start = Offset(140.dp.toPx(), 135.dp.toPx()),
            end = Offset(155.dp.toPx(), 155.dp.toPx()),
            strokeWidth = figureStroke,
            cap = StrokeCap.Round
        )
    }
}