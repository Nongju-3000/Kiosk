package com.wook.web.lighten.nanumAED.utils;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.jitsi.meet.sdk.BroadcastIntentHelper;

public class FinishService extends Service {

    public final static String ACTION_INITIALIZE = "ACTION_INITIALIZE";
    private String room;
    private String UserName;
    private String token;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference databaseReference = firebaseDatabase.getReference();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setReady(intent);
        return START_NOT_STICKY;
    }

    private void setReady(Intent intent){
        String action = intent.getAction();
        if(action.equals(ACTION_INITIALIZE)){
            sharedPreferences = getApplication().getSharedPreferences("DeviceCPR", MODE_PRIVATE);
            room = intent.getStringExtra("room");
            UserName = intent.getStringExtra("UserName");
            token = intent.getStringExtra("token");
        }
    }
    public class LocalBinder extends Binder {
        public FinishService getService() {
            return FinishService.this;
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
    boolean roomCheck = false;
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent hangupBroadcastIntent = BroadcastIntentHelper.buildHangUpIntent();
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(hangupBroadcastIntent);

        stopSelf();

        /*
        databaseReference.child("Room").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot datas: snapshot.getChildren()){
                    Print.e("Test", "datas");
                    if(datas.getKey().equals(room))
                        roomCheck = true;
                }if(roomCheck){
                    databaseReference.child("Room").child(room).child("user").orderByChild("name").equalTo(UserName).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            int count = 0;
                            int count2 = 0;
                            for(DataSnapshot datas: snapshot.getChildren()){
                                if(UserName.equals(datas.child("name").getValue().toString())) {
                                    count++;
                                }
                                Print.e("Test", "token = "+token);
                                Print.e("Test", "datas = "+datas.child("token").getValue().toString());
                                if(token.equals(datas.child("token").getValue().toString())) {
                                    count2++;
                                }
                                Print.e("Test", "count2 = "+count2);
                            }
                            if(count != 0){
                                if(count2 != 0){
                                    long now = System.currentTimeMillis();
                                    Date date = new Date(now);
                                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                                    String getTime = sdf.format(date);

                                    ChatData chatData = new ChatData("아웃/" + UserName, getTime, UserName);
                                    databaseReference.child("Room").child(room).child("message").push().setValue(chatData);
                                    databaseReference.child("Room").child(room).child("user").child(token).setValue(null);

                                    stopSelf();
                                }
                            }
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Print.e("Test", "error = "+error.getMessage().toString());
                        }
                    });
                }
                else
                    Toast.makeText(getApplication(), "방이 없습니다.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Print.e("Test", "error "+error.getMessage().toString());
            }
        });*/
    }
    public void setOut(){

       /* databaseReference.child("Room").child(room).child("user").orderByChild("name").equalTo(UserName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = 0;
                for(DataSnapshot datas: snapshot.getChildren()){
                    Print.e("Test", "naem = "+UserName);
                    Print.e("Test", "name = "+datas.child("name").getValue().toString());
                    if(UserName.equals(datas.child("name").getValue().toString())) {
                        count++;
                        Print.e("Test", "count++!");
                        break;
                    }
                }
                if(count > 0) {
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssSSS");
                    String getTime = sdf.format(date);

                    ChatData chatData = new ChatData("아웃/" + UserName, getTime, UserName);
                    databaseReference.child("Room").child(room).child("message").push().setValue(chatData);
                    databaseReference.child("Room").child(room).child("user").child(token).setValue(null);

                    stopSelf();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/
    }
}
