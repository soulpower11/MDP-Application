package com.example.mdpapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MessageFragment extends Fragment {
    private static final String TAG = "MessageFragment";

    public static final String EVENT_MESSAGE_RECEIVED = "com.event.EVENT_MESSAGE_RECEIVED";
    public static final String EVENT_MESSAGE_SENT = "com.event.EVENT_MESSAGE_SENT";

    private static final String STATE_LOG = "log";

    Button btn_send, btn_clear;
    TextView tView;
    TextInputLayout tInput;

    BluetoothConnectionHelper bluetooth;
    String sendMsg;
    SpannableStringBuilder msgLog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        btn_send = view.findViewById(R.id.btn_send);
        btn_clear = view.findViewById(R.id.btn_clear);
        tView = view.findViewById(R.id.textView);
        tInput = view.findViewById(R.id.textInput);

        Context context = getActivity().getApplicationContext();
        bluetooth = MDPApplication.getBluetooth();
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_MESSAGE_RECEIVED));
        context.registerReceiver(mMessageReceiver, new IntentFilter(EVENT_MESSAGE_SENT));

        tView.setText("Application Started");
        msgLog = new SpannableStringBuilder();

        if(savedInstanceState != null){
            msgLog = new SpannableStringBuilder(Html.fromHtml(savedInstanceState.getString(STATE_LOG),0));
            if(msgLog.length() != 0){
                tView.setText(msgLog);
            }
        }

        tView.setMovementMethod(new ScrollingMovementMethod());

        tInput.getEditText().addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                //get the String from CharSequence with s.toString() and process it to validation
                sendMsg = s.toString();
            }
        });

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(sendMsg != null){
                    bluetooth.write(sendMsg);
                    tInput.getEditText().setText("");
                }
                else{
                    showToast("Message box is empty!");
                }
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tView.setText("");
                msgLog.clear();
                showToast("Message log cleared!");
            }
        });
    }

    private final BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();

            if(intent.getAction().equals(EVENT_MESSAGE_RECEIVED)){
                String message = intent.getStringExtra("key");
                logMsg("Message Received: " + message);
            }
            else if(intent.getAction().equals(EVENT_MESSAGE_SENT)){
                String message = intent.getStringExtra("key");
                logMsg("Message Sent: " + message);
            }
        }
    };

    public void logMsg(String message){
        try {
            DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            Date date = new Date();
            SpannableString styledResultText;

            //msgLog += "[" + dateFormat.format(date)+ "] " + message + "\n";
            message = "[" + dateFormat.format(date)+ "] " + message + "\n";
            styledResultText = new SpannableString(message);

            if(message.contains("Message Received:")){

//                styledResultText.setSpan(
//                        new AlignmentSpan.Standard(Layout.Alignment.ALIGN_NORMAL),
//                        0,
//                        message.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
                String length = "[" + dateFormat.format(date)+ "] Message Received:";

                styledResultText.setSpan(
                        new ForegroundColorSpan(Color.GRAY),
                        0,
                        length.lastIndexOf(":") + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

//                styledResultText.setSpan(
//                        new RelativeSizeSpan(2f),
//                        message.lastIndexOf(":") + 2,
//                        message.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );

            }else{
//                styledResultText.setSpan(
//                        new AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE),
//                        0,
//                        message.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );

                String length = "[" + dateFormat.format(date)+ "] Message Sent:";

                styledResultText.setSpan(
                        new ForegroundColorSpan(Color.DKGRAY),
                        0,
                        length.lastIndexOf(":") + 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                );

//                styledResultText.setSpan(
//                        new RelativeSizeSpan(2f),
//                        message.lastIndexOf(":") + 2,
//                        message.length(),
//                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//                );
            }

            msgLog.append(styledResultText);

            tView.setText(msgLog,  TextView.BufferType.SPANNABLE);

        }catch (Exception e){
        }
    }

    //toast message function
    private void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        String log = Html.toHtml(msgLog, 0);
        Log.d(TAG, "onSaveInstanceState: before: \n" + log);
        if (log.length() != 0) {
            log = log.replace("<p dir=\"ltr\">", "");
            log = log.replace("</p>", "");
        }
        Log.d(TAG, "onSaveInstanceState: after: \n" + log);
        savedInstanceState.putString(STATE_LOG, log);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: ");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop: ");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: ");
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach: ");
        super.onDetach();
    }
}