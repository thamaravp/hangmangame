package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HangmanTheme {
                HangmanGame()
            }
        }
    }
}

@Composable
fun HangmanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6366F1),
            secondary = Color(0xFF06B6D4),
            background = Color(0xFF1E293B),
            surface = Color(0xFF334155),
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}

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
        "CODING" to "Writing instructions for computers"
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

    // Main UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        GameHeader()

        // Hangman Drawing
        HangmanDrawingCard(wrongGuesses = wrongGuesses)

        // Progress
        GameProgressIndicator(
            wrongGuesses = wrongGuesses,
            maxWrongGuesses = maxWrongGuesses
        )

        // Word Display
        WordDisplayCard(
            currentWord = currentWord,
            guessedLetters = guessedLetters
        )

        // Hint Section
        HintSection(
            hint = currentHint,
            hintShown = hintShown,
            onShowHint = { showHint() },
            gameOver = gameOver
        )

        // Game Status
        if (gameOver) {
            GameStatusCard(
                gameWon = gameWon,
                currentWord = currentWord
            )
        }

        // Alphabet Grid
        AlphabetGrid(
            guessedLetters = guessedLetters,
            currentWord = currentWord,
            gameOver = gameOver,
            onLetterClick = { makeGuess(it) }
        )

        // New Game Button
        NewGameButton(onClick = { resetGame() })

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun GameHeader() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = "🎯 HANGMAN",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}

@Composable
fun HangmanDrawingCard(wrongGuesses: Int) {
    Card(
        modifier = Modifier.size(200.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            drawHangman(wrongGuesses)
        }
    }
}

@Composable
fun GameProgressIndicator(wrongGuesses: Int, maxWrongGuesses: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Wrong Guesses",
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    text = "$wrongGuesses / $maxWrongGuesses",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { wrongGuesses.toFloat() / maxWrongGuesses },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF10B981)
                    wrongGuesses <= 4 -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }
            )
        }
    }
}

@Composable
fun WordDisplayCard(currentWord: String, guessedLetters: Set<Char>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = currentWord.map { letter ->
                if (letter in guessedLetters) letter else '_'
            }.joinToString("  "),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        )
    }
}

@Composable
fun HintSection(
    hint: String,
    hintShown: Boolean,
    onShowHint: () -> Unit,
    gameOver: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💡 Hint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Button(
                    onClick = onShowHint,
                    enabled = !hintShown && !gameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B)
                    ),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Text(
                        text = "!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            if (hintShown) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF59E0B).copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = hint,
                        fontSize = 14.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameStatusCard(gameWon: Boolean, currentWord: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (gameWon) "🎉 VICTORY! 🎉" else "💀 GAME OVER 💀",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444),
                textAlign = TextAlign.Center
            )
            if (!gameWon) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "The word was: $currentWord",
                    fontSize = 16.sp,
                    color = Color.White
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
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(12.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.height(180.dp)
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
                        }
                    ),
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
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
fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8B5CF6)
        ),
        shape = RoundedCornerShape(25.dp)
    ) {
        Text(
            text = "🎮 NEW GAME",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

fun DrawScope.drawHangman(wrongGuesses: Int) {
    val strokeWidth = 6.dp.toPx()
    val color = Color.White

    // Base
    if (wrongGuesses >= 1) {
        drawLine(
            color = color,
            start = Offset(40.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(120.dp.toPx(), size.height - 20.dp.toPx()),
            strokeWidth = strokeWidth
        )
    }

    // Pole
    if (wrongGuesses >= 2) {
        drawLine(
            color = color,
            start = Offset(80.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(80.dp.toPx(), 40.dp.toPx()),
            strokeWidth = strokeWidth
        )
    }

    // Top beam
    if (wrongGuesses >= 3) {
        drawLine(
            color = color,
            start = Offset(80.dp.toPx(), 40.dp.toPx()),
            end = Offset(120.dp.toPx(), 40.dp.toPx()),
            strokeWidth = strokeWidth
        )
    }

    // Noose
    if (wrongGuesses >= 4) {
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 40.dp.toPx()),
            end = Offset(120.dp.toPx(), 60.dp.toPx()),
            strokeWidth = strokeWidth - 2.dp.toPx()
        )
    }

    // Head
    if (wrongGuesses >= 5) {
        drawCircle(
            color = color,
            radius = 12.dp.toPx(),
            center = Offset(120.dp.toPx(), 75.dp.toPx()),
            style = Stroke(width = 4.dp.toPx())
        )
    }

    // Body and limbs
    if (wrongGuesses >= 6) {
        // Body
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 87.dp.toPx()),
            end = Offset(120.dp.toPx(), 130.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        // Arms
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 100.dp.toPx()),
            end = Offset(105.dp.toPx(), 115.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 100.dp.toPx()),
            end = Offset(135.dp.toPx(), 115.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        // Legs
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 130.dp.toPx()),
            end = Offset(110.dp.toPx(), 150.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 130.dp.toPx()),
            end = Offset(130.dp.toPx(), 150.dp.toPx()),
            strokeWidth = 4.dp.toPx()
        )
    }
}