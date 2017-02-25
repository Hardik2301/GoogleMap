package com.example.imac.samplemap.data;

/**
 * Created by imac on 2/23/17.
 */

public interface OnAsyncTaskListener {

    void onTaskBegin();

    void onTaskComplete(boolean success, String response);
}
