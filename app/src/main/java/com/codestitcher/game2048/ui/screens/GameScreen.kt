package com.codestitcher.game2048.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codestitcher.game2048.game.GameColors
import com.codestitcher.game2048.game.GameEngine
import com.codestitcher.game2048.game.MoveDirection
import com.codestitcher.game2048.game.Tile
import com.codestitcher.game2048.utils.SoundHapticHelper
import kotlin.math.abs

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val engine = remember { GameEngine(context) }
    val hapticHelper = remember { SoundHapticHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            hapticHelper.release()
        }
    }

    var showRestartConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GameColors.Background)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. TOP HEADER & CONTROLS (Undo & Reset moved to the top, Sound/Haptic toggles removed)
            TopHeaderSection(
                score = engine.score,
                bestScore = engine.bestScore,
                canUndo = engine.canUndo,
                onUndo = {
                    hapticHelper.performMoveHaptic()
                    hapticHelper.playSwipeSound()
                    engine.undo()
                },
                onRestart = { showRestartConfirm = true }
            )

            // 2. CENTER: Game board is strictly centered in the display
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    tiles = engine.tiles,
                    onSwipe = { direction ->
                        val moved = engine.move(
                            direction = direction,
                            onMerge = {
                                hapticHelper.playMergeSound()
                                hapticHelper.performMergeHaptic()
                            },
                            onMove = {
                                hapticHelper.playSwipeSound()
                                hapticHelper.performMoveHaptic()
                            }
                        )
                        if (moved && engine.isGameOver) {
                            hapticHelper.performGameOverHaptic()
                        }
                    }
                )
            }

            // 3. BOTTOM FOOTER TIP
            Text(
                text = "SWIPE TO MERGE TILES • REACH 2048",
                color = GameColors.TextSubtle,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // OVERLAY: WIN CELEBRATION
        AnimatedVisibility(
            visible = engine.hasWon,
            enter = fadeIn(tween(250)) + scaleIn(spring(dampingRatio = 0.7f)),
            exit = fadeOut(tween(200)) + scaleOut()
        ) {
            WinDialog(
                score = engine.score,
                onKeepPlaying = {
                    engine.dismissWinDialogAndKeepPlaying()
                },
                onNewGame = {
                    engine.startNewGame()
                }
            )
        }

        // OVERLAY: COLORFUL & SMOOTH GAME OVER DIALOG (RED ACCENT)
        AnimatedVisibility(
            visible = engine.isGameOver,
            enter = fadeIn(tween(250)) + scaleIn(spring(dampingRatio = 0.75f, stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(tween(200)) + scaleOut()
        ) {
            GameOverDialog(
                score = engine.score,
                bestScore = engine.bestScore,
                canUndo = engine.canUndo,
                onUndo = {
                    hapticHelper.performMoveHaptic()
                    engine.undo()
                },
                onNewGame = {
                    hapticHelper.performMoveHaptic()
                    engine.startNewGame()
                }
            )
        }

        // CONFIRM RESTART DIALOG
        AnimatedVisibility(
            visible = showRestartConfirm,
            enter = fadeIn(tween(200)) + scaleIn(spring(dampingRatio = 0.8f)),
            exit = fadeOut(tween(150))
        ) {
            RestartConfirmDialog(
                onConfirm = {
                    showRestartConfirm = false
                    engine.startNewGame()
                },
                onCancel = { showRestartConfirm = false }
            )
        }
    }
}

@Composable
private fun TopHeaderSection(
    score: Int,
    bestScore: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onRestart: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Line 1: Title and Score Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "2048",
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Black,
                    color = GameColors.TextWhite,
                    letterSpacing = (-1.5).sp
                )
                Text(
                    text = "CLASSIC EDITION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.GoldAccentDark,
                    letterSpacing = 1.sp
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ScoreBox(label = "SCORE", value = score)
                ScoreBox(label = "BEST", value = bestScore)
            }
        }

        // Line 2: UNDO and RESET buttons cleanly positioned on the top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeaderActionButton(
                    label = "↺  UNDO",
                    enabled = canUndo,
                    onClick = onUndo
                )
                HeaderActionButton(
                    label = "↻  RESET",
                    enabled = true,
                    onClick = onRestart
                )
            }
        }
    }
}

@Composable
private fun ScoreBox(label: String, value: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.CardBackground)
            .border(1.dp, GameColors.CardBorder, RoundedCornerShape(10.dp))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = GameColors.TextMuted,
            letterSpacing = 1.sp
        )
        Text(
            text = "$value",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = GameColors.TextWhite
        )
    }
}

@Composable
private fun HeaderActionButton(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (enabled) GameColors.CardBackground else GameColors.EmptyCell.copy(alpha = 0.4f))
            .border(
                1.dp,
                if (enabled) GameColors.CardBorder else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) GameColors.TextWhite else GameColors.TextSubtle,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun GameBoard(
    tiles: List<Tile>,
    onSwipe: (MoveDirection) -> Unit
) {
    var totalDragX by remember { mutableFloatStateOf(0f) }
    var totalDragY by remember { mutableFloatStateOf(0f) }
    var swipeConsumed by remember { mutableStateOf(false) }
    val swipeThreshold = 50f

    val padding = 10.dp
    val spacing = 8.dp

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(GameColors.BoardBackground)
            .border(1.dp, GameColors.BoardBorder, RoundedCornerShape(16.dp))
            .padding(padding)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        totalDragX = 0f
                        totalDragY = 0f
                        swipeConsumed = false
                    },
                    onDragEnd = { swipeConsumed = false },
                    onDragCancel = { swipeConsumed = false },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        if (!swipeConsumed) {
                            totalDragX += dragAmount.x
                            totalDragY += dragAmount.y

                            val absX = abs(totalDragX)
                            val absY = abs(totalDragY)

                            if (absX > swipeThreshold || absY > swipeThreshold) {
                                swipeConsumed = true
                                if (absX > absY) {
                                    if (totalDragX > 0) onSwipe(MoveDirection.RIGHT)
                                    else onSwipe(MoveDirection.LEFT)
                                } else {
                                    if (totalDragY > 0) onSwipe(MoveDirection.DOWN)
                                    else onSwipe(MoveDirection.UP)
                                }
                            }
                        }
                    }
                )
            }
    ) {
        val totalSpacing = spacing * 3
        val boardInnerWidth = maxWidth
        val cellSize = (boardInnerWidth - totalSpacing) / 4

        // 1. Draw static empty grid slots
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                val offsetX = (c * (cellSize.value + spacing.value)).dp
                val offsetY = (r * (cellSize.value + spacing.value)).dp
                Box(
                    modifier = Modifier
                        .offset(x = offsetX, y = offsetY)
                        .size(cellSize)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GameColors.EmptyCell)
                )
            }
        }

        // 2. Draw animated colorful AMOLED tiles
        tiles.forEach { tile ->
            key(tile.id) {
                AnimatedTileCell(
                    tile = tile,
                    cellSize = cellSize,
                    spacing = spacing
                )
            }
        }
    }
}

@Composable
private fun AnimatedTileCell(
    tile: Tile,
    cellSize: Dp,
    spacing: Dp
) {
    val targetX = (tile.col * (cellSize.value + spacing.value)).dp
    val targetY = (tile.row * (cellSize.value + spacing.value)).dp

    // Ultra-smooth slide animation
    val animatedX by animateDpAsState(
        targetValue = targetX,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "tileX_${tile.id}"
    )
    val animatedY by animateDpAsState(
        targetValue = targetY,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "tileY_${tile.id}"
    )

    // Smooth scale pop on spawn or merge
    val scaleAnim = remember { Animatable(if (tile.isNew) 0.1f else 1f) }

    LaunchedEffect(tile.id, tile.value) {
        if (tile.isNew) {
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        } else if (tile.isMerged) {
            scaleAnim.snapTo(1.2f)
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .offset(x = animatedX, y = animatedY)
            .size(cellSize)
            .scale(scaleAnim.value)
            .clip(RoundedCornerShape(10.dp))
            .background(GameColors.getTileBrush(tile.value))
            .border(
                width = 1.dp,
                color = GameColors.getTileBorderColor(tile.value),
                shape = RoundedCornerShape(10.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        val fontSize = when {
            tile.value < 100 -> 30.sp
            tile.value < 1000 -> 25.sp
            tile.value < 10000 -> 19.sp
            else -> 14.sp
        }

        Text(
            text = "${tile.value}",
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            color = GameColors.getTileTextColor(tile.value),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GameOverDialog(
    score: Int,
    bestScore: Int,
    canUndo: Boolean,
    onUndo: () -> Unit,
    onNewGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1117))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(GameColors.RedAccent, GameColors.RedAccentDark.copy(alpha = 0.4f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Vivid Red Header with glow
            Text(
                text = "GAME OVER",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.RedAccent,
                letterSpacing = 2.sp
            )
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = "No more moves left!",
                fontSize = 13.sp,
                color = GameColors.TextMuted
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Score Summary Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF161B26))
                    .border(1.dp, Color(0xFF242C3D), RoundedCornerShape(12.dp))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GameColors.TextMuted)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$score", fontSize = 22.sp, fontWeight = FontWeight.Black, color = GameColors.TextWhite)
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(Color(0xFF242C3D))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "BEST", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GameColors.GoldAccentDark)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "$bestScore", fontSize = 22.sp, fontWeight = FontWeight.Black, color = GameColors.GoldAccent)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Vibrant Red "TRY AGAIN" Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(GameColors.RedAccent, GameColors.RedAccentDark)
                            )
                        )
                        .clickable { onNewGame() }
                        .padding(vertical = 13.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "TRY AGAIN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }

                // If user can undo
                if (canUndo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF181D29))
                            .border(1.dp, GameColors.RedAccent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .clickable { onUndo() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "↺  UNDO LAST MOVE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GameColors.TextWhite,
                            letterSpacing = 0.8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WinDialog(
    score: Int,
    onKeepPlaying: () -> Unit,
    onNewGame: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.88f)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF0F1117))
                .border(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        listOf(GameColors.GoldAccent, GameColors.GoldAccentDark)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOU WIN!",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = GameColors.GoldAccent,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Congratulations! You reached the 2048 tile with a score of $score!",
                fontSize = 13.sp,
                color = GameColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF181D29))
                        .border(1.dp, GameColors.CardBorder, RoundedCornerShape(12.dp))
                        .clickable { onKeepPlaying() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "KEEP PLAYING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GameColors.TextWhite)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(listOf(GameColors.GoldAccent, GameColors.GoldAccentDark))
                        )
                        .clickable { onNewGame() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "NEW GAME", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.Black)
                }
            }
        }
    }
}

@Composable
private fun RestartConfirmDialog(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {},
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF0F1117))
                .border(1.dp, GameColors.CardBorder, RoundedCornerShape(18.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RESET GAME?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.TextWhite
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your current progress will be lost.",
                fontSize = 13.sp,
                color = GameColors.TextMuted,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF181D29))
                        .border(1.dp, GameColors.CardBorder, RoundedCornerShape(10.dp))
                        .clickable { onCancel() }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "CANCEL", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GameColors.TextMuted)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GameColors.RedAccent)
                        .clickable { onConfirm() }
                        .padding(vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "RESET", fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}
