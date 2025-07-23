package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                HangmanGame()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HangmanGame() {
    val words = listOf(
        "ANDROID", "KOTLIN", "COMPOSE", "MOBILE", "DEVELOPMENT",
        "PROGRAMMING", "COMPUTER", "TECHNOLOGY", "SOFTWARE", "CODING",
        "JETPACK", "GOOGLE", "STUDIO", "APPLICATION", "FRAMEWORK"
    )

    var currentWord by remember { mutableStateOf(words.random()) }
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }
    var wrongGuesses by remember { mutableStateOf(0) }
    val maxWrongGuesses = 6

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "HANGMAN",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hangman Drawing
        Card(
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawHangman(wrongGuesses)
            }
        }

        // Wrong guesses counter
        Text(
            text = "Wrong guesses: $wrongGuesses / $maxWrongGuesses",
            fontSize = 16.sp,
            color = if (wrongGuesses > 3) Color.Red else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Current word display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = currentWord.map { letter ->
                    if (letter in guessedLetters) letter else '_'
                }.joinToString(" "),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }

        // Game status
        if (gameOver) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (gameWon) Color.Green.copy(alpha = 0.1f)
                    else Color.Red.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (gameWon) "🎉 YOU WON! 🎉" else "💀 GAME OVER 💀",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (gameWon) Color.Green else Color.Red
                    )
                    if (gameLost) {
                        Text(
                            text = "The word was: $currentWord",
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }

        // Alphabet buttons
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            items(('A'..'Z').toList()) { letter ->
                val isGuessed = letter in guessedLetters
                val isCorrect = letter in currentWord

                Button(
                    onClick = { makeGuess(letter) },
                    enabled = !isGuessed && !gameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !isGuessed -> MaterialTheme.colorScheme.primary
                            isCorrect -> Color.Green
                            else -> Color.Red
                        },
                        disabledContainerColor = when {
                            isCorrect -> Color.Green.copy(alpha = 0.6f)
                            isGuessed -> Color.Red.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        }
                    ),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = letter.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Reset button
        Button(
            onClick = { resetGame() },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(
                text = "NEW GAME",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun DrawScope.drawHangman(wrongGuesses: Int) {
    val strokeWidth = 8.dp.toPx()
    val color = Color.Black

    // Base
    if (wrongGuesses >= 1) {
        drawLine(
            color = color,
            start = Offset(50.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(150.dp.toPx(), size.height - 20.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Pole
    if (wrongGuesses >= 2) {
        drawLine(
            color = color,
            start = Offset(100.dp.toPx(), size.height - 20.dp.toPx()),
            end = Offset(100.dp.toPx(), 30.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Top beam
    if (wrongGuesses >= 3) {
        drawLine(
            color = color,
            start = Offset(100.dp.toPx(), 30.dp.toPx()),
            end = Offset(150.dp.toPx(), 30.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Noose
    if (wrongGuesses >= 4) {
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 30.dp.toPx()),
            end = Offset(150.dp.toPx(), 60.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Head
    if (wrongGuesses >= 5) {
        drawCircle(
            color = color,
            radius = 15.dp.toPx(),
            center = Offset(150.dp.toPx(), 75.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }

    // Body
    if (wrongGuesses >= 6) {
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 90.dp.toPx()),
            end = Offset(150.dp.toPx(), 140.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Left arm
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 110.dp.toPx()),
            end = Offset(130.dp.toPx(), 120.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Right arm
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 110.dp.toPx()),
            end = Offset(170.dp.toPx(), 120.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Left leg
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 140.dp.toPx()),
            end = Offset(130.dp.toPx(), 160.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Right leg
        drawLine(
            color = color,
            start = Offset(150.dp.toPx(), 140.dp.toPx()),
            end = Offset(170.dp.toPx(), 160.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}