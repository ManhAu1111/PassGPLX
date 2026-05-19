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
            id = "c1",
            name = "Câu điểm liệt",
            description = "60 câu hỏi về tình huống mất an toàn giao thông nghiêm trọng",
            isParalyzing = true,
            filter = { paralyzingQuestionIds.contains(it.id) }
        ),
        Category(
            id = "c2",
            name = "Khái niệm và quy tắc",
            description = "Câu 1 - 166: Khái niệm và quy tắc giao thông đường bộ",
            filter = { it.id.toIntOrNull() in 1..166 }
        ),
        Category(
            id = "c3",
            name = "Nghiệp vụ vận tải",
            description = "Câu 167 - 192: Nghiệp vụ vận tải",
            filter = { it.id.toIntOrNull() in 167..192 }
        ),
        Category(
            id = "c4",
            name = "Văn hóa & Đạo đức",
            description = "Câu 193 - 213: Văn hóa giao thông và đạo đức người lái xe",
            filter = { it.id.toIntOrNull() in 193..213 }
        ),
        Category(
            id = "c5",
            name = "Kỹ thuật lái xe",
            description = "Câu 214 - 269: Kỹ thuật lái xe",
            filter = { it.id.toIntOrNull() in 214..269 }
        ),
        Category(
            id = "c6",
            name = "Cấu tạo & Sửa chữa",
            description = "Câu 270 - 304: Cấu tạo và sửa chữa xe ô tô",
            filter = { it.id.toIntOrNull() in 270..304 }
        ),
        Category(
            id = "c7",
            name = "Biển báo đường bộ",
            description = "Câu 305 - 486: Hệ thống biển báo hiệu đường bộ",
            filter = { it.id.toIntOrNull() in 305..486 }
        ),
        Category(
            id = "c8",
            name = "Sa hình",
            description = "Câu 487 - 600: Giải các thế sa hình",
            filter = { it.id.toIntOrNull() in 487..600 }
        )
    )
}
