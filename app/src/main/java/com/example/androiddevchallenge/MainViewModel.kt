/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge

import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.lang.Exception

class MainViewModel : ViewModel() {
    private val _minute = MutableLiveData("0")
    private val _second = MutableLiveData("10")
    private val _time = MutableLiveData("00:00:00")
    private var valueAnimator: ValueAnimator? = null
    val minute: LiveData<String> = _minute
    val second: LiveData<String> = _second
    val time: LiveData<String> = _time
    val showCountdown = MutableLiveData(false)
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
