package com.example.wordle.data

data class Position(var row: Int, var col: Int) {
    fun nextColumn() {
        col += 1
    }

    fun previousColumn() {
        col -= 1
    }

    fun nextRow() {
        row += 1
        col = 0
    }

    fun reset() {
        row = 0
        col = 0
    }
}