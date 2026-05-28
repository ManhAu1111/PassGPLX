package com.example.passgplx.data

import com.example.passgplx.models.Category

object QuestionDataHelper {
    
    val paralyzingQuestionIds = setOf(
        "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30",
        "33", "35", "36", "37", "40", "43", "45", "46", "47", "48", "49", "50", "51", "52", "53",
        "84", "91", "97", "99", "100", "109", "112", "114", "118", "119", "143", "145", "147",
        "150", "152", "153", "160", "199", "209", "211", "214", "221", "227", "231", "242",
        "245", "258", "260", "261", "262", "266"
    )

    val categories = listOf(
        Category(
            id = "c0",
            name = "Tất cả các câu",
            description = "Tập hợp toàn bộ 600 câu hỏi ôn tập",
            filter = { true }
        ),
        Category(
            id = "c1",
            name = "Câu điểm liệt",
            description = "60 câu hỏi về tình huống mất an toàn giao thông nghiêm trọng",
            isParalyzing = true,
            filter = { paralyzingQuestionIds.contains(it.id) }
        ),
        Category(
            id = "c2",
            name = "Khái niệm quy tắc lý thuyết",
            description = "Câu 1 - 180: Khái niệm và quy tắc giao thông đường bộ",
            filter = { it.id.toIntOrNull() in 1..180 }
        ),
        Category(
            id = "c3",
            name = "Văn hóa giao thông",
            description = "Câu 181 - 205: Văn hóa giao thông và đạo đức người lái xe",
            filter = { it.id.toIntOrNull() in 181..205 }
        ),
        Category(
            id = "c4",
            name = "Kỹ thuật lái xe",
            description = "Câu 206 - 263: Hướng dẫn kỹ thuật điều khiển xe",
            filter = { it.id.toIntOrNull() in 206..263 }
        ),
        Category(
            id = "c5",
            name = "Cấu tạo sửa chữa",
            description = "Câu 264 - 300: Cấu tạo cơ bản và sửa chữa xe ô tô",
            filter = { it.id.toIntOrNull() in 264..300 }
        ),
        Category(
            id = "c6",
            name = "Biển báo",
            description = "Câu 301 - 485: Hệ thống biển báo hiệu đường bộ",
            filter = { it.id.toIntOrNull() in 301..485 }
        ),
        Category(
            id = "c7",
            name = "Tình huống giao thông",
            description = "Câu 486 - 600: Sa hình và tình huống giao thông",
            filter = { it.id.toIntOrNull() in 486..600 }
        )
    )
}
