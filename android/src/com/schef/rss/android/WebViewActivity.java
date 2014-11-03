package com.schef.rss.android;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

/**
 * Created by scheffela on 9/6/14.
 */
public class WebViewActivity extends Activity {

    public static String URL = "url_param";

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_webview);
        webView = (WebView) findViewById(R.id.webView);

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        } else {

            final ImageView iv = (ImageView) findViewById(R.id.webHoneImageView);

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
//        final Button closeButton = (Button) findViewById(R.id.button_close);

            String url = getIntent().getStringExtra(URL);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            webView.setWebChromeClient(new WebChromeClient() {
            });

//        webView.getSettings().setBuiltInZoomControls(true);


//            webView.setWebViewClient(new WebViewClient() {
//
//                @Override
//                public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                }
//
//                @Override
//                public void onPageFinished(WebView view, String url) {
//                }
//            });

//            webView.setDownloadListener(new DownloadListener() {
//                @Override
//                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
//                }
//            });

            webView.loadUrl(url);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String url = getIntent().getStringExtra(URL);
        webView.loadUrl(url);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        webView.restoreState(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}
