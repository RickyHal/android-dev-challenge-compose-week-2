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

import android.app.Service
import android.os.Bundle
import android.os.Vibrator
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.androiddevchallenge.ui.theme.MyTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this).get(MainViewModel::class.java)
    }

    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                PageControl(viewModel) {
                    val minute = viewModel.getMinute()
                    val second = viewModel.getSecond()
                    if (minute <= 0 && second == 0) {
                        Toast.makeText(
                            this,
                            "Minute and second must Greater than 0",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        viewModel.start()
                    }
                }
            }
        }
        viewModel.second.observe(this) {
            if (viewModel.getMinute() <= 0 && viewModel.getSecond() <= 0) {
                (getSystemService(Service.VIBRATOR_SERVICE) as Vibrator).vibrate(500L)
                Toast.makeText(this, "Bomb~", Toast.LENGTH_SHORT).show()
                lifecycleScope.launch {
                    delay(1000)
                    viewModel.stop()
                }
            }
        }
        viewModel.showCountdown.observe(this) {
            if (it) {
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                )
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun PageControl(viewModel: MainViewModel, onStart: () -> Unit) {
    val showCountdownTime: Boolean by viewModel.showCountdown.observeAsState(false)
    Crossfade(targetState = showCountdownTime, modifier = Modifier.background(Color.Black)) {
        if (!it) {
            Main(viewModel = viewModel, onStart)
        } else {
            CountdownTimeApp(viewModel)
        }
    }
}

@Composable
fun Main(viewModel: MainViewModel, onStart: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Countdown Timer")
                }
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Please set countdown time",
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val minute: String by viewModel.minute.observeAsState("0")
                val second: String by viewModel.second.observeAsState("10")
                TextField(
                    value = minute,
                    modifier = Modifier
                        .padding(start = 20.dp, end = 10.dp)
                        .width(120.dp)
                        .background(Color.White),
                    onValueChange = { minutes -> viewModel.onMinuteChange(minutes) },
                    label = {
                        Text(text = "minute")
                    }
                )
                TextField(
                    value = second,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 20.dp)
                        .width(120.dp)
                        .background(Color.White),
                    onValueChange = { seconds -> viewModel.onSecondChange(seconds) },
                    label = {
                        Text(text = "second")
                    }
                )
            }
            Button(onClick = onStart) {
                Text(text = "Start")
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun CountdownTimeApp(viewModel: MainViewModel) {
    val time: String by viewModel.time.observeAsState("00:00:00")
    val showCountdownTime: Boolean by viewModel.showCountdown.observeAsState(false)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibilityBlock(showCountdownTime, -300) {
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                val color by animateColorAsState(
                    targetValue = if (viewModel.getMinute() == 0 && viewModel.getSecond() <= 3) Color.Red else Color.Green,
                    animationSpec = tween(durationMillis = 250)
                )
                Text(
                    text = time,
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.CenterVertically),
                    color = color,
                    textAlign = TextAlign.Center,
                    fontSize = 100.sp
                )
            }
        }
        AnimatedVisibilityBlock(showCountdownTime, 300) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                val isPausing: Boolean by viewModel.isPausing.observeAsState(false)
                val pauseBtnString = if (isPausing) "Resume" else "Pause"
                FunctionButton(pauseBtnString, viewModel::pause)
                FunctionButton("Stop", viewModel::stop)
            }
        }
    }
}

@ExperimentalAnimationApi
@Composable
fun AnimatedVisibilityBlock(
    showCountdownTime: Boolean,
    offset: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = showCountdownTime,
        enter = slideInVertically(
            initialOffsetY = { it / 2 + offset },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        ),
        exit = slideOutVertically(targetOffsetY = { it / 2 + offset }),
        content = content
    )
}

@Composable
fun FunctionButton(text: String, onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .padding(start = 20.dp, end = 20.dp),
        onClick = onClick
    ) {
        Text(text = text)
    }
}