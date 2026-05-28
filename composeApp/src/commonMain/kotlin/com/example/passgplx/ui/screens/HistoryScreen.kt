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
                    containerColor = MaterialTheme.colorScheme.background,
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
    val passingScore = LicenseType.valueOf(record.data.licenseType).passingScore
    val isPassed = record.data.isPassed(passingScore)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (isPassed) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFE53935).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${record.data.score}",
                    color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFE53935),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hạng ${record.data.licenseType}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isPassed && record.data.score >= passingScore) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Trượt điểm liệt",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            Surface(
                color = if (isPassed) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color(0xFFE53935).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isPassed) "ĐẠT" else "TRƯỢT",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = if (isPassed) Color(0xFF4CAF50) else Color(0xFFE53935)
                )
            }
        }
    }
}
