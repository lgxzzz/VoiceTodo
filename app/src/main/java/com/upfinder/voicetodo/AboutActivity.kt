package com.upfinder.voicetodo


import android.os.Bundle

import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_about.*


class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        // 启用javascript
        webview.getSettings().setJavaScriptEnabled(true);
        // 从assets目录下面的加载html
        webview.loadUrl("file:///android_asset/about.html");
        webview.addJavascriptInterface(this@AboutActivity,"android");
    }


}