package com.example.wordle.util

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import com.example.wordle.data.Letter
import com.example.wordle.R

fun slightlyScaleUpAnimation(editView: EditText, dur: Long = 100): ValueAnimator {
    return ValueAnimator.ofFloat(editView.scaleX, 1.1f, 1f).apply {
        duration = dur
        addUpdateListener {
            editView.scaleX = animatedValue as Float
            editView.scaleY = animatedValue as Float
        }
        start()
    }
}

fun shakeAnimation(layout: LinearLayout): () -> Unit {
    var lock = false
    return fun() {
        if (!lock) {
            lock = true
            ValueAnimator.ofFloat(
                layout.x,
                layout.x + 20f,
                layout.x - 20f,
                layout.x + 10f,
                layout.x - 10f,
                layout.x - 5f,
                layout.x + 5f,
                layout.x
            ).apply {
                duration = 400
                addUpdateListener { a ->
                    layout.x = a.animatedValue as Float
                }
                doOnEnd { lock = false }
                start()
            }
        }
    }
}

fun flipTextView(
    editText: EditText,
    colorTextView: Int,
    dur: Long = 80L,
    reset: Boolean = false
): AnimatorSet {

    val flip90degrees = ObjectAnimator.ofFloat(editText, "rotationX", 0f, 90f).apply {
        duration = dur
        doOnEnd {
            editText.setTextColor(editText.resources.getColor(R.color.white))
        }
    }

    val textViewBackgroundColorAnimation =
        if (reset) ObjectAnimator.ofArgb(
            editText,
            "backgroundColor",
            editText.resources.getColor(R.color.white)
        ).apply {
            duration = dur
            doOnEnd {
                editText.setBackgroundResource(R.drawable.border)
            }

        } else ObjectAnimator.ofArgb(editText, "backgroundColor", colorTextView).apply {
            duration = dur
        }
    val flip90degreesBack = ObjectAnimator.ofFloat(editText, "rotationX", 90f, 0f).apply {
        duration = dur
    }


    return AnimatorSet().apply {
        interpolator = AccelerateDecelerateInterpolator()

        play(flip90degrees).before(textViewBackgroundColorAnimation)
        play(textViewBackgroundColorAnimation).before(flip90degreesBack)
        play(flip90degreesBack)

    }
}

fun flipListOfTextViews(
    editTexts: List<EditText>,
    letters: List<Letter>,
    reset: Boolean = false,
    doOnEnd: () -> Unit
): AnimatorSet {
    val animations = editTexts.mapIndexed { index, editText ->

        flipTextView(
            editText,
            ContextCompat.getColor(editText.context, letters[index].backgroundColor),
            reset = reset
        )
    }
    return AnimatorSet().apply {
        playSequentially(animations)
        doOnEnd { doOnEnd() }
    }
}

fun winAnimator(textViews: List<EditText>, doOnEnd: () -> Unit): AnimatorSet {
    val dur = 200L
    return AnimatorSet().apply {
        playSequentially(
            textViews.mapIndexed { _, tw ->
                //tw.bringToFront()
                slightlyScaleUpAnimation(tw, dur)
            }
        )
        doOnEnd {
            doOnEnd()
        }
    }
}

fun showInfo(tw:TextView, message: String) {
    tw.text = message
    val alpha = ObjectAnimator.ofFloat(tw, "alpha", 0f, 1f, 1f, 1f, 1f, 1f, 0f).apply {
        duration = 2000L
    }
    alpha.start();
}


fun flipLetter(
    listOfTextViews: List<EditText>,
    letters: List<Letter>,
    reset: Boolean = false,
    doOnEnd: () -> Unit
) {
    flipListOfTextViews(
        listOfTextViews,
        letters,
        reset = reset
    ) {
        doOnEnd()
    }.start()
}