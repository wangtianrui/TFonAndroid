package com.example.rui.mnist.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.rui.mnist.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    @BindView(R.id.main_long_distanc_button)
    Button mainLongDistancButton;
    @BindView(R.id.main_mnist_button)
    Button mainMnistButton;

    private FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.main_long_distanc_button, R.id.main_mnist_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.main_long_distanc_button:
                Intent i = new Intent(MainActivity.this, RemoteActivity.class);
                startActivity(i);
                break;
            case R.id.main_mnist_button:
                i = new Intent(MainActivity.this, MnistActivity.class);
                startActivity(i);
                break;
        }
    }
}
