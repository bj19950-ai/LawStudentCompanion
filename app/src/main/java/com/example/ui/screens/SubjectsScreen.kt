package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataProvider
import com.example.model.LawSubject
import com.example.ui.theme.AppBackground
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class SubjectDetailType {
    LECTURES,
    TD,
    LEGAL_TEXTS,
    PROGRESS
}

@Composable
fun SubjectsScreen(
    onAskAboutSubject: (String) -> Unit
) {
    val subjects = MockDataProvider.subjects

    var activeDialogSubject by remember { mutableStateOf<LawSubject?>(null) }
    var activeDetailType by remember { mutableStateOf(SubjectDetailType.LECTURES) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("subjects_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = NavyPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "المقررات والمواد الدراسية",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NavyPrimary
                    )
                }

                Text(
                    text = "محاضرات، دروس مسيرة، ونصوص قانونية مع متابعة الإنجاز",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Subjects List
        items(subjects, key = { it.id }) { subject ->
            SubjectCard(
                subject = subject,
                onActionClick = { type ->
                    activeSubjectDetail(subject, type).also {
                        activeDialogSubject = subject
                        activeDetailType = type
                    }
                }
            )
        }
    }

    // Detail Dialog when tapping any of the 4 icons
    activeDialogSubject?.let { subject ->
        SubjectDetailDialog(
            subject = subject,
            type = activeDetailType,
            onDismiss = { activeDialogSubject = null },
            onAskAssistant = { query ->
                activeDialogSubject = null
                onAskAboutSubject(query)
            }
        )
    }
}

private fun activeSubjectDetail(subject: LawSubject, type: SubjectDetailType) {
    // Helper to trigger state change
}

@Composable
private fun SubjectCard(
    subject: LawSubject,
    onActionClick: (SubjectDetailType) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(12.dp))
            .testTag("subject_card_${subject.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Subject title & code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subject.name,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyPrimary
                    )
                    Text(
                        text = "${subject.code} • ${subject.professor}",
                        fontFamily = CairoFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                // Progress Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldSecondary.copy(alpha = 0.18f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary)
                ) {
                    Text(
                        text = "${(subject.progress * 100).toInt()}% منجز",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = NavyDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subject.description,
                fontFamily = CairoFontFamily,
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Mini progress indicator
            LinearProgressIndicator(
                progress = { subject.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = GoldSecondary,
                trackColor = Color(0xFFE2E8F0)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // The 4 Required Action Icons with Text
            // (المحاضرات - الدروس المسيرة - النصوص القانونية - التقدم)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                    .padding(vertical = 10.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SubjectActionItem(
                    label = "المحاضرات",
                    icon = Icons.Default.School,
                    count = "${subject.lecturesCount}",
                    tint = NavyPrimary,
                    onClick = { onActionClick(SubjectDetailType.LECTURES) },
                    testTag = "action_lectures_${subject.id}"
                )

                SubjectActionItem(
                    label = "الدروس المسيرة",
                    icon = Icons.Default.Assignment,
                    count = "${subject.tdCount}",
                    tint = Color(0xFF059669),
                    onClick = { onActionClick(SubjectDetailType.TD) },
                    testTag = "action_td_${subject.id}"
                )

                SubjectActionItem(
                    label = "النصوص القانونية",
                    icon = Icons.Default.Gavel,
                    count = "${subject.textsCount}",
                    tint = GoldDark,
                    onClick = { onActionClick(SubjectDetailType.LEGAL_TEXTS) },
                    testTag = "action_texts_${subject.id}"
                )

                SubjectActionItem(
                    label = "التقدم",
                    icon = Icons.Default.TrendingUp,
                    count = "${(subject.progress * 100).toInt()}%",
                    tint = Color(0xFF4338CA),
                    onClick = { onActionClick(SubjectDetailType.PROGRESS) },
                    testTag = "action_progress_${subject.id}"
                )
            }
        }
    }
}

@Composable
private fun SubjectActionItem(
    label: String,
    icon: ImageVector,
    count: String,
    tint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextPrimary
        )

        Text(
            text = count,
            fontFamily = CairoFontFamily,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun SubjectDetailDialog(
    subject: LawSubject,
    type: SubjectDetailType,
    onDismiss: () -> Unit,
    onAskAssistant: (String) -> Unit
) {
    val (title, icon) = when (type) {
        SubjectDetailType.LECTURES -> Pair("محاضرات ${subject.name}", Icons.Default.School)
        SubjectDetailType.TD -> Pair("الدروس المسيرة (TD) - ${subject.name}", Icons.Default.Assignment)
        SubjectDetailType.LEGAL_TEXTS -> Pair("النصوص والمواد القانونية - ${subject.name}", Icons.Default.Gavel)
        SubjectDetailType.PROGRESS -> Pair("خطة التقدم والإنجاز - ${subject.name}", Icons.Default.TrendingUp)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NavyPrimary
                )
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (type) {
                    SubjectDetailType.LECTURES -> {
                        item {
                            Text(
                                text = "قائمة المحاضرات المقررة (${subject.lecturesCount} محاضرات):",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        items(subject.lectures) { lecture ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = lecture.title,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = lecture.date,
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    lecture.keyPoints.forEach { point ->
                                        Text(
                                            text = "• $point",
                                            fontFamily = CairoFontFamily,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    SubjectDetailType.TD -> {
                        item {
                            Text(
                                text = "أوراق الأعمال الموجهة والتمارين (${subject.tdCount} حصص):",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        items(subject.tdSheets) { sheet ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = sheet,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = TextPrimary
                                    )
                                }
                            }
                        }
                    }

                    SubjectDetailType.LEGAL_TEXTS -> {
                        item {
                            Text(
                                text = "أهم المواد والنصوص القانونية المعتمدة:",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                        }
                        items(subject.keyArticles) { article ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFFBEB),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldSecondary),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = article.title,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = NavyDark
                                        )
                                        Text(
                                            text = article.articleNumber,
                                            fontFamily = CairoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = GoldDark
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.content,
                                        fontFamily = CairoFontFamily,
                                        fontSize = 12.sp,
                                        color = TextPrimary,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }

                    SubjectDetailType.PROGRESS -> {
                        item {
                            Column {
                                Text(
                                    text = "نسبة الاستيعاب والمراجعة: ${(subject.progress * 100).toInt()}%",
                                    fontFamily = CairoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = NavyPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { subject.progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = GoldSecondary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "• المحاضرات المحضورة والمراجعة: ${((subject.lecturesCount * subject.progress)).toInt()} من ${subject.lecturesCount}",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "• بطاقات الاستشارة المحلولة: 3 بطاقات",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "• الجاهزية للامتحان السداسي: ممتازة",
                                    fontFamily = CairoFontFamily,
                                    fontSize = 12.sp,
                                    color = Color(0xFF059669),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NavyPrimary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "إغلاق",
                    fontFamily = CairoFontFamily,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onAskAssistant("لخص لي موضوعات مادة ${subject.name}")
                }
            ) {
                Text(
                    text = "اسأل المساعد عن المادة",
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = GoldDark
                )
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}
