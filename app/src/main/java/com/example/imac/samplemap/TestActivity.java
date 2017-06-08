package com.example.imac.samplemap;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.imac.samplemap.view.MarkerView;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by imac on 4/14/17.
 */

public class TestActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test);

        Button btn=(Button)findViewById(R.id.button);

        RelativeLayout lnv= (RelativeLayout) findViewById(R.id.lnv_main);
        MarkerView mMarker = new MarkerView(this);
        ViewGroup.LayoutParams param= new ViewGroup.LayoutParams(400,400);
        mMarker.setLayoutParams(param);
        //mMarker.setText("120");
        lnv.addView(mMarker);
    }
}
