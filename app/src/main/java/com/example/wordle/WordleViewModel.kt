package com.example.wordle

import android.util.Log
import androidx.lifecycle.ViewModel
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.wordle.data.Letter
import com.example.wordle.data.Position
import com.example.wordle.util.listToWord
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import com.example.wordle.util.isWordInWordlist
import java.util.*

const val WORD_LENGTH = 5
const val NUMBER_OF_ROWS = 6
val DEFAULT_LETTER = Letter("", R.drawable.border, R.color.black)

class WordleViewModel(private val context: Context) : ViewModel() {
    private var wordleWords: List<String>
    var wordle: String;
    val wining: List<String>
    val alerts: List<String>
    val allowedLetters: String

    var guess: String = ""
    val signal = MutableSharedFlow<Signal>()
    val listOfEditTextValues =
        List(NUMBER_OF_ROWS) { List(WORD_LENGTH) { MutableStateFlow(DEFAULT_LETTER) } }

    val currentPosition = Position(0, 0)

    init {
        wordleWords = loadWordleWords()
        wordle = wordleWords.random()
        allowedLetters = context.getString(R.string.allowed_letters)
        wining = context.getString(R.string.wining).split(",")
        alerts = context.getString(R.string.alerts).split(",")
    }

    private fun loadWordleWords(): List<String> {
        val inputStream = context.resources.openRawResource(R.raw.words_wordle)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val listOfWords = mutableListOf<String>()
        var mLine = reader.readLine()
        while (mLine != null) {
            listOfWords.add(mLine)
            mLine = reader.readLine()
        }
        reader.close()
        return listOfWords
    }


    fun setLetter(col: Int, letter: String) {
        Log.i("setLetter", letter)
        Log.i("col", col.toString())
        if (currentPosition.col < WORD_LENGTH) {
            currentPosition.nextColumn()
            viewModelScope.launch {
                listOfEditTextValues[currentPosition.row][col].emit(Letter(letter, R.drawable.border, R.color.black))
            }
        }
    }

    fun deleteLetter(col: Int) {
        Log.i("delete", col.toString())
        if (col > 0) {
            currentPosition.previousColumn()
            viewModelScope.launch {
                listOfEditTextValues[currentPosition.row][col].emit(DEFAULT_LETTER)
            }
        }
    }

    fun resetGame() {
        currentPosition.reset()
        viewModelScope.launch {
            listOfEditTextValues.forEach { list ->
                list.forEach { editTextValue ->
                    editTextValue.emit(DEFAULT_LETTER);
                }
            }
        }
        wordle = wordleWords.random()
    }


    suspend fun checkRow() {
        guess =
            listToWord(listOfEditTextValues[currentPosition.row].filter { it.value.letter != "" }
                .map { it.value.letter }).lowercase(
                Locale.getDefault()
            )

        Log.i("GUESS",guess);
        when {
            guess.length < 5 -> {
                signal.emit(Signal.NEEDLETTER)
            }
            wordle == guess -> {
                signal.emit(Signal.WIN)
            }
            isWordInWordlist(wordleWords, guess) -> {
                if (currentPosition.row == 5) {
                    signal.emit(Signal.GAMEOVER)
                } else {
                    signal.emit((Signal.NEXTTRY))
                }
            }
            else -> {
                signal.emit(Signal.NOTAWORD)
            }
        }
    }

    fun checkColor(): List<Letter> {
        val list = mutableListOf<Letter>()
        listOfEditTextValues[currentPosition.row].forEachIndexed { index, flow ->
            list.add(when (guess[index]) {
                wordle[index] -> {
                    Letter(flow.value.letter, R.color.green, R.color.white)
                }
                in wordle.filterIndexed { i, s -> guess[i] != s } -> {
                    Letter(flow.value.letter, R.color.yellow, R.color.white)
                }
                else -> {
                    Letter(flow.value.letter, R.color.dark_gray, R.color.white)
                }
            })
        }
        return list
    }

    fun emitColor(list: List<Letter> = checkColor()) {
        viewModelScope.launch {
            listOfEditTextValues[currentPosition.row].forEachIndexed { index, flow ->
                val letter = list[index]
                flow.emit(letter)
            }
        }
    }
}
