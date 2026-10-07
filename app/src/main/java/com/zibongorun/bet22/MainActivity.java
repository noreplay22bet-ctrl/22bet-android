package com.zibongorun.bet22;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.webkit.*;
import android.content.*;
import android.graphics.Color;
import android.widget.Toast;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import androidx.webkit.*;
import java.util.Collections;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private WebView web;
    private long lastBack;
    private static final String ORIGIN =
        "https://appassets.androidplatform.net";

    @SuppressWarnings("deprecation")
    private void setWhiteSystemBars() {
        Window window = getWindow();

        window.clearFlags(
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS |
            WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        );
        window.addFlags(
            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
        );

        window.setStatusBarColor(Color.WHITE);
        window.setNavigationBarColor(
            Build.VERSION.SDK_INT >= 26 ? Color.WHITE : Color.BLACK
        );
        window.getDecorView().setBackgroundColor(Color.WHITE);

        if (Build.VERSION.SDK_INT >= 29) {
            window.setStatusBarContrastEnforced(false);
            window.setNavigationBarContrastEnforced(false);
        }

        if (Build.VERSION.SDK_INT >= 30) {
            WindowInsetsController controller =
                window.getInsetsController();
            if (controller != null) {
                int appearance =
                    WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS |
                    WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(
                    appearance, appearance
                );
            }
        } else {
            int flags = window.getDecorView().getSystemUiVisibility();
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setWhiteSystemBars();

        web = new WebView(this);
        setContentView(web);
        setWhiteSystemBars();

        web.setBackgroundColor(Color.WHITE);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setDomStorageEnabled(true);
        web.getSettings().setAllowFileAccess(false);
        web.getSettings().setAllowContentAccess(false);
        web.getSettings().setMixedContentMode(
            WebSettings.MIXED_CONTENT_NEVER_ALLOW
        );

        WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
            .addPathHandler(
                "/assets/",
                new WebViewAssetLoader.AssetsPathHandler(this)
            )
            .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(
                WebView v, WebResourceRequest r
            ) {
                return loader.shouldInterceptRequest(r.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                WebView v, WebResourceRequest r
            ) {
                Uri u = r.getUrl();

                if ("appassets.androidplatform.net".equals(u.getHost())
                    && "https".equals(u.getScheme())) {
                    return false;
                }

                if (r.isForMainFrame() && r.hasGesture()
                    && ("https".equals(u.getScheme())
                        || "mailto".equals(u.getScheme())
                        || "tel".equals(u.getScheme()))) {
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, u));
                    } catch (Exception ignored) {}
                }

                return true;
            }
        });

        web.setWebChromeClient(new WebChromeClient());

        if (WebViewFeature.isFeatureSupported(
            WebViewFeature.WEB_MESSAGE_LISTENER
        )) {
            WebViewCompat.addWebMessageListener(
                web, "NativeApp", Collections.singleton(ORIGIN),
                (view, message, sourceOrigin, mainFrame, reply) -> {
                    if (!mainFrame
                        || !ORIGIN.equals(sourceOrigin.toString())) {
                        return;
                    }

                    try {
                        JSONObject data =
                            new JSONObject(message.getData());
                        String text = data.optString("text");
                        if (text.length() > 20000) return;

                        if ("copy".equals(data.optString("action"))) {
                            android.content.ClipboardManager clipboard =
                                (android.content.ClipboardManager)
                                    getSystemService(CLIPBOARD_SERVICE);
                            clipboard.setPrimaryClip(
                                ClipData.newPlainText("Account", text)
                            );
                            Toast.makeText(
                                this, "Copied!", Toast.LENGTH_SHORT
                            ).show();
                        } else if (
                            "share".equals(data.optString("action"))
                        ) {
                            Intent send = new Intent(Intent.ACTION_SEND);
                            send.setType("text/plain");
                            send.putExtra(Intent.EXTRA_TEXT, text);
                            startActivity(
                                Intent.createChooser(send, "Share account")
                            );
                        }
                    } catch (Exception ignored) {}
                }
            );
        }

        web.loadUrl(ORIGIN + "/assets/index.html");
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        web.evaluateJavascript(
            "(function(){return typeof androidHandleBack==='function'"
                + " ? androidHandleBack() : false;})()",
            value -> {
                if ("true".equals(value)) {
                    lastBack = 0;
                    return;
                }

                long now = android.os.SystemClock.elapsedRealtime();
                if (lastBack != 0 && now - lastBack < 2000) {
                    finish();
                } else {
                    lastBack = now;
                    Toast.makeText(
                        this,
                        "Press Back again to exit",
                        Toast.LENGTH_SHORT
                    ).show();
                }
            }
        );
    }

    @Override
    protected void onPause() {
        if (web != null) web.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setWhiteSystemBars();
        if (web != null) web.onResume();
    }

    @Override
    protected void onDestroy() {
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
