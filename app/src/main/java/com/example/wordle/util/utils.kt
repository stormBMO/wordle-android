package com.example.wordle.util


fun isWordInWordlist(wordList: List<String>, word: String): Boolean {
    return wordList.indexOf(word) > -1
}

fun listToWord(list: List<String>): String {
    return list.joinToString("")
}
