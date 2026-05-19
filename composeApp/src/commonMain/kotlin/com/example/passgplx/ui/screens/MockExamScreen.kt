package com.example.passgplx.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.passgplx.models.Answer
import com.example.passgplx.models.Question
import com.example.passgplx.viewmodels.MockExamViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MockExamScreen() {
    val viewModel = viewModel { MockExamViewModel() }
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = state.currentIndex,
        pageCount = { state.questions.size }
    )

    // Sync pager with view model if needed
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != state.currentIndex) {
            viewModel.goToQuestion(pagerState.currentPage)
        }
    }

    LaunchedEffect(state.currentIndex) {
        if (pagerState.currentPage != state.currentIndex) {
            pagerState.animateScrollToPage(state.currentIndex)
        }
    }

    var showBottomSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Time")
                        Spacer(modifier = Modifier.width(8.dp))
                        val minutes = state.timeRemainingSeconds / 60
                        val seconds = state.timeRemainingSeconds % 60
                        Text(
                            text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                            style = MaterialTheme.typography.titleLarge,
                            color = if (state.timeRemainingSeconds < 60) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (!state.isSubmitted) {
                        Button(onClick = { viewModel.submitExam() }) {
                            Text("Nộp bài")
                        }
                    } else {
                        Button(onClick = { viewModel.startNewExam() }) {
                            Text("Thi lại")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showBottomSheet = true },
                text = { Text("${state.currentIndex + 1}/${state.questions.size}") },
                icon = { Icon(Icons.Default.AccessTime, null) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isSubmitted) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.score >= 21) Color(0xFF4CAF50).copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (state.score >= 21) "ĐẠT" else "KHÔNG ĐẠT",
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (state.score >= 21) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Điểm: ${state.score}/${state.questions.size}",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val question = state.questions[page]
                ExamQuestionCard(
                    question = question,
                    selectedAnswerId = state.selectedAnswers[question.id],
                    isSubmitted = state.isSubmitted,
                    onAnswerSelected = { answerId ->
                        viewModel.selectAnswer(question.id, answerId)
                    }
                )
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false }
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.padding(16.dp)
                ) {
                    itemsIndexed(state.questions) { index, question ->
                        val isAnswered = state.selectedAnswers.containsKey(question.id)
                        val isCurrent = state.currentIndex == index
                        val color = when {
                            state.isSubmitted -> {
                                val selectedAnswerId = state.selectedAnswers[question.id]
                                val isCorrect = question.answers.find { it.correct }?.id == selectedAnswerId
                                if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            }
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = when {
                            state.isSubmitted || isCurrent -> MaterialTheme.colorScheme.onPrimary
                            isAnswered -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable {
                                    viewModel.goToQuestion(index)
                                    showBottomSheet = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
@Composable
fun ExamQuestionCard(
    question: Question,
    selectedAnswerId: String?,
    isSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Câu ${question.id}: ${question.question}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!question.image.isNullOrEmpty()) {
                    var imageBitmap by remember(question.image) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
                    
                    LaunchedEffect(question.image) {
                        try {
                            val bytes = passgplx.composeapp.generated.resources.Res.readBytes("files/questions/${question.image}")
                            imageBitmap = bytes.decodeToImageBitmap()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }

                    if (imageBitmap != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        androidx.compose.foundation.Image(
                            bitmap = imageBitmap!!,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp),
                            contentScale = androidx.compose.ui.layout.ContentScale.Fit
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                question.answers.forEach { answer ->
                    ExamAnswerRow(
                        answer = answer,
                        isSelected = selectedAnswerId == answer.id,
                        isSubmitted = isSubmitted,
                        onClick = { onAnswerSelected(answer.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ExamAnswerRow(
    answer: Answer,
    isSelected: Boolean,
    isSubmitted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSubmitted && answer.correct -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        isSubmitted && isSelected && !answer.correct -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = when {
        isSubmitted && answer.correct -> Color(0xFF4CAF50)
        isSubmitted && isSelected && !answer.correct -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !isSubmitted, onClick = onClick),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected || (isSubmitted && answer.correct),
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSubmitted && answer.correct) Color(0xFF4CAF50) 
                                    else if (isSubmitted && isSelected) MaterialTheme.colorScheme.error
                                    else MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = answer.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
