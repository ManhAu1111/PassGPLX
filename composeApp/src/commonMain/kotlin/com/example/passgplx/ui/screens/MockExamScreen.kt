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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Description
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
import com.example.passgplx.data.ImageBitmapCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.example.passgplx.ui.components.shimmerEffect
import com.example.passgplx.ui.components.QuestionCardSkeleton
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MockExamScreen(
    pastExamRecord: com.example.passgplx.models.MockExamRecord? = null,
    onPastExamConsumed: () -> Unit = {},
    onNavBarVisibilityChanged: (Boolean) -> Unit = {}
) {
    val viewModel = viewModel { MockExamViewModel() }
    val state by viewModel.state.collectAsState()
    val hasSavedExam by viewModel.hasSavedExam.collectAsState()
    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pastExamRecord) {
        if (pastExamRecord != null) {
            viewModel.loadPastExam(pastExamRecord)
            onPastExamConsumed()
        }
    }

    LaunchedEffect(state.isExamStarted) {
        val showNavBar = !state.isExamStarted
        onNavBarVisibilityChanged(showNavBar)
    }

    if (state.isLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Thi Thử", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            Box(
                modifier = androidx.compose.ui.Modifier.fillMaxSize().padding(padding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                QuestionCardSkeleton()
            }
        }
        return
    }

    if (!state.isExamStarted) {
        MockExamSetupScreen(
            selectedLicenseType = state.selectedLicenseType,
            hasSavedExam = hasSavedExam,
            onLicenseTypeSelected = { viewModel.selectLicenseType(it) },
            onStartExam = { viewModel.startNewExam() },
            onContinueExam = { viewModel.continueExam() }
        )
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

    var showBottomSheet by remember { mutableStateOf(false) }
    var showSubmitDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Surface(
                        color = if (timeRemaining < 60 && !state.isSubmitted) MaterialTheme.colorScheme.error.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                Icons.Default.AccessTime, 
                                contentDescription = "Time",
                                tint = if (timeRemaining < 60 && !state.isSubmitted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val minutes = timeRemaining / 60
                            val seconds = timeRemaining % 60
                            Text(
                                text = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (timeRemaining < 60 && !state.isSubmitted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                },
                actions = {
                    if (!state.isSubmitted) {
                        Button(
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier.padding(end = 16.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Nộp bài", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.quitExam() },
                            modifier = Modifier.padding(end = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Thoát", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) } },
                        enabled = pagerState.currentPage > 0,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("< Câu trước")
                    }
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.clickable { showBottomSheet = true }
                    ) {
                        Text(
                            text = "Tiến độ: ${pagerState.currentPage + 1}/${state.questions.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    OutlinedButton(
                        onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                        enabled = pagerState.currentPage < state.questions.size - 1,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                    ) {
                        Text("Câu sau >")
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (state.isSubmitted) {
                val passed = state.score >= state.selectedLicenseType.passingScore
                val bannerColor = if (passed) Color(0xFF4CAF50) else Color(0xFFE53935)
                val bannerIcon = if (passed) Icons.Default.CheckCircle else Icons.Default.Cancel
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = bannerColor.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = bannerIcon,
                            contentDescription = null,
                            tint = bannerColor,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = if (passed) "KẾT QUẢ: ĐẠT" else "KẾT QUẢ: KHÔNG ĐẠT",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = bannerColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Số điểm: ${state.score} / ${state.questions.size}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
                    index = page,
                    selectedAnswerProvider = { state.selectedAnswers[question.id] },
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
                        val isCurrent = pagerState.currentPage == index
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
                                    coroutineScope.launch { pagerState.animateScrollToPage(index) }
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

        if (showSubmitDialog) {
            val unansweredCount = state.questions.size - state.selectedAnswers.size
            val dialogText = if (unansweredCount > 0) {
                "Bạn còn $unansweredCount câu chưa làm. Bạn có chắc chắn muốn nộp bài?"
            } else {
                "Bạn có chắc chắn muốn nộp bài?"
            }

            AlertDialog(
                onDismissRequest = { showSubmitDialog = false },
                title = { Text("Xác nhận nộp bài") },
                text = { Text(dialogText) },
                confirmButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.submitExam()
                            showSubmitDialog = false
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Nộp bài")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = { showSubmitDialog = false },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                    ) {
                        Text("Làm tiếp")
                    }
                }
            )
        }
    }
}

@OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
@Composable
fun ExamQuestionCard(
    question: Question,
    index: Int,
    selectedAnswerProvider: () -> String?,
    isSubmitted: Boolean,
    onAnswerSelected: (String) -> Unit
) {
    val selectedAnswerId = selectedAnswerProvider()
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
                    text = "Câu ${index + 1}: ${question.question}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!question.image.isNullOrEmpty()) {
                    var imageBitmap by remember(question.image) { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(ImageBitmapCache.get(question.image ?: "")) }
                    
                    if (imageBitmap == null) {
                        LaunchedEffect(question.image) {
                            val bmp = withContext(Dispatchers.IO) {
                                try {
                                    val bytes = passgplx.composeapp.generated.resources.Res.readBytes("files/questions/${question.image}")
                                    bytes.decodeToImageBitmap()
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            if (bmp != null) {
                                ImageBitmapCache.put(question.image ?: "", bmp)
                                imageBitmap = bmp
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = imageBitmap!!,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .shimmerEffect()
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                question.answers.forEachIndexed { ansIndex, answer ->
                    ExamAnswerRow(
                        answer = answer,
                        index = ansIndex,
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
    index: Int,
    isSelected: Boolean,
    isSubmitted: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSubmitted && answer.correct -> Color(0xFF4CAF50).copy(alpha = 0.15f)
        isSubmitted && isSelected && !answer.correct -> Color(0xFFE53935).copy(alpha = 0.15f)
        isSelected -> MaterialTheme.colorScheme.primaryContainer // Light teal
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        isSubmitted && answer.correct -> Color(0xFF4CAF50)
        isSubmitted && isSelected && !answer.correct -> Color(0xFFE53935)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }
    
    val borderWidth = if (isSelected || (isSubmitted && (answer.correct || isSelected))) 2.dp else 1.dp

    val labelChar = ('A' + index).toString()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isSubmitted, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labelColor = if (isSubmitted && answer.correct) Color(0xFF4CAF50)
                             else if (isSubmitted && isSelected) Color(0xFFE53935)
                             else if (isSelected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onSurfaceVariant
                             
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isSelected || (isSubmitted && answer.correct)) labelColor else labelColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelChar,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected || (isSubmitted && answer.correct)) Color.White else labelColor
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = answer.text,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockExamSetupScreen(
    selectedLicenseType: com.example.passgplx.models.LicenseType,
    hasSavedExam: Boolean,
    onLicenseTypeSelected: (com.example.passgplx.models.LicenseType) -> Unit,
    onStartExam: () -> Unit,
    onContinueExam: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thi Thử", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "Chọn hạng bằng lái",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLicenseType.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Black,
                                unfocusedBorderColor = Color.Black,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            com.example.passgplx.models.LicenseType.entries.forEach { licenseType ->
                                DropdownMenuItem(
                                    text = { Text(licenseType.displayName) },
                                    onClick = {
                                        onLicenseTypeSelected(licenseType)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Số câu hỏi: ${selectedLicenseType.totalMockQuestions}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Thời gian: ${selectedLicenseType.timeMinutes} phút",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Điểm đạt: ${selectedLicenseType.passingScore}/${selectedLicenseType.totalMockQuestions}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (hasSavedExam) {
                Button(
                    onClick = onContinueExam,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("TIẾP TỤC BÀI ĐANG THI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onStartExam,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("BẮT ĐẦU BÀI MỚI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onStartExam,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("BẮT ĐẦU THI", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
