package com.codestitcher.game2048.game

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.random.Random

data class Tile(
    val id: Long,
    val value: Int,
    val row: Int,
    val col: Int,
    val isNew: Boolean = false,
    val isMerged: Boolean = false
)

data class GameSnapshot(
    val tiles: List<Tile>,
    val score: Int
)

enum class MoveDirection {
    UP, DOWN, LEFT, RIGHT
}

class GameEngine(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("codestitcher_2048_prefs", Context.MODE_PRIVATE)

    var tiles by mutableStateOf<List<Tile>>(emptyList())
        private set

    var score by mutableIntStateOf(0)
        private set

    var bestScore by mutableIntStateOf(prefs.getInt("key_best_score", 0))
        private set

    var isGameOver by mutableStateOf(false)
        private set

    var hasWon by mutableStateOf(false)
        private set

    var keepPlayingAfterWin by mutableStateOf(false)

    private var idCounter: Long = 1L

    private val undoHistory = ArrayDeque<GameSnapshot>()

    val canUndo: Boolean
        get() = undoHistory.isNotEmpty() && !isGameOver

    init {
        startNewGame()
    }

    fun dismissWinDialogAndKeepPlaying() {
        keepPlayingAfterWin = true
        hasWon = false
    }

    fun startNewGame() {
        score = 0
        isGameOver = false
        hasWon = false
        keepPlayingAfterWin = false
        undoHistory.clear()
        idCounter = 1L

        val initialTiles = mutableListOf<Tile>()
        spawnRandomTile(initialTiles)
        spawnRandomTile(initialTiles)
        tiles = initialTiles
    }

    fun move(
        direction: MoveDirection,
        onMerge: (() -> Unit)? = null,
        onMove: (() -> Unit)? = null
    ): Boolean {
        if (isGameOver) return false

        val prevSnapshot = GameSnapshot(
            tiles = tiles.map { it.copy(isNew = false, isMerged = false) },
            score = score
        )

        var moveScoreGain = 0
        var mergedOccurred = false
        var changed = false
        val newTiles = mutableListOf<Tile>()

        when (direction) {
            MoveDirection.LEFT -> {
                for (r in 0 until 4) {
                    val line = tiles.filter { it.row == r }.sortedBy { it.col }
                    var targetCol = 0
                    var i = 0
                    while (i < line.size) {
                        val current = line[i]
                        if (i + 1 < line.size && current.value == line[i + 1].value) {
                            val next = line[i + 1]
                            val mergedVal = current.value * 2
                            moveScoreGain += mergedVal
                            mergedOccurred = true
                            changed = true
                            newTiles.add(
                                Tile(
                                    id = idCounter++,
                                    value = mergedVal,
                                    row = r,
                                    col = targetCol,
                                    isMerged = true,
                                    isNew = false
                                )
                            )
                            i += 2
                        } else {
                            if (current.col != targetCol) {
                                changed = true
                            }
                            newTiles.add(
                                Tile(
                                    id = current.id,
                                    value = current.value,
                                    row = r,
                                    col = targetCol,
                                    isMerged = false,
                                    isNew = false
                                )
                            )
                            i += 1
                        }
                        targetCol++
                    }
                }
            }

            MoveDirection.RIGHT -> {
                for (r in 0 until 4) {
                    val line = tiles.filter { it.row == r }.sortedByDescending { it.col }
                    var targetCol = 3
                    var i = 0
                    while (i < line.size) {
                        val current = line[i]
                        if (i + 1 < line.size && current.value == line[i + 1].value) {
                            val next = line[i + 1]
                            val mergedVal = current.value * 2
                            moveScoreGain += mergedVal
                            mergedOccurred = true
                            changed = true
                            newTiles.add(
                                Tile(
                                    id = idCounter++,
                                    value = mergedVal,
                                    row = r,
                                    col = targetCol,
                                    isMerged = true,
                                    isNew = false
                                )
                            )
                            i += 2
                        } else {
                            if (current.col != targetCol) {
                                changed = true
                            }
                            newTiles.add(
                                Tile(
                                    id = current.id,
                                    value = current.value,
                                    row = r,
                                    col = targetCol,
                                    isMerged = false,
                                    isNew = false
                                )
                            )
                            i += 1
                        }
                        targetCol--
                    }
                }
            }

            MoveDirection.UP -> {
                for (c in 0 until 4) {
                    val line = tiles.filter { it.col == c }.sortedBy { it.row }
                    var targetRow = 0
                    var i = 0
                    while (i < line.size) {
                        val current = line[i]
                        if (i + 1 < line.size && current.value == line[i + 1].value) {
                            val next = line[i + 1]
                            val mergedVal = current.value * 2
                            moveScoreGain += mergedVal
                            mergedOccurred = true
                            changed = true
                            newTiles.add(
                                Tile(
                                    id = idCounter++,
                                    value = mergedVal,
                                    row = targetRow,
                                    col = c,
                                    isMerged = true,
                                    isNew = false
                                )
                            )
                            i += 2
                        } else {
                            if (current.row != targetRow) {
                                changed = true
                            }
                            newTiles.add(
                                Tile(
                                    id = current.id,
                                    value = current.value,
                                    row = targetRow,
                                    col = c,
                                    isMerged = false,
                                    isNew = false
                                )
                            )
                            i += 1
                        }
                        targetRow++
                    }
                }
            }

            MoveDirection.DOWN -> {
                for (c in 0 until 4) {
                    val line = tiles.filter { it.col == c }.sortedByDescending { it.row }
                    var targetRow = 3
                    var i = 0
                    while (i < line.size) {
                        val current = line[i]
                        if (i + 1 < line.size && current.value == line[i + 1].value) {
                            val next = line[i + 1]
                            val mergedVal = current.value * 2
                            moveScoreGain += mergedVal
                            mergedOccurred = true
                            changed = true
                            newTiles.add(
                                Tile(
                                    id = idCounter++,
                                    value = mergedVal,
                                    row = targetRow,
                                    col = c,
                                    isMerged = true,
                                    isNew = false
                                )
                            )
                            i += 2
                        } else {
                            if (current.row != targetRow) {
                                changed = true
                            }
                            newTiles.add(
                                Tile(
                                    id = current.id,
                                    value = current.value,
                                    row = targetRow,
                                    col = c,
                                    isMerged = false,
                                    isNew = false
                                )
                            )
                            i += 1
                        }
                        targetRow--
                    }
                }
            }
        }

        if (changed) {
            // Save undo
            if (undoHistory.size >= 10) {
                undoHistory.removeFirst()
            }
            undoHistory.addLast(prevSnapshot)

            score += moveScoreGain
            if (score > bestScore) {
                bestScore = score
                prefs.edit().putInt("key_best_score", bestScore).apply()
            }

            spawnRandomTile(newTiles)
            tiles = newTiles

            // Check Win (2048 reached)
            if (!hasWon && !keepPlayingAfterWin && tiles.any { it.value >= 2048 }) {
                hasWon = true
            }

            // Check Game Over
            if (!hasValidMoves(tiles)) {
                isGameOver = true
            }

            if (mergedOccurred) {
                onMerge?.invoke()
            } else {
                onMove?.invoke()
            }
            return true
        }

        return false
    }

    fun undo() {
        if (undoHistory.isEmpty()) return
        val lastState = undoHistory.removeLast()
        tiles = lastState.tiles
        score = lastState.score
        isGameOver = false
    }

    private fun spawnRandomTile(currentTiles: MutableList<Tile>) {
        val occupied = currentTiles.map { Pair(it.row, it.col) }.toSet()
        val emptyCells = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until 4) {
            for (c in 0 until 4) {
                if (!occupied.contains(Pair(r, c))) {
                    emptyCells.add(Pair(r, c))
                }
            }
        }

        if (emptyCells.isNotEmpty()) {
            val (r, c) = emptyCells[Random.nextInt(emptyCells.size)]
            val valSpawn = if (Random.nextFloat() < 0.90f) 2 else 4
            currentTiles.add(
                Tile(
                    id = idCounter++,
                    value = valSpawn,
                    row = r,
                    col = c,
                    isNew = true,
                    isMerged = false
                )
            )
        }
    }

    private fun hasValidMoves(currentTiles: List<Tile>): Boolean {
        if (currentTiles.size < 16) return true

        val grid = Array(4) { IntArray(4) { 0 } }
        for (t in currentTiles) {
            if (t.row in 0..3 && t.col in 0..3) {
                grid[t.row][t.col] = t.value
            }
        }

        // Horizontal matches
        for (r in 0 until 4) {
            for (c in 0 until 3) {
                if (grid[r][c] == grid[r][c + 1]) return true
            }
        }

        // Vertical matches
        for (c in 0 until 4) {
            for (r in 0 until 3) {
                if (grid[r][c] == grid[r + 1][c]) return true
            }
        }

        return false
    }
}
