package com.zibongorun.bet22;

import android.app.Activity;
import android.os.Bundle;
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
    private long lastBack;
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(12,62,68));
        getWindow().setNavigationBarColor(Color.rgb(12,62,68));
        web = new WebView(this);
        setContentView(web);
        web.setBackgroundColor(Color.rgb(12,62,68));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
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
    @Override protected void onResume() { super.onResume(); if (web != null) web.onResume(); }
    @Override protected void onDestroy() { web.destroy(); super.onDestroy(); }
}
