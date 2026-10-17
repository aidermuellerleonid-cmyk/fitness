package de.fitapp.app.ui.goals

import android.graphics.Color as AndroidColor
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** The original Ziele-Kalender, included as an offline page inside FitApp. */
@Composable
fun GoalsScreen() {
    AndroidView(
        modifier = Modifier,
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/ziele/index.html")
            }
        }
    )
}
