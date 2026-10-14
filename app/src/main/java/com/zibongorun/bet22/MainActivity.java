package com.zibongorun.bet22;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.graphics.BitmapFactory;
import android.util.Base64;
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
    private ImageView launchCover;
    private static final String LAUNCH_IMAGE = "UklGRlYjAABXRUJQVlA4IEojAADQvwGdASpJA04HPlEok0cjoqQhIFJ4EIAKCWlu4W2eQ6NfpQi/+b5cl3G/g3jyAP/5/s/WAPH1hLl/bvxH+UnfGHd7TP2P3VfMH/Z+qv7yvcA/TT/L/lR2uP3I9QH80/sv/K/1XvPekv/EeoB/Zf9R1qHoMftp6bP7X/DF+3H7Yezj/1db/7DOlXwKIAO9QmgDQOkV649gv9aQGv6diCo57Yh8VLLy/QINOEGm2xD4qWXl+K/UtxD6L2XrCBee2IfEFr7UIeIiTicC89sQ+IKjntiHxBUc9sQ+Z5naKDzuQKwmLmM23pZFABDxUu1OMWCHiE4bLBEVgYxD5lst8VVxm4BMl6EcDjMn0ZeX57acxDGgCpHNGjLzB/OBhTLfFVm4c82xD4gqQPYh8WQI4HGZPpJ/nAwHtOkMS8u7WEPEFSJvz2xmtkYh4qWXxPLeFJest8VUmywQ8Ticjn0dqYD2IfEFR3ICo6jiHzKwOZQeIL1Zfn9SKjnu2PEJw7TmNMYX0Db9dHrdOYhiOB8Neo4h8Qno0Di0NShkvCkD2IfEFRz3bHmVfrpoibLBHxwL1G1qtVMXXaEcC892yKtGXmcNee2IfEFr63cLTq7MsnHJFGXrCBegQab4IeIKkD2M1oPEFX/o4qQQAYCcLI6SnxBVYhccgj44TT4dOYgq/jZpl63EPiCo57acxDEcDCmECajIyY8SRG1LlmNS92rW4X9Zg2kRFCM4eYqVSVgxmRaBkYsEPFiq92Fp9oqLSAb2ThBA8VLLgiUvSBgvPbEO01ornJqWy60wl4h8NTARDfXn3hO+N9mMLkkiIiERKQCsuDCHettWmREQiXuG9zn1Wkf4AKIiIhERERCIl8NL6QM4iVMEQl2kqW0Def1cu4LR1hEREkkREkLQORWoRE4QQliVcIiUkr2I1JsmwiIiKCd7EblE/BLGERE/mIhEsB7GYITPXZl5UXrX7oT2RCIlKyIiIjIWVgTfMiTA9kQ7Yh8daTBCWJVxeyeH8NRJtKx3IJxdZN7YrbQHQpAbIJKlrzxELH740IiIiSrCIk3boJMsmthhRUhIKesiUvPExJ8I5txEImNfoiJIdrSInQ6x0yKkRERI4iirpP9SSOpESrlLGfmMRESTPvSWfXnOiIiE6osSXsxGfs6iJE4lENzJrNjxElxN0rE9extC2iERbvRvFetwucn+gohESuZV0REIiImFTXTW9FE9S1bnmEVVwLodhkuJgiJdCIREREQiIkEw2c0KFaxewYVi4hLtWtwvJxVoRE/mIlqERESUSPBUmiEREo1L4Y1tq1uFysBYhQYS+niIiE1v6DmCshiQTcSX9hcpxDtiHu0ShbdtpWOhKMXip/oigcGTHkla0iHYirCIiaGtGMeJIJCSQ28hLy1ZLGES8Q7TWWNB0REQiIlKlnyESORJLJ3ghsiERERIor5joiaylK17EImNjabMEIiIiVYKvND9XMdERCWRGIwmTHiIx4osSlm1uF/TFUImQicTtNpVwl2rW4g6idERCJ68EInmeETzjFEImyqAFgQcExEkquMiIlKyIiIhRwhFMCxCIiIhMnr0ZcL2ROIiEREREImEDYFZhriIiEREREIiUwE6DAeGpdERELe2IiIlF5FEQiIiIx4iFrQpOdIp9+TaVcJYwo4iqkRER7lz357kqyGITZ1URCIlK3PQvWREREIiJWMREREevRl5flIS7SIkciIjKVNpERI5ERCLEszxeyUyIbTQ9314bbREQiIiIhESvKqewJtESXRKmCITXhee2IdplohEpWvYhETHxl8vEbHxLTVyxEQia8Lmhy0hcRCIiIiYdOIiJaEcgh3aIlbMREQiWMIiifnLwtQrERES6EQiXj7MUZMg3gdEREImM2IRPEtqJtEmWqHafLQGioyxERjxEaj+IiNWHIiEUI8YZ6KzgH6Kk/EcJYeOQUuGtl0REQiIiIhNEiJSURweiJe8gbXSCVICZHtQf9b0RN8REjkRMKBESjYZb4ihwiJmpaoiFIhEvdpEf6mCEREQiJSsiIiIRU9TEmh7tEREIRPNwjnCdVVFswiIky0QiIiIRLGERPESrjQOtG0w9EklBzzCXaq4REm0REmOgVuuERETCgTYj1LmRGPERqP5tmnioiEREqM0cSB/SlZFE9BsQnbYWtCIiI6CqIkgkIiIiO5AArHMJzycUeagpSGx8ICntwY7wX7gF5ZjxlWBzThFk5G/oz0lSZpoOjVu0wy4iIkkhTzIeIVohERLnRKeXnavSDBXXjLNshYlMcqjKCjAVjWJILYCXGEDcaOPhQhp2JEM5kQlUOHfFD2I5gFU4SN3rXwHh0l/1j0EjTAT1ku1bkUoSyIb6870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";
    private void hideLaunchCover() {
        if (launchCover != null) { root.removeView(launchCover); launchCover = null; }
    }
    private long lastBack;
    private boolean splashBars = true;
    private static final String ORIGIN = "https://appassets.androidplatform.net";
    private static final String HTML_UPDATE_URL = "https://raw.githubusercontent.com/noreplay22bet-ctrl/22bet-android/main/app/src/main/assets/index.html";
    private File cachedHtml;
    private void openUpdatedApp() {
        cachedHtml = new File(getFilesDir(), "live-index.html");
        // Display the saved or bundled app immediately. Updates apply on next launch.
        web.loadUrl(ORIGIN + "/assets/index.html");
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
            // Do not reload the page or reset the active login/navigation after an update.
        }, "HTML-update").start();
    }
    @SuppressWarnings("deprecation")
    private void applySystemBars() {
        int color = splashBars ? Color.rgb(3,49,56) : Color.rgb(237,243,247);
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
        launchCover = new ImageView(this);
        launchCover.setBackgroundColor(Color.rgb(3,49,56));
        launchCover.setScaleType(ImageView.ScaleType.FIT_XY);
        byte[] launchImage = Base64.decode(LAUNCH_IMAGE, Base64.DEFAULT);
        launchCover.setImageBitmap(BitmapFactory.decodeByteArray(launchImage, 0, launchImage.length));
        root.addView(launchCover, new FrameLayout.LayoutParams(-1, -1));
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
        web.setBackgroundColor(Color.rgb(3,49,56));
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this)).build();
        web.setWebViewClient(new WebViewClient() {
            @Override public void onPageCommitVisible(WebView v, String url) {
                if ((ORIGIN + "/assets/index.html").equals(url)) hideLaunchCover();
            }
            @Override public void onPageFinished(WebView v, String url) {
                if ((ORIGIN + "/assets/index.html").equals(url)) {
                    hideLaunchCover();
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
                        if ("clearCache".equals(data.optString("action"))) {
                            runOnUiThread(() -> {
                                try {
                                    web.clearCache(true);
                                    web.evaluateJavascript("window.onAppCacheCleared && window.onAppCacheCleared(true)", null);
                                } catch (Exception error) {
                                    web.evaluateJavascript("window.onAppCacheCleared && window.onAppCacheCleared(false)", null);
                                }
                            });
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
