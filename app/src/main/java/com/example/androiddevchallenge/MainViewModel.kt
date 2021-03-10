package com.example.androiddevchallenge

import android.animation.ValueAnimator
import android.util.Log
import android.view.animation.LinearInterpolator
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.ticker
import java.lang.Exception

class MainViewModel : ViewModel() {
    /** Total time for seconds **/
    private val totalTime: Int = 0

    private val _minute = MutableLiveData("0")
    private val _second = MutableLiveData("10")
    private val _time = MutableLiveData<String>("00:00:00")
    val minute: LiveData<String> = _minute
    val second: LiveData<String> = _second
    val time: LiveData<String> = _time
    val showCountdown = MutableLiveData(false)
    private var valueAnimator: ValueAnimator? = null
    var isPausing = MutableLiveData(false)

    fun onMinuteChange(minutes: String) {
        if (minutes.isEmpty() || minutes.isBlank()) {
            _minute.value = ""
            return
        }
        kotlin.runCatching {
            val minutesNumber = minutes.toInt()
            if (minutesNumber in 1..59) {
                _minute.value = minutes
            }
        }
    }

    fun onSecondChange(seconds: String) {
        if (seconds.isEmpty() || seconds.isBlank()) {
            _second.value = ""
            return
        }
        kotlin.runCatching {
            val secondsNumber = seconds.toInt()
            if (secondsNumber in 1..59) {
                _second.value = seconds
            }
        }
    }

    fun getMinute(): Int {
        return try {
            _minute.value?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun getSecond(): Int {
        return try {
            _second.value?.toInt() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private fun formatNumber(number: Int): String {
        if (number <= 0) return "00"
        return if (number < 10) {
            return "0$number"
        } else "$number"
    }

    fun start() {
        valueAnimator = ValueAnimator.ofInt(getMinute() * 60 + getSecond(), 0)
        valueAnimator?.interpolator = LinearInterpolator()
        valueAnimator?.duration = (getMinute() * 60 + getSecond()) * 1000L
        valueAnimator?.addUpdateListener {
            _minute.value = ((it.animatedValue as Int) / 60).toString()
            _second.value = ((it.animatedValue as Int) % 60).toString()
            _time.value = "${formatNumber(getMinute())}:${formatNumber(getSecond())}:00"
        }
        valueAnimator?.start()
        showCountdown.value = true
    }

    fun pause() {
        if (isPausing.value == true) {
            isPausing.value = false
            start()
        } else {
            valueAnimator?.cancel()
            isPausing.value = true
        }
    }

    fun stop() {
        valueAnimator?.cancel()
        showCountdown.value = false
        _minute.value = ""
        _second.value = "10"
    }
}