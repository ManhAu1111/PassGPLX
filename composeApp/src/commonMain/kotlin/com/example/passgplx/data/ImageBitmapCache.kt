package com.example.passgplx.data

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Singleton LRU-style cache cho ImageBitmap của biển báo giao thông.
 * Tránh việc mỗi TrafficSignCard phải đọc + decode PNG từ disk
 * mỗi lần xuất hiện trên màn hình khi scroll.
 *
 * Dùng LinkedHashMap với accessOrder = true để tự động evict
 * các entry cũ nhất khi cache đầy (giới hạn MAX_SIZE).
 */
object ImageBitmapCache {
    private const val MAX_SIZE = 120

    private val cache = LinkedHashMap<String, ImageBitmap>()

    fun get(key: String): ImageBitmap? {
        val bitmap = cache[key] ?: return null
        // Di chuyển phần tử vừa truy cập xuống cuối map để giữ thứ tự LRU (Least Recently Used)
        cache.remove(key)
        cache[key] = bitmap
        return bitmap
    }

    fun put(key: String, bitmap: ImageBitmap) {
        if (cache.containsKey(key)) {
            cache.remove(key)
        } else if (cache.size >= MAX_SIZE) {
            // Loại bỏ phần tử ít được sử dụng nhất (phần tử đầu tiên trong map)
            val eldestKey = cache.keys.firstOrNull()
            if (eldestKey != null) {
                cache.remove(eldestKey)
            }
        }
        cache[key] = bitmap
    }

    fun contains(key: String): Boolean = cache.containsKey(key)
}
