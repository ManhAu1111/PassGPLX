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

    private val cache = object : LinkedHashMap<String, ImageBitmap>(MAX_SIZE, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>?): Boolean {
            return size > MAX_SIZE
        }
    }

    fun get(key: String): ImageBitmap? = cache[key]

    fun put(key: String, bitmap: ImageBitmap) {
        cache[key] = bitmap
    }

    fun contains(key: String): Boolean = cache.containsKey(key)
}
