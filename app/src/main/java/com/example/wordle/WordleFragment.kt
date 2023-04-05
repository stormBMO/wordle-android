package com.example.wordle

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.wordle.databinding.GameFragmentBinding
import com.example.wordle.util.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class WordleFragment : Fragment() {

    private var _binding: GameFragmentBinding? = null
    private val binding get() = _binding!!

    private lateinit var context: Context

    private val viewModel: WordleViewModel by viewModels {
        WordleViewModelFactory(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = GameFragmentBinding.inflate(inflater, container, false)
        context = requireContext()
        return binding.root
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.firstRow1.requestFocus()
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.firstRow1, InputMethodManager.SHOW_IMPLICIT)

        val listOfTextViews = listOf(
            listOf(
                binding.firstRow1,
                binding.firstRow2,
                binding.firstRow3,
                binding.firstRow4,
                binding.firstRow5
            ),
            listOf(
                binding.secondRow1,
                binding.secondRow2,
                binding.secondRow3,
                binding.secondRow4,
                binding.secondRow5
            ),
            listOf(
                binding.thirdRow1,
                binding.thirdRow2,
                binding.thirdRow3,
                binding.thirdRow4,
                binding.thirdRow5
            ),
            listOf(
                binding.fourthRow1,
                binding.fourthRow2,
                binding.fourthRow3,
                binding.fourthRow4,
                binding.fourthRow5
            ),
            listOf(
                binding.fifthRow1,
                binding.fifthRow2,
                binding.fifthRow3,
                binding.fifthRow4,
                binding.fifthRow5
            ),
            listOf(
                binding.sixthRow1,
                binding.sixthRow2,
                binding.sixthRow3,
                binding.sixthRow4,
                binding.sixthRow5
            )
        )

        val lettersRow = listOf(
            binding.firstLettersRow,
            binding.secondLettersRow,
            binding.thirdLettersRow,
            binding.fourthLettersRow,
            binding.fifthLettersRow,
            binding.sixthLettersRow
        )

        var shakeAnimation = shakeAnimation(lettersRow[viewModel.currentPosition.row])

        listOfTextViews.forEachIndexed { row, list ->
            list.forEachIndexed { col, editText ->
                editText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if(viewModel.allowedLetters.indexOf(s.toString()) < 0){
                            shakeAnimation();
                            editText.setText("");
                            showInfo(binding.info, viewModel.alerts[2])
                            return;
                        }
                        if (s?.length == 0 && col > 0) {
                            lifecycleScope.launch {
                                viewModel.deleteLetter(col);
                            }
                            return;
                        }
                        if (s?.length == 1 && col < list.lastIndex) {
                            editText.clearFocus()
                            list[col + 1].requestFocus()
                        }
                        lifecycleScope.launch {
                            viewModel.setLetter(col, s.toString());
                        }
                    }
                })


                editText.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->
                    if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                        lifecycleScope.launch {
                            viewModel.checkRow()
                        }
                        return@OnKeyListener true
                    }
                    if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                        if (editText.text.toString().isEmpty() && col > 0) {
                            editText.clearFocus()
                            list[col - 1].requestFocus()
                            return@OnKeyListener true
                        }
                    }
                    false
                })

                lifecycleScope.launch {
                    viewModel.listOfEditTextValues[row][col].collect { s ->
                        if (s.letter != " " && s.backgroundColor == R.color.white) {
                            slightlyScaleUpAnimation(editText)
                        }
                        editText.apply {
                            background = context.getDrawable(s.backgroundColor)
                            setTextColor(context.getColor(s.textColor))
                        }
                    }
                }
            }
        }

        fun finishGameAndReset() {
            viewModel.emitColor()
            viewModel.resetGame()
            listOfTextViews[0][0].requestFocus();
            shakeAnimation =
                shakeAnimation(lettersRow[viewModel.currentPosition.row])
            listOfTextViews.forEachIndexed { row, list ->
                list.forEachIndexed { col, editText ->
                    editText.setText("")
                }
            }
        }


        lifecycleScope.launch {
            viewModel.signal.collect {
                when (it) {
                    Signal.NOTAWORD -> {
                        showInfo(binding.info, viewModel.alerts[0])
                        shakeAnimation()
                    }
                    Signal.NEEDLETTER -> {
                        showInfo(binding.info, viewModel.alerts[1])
                        shakeAnimation()
                    }
                    Signal.NEXTTRY -> {
                        flipLetter(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            viewModel.emitColor()
                            if(viewModel.currentPosition.row < NUMBER_OF_ROWS){
                                viewModel.currentPosition.nextRow()
                            }
                            shakeAnimation =
                                    shakeAnimation(lettersRow[viewModel.currentPosition.row])
                            listOfTextViews[viewModel.currentPosition.row][0].requestFocus();
                        }
                    }
                    Signal.GAMEOVER -> {
                        showInfo(binding.info, viewModel.wordle)
                        flipLetter(
                            listOfTextViews[viewModel.currentPosition.row],
                            viewModel.checkColor(),
                        ) {
                            finishGameAndReset()
                        }
                    }
                    Signal.WIN -> {
                        val cur = listOfTextViews[viewModel.currentPosition.row]
                        showInfo(binding.info, viewModel.wining[viewModel.currentPosition.row])
                        flipLetter(
                            cur,
                            viewModel.checkColor(),
                        ) {
                            winAnimator(cur){
                                finishGameAndReset()
                            }.start()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}

