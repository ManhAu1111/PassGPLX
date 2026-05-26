package com.example.passgplx.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.passgplx.data.TrafficSignRepository
import com.example.passgplx.models.TrafficSign
import com.example.passgplx.toImageBitmap
import com.example.passgplx.ml.SignRecognitionResult
import com.example.passgplx.ml.rememberSignRecognizer
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import passgplx.composeapp.generated.resources.Res

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SignDetectionScreen() {
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }
    var recognitionResult by remember { mutableStateOf<SignRecognitionResult?>(null) }
    var signs by remember { mutableStateOf<List<TrafficSign>>(emptyList()) }
    var detailedSign by remember { mutableStateOf<TrafficSign?>(null) }
    val scope = rememberCoroutineScope()
    val signRecognizer = rememberSignRecognizer()

    val cameraState = rememberPeekabooCameraState(onCapture = { byteArrays ->
        byteArrays?.let {
            capturedImage = it.toImageBitmap()
            isCameraOpen = false
            recognitionResult = signRecognizer.recognize(it)
        }
    })

    // Dùng TrafficSignRepository singleton – data.json chỉ parse 1 lần toàn app
    LaunchedEffect(Unit) {
        signs = TrafficSignRepository.getSigns()
    }

    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                capturedImage = it.toImageBitmap()
                recognitionResult = signRecognizer.recognize(it)
            }
        }
    )

    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("Nhận diện biển báo", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraOpen) {
                PeekabooCamera(
                    state = cameraState,
                    modifier = Modifier.fillMaxSize(),
                    permissionDeniedContent = {
                        Text("Cần cấp quyền camera để sử dụng tính năng này", textAlign = TextAlign.Center)
                    }
                )
                
                IconButton(
                    onClick = { cameraState.capture() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .size(64.dp)
                        .background(Color.White, RoundedCornerShape(32.dp))
                ) {
                    Icon(Icons.Default.Camera, contentDescription = "Chụp ảnh", tint = Color.Black)
                }
            } else if (capturedImage != null) {
                Image(
                    bitmap = capturedImage!!,
                    contentDescription = "Selected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                
                IconButton(
                    onClick = { 
                        capturedImage = null
                        recognitionResult = null 
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Xóa ảnh", tint = Color.White)
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Chưa có ảnh nào được chọn",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = { isCameraOpen = true },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chụp ảnh")
            }
            
            OutlinedButton(
                onClick = { singleImagePicker.launch() },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thư viện")
            }
        }

        if (capturedImage != null && !isCameraOpen) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "KẾT QUẢ NHẬN DIỆN",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (recognitionResult != null) {
                    val isConfident = recognitionResult!!.confidence >= 0.35f
                    val matchingSign = if (isConfident) signs.find { it.code == recognitionResult!!.label } else null
                    
                    if (isConfident && matchingSign != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Cột trái
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                val resourceName = matchingSign.image.removeSuffix(".png").lowercase()
                                Image(
                                    painter = painterResource(getDrawableResource(resourceName)),
                                    contentDescription = matchingSign.name,
                                    modifier = Modifier.size(90.dp).padding(bottom = 8.dp),
                                    contentScale = ContentScale.Fit
                                )
                                Text(
                                    text = matchingSign.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Cột phải
                            Column(
                                modifier = Modifier.weight(1.5f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Mã biển: ${matchingSign.code}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                Text(
                                    text = "Loại: ${matchingSign.type}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
                                    if (recognitionResult!!.confidence < 0.50f) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }
                                    Text(
                                        text = "Độ tin cậy: ${(recognitionResult!!.confidence * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (recognitionResult!!.confidence < 0.50f) Color(0xFFE65100) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                TextButton(
                                    onClick = { detailedSign = matchingSign },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text("Xem chi tiết", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    } else {
                        // Dưới ngưỡng 35% hoặc không tìm thấy biển phù hợp
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Không nhận diện được biển báo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Độ tin cậy quá thấp (< 35%). Hãy thử chụp lại rõ hơn.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Không nhận diện được biển báo",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(100.dp))
    }
    } // Close Scaffold

    if (detailedSign != null) {
        TrafficSignDetailDialog(
            sign = detailedSign!!,
            onDismiss = { detailedSign = null }
        )
    }
}
