package com.wook.web.lighten.nanumAED.firebase;


import com.wook.web.lighten.nanumAED.utils.Print;

public class FirebaseInstanceIDService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIIDService";

    public String getToken() {
        return token;
    }

    private String token = "";

    // [START refresh_token] push 알림을 특정 타겟에게 보낼 때 사용되는 고유 키 값
    /*@Override
    public void onTokenRefresh() {

        // Get updated InstanceID token.
        token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "token : "+ token);
    }*/

    @Override
    public void onNewToken(String token){
        Print.d(TAG, "token : "+token);
    }
}
