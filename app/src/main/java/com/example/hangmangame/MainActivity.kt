package com.example.hangmangame

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

// Data Classes and Enums
data class GameState(
    val currentWord: String = "",
    val guessedLetters: Set<Char> = emptySet(),
    val wrongGuesses: Int = 0,
    val maxWrongGuesses: Int = 6,
    val gameStatus: GameStatus = GameStatus.PLAYING,
    val score: Int = 0,
    val streak: Int = 0
) {
    fun isGameWon(): Boolean = currentWord.all { it.uppercaseChar() in guessedLetters }

    fun isGameLost(): Boolean = wrongGuesses >= maxWrongGuesses

    fun isGameOver(): Boolean = gameStatus != GameStatus.PLAYING

    fun makeGuess(letter: Char): GameState {
        if (letter in guessedLetters || isGameOver()) return this

        val newGuessedLetters = guessedLetters + letter.uppercaseChar()
        val isCorrectGuess = letter.uppercaseChar() in currentWord
        val newWrongGuesses = if (isCorrectGuess) wrongGuesses else wrongGuesses + 1

        val newGameState = copy(
            guessedLetters = newGuessedLetters,
            wrongGuesses = newWrongGuesses
        )

        return when {
            newGameState.isGameWon() -> newGameState.copy(
                gameStatus = GameStatus.WON,
                score = score + (maxWrongGuesses - wrongGuesses + 1) * 10,
                streak = streak + 1
            )
            newGameState.isGameLost() -> newGameState.copy(
                gameStatus = GameStatus.LOST,
                streak = 0
            )
            else -> newGameState
        }
    }

    fun resetGame(newWord: String): GameState = GameState(
        currentWord = newWord,
        score = score,
        streak = if (gameStatus == GameStatus.WON) streak else 0
    )
}

enum class GameStatus {
    PLAYING, WON, LOST
}

enum class Difficulty {
    EASY, MEDIUM, HARD
}

// Word Repository
object WordRepository {
    private val easyWords = listOf(
        "CAT", "DOG", "SUN", "MOON", "STAR", "TREE", "BOOK", "GAME", "LOVE", "HOME"
    )

    private val mediumWords = listOf(
        "ANDROID", "KOTLIN", "COMPOSE", "MOBILE", "CODING", "DESIGN", "STUDIO", "GOOGLE", "PHONE", "TABLET"
    )

    private val hardWords = listOf(
        "PROGRAMMING", "DEVELOPMENT", "TECHNOLOGY", "ARCHITECTURE", "FRAMEWORK", "ALGORITHM",
        "INTERFACE", "INHERITANCE", "POLYMORPHISM", "ENCAPSULATION", "ABSTRACTION", "COMPOSITION"
    )

    fun getRandomWord(difficulty: Difficulty = Difficulty.MEDIUM): String {
        return when (difficulty) {
            Difficulty.EASY -> easyWords.random()
            Difficulty.MEDIUM -> mediumWords.random()
            Difficulty.HARD -> hardWords.random()
        }
    }

    fun getHint(word: String): String {
        return when (word) {
            "ANDROID" -> "Mobile operating system by Google"
            "KOTLIN" -> "Modern programming language for Android"
            "COMPOSE" -> "Modern UI toolkit for Android"
            "PROGRAMMING" -> "The process of creating software"
            "DEVELOPMENT" -> "The process of building applications"
            "TECHNOLOGY" -> "Application of scientific knowledge"
            "ARCHITECTURE" -> "Design structure of software systems"
            "FRAMEWORK" -> "Reusable software platform"
            "ALGORITHM" -> "Step-by-step problem-solving procedure"
            else -> "A word related to technology"
        }
    }
}

// Theme
private val Purple80 = Color(0xFFD0BCFF)
private val PurpleGrey80 = Color(0xFFCCC2DC)
private val Pink80 = Color(0xFFEFB8C8)
private val Purple40 = Color(0xFF6650a4)
private val PurpleGrey40 = Color(0xFF625b71)
private val Pink40 = Color(0xFF7D5260)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun HangmanGameTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

// Main Activity
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
                    HangmanGameScreen()
                }
            }
        }
    }
}

// Main Game Screen
@Composable
fun HangmanGameScreen() {
    var gameState by remember { mutableStateOf(GameState(currentWord = WordRepository.getRandomWord())) }
    var difficulty by remember { mutableStateOf(Difficulty.MEDIUM) }
    var showHint by remember { mutableStateOf(false) }

    // Animation states
    val pulseAnimation = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulseAnimation.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    fun startNewGame() {
        gameState = gameState.resetGame(WordRepository.getRandomWord(difficulty))
        showHint = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with title and stats
        GameHeader(
            score = gameState.score,
            streak = gameState.streak,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Difficulty selector
        DifficultySelector(
            selectedDifficulty = difficulty,
            onDifficultyChanged = { newDifficulty ->
                difficulty = newDifficulty
                startNewGame()
            },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hangman drawing with animation
        AnimatedVisibility(
            visible = true,
            enter = slideInFromTop() + fadeIn(),
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            HangmanDrawing(
                wrongGuesses = gameState.wrongGuesses,
                maxWrongGuesses = gameState.maxWrongGuesses,
                modifier = Modifier.size(220.dp)
            )
        }

        // Game status with animations
        GameStatusCard(
            gameState = gameState,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .scale(if (gameState.isGameOver()) pulseScale else 1f)
        )

        // Word display with letter animations
        WordDisplay(
            word = gameState.currentWord,
            guessedLetters = gameState.guessedLetters,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Hint section
        if (showHint) {
            HintCard(
                hint = WordRepository.getHint(gameState.currentWord),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        // Hint button
        if (!gameState.isGameOver() && !showHint) {
            OutlinedButton(
                onClick = { showHint = true },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("💡 Show Hint")
            }
        }

        // Alphabet grid
        if (!gameState.isGameOver()) {
            AlphabetGrid(
                guessedLetters = gameState.guessedLetters,
                correctLetters = gameState.currentWord.toSet(),
                onLetterClick = { letter ->
                    gameState = gameState.makeGuess(letter)
                },
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        // Action buttons
        ActionButtons(
            gameState = gameState,
            onNewGame = { startNewGame() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun slideInFromTop(): EnterTransition {
    return slideInVertically(
        initialOffsetY = { -it },
        animationSpec = tween(600, easing = EaseOutBounce)
    )
}

// Game Header Component
@Composable
fun GameHeader(
    score: Int,
    streak: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title with gradient background
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = "🎯 HANGMAN MASTER",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Stats row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatCard(
                title = "Score",
                value = score.toString(),
                icon = "🏆"
            )
            StatCard(
                title = "Streak",
                value = streak.toString(),
                icon = "🔥"
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = icon,
                fontSize = 24.sp
            )
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Difficulty Selector Component
@Composable
fun DifficultySelector(
    selectedDifficulty: Difficulty,
    onDifficultyChanged: (Difficulty) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Difficulty Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Difficulty.values().forEach { difficulty ->
                    DifficultyChip(
                        difficulty = difficulty,
                        isSelected = difficulty == selectedDifficulty,
                        onClick = { onDifficultyChanged(difficulty) }
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyChip(
    difficulty: Difficulty,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "contentColor"
    )

    val (emoji, label) = when (difficulty) {
        Difficulty.EASY -> "😊" to "Easy"
        Difficulty.MEDIUM -> "🤔" to "Medium"
        Difficulty.HARD -> "😤" to "Hard"
    }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .selectable(
                selected = isSelected,
                onClick = onClick
            ),
        color = backgroundColor,
        contentColor = contentColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = emoji)
            Text(
                text = label,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

// Hangman Drawing Component
@Composable
fun HangmanDrawing(
    wrongGuesses: Int,
    maxWrongGuesses: Int,
    modifier: Modifier = Modifier
) {
    // Animation for drawing parts
    val animatedProgress by animateFloatAsState(
        targetValue = wrongGuesses.toFloat() / maxWrongGuesses.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = EaseInOutCubic),
        label = "drawingProgress"
    )

    // Shake animation when game is lost
    val shakeOffset by animateFloatAsState(
        targetValue = if (wrongGuesses >= maxWrongGuesses) 10f else 0f,
        animationSpec = if (wrongGuesses >= maxWrongGuesses) {
            infiniteRepeatable(
                animation = tween(100),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        },
        label = "shakeOffset"
    )

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawHangmanParts(wrongGuesses, shakeOffset)
            }
        }
    }
}

fun DrawScope.drawHangmanParts(wrongGuesses: Int, shakeOffset: Float) {
    val strokeWidth = 6.dp.toPx()
    val color = Color(0xFF2E2E2E)
    val centerX = size.width / 2
    val centerY = size.height / 2

    // Apply shake offset
    val offsetX = if (wrongGuesses >= 6) shakeOffset else 0f

    // Base (gallows base)
    if (wrongGuesses >= 1) {
        drawLine(
            color = color,
            start = Offset(centerX - 60.dp.toPx() + offsetX, size.height - 30.dp.toPx()),
            end = Offset(centerX + 60.dp.toPx() + offsetX, size.height - 30.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Vertical pole
    if (wrongGuesses >= 2) {
        drawLine(
            color = color,
            start = Offset(centerX - 30.dp.toPx() + offsetX, size.height - 30.dp.toPx()),
            end = Offset(centerX - 30.dp.toPx() + offsetX, 50.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Top beam
    if (wrongGuesses >= 3) {
        drawLine(
            color = color,
            start = Offset(centerX - 30.dp.toPx() + offsetX, 50.dp.toPx()),
            end = Offset(centerX + 30.dp.toPx() + offsetX, 50.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Noose
    if (wrongGuesses >= 4) {
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 50.dp.toPx()),
            end = Offset(centerX + 30.dp.toPx() + offsetX, 80.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }

    // Head
    if (wrongGuesses >= 5) {
        drawCircle(
            color = color,
            radius = 20.dp.toPx(),
            center = Offset(centerX + 30.dp.toPx() + offsetX, 100.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
    }

    // Body and limbs
    if (wrongGuesses >= 6) {
        // Body
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 120.dp.toPx()),
            end = Offset(centerX + 30.dp.toPx() + offsetX, 180.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Left arm
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 140.dp.toPx()),
            end = Offset(centerX + 10.dp.toPx() + offsetX, 160.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Right arm
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 140.dp.toPx()),
            end = Offset(centerX + 50.dp.toPx() + offsetX, 160.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Left leg
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 180.dp.toPx()),
            end = Offset(centerX + 10.dp.toPx() + offsetX, 210.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Right leg
        drawLine(
            color = color,
            start = Offset(centerX + 30.dp.toPx() + offsetX, 180.dp.toPx()),
            end = Offset(centerX + 50.dp.toPx() + offsetX, 210.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

// Game Status Card Component
@Composable
fun GameStatusCard(
    gameState: GameState,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = gameState.isGameOver(),
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) + fadeIn(),
        exit = scaleOut() + fadeOut(),
        modifier = modifier
    ) {
        when (gameState.gameStatus) {
            GameStatus.WON -> WinCard(gameState)
            GameStatus.LOST -> LoseCard(gameState)
            GameStatus.PLAYING -> Unit
        }
    }

    // Progress indicator for ongoing game
    if (!gameState.isGameOver()) {
        ProgressCard(gameState)
    }
}

@Composable
fun WinCard(gameState: GameState) {
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    val celebrationScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "celebrationScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4CAF50).copy(alpha = 0.2f),
                        Color(0xFF8BC34A).copy(alpha = 0.1f)
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉",
                fontSize = (32 * celebrationScale).sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "CONGRATULATIONS!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2E7D32),
                textAlign = TextAlign.Center
            )
            Text(
                text = "You guessed the word!",
                fontSize = 16.sp,
                color = Color(0xFF388E3C),
                modifier = Modifier.padding(top = 4.dp)
            )
            if (gameState.streak > 1) {
                Text(
                    text = "🔥 ${gameState.streak} wins in a row!",
                    fontSize = 14.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun LoseCard(gameState: GameState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFF44336).copy(alpha = 0.2f),
                        Color(0xFFE57373).copy(alpha = 0.1f)
                    )
                )
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💀",
                fontSize = 32.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "GAME OVER",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD32F2F),
                textAlign = TextAlign.Center
            )
            Text(
                text = "The word was:",
                fontSize = 14.sp,
                color = Color(0xFFE57373),
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = gameState.currentWord,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF44336),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ProgressCard(gameState: GameState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Wrong Guesses: ${gameState.wrongGuesses} / ${gameState.maxWrongGuesses}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { gameState.wrongGuesses.toFloat() / gameState.maxWrongGuesses.toFloat() },
                modifier = Modifier.fillMaxWidth(),
                color = when {
                    gameState.wrongGuesses <= 2 -> Color(0xFF4CAF50)
                    gameState.wrongGuesses <= 4 -> Color(0xFFFF9800)
                    else -> Color(0xFFF44336)
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}

// Word Display Component
@Composable
fun WordDisplay(
    word: String,
    guessedLetters: Set<Char>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsIndexed(word.toList()) { index, letter ->
                LetterBox(
                    letter = letter,
                    isRevealed = letter in guessedLetters,
                    animationDelay = index * 100
                )
            }
        }
    }
}

@Composable
fun LetterBox(
    letter: Char,
    isRevealed: Boolean,
    animationDelay: Int,
    modifier: Modifier = Modifier
) {
    // Animation for letter reveal with proper delay handling
    var shouldAnimate by remember { mutableStateOf(false) }

    LaunchedEffect(isRevealed) {
        if (isRevealed) {
            delay(animationDelay.toLong())
            shouldAnimate = true
        }
    }

    val revealAnimation by animateFloatAsState(
        targetValue = if (shouldAnimate) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "letterReveal"
    )

    // Color animation
    val backgroundColor by animateColorAsState(
        targetValue = if (isRevealed)
            MaterialTheme.colorScheme.primaryContainer
        else
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isRevealed)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.outline,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = isRevealed,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut()
        ) {
            Text(
                text = letter.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // Underscore for unrevealed letters
        if (!isRevealed) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(3.dp)
                    .background(
                        MaterialTheme.colorScheme.outline,
                        RoundedCornerShape(2.dp)
                    )
                    .align(Alignment.BottomCenter)
                    .offset(y = (-8).dp)
            )
        }
    }
}

// Alphabet Grid Component
@Composable
fun AlphabetGrid(
    guessedLetters: Set<Char>,
    correctLetters: Set<Char>,
    onLetterClick: (Char) -> Unit,
    modifier: Modifier = Modifier
) {
    val alphabet = ('A'..'Z').toList()
    val haptic = LocalHapticFeedback.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(6),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(alphabet) { index, letter ->
            LetterButton(
                letter = letter,
                isGuessed = letter in guessedLetters,
                isCorrect = letter in correctLetters,
                animationDelay = index * 50,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLetterClick(letter)
                }
            )
        }
    }
}

@Composable
fun LetterButton(
    letter: Char,
    isGuessed: Boolean,
    isCorrect: Boolean,
    animationDelay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Entry animation with proper delay handling
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(animationDelay.toLong())
        isVisible = true
    }

    // Button state animations
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !isGuessed -> MaterialTheme.colorScheme.primary
            isCorrect -> Color(0xFF4CAF50)
            else -> Color(0xFFF44336)
        },
        animationSpec = tween(300),
        label = "backgroundColor"
    )

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    Button(
        onClick = onClick,
        enabled = !isGuessed,
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = backgroundColor.copy(alpha = 0.7f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (isGuessed) 0.dp else 4.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        )
    ) {
        Text(
            text = letter.toString(),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isGuessed && !isCorrect)
                Color.White.copy(alpha = 0.7f)
            else
                Color.White
        )
    }
}

// Hint Card Component
@Composable
fun HintCard(
    hint: String,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy
            )
        ) + fadeIn(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "💡 Hint",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = hint,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

// Action Buttons Component
@Composable
fun ActionButtons(
    gameState: GameState,
    onNewGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // New Game Button with delayed animation
        var showNewGameButton by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            delay(300)
            showNewGameButton = true
        }

        AnimatedVisibility(
            visible = showNewGameButton,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + fadeIn()
        ) {
            Button(
                onClick = onNewGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (gameState.isGameOver())
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Text(
                    text = if (gameState.isGameOver()) "🎮 Play Again" else "🔄 New Game",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Additional action button for game over state with delayed animation
        var showStatsButton by remember { mutableStateOf(false) }

        LaunchedEffect(gameState.isGameOver()) {
            if (gameState.isGameOver()) {
                delay(500)
                showStatsButton = true
            } else {
                showStatsButton = false
            }
        }

        AnimatedVisibility(
            visible = showStatsButton,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy
                )
            ) + fadeIn(),
            exit = slideOutVertically() + fadeOut()
        ) {
            OutlinedButton(
                onClick = { /* Could add share functionality */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = "📊 View Stats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}