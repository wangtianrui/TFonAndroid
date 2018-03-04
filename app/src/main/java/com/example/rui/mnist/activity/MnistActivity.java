package com.example.rui.mnist.activity;

import android.content.ClipData;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rui.mnist.R;
import com.example.rui.mnist.bean.MnistItem;
import com.example.rui.mnist.tensorflow.MnistClassifier;

import com.example.rui.mnist.ui.PrinterView;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Locale;

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
        Log.i("Test :", "click01: ");

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
                MnistClassifier mnistClassifier = new MnistClassifier(getAssets());
                float[] result = mnistClassifier.getResult(printerView.getData(28, 28));
                List<MnistItem> items = new ArrayList<>(10);
                for (int i = 0; i < result.length; i++) {
                    items.add(new MnistItem(result[i], i));
                }
                Collections.sort(items);
                StringBuilder builder = new StringBuilder();
                for (int i = 0; i < 3; i++) {
                    MnistItem item = items.get(i);
                    builder.append(item.getIndex())
                            .append(": ")
                            .append(String.format(Locale.getDefault(), "%.1f%%", item.getValue() * 100))
                            .append("\n");
                }
                resultTextView.setText(builder.toString());
                break;
        }
    }

}
