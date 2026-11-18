package com.raularribas.firelauncher.presentation.home

const val APP_ROWS = 2

enum class MoveDirection {
    LEFT, RIGHT, UP, DOWN;

    fun target(index: Int, rows: Int = APP_ROWS): Int = when (this) {
        LEFT -> index - rows
        RIGHT -> index + rows
        UP -> if (index % rows > 0) index - 1 else -1
        DOWN -> if (index % rows < rows - 1) index + 1 else -1
    }
}
