package com.devon_dickson.apps.oncampus;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by ddickson1 on 4/15/2016.
 */
@SuppressLint("ParcelCreator")
public class ApiServiceResultReceiver extends ResultReceiver{

    private Receiver mReceiver;

    public ApiServiceResultReceiver(Handler handler) {
        super(handler);
    }

    public void setReceiver(Receiver receiver) {
        mReceiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (mReceiver != null) {
            mReceiver.onReceiveResult(resultCode, resultData);
        }
    }
}
