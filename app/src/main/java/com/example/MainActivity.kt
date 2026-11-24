package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MockDataProvider
import com.example.model.ChatMessage
import com.example.model.ScreenTab
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.SubjectsScreen
import com.example.ui.screens.WebViewScreen
import com.example.ui.theme.CairoFontFamily
import com.example.ui.theme.GoldSecondary
import com.example.ui.theme.LawCompanionTheme
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LawCompanionTheme {
                // Ensure complete Right-to-Left (RTL) layout direction across the entire app
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    LawCompanionApp()
                }
            }
        }
    }
}

@Composable
fun LawCompanionApp() {
    var selectedTab by remember { mutableStateOf(ScreenTab.HOME) }
    var isWebViewMode by remember { mutableStateOf(true) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val chatMessages = remember {
        mutableStateListOf<ChatMessage>().apply {
            addAll(MockDataProvider.initialChatMessages)
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val context = androidx.compose.ui.platform.LocalContext.current

    // --- تعديل: طلب صلاحيات الميكروفون والإشعارات معاً ---
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf<String>()

        // 1. صلاحية الميكروفون
        if (androidx.core.content.ContextCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(android.Manifest.permission.RECORD_AUDIO)
        }

        // 2. صلاحية الإشعارات (لأندرويد 13 فما فوق)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
    // ----------------------------------------------------

    // Handle back button: return to Home tab if in native view mode
    BackHandler(enabled = !isWebViewMode && selectedTab != ScreenTab.HOME) {
        selectedTab = ScreenTab.HOME
    }

    val onSendMessage: (String) -> Unit = { userPrompt ->
        val userMsg = ChatMessage(
            id = "user_${System.currentTimeMillis()}",
            isUser = true,
            text = userPrompt,
            timestamp = "الآن"
        )
        chatMessages.add(userMsg)

        coroutineScope.launch {
            delay(400) // Realistic legal assistant thinking delay
            val assistantReply = MockDataProvider.getAssistantResponse(userPrompt)
            chatMessages.add(assistantReply)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("app_scaffold"),
        topBar = {
            if (!isWebViewMode) {
                AppTopBar(
                    isWebViewMode = isWebViewMode,
                    onToggleWebView = { isWebViewMode = !isWebViewMode },
                    onShowAboutDialog = { showAboutDialog = true },
                    onResetData = {
                        chatMessages.clear()
                        chatMessages.addAll(MockDataProvider.initialChatMessages)
                    }
                )
            }
        },
        bottomBar = {
            if (!isWebViewMode) {
                AppBottomNav(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> selectedTab = tab }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (isWebViewMode) {
                WebViewScreen()
            } else {
                Crossfade(targetState = selectedTab, label = "TabCrossfade") { tab ->
                    when (tab) {
                        ScreenTab.HOME -> HomeScreen(
                            onNavigateToTab = { targetTab -> selectedTab = targetTab },
                            onAskAssistant = { query ->
                                selectedTab = ScreenTab.ASSISTANT
                                onSendMessage(query)
                            }
                        )

                        ScreenTab.SCHEDULE -> ScheduleScreen()

                        ScreenTab.SUBJECTS -> SubjectsScreen(
                            onAskAboutSubject = { query ->
                                selectedTab = ScreenTab.ASSISTANT
                                onSendMessage(query)
                            }
                        )

                        ScreenTab.ASSISTANT -> AssistantScreen(
                            messages = chatMessages,
                            onSendMessage = onSendMessage
                        )
                    }
                }
            }
        }
    }

    // About Application Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Balance,
                        contentDescription = null,
                        tint = GoldSecondary,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "عن رفيق طالب الحقوق",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = NavyPrimary
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "«رفيق طالب الحقوق» هو التطبيق الأكاديمي المتكامل المخصص لطلبة كليات العلوم القانونية والحقوق.",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp,
                        color = TextPrimary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF8FAFC),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "• تصميم مخصص من اليمين لليسار (RTL)",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• خط Cairo العربي الأنيق",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• بطاقة الحصة القادمة ومراجعة ما قبل الحصة",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• جدول المحاضرات والـ TD الأسبوعي",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• مكتبة المواد (محاضرات، TD، نصوص قانونية، تقدم)",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• مساعد ذكي بأزرار سريعة وإجابات قانونية",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Text(
                                text = "• يدعم كلاً من العرض الأصلي والويب (WebView / Tailwind)",
                                fontFamily = CairoFontFamily,
                                fontSize = 12.sp,
                                color = NavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NavyPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "حسناً",
                        fontFamily = CairoFontFamily,
                        fontSize = 13.sp
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }
}