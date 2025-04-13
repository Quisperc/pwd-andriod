package com.example.pwd.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pwd.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 淡入淡出动画状态
    var startAnimation by remember { mutableStateOf(false) }
    var startExitAnimation by remember { mutableStateOf(false) }
    
    // 淡入动画
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "Splash Alpha Animation"
    )
    
    // 淡出动画
    val alphaExitAnim = animateFloatAsState(
        targetValue = if (startExitAnimation) 0f else 1f,
        animationSpec = tween(durationMillis = 40),
        label = "Splash Exit Animation",
        finishedListener = {
            // 当动画完成后调用回调函数
            onSplashFinished()
        }
    )
    
    // 启动动画序列
    LaunchedEffect(key1 = true) {
        startAnimation = true
        // 显示时间
        delay(1500)
        // 当动画完成后调用回调函数
        // onSplashFinished()
        // 开始淡出
        startExitAnimation = true
    }
    
    // 计算两个动画的组合透明度
    val combinedAlpha = alphaAnim.value * alphaExitAnim.value
    
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 背景图片铺满全屏
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo Background",
            modifier = Modifier
                .fillMaxSize()
                .alpha(combinedAlpha),
            contentScale = ContentScale.Crop // 确保图片填满屏幕，必要时进行裁剪
        )
        
        // 应用名称
        Text(
            text = "密码管理器",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .alpha(combinedAlpha)
                .padding(bottom = 50.dp)
        )
    }
} 