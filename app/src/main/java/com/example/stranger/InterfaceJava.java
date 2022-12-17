package com.example.stranger;

import android.webkit.JavascriptInterface;

import com.example.stranger.Activities.VideoCallActivity;

public class InterfaceJava {

    VideoCallActivity callActivity;

    public InterfaceJava(VideoCallActivity callActivity) {
        this.callActivity = callActivity;
    }

    @JavascriptInterface
    public void onPeerConnected(){
        callActivity.onPeerConnected();
    }

}
