package com.example.wordle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.io.InputStream

class WordleViewModelFactory(
    private val stringResources: List<String>,
    private val rawResources: InputStream
    ) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(WordleViewModel::class.java)) {
            return WordleViewModel(stringResources, rawResources) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}