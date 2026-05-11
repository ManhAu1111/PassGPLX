package com.example.passgplx

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.preat.peekaboo.image.picker.SelectionMode
import com.preat.peekaboo.image.picker.rememberImagePickerLauncher
import com.preat.peekaboo.ui.camera.PeekabooCamera
import com.preat.peekaboo.ui.camera.rememberPeekabooCameraState
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import passgplx.composeapp.generated.resources.*

@Serializable
data class TrafficSign(
    val id: String,
    val code: String,
    val name: String,
    val type: String,
    val category: String,
    val detail: String,
    val image: String
)

expect fun ByteArray.toImageBitmap(): ImageBitmap

sealed class Screen(val route: String, val icon: ImageVector) {
    object Review : Screen("review", Icons.Default.Book)
    object MockExam : Screen("mock_exam", Icons.AutoMirrored.Filled.Assignment)
    object TrafficSigns : Screen("traffic_signs", Icons.Default.Warning)
    object SignDetection : Screen("sign_detection", Icons.Default.CameraAlt)
}

val items = listOf(
    Screen.Review,
    Screen.MockExam,
    Screen.TrafficSigns,
    Screen.SignDetection,
)

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()
        
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    items.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(28.dp)
                                )
                            },
                            label = null,
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().displayName) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Review.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                },
                exitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(400)
                    ) + fadeOut(animationSpec = tween(400))
                },
                popEnterTransition = {
                    slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    ) + fadeIn(animationSpec = tween(400))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(400)
                    ) + fadeOut(animationSpec = tween(400))
                }
            ) {
                composable(Screen.Review.route) {
                    ReviewScreen()
                }
                composable(Screen.MockExam.route) {
                    MockExamScreen()
                }
                composable(Screen.TrafficSigns.route) {
                    TrafficSignsScreen()
                }
                composable(Screen.SignDetection.route) {
                    SignDetectionScreen()
                }
            }
        }
    }
}

@Composable
fun ReviewScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Màn hình Ôn tập", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
fun MockExamScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Màn hình Thi thử", style = MaterialTheme.typography.headlineMedium)
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TrafficSignsScreen() {
    var signs by remember { mutableStateOf<List<TrafficSign>>(emptyList()) }
    var selectedSign by remember { mutableStateOf<TrafficSign?>(null) }

    LaunchedEffect(Unit) {
        try {
            val jsonString = Res.readBytes("files/data.json").decodeToString()
            signs = Json.decodeFromString<List<TrafficSign>>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    if (signs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val categories = signs.map { it.type }.distinct()
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    
    val filteredSigns = signs.filter { it.type == selectedCategory }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = categories.indexOf(selectedCategory),
            edgePadding = 16.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            categories.forEach { category ->
                Tab(
                    selected = selectedCategory == category,
                    onClick = { selectedCategory = category },
                    text = { Text(category) }
                )
            }
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredSigns) { sign ->
                TrafficSignCard(sign, onClick = { selectedSign = sign })
            }
        }
    }

    if (selectedSign != null) {
        TrafficSignDetailDialog(
            sign = selectedSign!!,
            onDismiss = { selectedSign = null }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TrafficSignCard(sign: TrafficSign, onClick: () -> Unit) {
    val resourceName = sign.image.removeSuffix(".png").lowercase()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(getDrawableResource(resourceName)),
                    contentDescription = sign.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Text(
                text = sign.code,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = sign.name,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun TrafficSignDetailDialog(sign: TrafficSign, onDismiss: () -> Unit) {
    val resourceName = sign.image.removeSuffix(".png").lowercase()
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(getDrawableResource(resourceName)),
                        contentDescription = sign.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Text(
                    text = sign.code,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = sign.name,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = sign.type,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                if (sign.detail.isNotEmpty()) {
                    Text(
                        text = "Chi tiết:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    Text(
                        text = sign.detail,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 24.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Đóng")
                }
            }
        }
    }
}

@Composable
fun SignDetectionScreen() {
    var capturedImage by remember { mutableStateOf<ImageBitmap?>(null) }
    var isCameraOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    val singleImagePicker = rememberImagePickerLauncher(
        selectionMode = SelectionMode.Single,
        scope = scope,
        onResult = { byteArrays ->
            byteArrays.firstOrNull()?.let {
                capturedImage = it.toImageBitmap()
            }
        }
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Nhận diện biển báo",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (isCameraOpen) {
                val state = rememberPeekabooCameraState(onCapture = { byteArrays ->
                    byteArrays?.let {
                        capturedImage = it.toImageBitmap()
                        isCameraOpen = false
                    }
                })
                PeekabooCamera(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    permissionDeniedContent = {
                        Text("Cần cấp quyền camera để sử dụng tính năng này", textAlign = TextAlign.Center)
                    }
                )
                
                IconButton(
                    onClick = { state.capture() },
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
                    onClick = { capturedImage = null },
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Thư viện")
            }
        }

        if (capturedImage != null && !isCameraOpen) {
            Spacer(modifier = Modifier.height(32.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "KẾT QUẢ NHẬN DIỆN",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Biển báo cấm đi ngược chiều", // Placeholder
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Độ tin cậy: 98%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
fun getDrawableResource(name: String): DrawableResource {
    return when (name) {
        "p_101" -> Res.drawable.p_101
        "p_102" -> Res.drawable.p_102
        "p_103a" -> Res.drawable.p_103a
        "p_103b" -> Res.drawable.p_103b
        "p_103c" -> Res.drawable.p_103c
        "p_104" -> Res.drawable.p_104
        "p_105" -> Res.drawable.p_105
        "p_106a" -> Res.drawable.p_106a
        "p_106b" -> Res.drawable.p_106b
        "p_106c" -> Res.drawable.p_106c
        "p_107" -> Res.drawable.p_107
        "p_107a" -> Res.drawable.p_107a
        "p_107b" -> Res.drawable.p_107b
        "p_108" -> Res.drawable.p_108
        "p_109" -> Res.drawable.p_109
        "p_110a" -> Res.drawable.p_110a
        "p_110b" -> Res.drawable.p_110b
        "p_111a" -> Res.drawable.p_111a
        "p_111b" -> Res.drawable.p_111b
        "p_111c" -> Res.drawable.p_111c
        "p_111d" -> Res.drawable.p_111d
        "p_112" -> Res.drawable.p_112
        "p_113" -> Res.drawable.p_113
        "p_114" -> Res.drawable.p_114
        "p_115" -> Res.drawable.p_115
        "p_116" -> Res.drawable.p_116
        "p_117" -> Res.drawable.p_117
        "p_118" -> Res.drawable.p_118
        "p_119" -> Res.drawable.p_119
        "p_120" -> Res.drawable.p_120
        "p_121" -> Res.drawable.p_121
        "p_123a" -> Res.drawable.p_123a
        "p_123b" -> Res.drawable.p_123b
        "p_124a" -> Res.drawable.p_124a
        "p_124b" -> Res.drawable.p_124b
        "p_125" -> Res.drawable.p_125
        "p_126" -> Res.drawable.p_126
        "p_127" -> Res.drawable.p_127
        "p_127a" -> Res.drawable.p_127a
        "p_128" -> Res.drawable.p_128
        "p_129" -> Res.drawable.p_129
        "p_130" -> Res.drawable.p_130
        "p_131a" -> Res.drawable.p_131a
        "p_131b" -> Res.drawable.p_131b
        "p_131c" -> Res.drawable.p_131c
        "p_132" -> Res.drawable.p_132
        "dp_133" -> Res.drawable.dp_133
        "dp_134" -> Res.drawable.dp_134
        "dp_135" -> Res.drawable.dp_135
        "p_136" -> Res.drawable.p_136
        "p_137" -> Res.drawable.p_137
        "p_138" -> Res.drawable.p_138
        "p_139" -> Res.drawable.p_139
        "p_140" -> Res.drawable.p_140
        "w_201a" -> Res.drawable.w_201a
        "w_201b" -> Res.drawable.w_201b
        "w_201c" -> Res.drawable.w_201c
        "w_201d" -> Res.drawable.w_201d
        "w_202a" -> Res.drawable.w_202a
        "w_202b" -> Res.drawable.w_202b
        "w_203a" -> Res.drawable.w_203a
        "w_203b" -> Res.drawable.w_203b
        "w_203c" -> Res.drawable.w_203c
        "w_204" -> Res.drawable.w_204
        "w_205a" -> Res.drawable.w_205a
        "w_205b" -> Res.drawable.w_205b
        "w_205c" -> Res.drawable.w_205c
        "w_205d" -> Res.drawable.w_205d
        "w_205e" -> Res.drawable.w_205e
        "w_206" -> Res.drawable.w_206
        "w_207a" -> Res.drawable.w_207a
        "w_207b" -> Res.drawable.w_207b
        "w_207c" -> Res.drawable.w_207c
        "w_207d" -> Res.drawable.w_207d
        "w_207e" -> Res.drawable.w_207e
        "w_207f" -> Res.drawable.w_207f
        "w_207g" -> Res.drawable.w_207g
        "w_207h" -> Res.drawable.w_207h
        "w_207i" -> Res.drawable.w_207i
        "w_207k" -> Res.drawable.w_207k
        "w_207l" -> Res.drawable.w_207l
        "w_208" -> Res.drawable.w_208
        "w_209" -> Res.drawable.w_209
        "w_210" -> Res.drawable.w_210
        "w_211a" -> Res.drawable.w_211a
        "w_211b" -> Res.drawable.w_211b
        "w_212" -> Res.drawable.w_212
        "w_213" -> Res.drawable.w_213
        "w_214" -> Res.drawable.w_214
        "w_215a" -> Res.drawable.w_215a
        "w_215b" -> Res.drawable.w_215b
        "w_215c" -> Res.drawable.w_215c
        "w_216a" -> Res.drawable.w_216a
        "w_216b" -> Res.drawable.w_216b
        "w_217" -> Res.drawable.w_217
        "w_218" -> Res.drawable.w_218
        "w_219" -> Res.drawable.w_219
        "w_220" -> Res.drawable.w_220
        "w_221a" -> Res.drawable.w_221a
        "w_221b" -> Res.drawable.w_221b
        "w_222a" -> Res.drawable.w_222a
        "w_222b" -> Res.drawable.w_222b
        "w_223a" -> Res.drawable.w_223a
        "w_223b" -> Res.drawable.w_223b
        "w_224" -> Res.drawable.w_224
        "w_225" -> Res.drawable.w_225
        "w_226" -> Res.drawable.w_226
        "w_227" -> Res.drawable.w_227
        "w_228a" -> Res.drawable.w_228a
        "w_228b" -> Res.drawable.w_228b
        "w_228c" -> Res.drawable.w_228c
        "w_228d" -> Res.drawable.w_228d
        "w_229" -> Res.drawable.w_229
        "w_230" -> Res.drawable.w_230
        "w_231" -> Res.drawable.w_231
        "w_232" -> Res.drawable.w_232
        "w_233" -> Res.drawable.w_233
        "w_234" -> Res.drawable.w_234
        "w_235" -> Res.drawable.w_235
        "w_236" -> Res.drawable.w_236
        "w_237" -> Res.drawable.w_237
        "w_238" -> Res.drawable.w_238
        "w_239a" -> Res.drawable.w_239a
        "w_239b" -> Res.drawable.w_239b
        "w_240" -> Res.drawable.w_240
        "w_241" -> Res.drawable.w_241
        "w_243a" -> Res.drawable.w_243a
        "w_243b" -> Res.drawable.w_243b
        "w_243c" -> Res.drawable.w_243c
        "w_244" -> Res.drawable.w_244
        "w_245a" -> Res.drawable.w_245a
        "w_246a" -> Res.drawable.w_246a
        "w_246b" -> Res.drawable.w_246b
        "w_246c" -> Res.drawable.w_246c
        "w_247" -> Res.drawable.w_247
        "r_122" -> Res.drawable.r_122
        "r_301a" -> Res.drawable.r_301a
        "r_301b" -> Res.drawable.r_301b
        "r_301c" -> Res.drawable.r_301c
        "r_301d" -> Res.drawable.r_301d
        "r_301e" -> Res.drawable.r_301e
        "r_301f" -> Res.drawable.r_301f
        "r_301g" -> Res.drawable.r_301g
        "r_301h" -> Res.drawable.r_301h
        "r_302a" -> Res.drawable.r_302a
        "r_302b" -> Res.drawable.r_302b
        "r_302c" -> Res.drawable.r_302c
        "r_303" -> Res.drawable.r_303
        "r_304" -> Res.drawable.r_304
        "r_305" -> Res.drawable.r_305
        "r_306" -> Res.drawable.r_306
        "r_307" -> Res.drawable.r_307
        "r_308a" -> Res.drawable.r_308a
        "r_308b" -> Res.drawable.r_308b
        "r_309" -> Res.drawable.r_309
        "r_310a" -> Res.drawable.r_310a
        "r_310b" -> Res.drawable.r_310b
        "r_310c" -> Res.drawable.r_310c
        "r_403a" -> Res.drawable.r_403a
        "r_403b" -> Res.drawable.r_403b
        "r_403c" -> Res.drawable.r_403c
        "r_403d" -> Res.drawable.r_403d
        "r_403e" -> Res.drawable.r_403e
        "r_403f" -> Res.drawable.r_403f
        "r_404a" -> Res.drawable.r_404a
        "r_404b" -> Res.drawable.r_404b
        "r_404c" -> Res.drawable.r_404c
        "r_404d" -> Res.drawable.r_404d
        "r_404e" -> Res.drawable.r_404e
        "r_404f" -> Res.drawable.r_404f
        "r_411" -> Res.drawable.r_411
        "r_412a" -> Res.drawable.r_412a
        "r_412b" -> Res.drawable.r_412b
        "r_412c" -> Res.drawable.r_412c
        "r_412d" -> Res.drawable.r_412d
        "r_412e" -> Res.drawable.r_412e
        "r_412f" -> Res.drawable.r_412f
        "r_412g" -> Res.drawable.r_412g
        "r_412i" -> Res.drawable.r_412i
        "r_412j" -> Res.drawable.r_412j
        "r_412k" -> Res.drawable.r_412k
        "r_412l" -> Res.drawable.r_412l
        "r_412m" -> Res.drawable.r_412m
        "r_412n" -> Res.drawable.r_412n
        "r_412o" -> Res.drawable.r_412o
        "r_415a" -> Res.drawable.r_415a
        "r_415b" -> Res.drawable.r_415b
        "r_420" -> Res.drawable.r_420
        "r_421" -> Res.drawable.r_421
        "r_e_9a" -> Res.drawable.r_e_9a
        "r_e_9b" -> Res.drawable.r_e_9b
        "r_e_9c" -> Res.drawable.r_e_9c
        "r_e_9d" -> Res.drawable.r_e_9d
        "r_e_11a" -> Res.drawable.r_e_11a
        "r_e_11b" -> Res.drawable.r_e_11b
        "i_401" -> Res.drawable.i_401
        "i_402" -> Res.drawable.i_402
        "i_405a" -> Res.drawable.i_405a
        "i_405b" -> Res.drawable.i_405b
        "i_405c" -> Res.drawable.i_405c
        "i_406" -> Res.drawable.i_406
        "i_407a" -> Res.drawable.i_407a
        "i_407b" -> Res.drawable.i_407b
        "i_407c" -> Res.drawable.i_407c
        "i_408" -> Res.drawable.i_408
        "i_408a" -> Res.drawable.i_408a
        "i_409" -> Res.drawable.i_409
        "i_410" -> Res.drawable.i_410
        "i_416" -> Res.drawable.i_416
        "i_418" -> Res.drawable.i_418
        "i_423a" -> Res.drawable.i_423a
        "i_423b" -> Res.drawable.i_423b
        "i_423c" -> Res.drawable.i_423c
        "i_424a" -> Res.drawable.i_424a
        "i_424b" -> Res.drawable.i_424b
        "i_424c" -> Res.drawable.i_424c
        "i_424d" -> Res.drawable.i_424d
        else -> Res.drawable.compose_multiplatform
    }
}
