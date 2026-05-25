package com.example.passgplx.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background

/** ─── Shared helper ──────────────────────────────────────────── */
@Composable
private fun ShimmerBox(modifier: Modifier) {
    Box(modifier = modifier.shimmerEffect())
}

/** ─── History Screen skeleton ───────────────────────────────── */
@Composable
fun HistoryScreenSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(6) {
            ExamRecordCardSkeleton()
        }
    }
}

@Composable
fun ExamRecordCardSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(Modifier.fillMaxWidth(0.5f).height(16.dp).clip(RoundedCornerShape(4.dp)))
            ShimmerBox(Modifier.fillMaxWidth(0.7f).height(12.dp).clip(RoundedCornerShape(4.dp)))
        }
        ShimmerBox(Modifier.width(40.dp).height(20.dp).clip(RoundedCornerShape(4.dp)))
    }
}

/** ─── Review Screen category-list skeleton ──────────────────── */
@Composable
fun CategoryListSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) {
            CategoryCardSkeleton()
        }
    }
}

@Composable
fun CategoryCardSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerBox(Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(Modifier.fillMaxWidth(0.6f).height(18.dp).clip(RoundedCornerShape(4.dp)))
            ShimmerBox(Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

/** ─── QuestionCard skeleton (for Review & MockExam pager) ──── */
@Composable
fun QuestionCardSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Question card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerBox(Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(4.dp)))
            ShimmerBox(Modifier.fillMaxWidth(0.85f).height(20.dp).clip(RoundedCornerShape(4.dp)))
            ShimmerBox(Modifier.fillMaxWidth(0.6f).height(20.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(Modifier.height(8.dp))
            // image placeholder
            ShimmerBox(Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(8.dp)))
        }
        // 4 answer skeletons
        repeat(4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ShimmerBox(Modifier.size(20.dp).clip(CircleShape))
                ShimmerBox(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(4.dp)))
            }
        }
    }
}

/** ─── Traffic signs grid skeleton ─────────────────────────────
 *  (the individual TrafficSignCard already has a shimmer for the
 *  image; this wraps the header + grid for the initial data load)
 * ─────────────────────────────────────────────────────────────*/
@Composable
fun TrafficSignGridSkeleton() {
    Column(modifier = Modifier.fillMaxSize()) {
        // Tab row skeleton
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(4) {
                ShimmerBox(Modifier.width(64.dp).height(20.dp).align(androidx.compose.ui.Alignment.CenterVertically).clip(RoundedCornerShape(4.dp)))
            }
        }
        // Grid cards
        val columns = 2
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    repeat(columns) {
                        TrafficSignCardSkeleton(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TrafficSignCardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ShimmerBox(Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)))
        ShimmerBox(Modifier.fillMaxWidth(0.5f).height(14.dp).clip(RoundedCornerShape(4.dp)))
        ShimmerBox(Modifier.fillMaxWidth(0.8f).height(12.dp).clip(RoundedCornerShape(4.dp)))
        ShimmerBox(Modifier.fillMaxWidth(0.65f).height(12.dp).clip(RoundedCornerShape(4.dp)))
    }
}
