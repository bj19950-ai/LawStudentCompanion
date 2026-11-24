package com.example.model

enum class ScreenTab(val titleAr: String) {
    HOME("الرئيسية"),
    SCHEDULE("الجدول"),
    SUBJECTS("المواد"),
    ASSISTANT("المساعد")
}

data class NextClassInfo(
    val subject: String,
    val type: String, // محاضرة أو TD
    val time: String,
    val remainingTime: String,
    val hall: String,
    val professor: String,
    val lastTopic: String,
    val nextTopic: String,
    val summaryNotes: String
)

data class ScheduleItem(
    val id: String,
    val dayAr: String,
    val subject: String,
    val type: String,
    val time: String,
    val hall: String,
    val professor: String,
    val isLecture: Boolean
)

data class LegalText(
    val title: String,
    val articleNumber: String,
    val content: String
)

data class SubjectLecture(
    val title: String,
    val date: String,
    val keyPoints: List<String>
)

data class LawSubject(
    val id: String,
    val name: String,
    val code: String,
    val semester: String,
    val professor: String,
    val progress: Float, // 0.0 to 1.0
    val lecturesCount: Int,
    val tdCount: Int,
    val textsCount: Int,
    val description: String,
    val keyArticles: List<LegalText>,
    val lectures: List<SubjectLecture>,
    val tdSheets: List<String>
)

data class ChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val suggestedActions: List<String> = emptyList()
)
