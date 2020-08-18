package br.com.pocanimation

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd


class CollapseLoadingButton @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var mState: LoadingState = LoadingState.IDLE
    val btProgress: Button by lazy { findViewById<Button>(R.id.btProgress) }
    private val progressLoading: ProgressBar by lazy { findViewById<ProgressBar>(R.id.progressCircular) }

    private val roundWidthSize: Int by lazy {
        progressLoading.measuredWidth * MULTIPLIER_BIAS
    }

    private val buttonBackground: Drawable
    private val btText: String
    private val attributesTypedArray: TypedArray

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.progress_button, this, true)
        attributesTypedArray =
            context.theme.obtainStyledAttributes(attrs, R.styleable.CollapseLoadingButton, 0, 0)
        btText = attributesTypedArray.getString(R.styleable.CollapseLoadingButton_textButton) ?: ""
        buttonBackground =
            attributesTypedArray.getDrawable(R.styleable.CollapseLoadingButton_backgroundButton)
                ?: resources.getDrawable(R.drawable.rectangule)
        setupButton()
        setupLoading(context)

        attributesTypedArray.recycle()
    }

    private fun setupButton() {
        btProgress.apply {
            text = btText
            setTextColor(
                attributesTypedArray.getColor(
                    R.styleable.CollapseLoadingButton_buttonTextColor,
                    resources.getColor(R.color.white)
                )
            )
            background =
                attributesTypedArray.getDrawable(R.styleable.CollapseLoadingButton_backgroundButton)
        }
    }

    private fun setupLoading(context: Context) {
        progressLoading.apply {
            indeterminateDrawable.setColorFilter(
                attributesTypedArray.getColor(
                    R.styleable.CollapseLoadingButton_colorProgressBar,
                    context.resources.getColor(R.color.white)
                ),
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    private fun cornerAnimator(drawable: Drawable): ObjectAnimator {
        return with(drawable) {
            ObjectAnimator.ofFloat(
                this is GradientDrawable,
                "cornerRadius",
                DEFAULT_CORNER_VALUE,
                FINAL_CORNER_VALUE
            )
        }
    }

    private fun widthAnimation(initialWidth: Int, toWidth: Int) =
        ValueAnimator.ofInt(initialWidth, toWidth)


    private fun ValueAnimator.addUpdateListener() {
        addUpdateListener { valueAnimator ->
            val layoutParams = layoutParams
            layoutParams.width = valueAnimator.animatedValue as Int
            setLayoutParams(layoutParams)
        }
    }

    private fun isLoadOff() {
        if (mState !== LoadingState.PROGRESS) {
            return
        }
        val widthAnimation = widthAnimation(
            initialWidth = roundWidthSize,
            toWidth = rootView.measuredWidth
        )
        widthAnimation.addUpdateListener()
        widthAnimation.loadOfAnimatorSet()
    }

    private fun isLoadOn() {
        if (mState !== LoadingState.IDLE) {
            return
        }

        mState = LoadingState.PROGRESS
        btProgress.text = null
        btProgress.isClickable = false

        val widthAnimation = widthAnimation(
            initialWidth = rootView.measuredWidth,
            toWidth = roundWidthSize
        )

        widthAnimation.addUpdateListener()
        widthAnimation.loadOnAnimatorSet()
    }

    fun toggleProgress(isLoading: Boolean) {
        if (isLoading) isLoadOn()
        else isLoadOff()
    }

    private fun ValueAnimator.loadOfAnimatorSet() {
        with(AnimatorSet()) {
            duration = ANIMATION_DURATION
            playTogether(
                cornerAnimator(
                    buttonBackground
                ), this@loadOfAnimatorSet
            )
            doOnEnd {
                progressLoading.visibility = View.INVISIBLE
                btProgress.text = btText
                mState = LoadingState.IDLE
                btProgress.isClickable = true
            }
            start()
        }
    }

    private fun ValueAnimator.loadOnAnimatorSet() {
        with(AnimatorSet()) {
            duration = ANIMATION_DURATION
            playTogether(
                cornerAnimator(
                    buttonBackground
                ),
                this@loadOnAnimatorSet
            )
            doOnEnd {
                progressLoading.visibility = View.VISIBLE
                mState = LoadingState.PROGRESS
            }
            start()
        }
    }

    internal enum class LoadingState {
        PROGRESS, IDLE
    }

    companion object {
        private const val MULTIPLIER_BIAS = 4
        private const val ANIMATION_DURATION = 300L
        private const val DEFAULT_CORNER_VALUE = 1000F
        private const val FINAL_CORNER_VALUE = 150F
    }
}