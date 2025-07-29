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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main Activity class that serves as the entry point for the Hangman game application.
 * Extends ComponentActivity to support Jetpack Compose UI.
 */
class MainActivity : ComponentActivity() {
    /**
     * Called when the activity is first created.
     * Sets up the Compose content with the custom theme and main game component.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Apply custom dark theme to the entire app
            HangmanTheme {
                // Launch the main game screen
                HangmanGame()
            }
        }
    }
}

/**
 * Custom theme composable that defines the color scheme and styling for the Hangman game.
 * Uses Material 3 dark color scheme with custom colors for a modern, dark appearance.
 *
 * @param content The composable content to apply the theme to
 */
@Composable
fun HangmanTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF6366F1),        // Indigo primary color
            secondary = Color(0xFF06B6D4),      // Cyan secondary color
            background = Color(0xFF1E293B),     // Dark slate background
            surface = Color(0xFF334155),        // Lighter slate for cards/surfaces
            onPrimary = Color.White,            // White text on primary
            onSecondary = Color.White,          // White text on secondary
            onBackground = Color.White,         // White text on background
            onSurface = Color.White             // White text on surfaces
        ),
        content = content
    )
}

/**
 * Main game composable that contains all the game logic and UI components.
 * Manages the complete Hangman game state and user interactions.
 */
@Composable
fun HangmanGame() {
    // ==================== GAME DATA ====================

    /**
     * Dictionary of words with their corresponding hints.
     * Each entry contains a word (key) and its hint (value) to help players guess.
     */
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

    // ==================== GAME STATE ====================

    /**
     * Current word-hint pair selected for the game.
     * Randomly chosen from the wordsWithHints map.
     */
    var currentWordEntry by remember { mutableStateOf(wordsWithHints.entries.random()) }

    /**
     * The current word that the player needs to guess (uppercase).
     */
    var currentWord by remember { mutableStateOf(currentWordEntry.key) }

    /**
     * The hint associated with the current word.
     */
    var currentHint by remember { mutableStateOf(currentWordEntry.value) }

    /**
     * Set of letters that the player has already guessed.
     * Used to track progress and prevent duplicate guesses.
     */
    var guessedLetters by remember { mutableStateOf(setOf<Char>()) }

    /**
     * Number of incorrect guesses made by the player.
     * Determines how much of the hangman figure to draw.
     */
    var wrongGuesses by remember { mutableStateOf(0) }

    /**
     * Boolean flag indicating whether the hint has been revealed to the player.
     */
    var hintShown by remember { mutableStateOf(false) }

    /**
     * Maximum number of wrong guesses allowed before the game ends.
     * Corresponds to the complete hangman drawing (6 parts).
     */
    val maxWrongGuesses = 6

    // ==================== GAME LOGIC ====================

    /**
     * Checks if the player has won by guessing all letters in the word.
     */
    val gameWon = currentWord.all { it in guessedLetters }

    /**
     * Checks if the player has lost by exceeding the maximum wrong guesses.
     */
    val gameLost = wrongGuesses >= maxWrongGuesses

    /**
     * Determines if the game is over (either won or lost).
     */
    val gameOver = gameWon || gameLost

    /**
     * Resets the game to its initial state with a new random word.
     * Called when the player starts a new game.
     */
    fun resetGame() {
        currentWordEntry = wordsWithHints.entries.random()
        currentWord = currentWordEntry.key
        currentHint = currentWordEntry.value
        guessedLetters = setOf()
        wrongGuesses = 0
        hintShown = false
    }

    /**
     * Processes a letter guess from the player.
     * Updates game state based on whether the guess is correct or incorrect.
     *
     * @param letter The letter guessed by the player
     */
    fun makeGuess(letter: Char) {
        // Only process if letter hasn't been guessed and game isn't over
        if (letter !in guessedLetters && !gameOver) {
            guessedLetters = guessedLetters + letter
            // If letter is not in the word, increment wrong guesses
            if (letter !in currentWord) {
                wrongGuesses++
            }
        }
    }

    /**
     * Reveals the hint to the player.
     * Can only be used once per game.
     */
    fun showHint() {
        hintShown = true
    }

    // ==================== RESPONSIVE LAYOUT SETUP ====================

    /**
     * Get device configuration for responsive design.
     */
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    /**
     * Calculate safe top padding based on screen height to handle different device types.
     * Accounts for notches, punch holes, and status bars on various devices.
     */
    val safeTopPadding = when {
        screenHeight > 800.dp -> 48.dp // Phones with notch/punch hole
        screenHeight > 700.dp -> 32.dp // Regular tall phones
        else -> 24.dp // Compact phones
    }

    // ==================== MAIN UI LAYOUT ====================

    /**
     * Main container with dark background that fills the entire screen.
     */
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E293B))
    ) {
        /**
         * Scrollable column containing all game components.
         * Handles system bars and provides proper spacing between elements.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding() // Automatically handles system bars
                .verticalScroll(rememberScrollState()) // Makes content scrollable
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    top = 16.dp, // Additional top padding after status bar
                    bottom = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp) // 16dp spacing between all children
        ) {
            // Extra spacing at the top for better visual balance
            Spacer(modifier = Modifier.height(8.dp))

            // Game title header
            GameHeader()

            // Hangman drawing that updates based on wrong guesses
            HangmanDrawingCard(wrongGuesses = wrongGuesses)

            // Progress indicator showing wrong guesses vs maximum allowed
            GameProgressIndicator(
                wrongGuesses = wrongGuesses,
                maxWrongGuesses = maxWrongGuesses
            )

            // Display of the current word with guessed letters revealed
            WordDisplayCard(
                currentWord = currentWord,
                guessedLetters = guessedLetters
            )

            // Hint section with button to reveal hint
            HintSection(
                hint = currentHint,
                hintShown = hintShown,
                onShowHint = { showHint() },
                gameOver = gameOver
            )

            // Game over message (victory or defeat)
            if (gameOver) {
                GameStatusCard(
                    gameWon = gameWon,
                    currentWord = currentWord
                )
            }

            // Interactive alphabet grid for letter selection
            AlphabetGrid(
                guessedLetters = guessedLetters,
                currentWord = currentWord,
                gameOver = gameOver,
                onLetterClick = { makeGuess(it) }
            )

            // Button to start a new game
            NewGameButton(onClick = { resetGame() })

            // Bottom safe area spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Header component displaying the game title with emoji and styling.
 * Uses a card design for visual consistency with other components.
 */
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

/**
 * Card component containing the hangman drawing canvas.
 * The drawing updates progressively based on the number of wrong guesses.
 *
 * @param wrongGuesses Current number of incorrect guesses (0-6)
 */
@Composable
fun HangmanDrawingCard(wrongGuesses: Int) {
    Card(
        modifier = Modifier.size(200.dp), // Square card for the drawing
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        /**
         * Canvas for custom drawing of the hangman figure.
         * Uses the drawHangman extension function to render based on wrong guesses.
         */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp) // Inner padding for the drawing area
        ) {
            drawHangman(wrongGuesses)
        }
    }
}

/**
 * Progress indicator showing the player's current status in the game.
 * Displays wrong guesses count and a colored progress bar.
 *
 * @param wrongGuesses Current number of incorrect guesses
 * @param maxWrongGuesses Maximum allowed incorrect guesses
 */
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
            // Header row showing label and current progress
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

            /**
             * Progress bar with color coding based on danger level:
             * - Green (0-2 wrong): Safe zone
             * - Yellow (3-4 wrong): Warning zone
             * - Red (5-6 wrong): Danger zone
             */
            LinearProgressIndicator(
                progress = { wrongGuesses.toFloat() / maxWrongGuesses },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when {
                    wrongGuesses <= 2 -> Color(0xFF10B981) // Green
                    wrongGuesses <= 4 -> Color(0xFFF59E0B) // Yellow
                    else -> Color(0xFFEF4444) // Red
                }
            )
        }
    }
}

/**
 * Component displaying the current word with guessed letters revealed.
 * Shows underscores for unguessed letters and actual letters for guessed ones.
 *
 * @param currentWord The word to be guessed
 * @param guessedLetters Set of letters already guessed by the player
 */
@Composable
fun WordDisplayCard(currentWord: String, guessedLetters: Set<Char>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF334155)),
        shape = RoundedCornerShape(16.dp)
    ) {
        /**
         * Transform each letter in the word:
         * - If guessed: show the actual letter
         * - If not guessed: show underscore
         * Join with double spaces for better readability
         */
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

/**
 * Hint section component with a button to reveal the hint and display area.
 * The hint can only be revealed once per game and is disabled when game is over.
 *
 * @param hint The hint text for the current word
 * @param hintShown Whether the hint has been revealed
 * @param onShowHint Callback function to reveal the hint
 * @param gameOver Whether the game has ended
 */
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
            // Header row with hint label and reveal button
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

                /**
                 * Circular button to reveal hint.
                 * Disabled if hint already shown or game is over.
                 */
                Button(
                    onClick = onShowHint,
                    enabled = !hintShown && !gameOver,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B) // Orange color
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

            // Show hint text if it has been revealed
            if (hintShown) {
                Spacer(modifier = Modifier.height(12.dp))

                /**
                 * Hint display card with semi-transparent orange background
                 * to distinguish it from other content.
                 */
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

/**
 * Game status card shown when the game ends (win or lose).
 * Displays appropriate message and reveals the word if the player lost.
 *
 * @param gameWon Whether the player won the game
 * @param currentWord The word that was being guessed
 */
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
            // Victory or defeat message with appropriate emoji and color
            Text(
                text = if (gameWon) "🎉 VICTORY! 🎉" else "💀 GAME OVER 💀",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (gameWon) Color(0xFF10B981) else Color(0xFFEF4444), // Green for win, red for loss
                textAlign = TextAlign.Center
            )

            // Show the correct word if the player lost
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

/**
 * Interactive alphabet grid allowing players to select letters.
 * Uses a 6-column grid layout with color-coded buttons based on guess status.
 *
 * @param guessedLetters Set of letters already guessed
 * @param currentWord The current word being guessed
 * @param gameOver Whether the game has ended
 * @param onLetterClick Callback function when a letter is clicked
 */
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
        /**
         * Lazy vertical grid for efficient rendering of alphabet buttons.
         * Fixed 6 columns to fit nicely on most screen sizes.
         */
        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(12.dp),
            modifier = Modifier.height(180.dp) // Fixed height to prevent layout shifts
        ) {
            // Generate buttons for each letter A-Z
            items(('A'..'Z').toList()) { letter ->
                val isGuessed = letter in guessedLetters
                val isCorrect = letter in currentWord

                /**
                 * Letter button with color coding:
                 * - Blue: Not yet guessed (default state)
                 * - Green: Guessed correctly (letter is in word)
                 * - Red: Guessed incorrectly (letter not in word)
                 */
                Button(
                    onClick = { onLetterClick(letter) },
                    enabled = !isGuessed && !gameOver, // Disable if already guessed or game over
                    colors = ButtonDefaults.buttonColors(
                        containerColor = when {
                            !isGuessed -> Color(0xFF6366F1) // Blue for unguessed
                            isCorrect -> Color(0xFF10B981)   // Green for correct
                            else -> Color(0xFFEF4444)        // Red for incorrect
                        }
                    ),
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp) // Remove default padding for circular buttons
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

/**
 * New game button component to restart the game.
 * Full-width button with game controller emoji and purple styling.
 *
 * @param onClick Callback function to start a new game
 */
@Composable
fun NewGameButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF8B5CF6) // Purple color
        ),
        shape = RoundedCornerShape(25.dp) // Rounded pill shape
    ) {
        Text(
            text = "🎮 NEW GAME",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

/**
 * Extension function for DrawScope to draw the hangman figure progressively.
 * Each wrong guess adds another part to the drawing, creating suspense.
 *
 * The drawing sequence:
 * 1. Base platform
 * 2. Vertical pole
 * 3. Horizontal beam
 * 4. Noose
 * 5. Head
 * 6. Body and limbs (arms and legs)
 *
 * @param wrongGuesses Number of wrong guesses (0-6) determining how much to draw
 */
fun DrawScope.drawHangman(wrongGuesses: Int) {
    val strokeWidth = 6.dp.toPx() // Thickness of main structural lines
    val color = Color.White // All drawing in white for visibility on dark background

    // 1. Base platform - foundation of the gallows
    if (wrongGuesses >= 1) {
        drawLine(
            color = color,
            start = Offset(40.dp.toPx(), size.height - 20.dp.toPx()), // Left end of base
            end = Offset(120.dp.toPx(), size.height - 20.dp.toPx()),   // Right end of base
            strokeWidth = strokeWidth
        )
    }

    // 2. Vertical pole - main support structure
    if (wrongGuesses >= 2) {
        drawLine(
            color = color,
            start = Offset(80.dp.toPx(), size.height - 20.dp.toPx()), // Bottom of pole (on base)
            end = Offset(80.dp.toPx(), 40.dp.toPx()),                 // Top of pole
            strokeWidth = strokeWidth
        )
    }

    // 3. Horizontal beam - top crossbeam
    if (wrongGuesses >= 3) {
        drawLine(
            color = color,
            start = Offset(80.dp.toPx(), 40.dp.toPx()),  // Left end (top of pole)
            end = Offset(120.dp.toPx(), 40.dp.toPx()),   // Right end
            strokeWidth = strokeWidth
        )
    }

    // 4. Noose - hanging rope
    if (wrongGuesses >= 4) {
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 40.dp.toPx()),  // Top (end of beam)
            end = Offset(120.dp.toPx(), 60.dp.toPx()),    // Bottom (where head will be)
            strokeWidth = strokeWidth - 2.dp.toPx()       // Slightly thinner than structural elements
        )
    }

    // 5. Head - circular outline representing the victim's head
    if (wrongGuesses >= 5) {
        drawCircle(
            color = color,
            radius = 12.dp.toPx(),                        // Head radius
            center = Offset(120.dp.toPx(), 75.dp.toPx()), // Center below noose
            style = Stroke(width = 4.dp.toPx())           // Outline only, not filled
        )
    }

    // 6. Body and limbs - final stage showing complete figure
    if (wrongGuesses >= 6) {
        // Body - vertical line from head to waist
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 87.dp.toPx()),  // Bottom of head
            end = Offset(120.dp.toPx(), 130.dp.toPx()),   // Waist level
            strokeWidth = 4.dp.toPx()                     // Thinner than structural elements
        )

        // Left arm - diagonal line from shoulder
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 100.dp.toPx()), // Shoulder level
            end = Offset(105.dp.toPx(), 115.dp.toPx()),   // Left hand position
            strokeWidth = 4.dp.toPx()
        )

        // Right arm - diagonal line from shoulder
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 100.dp.toPx()), // Shoulder level
            end = Offset(135.dp.toPx(), 115.dp.toPx()),   // Right hand position
            strokeWidth = 4.dp.toPx()
        )

        // Left leg - diagonal line from waist
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 130.dp.toPx()), // Waist level
            end = Offset(110.dp.toPx(), 150.dp.toPx()),   // Left foot position
            strokeWidth = 4.dp.toPx()
        )

        // Right leg - diagonal line from waist
        drawLine(
            color = color,
            start = Offset(120.dp.toPx(), 130.dp.toPx()), // Waist level
            end = Offset(130.dp.toPx(), 150.dp.toPx()),   // Right foot position
            strokeWidth = 4.dp.toPx()
        )
    }
}