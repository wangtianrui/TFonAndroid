package com.example.rui.mnist.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rui.mnist.R;
import com.example.rui.mnist.tensorflow.MyTSF;
import com.example.rui.mnist.ui.PrinterView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MnistActivity extends AppCompatActivity {

    @BindView(R.id.printer_view)
    PrinterView printerView;
    @BindView(R.id.result_text_view)
    TextView resultTextView;
    @BindView(R.id.clean_button)
    Button cleanButton;
    @BindView(R.id.detect_button)
    Button detectButton;
    @BindView(R.id.bottom_layout)
    LinearLayout bottomLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mnist_activity);
        ButterKnife.bind(this);
    }


    @OnClick({R.id.printer_view, R.id.result_text_view, R.id.clean_button, R.id.detect_button})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.clean_button:
                printerView.clean();
                resultTextView.setText(null);
                break;
            case R.id.detect_button:
                if (printerView.isEmpty()) {
                    resultTextView.setText("画板为空");
                    break;
                }
                MyTSF tf = new MyTSF(getAssets());
                float[] result = tf.getResult(printerView.getData(28, 28));
                String text = "";
                for (int i = 0; i < result.length; i++) {
                    text = text + i + ":" + result[i] + "; ";
                }
                resultTextView.setText(text);
                break;
        }
    }
}
