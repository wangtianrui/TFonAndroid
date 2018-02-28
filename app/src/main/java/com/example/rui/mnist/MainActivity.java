package com.example.rui.mnist;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.rui.mnist.tensorflow.MyTSF;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "click01: ");
        MyTSF mytsf=new MyTSF(getAssets());
        float[] result=mytsf.getAddResult();
        for (int i=0;i<result.length;i++){
            Log.i(TAG, "click01: "+result[i] );
        }
    }
}
