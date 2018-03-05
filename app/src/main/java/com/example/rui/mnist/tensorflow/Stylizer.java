package com.example.rui.mnist.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Rui on 2018/3/4.
 */

public class Stylizer {

    private String TAG = "Stylizer";
    private int[] intValues = new int[256*256];
    private float[] floatValues;
    private TensorFlowInferenceInterface inferenceInterface;

    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";
    private static final String INPUT_NAME = "input";
    private static final String STYLE_NAME = "style_num";
    private static final String OUTPUT_NAME = "transformer/expand/conv3/conv/Sigmoid";
    private static final int NUM_STYLES = 26;
    private float[] styleVals = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f
            , 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f};

    public Stylizer(AssetManager assetManager) {
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public Bitmap stylizeImage(String path, int styleValue) {
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        styleVals[styleValue] = 1.0f;
        Log.d(TAG, "stylizeImage: "+bitmap.getWidth());
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }
        Log.d(TAG, "stylizeImage: f"+floatValues[1]);

        inferenceInterface.feed(
                INPUT_NAME, floatValues, 1, bitmap.getWidth(), bitmap.getHeight(), 3);
        inferenceInterface.feed(STYLE_NAME, styleVals, NUM_STYLES);

        inferenceInterface.run(new String[]{OUTPUT_NAME}, false);
        inferenceInterface.fetch(OUTPUT_NAME, floatValues);
        Log.d(TAG, "stylizeImage: "+STYLE_NAME);
        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }
        Log.d(TAG, "stylizeImage:i "+intValues[1]);
        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return bitmap;
    }
}
