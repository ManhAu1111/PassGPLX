package com.example.passgplx.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Warning
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
import com.example.passgplx.models.Category
import com.example.passgplx.models.Question
import com.example.passgplx.viewmodels.ReviewViewModel
import com.example.passgplx.data.ImageBitmapCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.decodeToImageBitmap
import com.example.passgplx.ui.components.shimmerEffect
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.AccessTime
import com.example.passgplx.ui.components.CategoryListSkeleton
import com.example.passgplx.ui.components.QuestionCardSkeleton
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(onNavBarVisibilityChanged: (Boolean) -> Unit = {}) {
    val viewModel = viewModel { ReviewViewModel() }
    val questions by viewModel.questions.collectAsState()
    val activeCategories by viewModel.activeCategories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()

    var categoryToPrompt by remember { mutableStateOf<com.example.passgplx.models.Category?>(null) }

    LaunchedEffect(selectedCategory) {
        val showNavBar = selectedCategory == null
        onNavBarVisibilityChanged(showNavBar)
    }

    if (isLoading) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ôn tập lý thuyết", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)
            ) {
                // Skeleton dropdown
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
                    .clip(RoundedCornerShape(8.dp)).shimmerEffect())
                CategoryListSkeleton()
            }
        }
        return
    }

    if (selectedCategory == null) {
        // Show Category List
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ôn tập lý thuyết", fontWeight = FontWeight.Bold) },
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
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeCategories) { info ->
                        CategoryCard(
                            category = info.category, 
                            questionCount = info.questionCount, 
                            answeredCount = info.answeredCount
                        ) {
                            if (info.answeredCount > 0) {
                                categoryToPrompt = info.category
                            } else {
                                viewModel.selectCategory(info.category)
                            }
                        }
                    }
                }
            }
        }
    } else {
        val pagerState = rememberPagerState(pageCount = { questions.size })
        val coroutineScope = rememberCoroutineScope()
        var showBottomSheet by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = selectedCategory!!.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.selectCategory(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Tiến độ")
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
                            val currentId = questions.getOrNull(pagerState.currentPage)?.id ?: ""
                            val lastId = questions.lastOrNull()?.id ?: ""
                            Text(
                                text = "Câu $currentId/$lastId",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        OutlinedButton(
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) } },
                            enabled = pagerState.currentPage < questions.size - 1,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
                        ) {
                            Text("Câu sau >")
                        }
                    }
                }
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
                LinearProgressIndicator(
                    progress = { (pagerState.currentPage + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val question = questions[page]
                    QuestionCard(
                        question = question,
                        index = page,
                        selectedAnswerProvider = { selectedAnswers[question.id] },
                        onAnswerSelected = { answerId ->
                            viewModel.selectAnswer(question.id, answerId)
                        }
                    )
                }
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
                    itemsIndexed(questions) { index, question ->
                        val isCurrent = pagerState.currentPage == index
                        val isAnswered = selectedAnswers.containsKey(question.id)
                        
                        val color = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isAnswered -> MaterialTheme.colorScheme.primaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val contentColor = when {
                            isCurrent -> MaterialTheme.colorScheme.onPrimary
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
                                text = question.id,
                                color = contentColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    if (categoryToPrompt != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { categoryToPrompt = null },
            title = { Text("Tiếp tục ôn tập") },
            text = { Text("Bạn đang ôn tập dở bộ câu hỏi này. Bạn muốn làm tiếp hay làm lại từ đầu?") },
            confirmButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.selectCategory(categoryToPrompt)
                        categoryToPrompt = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Làm tiếp")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        viewModel.clearHistoryForCategory(categoryToPrompt!!)
                        viewModel.selectCategory(categoryToPrompt)
                        categoryToPrompt = null
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Làm lại từ đầu")
                }
            }
        )
    }
}

@Composable
fun CategoryCard(category: Category, questionCount: Int, answeredCount: Int, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (category.isParalyzing) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (category.isParalyzing) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (category.isParalyzing) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${category.description} (Đã làm $answeredCount/$questionCount câu)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (category.isParalyzing) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { if (questionCount > 0) answeredCount.toFloat() / questionCount else 0f },
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                    color = if (category.isParalyzing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    trackColor = if (category.isParalyzing) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                Icons.Default.ArrowForwardIos,
                contentDescription = null,
                tint = if (category.isParalyzing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@OptIn(org.jetbrains.compose.resources.ExperimentalResourceApi::class)
@Composable
fun QuestionCard(
    question: Question,
    index: Int,
    selectedAnswerProvider: () -> String?,
    onAnswerSelected: (String) -> Unit
) {
    val selectedAnswerId = selectedAnswerProvider()
    val showCorrectStatus = selectedAnswerId != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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
                    AnswerRow(
                        answer = answer,
                        index = ansIndex,
                        isSelected = selectedAnswerId == answer.id,
                        showCorrectStatus = showCorrectStatus,
                        onClick = {
                            if (!showCorrectStatus) {
                                onAnswerSelected(answer.id)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showCorrectStatus) {
            val isCorrect = question.answers.find { it.id == selectedAnswerId }?.correct == true
            val message = if (isCorrect) "Chính xác!" else "Chưa chính xác!"
            val color = if (isCorrect) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            
            Card(
                colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = message,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
fun AnswerRow(
    answer: Answer,
    index: Int,
    isSelected: Boolean,
    showCorrectStatus: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showCorrectStatus && answer.correct -> Color(0xFF4CAF50).copy(alpha = 0.15f) // Light Green
        showCorrectStatus && isSelected && !answer.correct -> Color(0xFFE53935).copy(alpha = 0.15f) // Light Red
        isSelected -> MaterialTheme.colorScheme.primaryContainer // Light teal
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val borderColor = when {
        showCorrectStatus && answer.correct -> Color(0xFF4CAF50)
        showCorrectStatus && isSelected && !answer.correct -> Color(0xFFE53935)
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    val borderWidth = if (isSelected || (showCorrectStatus && (answer.correct || isSelected))) 2.dp else 1.dp

    val labelChar = ('A' + index).toString()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !showCorrectStatus, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(borderWidth, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val labelColor = if (showCorrectStatus && answer.correct) Color(0xFF4CAF50)
                             else if (showCorrectStatus && isSelected) Color(0xFFE53935)
                             else if (isSelected) MaterialTheme.colorScheme.primary
                             else MaterialTheme.colorScheme.onSurfaceVariant
                             
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(if (isSelected || (showCorrectStatus && answer.correct)) labelColor else labelColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = labelChar,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected || (showCorrectStatus && answer.correct)) Color.White else labelColor
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
