package com.example.passgplx.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import org.jetbrains.compose.resources.decodeToImageBitmap
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen() {
    val viewModel = viewModel { ReviewViewModel() }
    val questions by viewModel.questions.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
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
                        containerColor = MaterialTheme.colorScheme.surface,
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(categories) { category ->
                    CategoryCard(category = category) {
                        viewModel.selectCategory(category)
                    }
                }
            }
        }
    } else {
        // Show Questions for Selected Category
        if (questions.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không có dữ liệu câu hỏi.", style = MaterialTheme.typography.bodyLarge)
            }
            return
        }

        val pagerState = rememberPagerState(pageCount = { questions.size })

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
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(end = 16.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${questions.size}",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
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
                    QuestionCard(question = questions[page])
                }
            }
        }
    }
}

@Composable
fun CategoryCard(category: Category, onClick: () -> Unit) {
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
                    text = category.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (category.isParalyzing) MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
fun QuestionCard(question: Question) {
    // Map of Answer ID -> Boolean (true if selected)
    var selectedAnswerId by remember(question.id) { mutableStateOf<String?>(null) }
    var showCorrectAnswer by remember(question.id) { mutableStateOf(false) }

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
                    AnswerRow(
                        answer = answer,
                        isSelected = selectedAnswerId == answer.id,
                        showCorrectStatus = showCorrectAnswer,
                        onClick = {
                            if (!showCorrectAnswer) {
                                selectedAnswerId = answer.id
                                showCorrectAnswer = true
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (showCorrectAnswer) {
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
    isSelected: Boolean,
    showCorrectStatus: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showCorrectStatus && answer.correct -> Color(0xFF4CAF50).copy(alpha = 0.2f) // Light Green
        showCorrectStatus && isSelected && !answer.correct -> MaterialTheme.colorScheme.error.copy(alpha = 0.2f) // Light Red
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val borderColor = when {
        showCorrectStatus && answer.correct -> Color(0xFF4CAF50)
        showCorrectStatus && isSelected && !answer.correct -> MaterialTheme.colorScheme.error
        isSelected -> MaterialTheme.colorScheme.primary
        else -> Color.Transparent
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = !showCorrectStatus, onClick = onClick),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected || (showCorrectStatus && answer.correct),
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (showCorrectStatus && answer.correct) Color(0xFF4CAF50) 
                                    else if (showCorrectStatus && isSelected) MaterialTheme.colorScheme.error
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
