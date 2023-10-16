package com.wook.web.lighten.nanumAED.activity;

import java.io.Serializable;

public class UserItem implements Serializable {

    public int getDepth() {
        return depth;
    }

    public int getAngle() {
        return angle;
    }

    public int getPosition() {
        return position;
    }

    public int getBreath() {
        return breath;
    }

    public int getDepth_correct() {
        return depth_correct;
    }

    public int getTime() {
        return time;
    }

    public float getHand_off_start(){return hand_off_start; }

    public int getHand_off_end(){return hand_off_end; }

    private int depth;
    private int time;
    private int depth_correct;
    private int angle;
    private int position;
    private int breath;
    private float hand_off_start;
    private int hand_off_end;


    public UserItem(int time, int depth, int depth_correct, int angle, int position){
        this.time = time;
        this.depth = depth;
        this.depth_correct = depth_correct;
        this.angle = angle;
        this.position = position;

    }

    public UserItem(int time, int breath){
        this.time = time;
        this.breath = breath;
    }

    public UserItem(int time, float hand_off_start , int hand_off_end){
        this.time = time;
        this.hand_off_start = hand_off_start;
        this.hand_off_end = hand_off_end;
    }


}

