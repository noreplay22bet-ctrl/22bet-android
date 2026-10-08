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
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private FrameLayout root;
    private long lastBack;
    private boolean splashBars = true;
    private static final String ORIGIN = "https://appassets.androidplatform.net";
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
        web.loadUrl(ORIGIN + "/assets/index.html");
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
