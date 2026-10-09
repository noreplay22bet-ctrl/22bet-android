package com.zibongorun.bet22;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.view.WindowInsetsController;
import android.webkit.*;
import android.content.*;
import android.graphics.Color;
import android.widget.Toast;
import android.net.Uri;
import androidx.webkit.*;
import java.util.Collections;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private FrameLayout root;
    private long lastBack;
    private boolean splashBars = true;
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final String HTML_UPDATE_URL = "https://raw.githubusercontent.com/noreplay22bet-ctrl/22bet-android/main/app/src/main/assets/index.html";
    private File cachedHtml;
    private void openUpdatedApp() {
        cachedHtml = new File(getFilesDir(), "live-index.html");
        new Thread(() -> {
            HttpURLConnection connection = null;
            File pending = new File(getFilesDir(), "live-index.pending");
            try {
                connection = (HttpURLConnection) new URL(HTML_UPDATE_URL).openConnection();
                connection.setConnectTimeout(4000);
                connection.setReadTimeout(4000);
                connection.setUseCaches(false);
                connection.setRequestProperty("Cache-Control", "no-cache");
                if (connection.getResponseCode() != 200) throw new IOException("Update unavailable");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                try (InputStream input = connection.getInputStream()) {
                    byte[] buffer = new byte[8192]; int count;
                    while ((count = input.read(buffer)) != -1) {
                        bytes.write(buffer, 0, count);
                        if (bytes.size() > 12 * 1024 * 1024) throw new IOException("Update too large");
                    }
                }
                String html = new String(bytes.toByteArray(), StandardCharsets.UTF_8);
                if (!html.contains("id=\"app-shell\"") || !html.contains("id=\"home-page\"") || !html.contains("</html>")) throw new IOException("Invalid HTML update");
                try (FileOutputStream output = new FileOutputStream(pending)) {bytes.writeTo(output); output.getFD().sync();}
                if (!pending.renameTo(cachedHtml)) throw new IOException("Cannot save update");
            } catch (Exception ignored) {
                pending.delete(); // Keep the previous complete update, or use bundled HTML.
            } finally {if (connection != null) connection.disconnect();}
            runOnUiThread(() -> {if (!isFinishing() && !isDestroyed()) web.loadUrl(ORIGIN + "/assets/index.html");});
        }, "HTML-update").start();
    }
    @SuppressWarnings("deprecation")
    private void applySystemBars() {
        int color = splashBars ? Color.rgb(3,51,55) : Color.rgb(237,243,247);
        getWindow().setStatusBarColor(color);
        getWindow().setNavigationBarColor(
            !splashBars && Build.VERSION.SDK_INT < 26 ? Color.BLACK : color);
        getWindow().getDecorView().setBackgroundColor(color);
        if (root != null) root.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= 29) {
            getWindow().setStatusBarContrastEnforced(false);
            getWindow().setNavigationBarContrastEnforced(false);
        }
        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(splashBars ? 0 : mask, mask);
            }
        } else {
            int flags = getWindow().getDecorView().getSystemUiVisibility();
            int mask = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) mask |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            getWindow().getDecorView().setSystemUiVisibility(
                splashBars ? flags & ~mask : flags | mask);
        }
    }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        applySystemBars();
        web = new WebView(this);
        root = new FrameLayout(this);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        if (Build.VERSION.SDK_INT >= 30) {
            // Paint our own bar backgrounds, including Android 15 edge-to-edge.
            getWindow().setDecorFitsSystemWindows(false);
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars = insets.getInsets(
                    WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                android.graphics.Insets keyboard = insets.getInsets(WindowInsets.Type.ime());
                view.setPadding(bars.left, bars.top, bars.right,
                    Math.max(bars.bottom, keyboard.bottom));
                return insets;
            });
            root.requestApplyInsets();
        }
        applySystemBars();
        web.setBackgroundColor(Color.rgb(3,51,55));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView v, String url) {
                if ((ORIGIN + "/assets/index.html").equals(url)) {
                    v.evaluateJavascript("if(window.syncAndroidSystemBars) window.syncAndroidSystemBars();", null);
                }
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView v, WebResourceRequest r) {
                if ((ORIGIN + "/assets/index.html").equals(r.getUrl().toString()) && cachedHtml != null && cachedHtml.isFile()) {
                    try {return new WebResourceResponse("text/html", "UTF-8", new FileInputStream(cachedHtml));}
                    catch (IOException ignored) {}
                }
                return loader.shouldInterceptRequest(r.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                Uri u = r.getUrl();
                if ("appassets.androidplatform.net".equals(u.getHost()) && "https".equals(u.getScheme())) return false;
                // External pages never replace the trusted local application.
                if (r.isForMainFrame() && r.hasGesture() && ("https".equals(u.getScheme()) || "mailto".equals(u.getScheme()) || "tel".equals(u.getScheme()))) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, u)); } catch (Exception ignored) {}
                }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(web, "NativeApp", Collections.singleton(ORIGIN),
                (view, message, sourceOrigin, mainFrame, reply) -> {
                    if (!mainFrame || !ORIGIN.equals(sourceOrigin.toString())) return;
                    try {
                        JSONObject data = new JSONObject(message.getData());
                        if ("systemBars".equals(data.optString("action"))) {
                            splashBars = data.optBoolean("splash", false);
                            runOnUiThread(this::applySystemBars);
                            return;
                        }
                        String text = data.optString("text");
                        if (text.length() > 20000) return;
                        if ("copy".equals(data.optString("action"))) {
                            ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Account", text));
                            Toast.makeText(this, "Copied!", Toast.LENGTH_SHORT).show();
                        } else if ("share".equals(data.optString("action"))) {
                            Intent send = new Intent(Intent.ACTION_SEND);
                            send.setType("text/plain"); send.putExtra(Intent.EXTRA_TEXT, text);
                            startActivity(Intent.createChooser(send, "Share account"));
                        }
                    } catch (Exception ignored) {}
                });
        }
        openUpdatedApp();
    }
    @SuppressWarnings("deprecation") @Override public void onBackPressed() {
        web.evaluateJavascript("(function(){return typeof androidHandleBack==='function' ? androidHandleBack() : false;})()", value -> {
            if ("true".equals(value)) { lastBack = 0; return; }
            long now = android.os.SystemClock.elapsedRealtime();
            if (lastBack != 0 && now-lastBack < 2000) finish();
            else { lastBack = now; Toast.makeText(this, "Press Back again to exit", Toast.LENGTH_SHORT).show(); }
        });
    }
    @Override protected void onPause() { web.onPause(); super.onPause(); }
    @Override protected void onResume() { super.onResume(); applySystemBars(); if (web != null) web.onResume(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
