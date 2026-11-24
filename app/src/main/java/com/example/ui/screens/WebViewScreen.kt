package com.example.ui.screens

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.PermissionRequest
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import java.util.Calendar
import java.util.Locale

// جسر الإشعارات (تم تفعيله ليعمل فعلياً عبر AlarmManager)
class AndroidNotificationBridge(private val context: Context) {
    @JavascriptInterface
    fun schedule(sessionId: String, subjectName: String, timeString: String) {
        try {
            // --- كود تجريبي لإظهار إشعار فوري ---
            if (timeString == "الآن") {
                val intent = Intent(context, com.example.NotificationReceiver::class.java).apply {
                    putExtra("subject", "تجربة التنبيهات 🎉")
                    putExtra("time", "تعمل بنجاح!")
                }
                val pendingIntent = PendingIntent.getBroadcast(context, 999, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 5000, pendingIntent)
                return
            }
            // ------------------------------------
            // استخراج وقت البدء فقط
            val regex = Regex("(\\d{1,2}):(\\d{2})")
            val match = regex.find(timeString) ?: return

            val hour = match.groupValues[1].toIntOrNull() ?: return
            val minute = match.groupValues[2].toIntOrNull() ?: return

            val intent = Intent(context, com.example.NotificationReceiver::class.java).apply {
                putExtra("subject", subjectName)
                putExtra("time", timeString)
            }

            val pendingIntent = PendingIntent.getBroadcast(
                context,
                sessionId.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val calendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                add(Calendar.MINUTE, -30) // تذكير قبل 30 دقيقة
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
            }
            Log.d("AndroidNotification", "Alarm set for: $subjectName at $timeString")
        } catch (e: Exception) {
            Log.e("AndroidNotification", "Error scheduling alarm", e)
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewScreen() {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // 1. إعداد محرك القراءة الصوتية (TTS)
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    DisposableEffect(Unit) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("ar")
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = if (result.resultCode == Activity.RESULT_OK) {
            WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        } else {
            null
        }
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    BackHandler(enabled = webViewInstance?.canGoBack() == true) {
        webViewInstance?.goBack()
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .testTag("webview_screen"),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    cacheMode = WebSettings.LOAD_DEFAULT
                    allowFileAccess = true
                    allowContentAccess = true
                    mediaPlaybackRequiresUserGesture = false
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // --- إضافات للسماح بقراءة ملفات vendor المحلية ---
                    @Suppress("DEPRECATION")
                    allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    allowUniversalAccessFromFileURLs = true

                }

                addJavascriptInterface(AndroidNotificationBridge(ctx), "AndroidNotification")

                // 2. ربط جسر القراءة الصوتية
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun speak(text: String) {
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "TTS_ID")
                    }
                    @JavascriptInterface
                    fun stop() {
                        tts?.stop()
                    }
                }, "AndroidTTS")

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("WebViewScreen", "Loaded: $url")
                    }
                    // 3. حماية التطبيق من الانهيار إذا نفدت ذاكرة الهاتف
                    override fun onRenderProcessGone(view: WebView?, detail: RenderProcessGoneDetail?): Boolean {
                        view?.destroy()
                        return true
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onPermissionRequest(request: PermissionRequest?) {
                        request?.grant(request.resources)
                    }

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d(
                            "WebViewConsole",
                            "${consoleMessage?.message()} [${consoleMessage?.sourceId()}:${consoleMessage?.lineNumber()}]"
                        )
                        return true
                    }

                    override fun onShowFileChooser(
                        webView: WebView?,
                        filePathCallbackInternal: ValueCallback<Array<Uri>>?,
                        fileChooserParams: FileChooserParams?
                    ): Boolean {
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = filePathCallbackInternal

                        return try {
                            val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                            }
                            fileChooserLauncher.launch(intent)
                            true
                        } catch (e: Exception) {
                            Log.e("WebViewScreen", "Failed to launch native file chooser", e)
                            filePathCallback?.onReceiveValue(null)
                            filePathCallback = null
                            false
                        }
                    }
                }
                loadUrl("file:///android_asset/web_app/index.html")
                webViewInstance = this
            }
        },
        update = { webView ->
            webViewInstance = webView
        },
        // 4. تنظيف الذاكرة عند الخروج
        onRelease = { webView ->
            webView.stopLoading()
            webView.removeJavascriptInterface("AndroidNotification")
            webView.removeJavascriptInterface("AndroidTTS")
            webView.loadUrl("about:blank")
            webView.destroy()
        }
    )
}