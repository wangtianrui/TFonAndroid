package com.example.rui.mnist.tensorflow;

import android.content.res.AssetManager;
import android.util.Log;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

/**
 * Created by Rui on 2018/3/2.
 */

public class MnistClassifier {
    private final String MODEL_PATH = "file:///android_asset/mnist.pb";
    public static final String INPUT_NAME = "input";
    public static final String KEEP_PROB_NAME = "keep_prob";
    public static final String OUTPUT_NAME = "output";

    private TensorFlowInferenceInterface inference;

    private final int width = 28;
    private final int heifht = 28;
    private float[] inputs = new float[width * heifht];
    private int[] INPUT_SHAPE = new int[]{1, width * heifht};

    public MnistClassifier(AssetManager assetManager) {
        this.inference = new TensorFlowInferenceInterface(assetManager, MODEL_PATH);
        inference.feed(KEEP_PROB_NAME, new float[]{1.0f}, 1);
    }

    public float[] getResult(float[] inputs) {
        try {
            this.inputs = inputs;
        } catch (Exception e) {
            e.printStackTrace();
        }
        float[] output = new float[10];

        inference.feed(INPUT_NAME, inputs, 1, width * heifht);
        inference.run(new String[]{OUTPUT_NAME}, false);
        inference.fetch(OUTPUT_NAME, output);

        return output;
    }
}
