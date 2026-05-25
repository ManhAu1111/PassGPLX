package com.example.passgplx

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.example.passgplx.ui.screens.MockExamScreen
import com.example.passgplx.ui.screens.ReviewScreen
import com.example.passgplx.ui.screens.SignDetectionScreen
import com.example.passgplx.ui.screens.TrafficSignsScreen
import com.example.passgplx.ui.screens.HistoryScreen
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import kotlin.math.abs

expect fun ByteArray.toImageBitmap(): ImageBitmap

sealed class Screen(val route: String, val icon: ImageVector) {
    object History : Screen("history", Icons.Default.History)
    object Review : Screen("review", Icons.Default.Book)
    object MockExam : Screen("mock_exam", Icons.AutoMirrored.Filled.Assignment)
    object TrafficSigns : Screen("traffic_signs", Icons.Default.Warning)
    object SignDetection : Screen("sign_detection", Icons.Default.CameraAlt)
}

val items = listOf(
    Screen.History,
    Screen.Review,
    Screen.MockExam,
    Screen.TrafficSigns,
    Screen.SignDetection,
)

@Composable
@Preview
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        val pagerState = rememberPagerState(pageCount = { items.size })
        val coroutineScope = rememberCoroutineScope()

        var navBarWidth by remember { mutableStateOf(0) }
        var isNavBarVisible by remember { mutableStateOf(true) }

        // State hoists for navigation from History
        var reviewPastExam by remember { mutableStateOf<com.example.passgplx.models.MockExamRecord?>(null) }

        Scaffold { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                HorizontalPager(
                    state = pagerState,
                    userScrollEnabled = isNavBarVisible,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    when (items[page]) {
                        Screen.History -> HistoryScreen(
                            onNavigateToMockExam = { examRecord ->
                                reviewPastExam = examRecord
                                coroutineScope.launch { pagerState.animateScrollToPage(items.indexOf(Screen.MockExam)) }
                            }
                        )
                        Screen.Review -> ReviewScreen(
                            onNavBarVisibilityChanged = { isNavBarVisible = it }
                        )
                        Screen.MockExam -> MockExamScreen(
                            pastExamRecord = reviewPastExam,
                            onPastExamConsumed = { reviewPastExam = null },
                            onNavBarVisibilityChanged = { isNavBarVisible = it }
                        )
                        Screen.TrafficSigns -> TrafficSignsScreen()
                        Screen.SignDetection -> SignDetectionScreen()
                    }
                }

                // Floating navbar overlay – không dùng bottomBar của Scaffold
                // nên nội dung màn hình vẽ đến tận đáy
                if (isNavBarVisible) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 20.dp)
                            .height(64.dp)
                            .onGloballyPositioned { coordinates ->
                                navBarWidth = coordinates.size.width
                            }
                            .pointerInput(Unit) {
                                awaitPointerEventScope {
                                    while (true) {
                                        val down = awaitFirstDown()
                                        var lastX = down.position.x
                                        var dragTriggered = false
    
                                        drag(down.id) { change ->
                                            val delta = change.position.x - change.previousPosition.x
                                            if (delta != 0f) {
                                                pagerState.dispatchRawDelta(-delta)
                                                lastX = change.position.x
                                                dragTriggered = true
                                                change.consume()
                                            }
                                        }
    
                                        if (dragTriggered && navBarWidth > 0) {
                                            val itemWidth = navBarWidth.toFloat() / items.size
                                            val targetIndex = (lastX / itemWidth)
                                                .toInt()
                                                .coerceIn(0, items.size - 1)
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(targetIndex)
                                            }
                                        }
                                    }
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            items.forEachIndexed { index, screen ->
                                NavBarItem(
                                    screen = screen,
                                    index = index,
                                    pagerState = pagerState,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * NavBarItem được tách riêng để Compose có thể skip recompose
 * những item không thay đổi, thay vì vẽ lại cả Row.
 *
 * Dùng graphicsLayer để scale trực tiếp trên GPU (không trigger re-layout).
 * animateFloatAsState tạo spring animation mượt khi chọn tab bằng tap.
 */
@Composable
private fun NavBarItem(
    screen: Screen,
    index: Int,
    pagerState: PagerState,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val isSelected = pagerState.currentPage == index
    val primaryColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(width = 56.dp, height = 32.dp)
                    // graphicsLayer scale không gây re-layout, chỉ tốn GPU
                    .graphicsLayer {
                        // Read pagerState properties inside graphicsLayer to avoid recomposition!
                        val currentPos = pagerState.currentPage + pagerState.currentPageOffsetFraction
                        val distance = abs(index.toFloat() - currentPos).coerceIn(0f, 1f)
                        val rawScale = 1f + 0.3f * (1f - distance)
                        scaleX = rawScale
                        scaleY = rawScale
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = screen.icon,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    tint = if (isSelected) primaryColor else unselectedColor
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(top = 3.dp)
                        .size(4.dp)
                        .background(
                            color = primaryColor,
                            shape = RoundedCornerShape(50)
                        )
                )
            }
        }
    }
}
