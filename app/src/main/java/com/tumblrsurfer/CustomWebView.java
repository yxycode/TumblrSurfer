package com.tumblrsurfer;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.CookieSyncManager;

//--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------

public class CustomWebView extends WebView
{
    public String Url;
    public String[] Data;
    
    public CustomWebView(Context context, AttributeSet attrs) {
        super(context, attrs);        
        // TODO Auto-generated constructor stub
    }
    
    public void onPageFinished(WebView view, String url) {
        CookieSyncManager.getInstance().sync();
    }    
}

//--------------------------------------------------------------------------------------------------
//##################################################################################################
//--------------------------------------------------------------------------------------------------