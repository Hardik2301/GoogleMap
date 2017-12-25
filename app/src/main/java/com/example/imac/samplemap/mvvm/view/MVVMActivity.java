package com.example.imac.samplemap.mvvm.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.example.imac.samplemap.R;

/**
 * Created by imac on 11/17/17.
 */

public class MVVMActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mvvm_activity);

        if (savedInstanceState == null) {
            FragmentMap fragment = new FragmentMap();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, fragment, FragmentMap.class.getName()).commit();
        }

    }
}
