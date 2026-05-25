package com.example.passgplx.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.passgplx.models.LicenseType
import com.example.passgplx.models.MockExamRecord
import com.example.passgplx.viewmodels.HistoryViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import com.example.passgplx.ui.components.HistoryScreenSkeleton
import com.example.passgplx.ui.components.shimmerEffect
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.CircleShape

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onNavigateToMockExam: (MockExamRecord) -> Unit
) {
    val viewModel = viewModel { HistoryViewModel() }
    val completedExams by viewModel.completedExams.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử thi thử", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            HistoryScreenSkeleton()
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (completedExams.isNotEmpty()) {
                items(completedExams) { record ->
                    ExamRecordCard(record, onClick = { onNavigateToMockExam(record) })
                }
            } else {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("Chưa có dữ liệu lịch sử thi thử.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun ExamRecordCard(record: MockExamRecord, onClick: () -> Unit) {
    val date = Instant.fromEpochMilliseconds(record.timestamp)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val dateString = "${date.dayOfMonth}/${date.monthNumber}/${date.year} ${date.hour}:${date.minute}"
    val isPassed = record.data.score >= LicenseType.valueOf(record.data.licenseType).passingScore

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isPassed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${record.data.score}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Hạng ${record.data.licenseType}", fontWeight = FontWeight.Bold)
                Text(dateString, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(if (isPassed) "ĐẠT" else "TRƯỢT", 
                fontWeight = FontWeight.Bold, 
                color = if (isPassed) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
            )
        }
    }
}
