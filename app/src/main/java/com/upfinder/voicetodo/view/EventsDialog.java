package com.upfinder.voicetodo.view;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.upfinder.voicetodo.R;
import com.upfinder.voicetodo.task.AddTaskActivity;
import com.upfinder.voicetodo.utils.UtilsKt;

import org.json.JSONArray;
import org.json.JSONObject;


public class EventsDialog extends Dialog {

    private boolean iscancelable;//控制点击dialog外部是否dismiss
    private boolean isBackCancelable;//控制返回键是否dismiss
    private View view;
    private Context context;

    private TextView mEventTitle;
    private Button mPreBtn;
    private Button mNextBtn;
    private RadioGroup mRgroup;
    private RadioButton mRbSuc;
    private RadioButton mRbFail;
    private RadioButton mRbIgnore;
    private JSONArray mEventsArrary;
    private JSONObject mCurrentEvent;
    private int mIndex = 0;
    private boolean isFinish = false;

    public EventsDialog(Context context, int layoutid, boolean isCancelable, boolean isBackCancelable) {
        super(context, R.style.MyDialog);
        Looper.getMainLooper();
        this.context = context;
        this.view = LayoutInflater.from(context).inflate(layoutid, null);
        this.iscancelable = isCancelable;
        this.isBackCancelable = isBackCancelable;
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(view);//这行一定要写在前面
        setCancelable(iscancelable);//点击外部不可dismiss
        setCanceledOnTouchOutside(isBackCancelable);


    }

    public void setEvents(String events){
        try {

            mEventsArrary = new JSONArray(events);
            mIndex = 0;
            refreshUI();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void initView() {

        mEventTitle = findViewById(R.id.eventTitle);
        mRgroup = findViewById(R.id.rgEventState);
        mPreBtn = findViewById(R.id.btPreEvent);
        mNextBtn = findViewById(R.id.btNextEvent);
        mRbSuc = findViewById(R.id.rbSuccess);
        mRbFail = findViewById(R.id.rbFail);
        mRbIgnore = findViewById(R.id.rbIgnore);

        mRgroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                try{
                    switch (i){
                        case R.id.rbSuccess:
                            mCurrentEvent.put("state",AddTaskActivity.EVENTS_STATE_SUCCESS);
                            break;
                        case R.id.rbFail:
                            mCurrentEvent.put("state",AddTaskActivity.EVENTS_STATE_FAIL);
                            break;
                        case R.id.rbIgnore:
                            mCurrentEvent.put("state",AddTaskActivity.EVENTS_STATE_IGNORE);
                            break;
                    }
                    mEventsArrary.put(mIndex,mCurrentEvent);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mPreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFinish)
                {
                    saveEvent();
                }else{
                    mIndex --;
                    refreshUI();
                }
            }
        });

        mNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isFinish)
                {
                    clearEvent();
                }else{
                    mIndex ++;
                    refreshUI();
                }
            }
        });
    }

    //刷新界面
    public void refreshUI(){
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendEmptyMessage(HANDLER_MSG_REFRESHUI);
    }

    //保存事件
    public void saveEvent(){

    };

    //清除事件
    public void clearEvent(){

    }

    public static final int HANDLER_MSG_REFRESHUI = 1;

    private Handler mHandler = new Handler(getContext().getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case  HANDLER_MSG_REFRESHUI:
                    try {
                        mCurrentEvent = mEventsArrary.getJSONObject(mIndex);
                        String event = mCurrentEvent.getString("event");
                        int state = mCurrentEvent.getInt("state");
                        mEventTitle.setText(event);
                        switch (state){
                            case AddTaskActivity.EVENTS_STATE_DEFAULT:
                            case AddTaskActivity.EVENTS_STATE_SUCCESS:
                                mRbSuc.setChecked(true);
                                mRbIgnore.setChecked(false);
                                mRbFail.setChecked(false);
                                break;
                            case AddTaskActivity.EVENTS_STATE_FAIL:
                                mRbSuc.setChecked(false);
                                mRbIgnore.setChecked(false);
                                mRbFail.setChecked(true);
                                break;
                            case AddTaskActivity.EVENTS_STATE_IGNORE:
                                mRbSuc.setChecked(false);
                                mRbIgnore.setChecked(true);
                                mRbFail.setChecked(false);
                                break;
                        }
                        //长度相等时，显示“保存”和“清除”
                        if ((mIndex+1) == mEventsArrary.length())
                        {
                            isFinish = true;
                            mPreBtn.setText("保存");
                            mNextBtn.setText("清除");
                        }else if(mIndex == 0)
                        {
                            isFinish = false;
                            mPreBtn.setEnabled(false);
                            mPreBtn.setText("上一项");
                            mNextBtn.setText("下一项");
                        }else {
                            isFinish = false;
                            mPreBtn.setEnabled(true);
                            mNextBtn.setEnabled(true);
                            mPreBtn.setText("上一项");
                            mNextBtn.setText("下一项");
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
}