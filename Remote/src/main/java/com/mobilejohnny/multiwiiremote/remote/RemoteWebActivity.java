package com.mobilejohnny.multiwiiremote.remote;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.*;
import android.widget.ProgressBar;

/**
 * Created by admin2 on 2015/3/18.
 */
public class RemoteWebActivity extends RemoteActivity {
    private WebView webView;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.web);
        progressBar =(ProgressBar) findViewById(R.id.progressBar);
        webView = (WebView)findViewById(R.id.webView);

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false;
            }
        });

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        webView.addJavascriptInterface(new RemoteJavascriptInterface(),"JsInterface");

        webView.loadUrl("file:///android_asset/index.html");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }
        },1000);

    }

    @Override
    protected void updateUI() {

    }

    private class RemoteJavascriptInterface {

        @JavascriptInterface
        public int getFPS()
        {
            return fps;
        }

        @JavascriptInterface
        public int addThrottle(int throttle)
        {
            throttle = rcThrottle + throttle;
            rcThrottle = constrain(throttle,1000,2000);
            return rcThrottle;
        }

        @JavascriptInterface
        public void setArm(boolean armed)
        {
            arm(armed);
        }

        @JavascriptInterface
        public void setLock(boolean lock)
        {
            if(lock)
            {
                lock();
            }
            else
            {
                unLock();
            }

        }

        @JavascriptInterface
        public String getRC()
        {
            return rcThrottle+","+rcRoll+","+rcPitch+","+rcAUX1+","+rcAUX2;
        }
    }
}