package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.RobotoMonoFontFamily

// Desktop Chrome User Agent required for Google Messages desktop client
private const val GOOGLE_MESSAGES_DESKTOP_UA =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
private const val GOOGLE_MESSAGES_WEB_URL = "https://messages.google.com/web"
// Official direct QR authentication URL that displays the QR code scanner directly
private const val GOOGLE_MESSAGES_AUTH_URL = "https://messages.google.com/web/authentication"

fun launchGoogleMessagesInSystemBrowser(context: Context, url: String = GOOGLE_MESSAGES_WEB_URL) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Cannot open default browser: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleMessagesWebScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var currentUrl by remember { mutableStateOf(GOOGLE_MESSAGES_AUTH_URL) }
    var isLoading by remember { mutableStateOf(true) }
    var loadProgress by remember { mutableIntStateOf(0) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showPairingGuide by remember { mutableStateOf(false) }
    var showSecurityExplainer by remember { mutableStateOf(false) }
    var canGoBackInWeb by remember { mutableStateOf(false) }
    var isBrowserBannerVisible by remember { mutableStateOf(true) }

    // Text zoom levels to adapt desktop layout comfortably on phone screens
    val zoomLevels = listOf(75, 85, 100, 115)
    var currentZoomIndex by remember { mutableIntStateOf(1) } // default 85% zoom for great phone readability

    // Handle back gesture: back in web history if possible, else return to main app
    BackHandler(enabled = canGoBackInWeb) {
        if (webViewRef?.canGoBack() == true) {
            webViewRef?.goBack()
        } else {
            onNavigateBack()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Surface(
                color = Color(0xFF09090B),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E1E24)),
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("google_web_back_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Google Messages",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.15f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "WEB SYNC",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "messages.google.com/web",
                                fontSize = 11.sp,
                                fontFamily = RobotoMonoFontFamily,
                                color = Color(0xFF71717A)
                            )
                        }

                        // Zoom control button
                        IconButton(
                            onClick = {
                                currentZoomIndex = (currentZoomIndex + 1) % zoomLevels.size
                                val zoom = zoomLevels[currentZoomIndex]
                                webViewRef?.settings?.textZoom = zoom
                                Toast.makeText(context, "Scale: $zoom%", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("google_web_zoom_btn")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF18181B),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF27272A)),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${zoomLevels[currentZoomIndex]}%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        // Pairing instructions helper
                        IconButton(
                            onClick = { showPairingGuide = true },
                            modifier = Modifier.testTag("google_web_help_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = "Pairing Guide",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Reload webview
                        IconButton(
                            onClick = {
                                hasError = false
                                webViewRef?.reload()
                            },
                            modifier = Modifier.testTag("google_web_reload_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Reload",
                                tint = Color.White
                            )
                        }

                        // Open in Chrome / external browser
                        IconButton(
                            onClick = {
                                launchGoogleMessagesInSystemBrowser(context, GOOGLE_MESSAGES_WEB_URL)
                            },
                            modifier = Modifier.testTag("google_web_open_browser_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.OpenInBrowser,
                                contentDescription = "Open in Browser",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Progress bar
                    if (isLoading) {
                        LinearProgressIndicator(
                            progress = { loadProgress / 100f },
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = Color(0xFF18181B),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                        )
                    }
                }
            }

            // Real Browser Launch Hero Card (solves "browser not secured" error)
            AnimatedVisibility(visible = isBrowserBannerVisible) {
                Surface(
                    color = Color(0xFF0F172A),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Security,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Google Account Sign-In Support",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Google blocks login in app WebViews ('Browser not secure'). Launch your real browser (Chrome/Samsung) to complete sign-in, or use In-App QR pairing below!",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    launchGoogleMessagesInSystemBrowser(context, GOOGLE_MESSAGES_WEB_URL)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("launch_system_browser_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.OpenInBrowser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Launch In Browser",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    hasError = false
                                    currentUrl = GOOGLE_MESSAGES_AUTH_URL
                                    webViewRef?.loadUrl(GOOGLE_MESSAGES_AUTH_URL)
                                    Toast.makeText(context, "Loaded In-App QR Pairing", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("in_app_qr_pair_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "In-App QR",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }

            // Webview / Error Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFF000000))
            ) {
                if (hasError) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Unable to Load Google Messages Web",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (errorMessage.isNotBlank()) errorMessage else "Google's security rules may require your actual system browser (Chrome/Samsung Internet) to complete authentication.",
                            fontSize = 13.sp,
                            color = Color(0xFFA1A1AA),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    launchGoogleMessagesInSystemBrowser(context, GOOGLE_MESSAGES_WEB_URL)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Open in Browser", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    hasError = false
                                    webViewRef?.loadUrl(GOOGLE_MESSAGES_AUTH_URL)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(text = "Retry QR View", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                webViewRef = this
                                layoutParams = android.view.ViewGroup.LayoutParams(
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                setBackgroundColor(android.graphics.Color.BLACK)

                                val cookieManager = CookieManager.getInstance()
                                cookieManager.setAcceptCookie(true)
                                cookieManager.setAcceptThirdPartyCookies(this, true)

                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    userAgentString = GOOGLE_MESSAGES_DESKTOP_UA
                                    useWideViewPort = true
                                    loadWithOverviewMode = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    textZoom = zoomLevels[currentZoomIndex]
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                                    mediaPlaybackRequiresUserGesture = false
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        super.onPageStarted(view, url, favicon)
                                        isLoading = true
                                        hasError = false
                                        canGoBackInWeb = view?.canGoBack() == true

                                        // If an account login / disallow page is detected, route to system browser
                                        if (url != null && (url.contains("accounts.google.com") || url.contains("signin/rejected"))) {
                                            launchGoogleMessagesInSystemBrowser(ctx, url)
                                            Toast.makeText(ctx, "Opened system browser for secure Google Account sign-in", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                        canGoBackInWeb = view?.canGoBack() == true
                                        // Injects standard dark styling hint for pure OLED black aesthetics
                                        view?.evaluateJavascript(
                                            """
                                            (function() {
                                                const meta = document.querySelector('meta[name="color-scheme"]');
                                                if (!meta) {
                                                    const m = document.createElement('meta');
                                                    m.name = 'color-scheme';
                                                    m.content = 'dark';
                                                    document.head.appendChild(m);
                                                }
                                            })();
                                            """.trimIndent(),
                                            null
                                        )
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        super.onReceivedError(view, request, error)
                                        if (request?.isForMainFrame == true) {
                                            hasError = true
                                            isLoading = false
                                            errorMessage = error?.description?.toString() ?: "Connection error"
                                        }
                                    }

                                    override fun shouldOverrideUrlLoading(
                                        view: WebView?,
                                        request: WebResourceRequest?
                                    ): Boolean {
                                        val url = request?.url?.toString() ?: return false

                                        // CRITICAL: Google OAuth and Account sign-in are prohibited inside embedded WebViews.
                                        // Intercept and launch real browser to completely eliminate "browser is not secure" error!
                                        if (url.contains("accounts.google.com") ||
                                            url.contains("accounts.youtube.com") ||
                                            url.contains("ServiceLogin") ||
                                            url.contains("oauth") ||
                                            url.contains("signin/rejected")
                                        ) {
                                            launchGoogleMessagesInSystemBrowser(ctx, url)
                                            Toast.makeText(ctx, "Opening your browser for secure Google Sign-In...", Toast.LENGTH_LONG).show()
                                            return true
                                        }

                                        // Allow messages.google.com navigation in-app
                                        return if (url.contains("messages.google.com")) {
                                            false
                                        } else {
                                            // Open external links in device browser
                                            launchGoogleMessagesInSystemBrowser(ctx, url)
                                            true
                                        }
                                    }
                                }

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        super.onProgressChanged(view, newProgress)
                                        loadProgress = newProgress
                                        if (newProgress >= 100) {
                                            isLoading = false
                                        }
                                    }
                                }

                                loadUrl(GOOGLE_MESSAGES_AUTH_URL)
                            }
                        },
                        update = { webView ->
                            webViewRef = webView
                            webView.settings.textZoom = zoomLevels[currentZoomIndex]
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Initial loading spinner overlay
                if (isLoading && loadProgress < 40 && !hasError) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading Google Messages...",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Direct authentication & pairing interface",
                                fontSize = 11.sp,
                                color = Color(0xFFA1A1AA)
                            )
                        }
                    }
                }
            }

            // Quick bottom pairing hint banner
            Surface(
                color = Color(0xFF09090B),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF27272A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "To pair in-app: Scan QR code with Google Messages",
                            fontSize = 11.sp,
                            color = Color(0xFFA1A1AA)
                        )
                    }

                    TextButton(
                        onClick = { showPairingGuide = true },
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "Pairing Guide",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    // Step-by-Step Pairing Guide Modal
    if (showPairingGuide) {
        AlertDialog(
            onDismissRequest = { showPairingGuide = false },
            containerColor = Color(0xFF0F0F12),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sync & Pairing Options",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Google offers two ways to access your messages on the web:",
                        fontSize = 13.sp,
                        color = Color(0xFFA1A1AA)
                    )

                    PairingStepItem(
                        stepNumber = "A",
                        title = "Option A: System Browser (Google Account Sign-In)",
                        detail = "Google strictly blocks Google Account logins inside embedded app WebViews ('browser is not secure'). Tap 'Launch In Browser' to open Chrome or Samsung Internet where Google sign-in is fully supported."
                    )

                    PairingStepItem(
                        stepNumber = "B",
                        title = "Option B: In-App QR Pairing (Direct)",
                        detail = "You can view messages directly in KNKT without any Google login by using QR code pairing! On this screen, tap 'In-App QR', open Google Messages on your phone ➔ Device pairing ➔ Scan the QR code."
                    )

                    PairingStepItem(
                        stepNumber = "✓",
                        title = "Stay Remembered",
                        detail = "Check 'Remember this computer' on the pairing screen so your connection stays active whenever you return to the app."
                    )
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            showPairingGuide = false
                            launchGoogleMessagesInSystemBrowser(context, GOOGLE_MESSAGES_WEB_URL)
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Open Browser", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { showPairingGuide = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Got it", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        )
    }
}

@Composable
private fun PairingStepItem(
    stepNumber: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stepNumber,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = detail,
                fontSize = 11.sp,
                color = Color(0xFFA1A1AA),
                lineHeight = 15.sp
            )
        }
    }
}
