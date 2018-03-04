package com.example.rui.mnist.tensorflow;

import android.graphics.Bitmap;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Rui on 2018/3/4.
 */

public class Stylizer {
    private int[] intValues;
    private float[] floatValues;
    private TensorFlowInferenceInterface inferenceInterface;

    private static final String MODEL_FILE = "file:///android_asset/stylize_quantized.pb";
    private static final String INPUT_NAME = "input";
    private static final String STYLE_NAME = "style_num";
    private static final String OUTPUT_NAME = "transformer/expand/conv3/conv/Sigmoid";
    private static final int NUM_STYLES = 26;


    private void stylizeImage(final Bitmap bitmap) {
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int i = 0; i < intValues.length; ++i) {
            final int val = intValues[i];
            floatValues[i * 3] = ((val >> 16) & 0xFF) / 255.0f;
            floatValues[i * 3 + 1] = ((val >> 8) & 0xFF) / 255.0f;
            floatValues[i * 3 + 2] = (val & 0xFF) / 255.0f;
        }
        // Copy the input data into TensorFlow.
        inferenceInterface.feed(
                INPUT_NAME, floatValues, 1, bitmap.getWidth(), bitmap.getHeight(), 3);
        //inferenceInterface.feed(STYLE_NAME, styleVals, NUM_STYLES);

        //inferenceInterface.run(new String[]{OUTPUT_NAME}, isDebug());
        inferenceInterface.fetch(OUTPUT_NAME, floatValues);

        for (int i = 0; i < intValues.length; ++i) {
            intValues[i] =
                    0xFF000000
                            | (((int) (floatValues[i * 3] * 255)) << 16)
                            | (((int) (floatValues[i * 3 + 1] * 255)) << 8)
                            | ((int) (floatValues[i * 3 + 2] * 255));
        }

        bitmap.setPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
    }
}
