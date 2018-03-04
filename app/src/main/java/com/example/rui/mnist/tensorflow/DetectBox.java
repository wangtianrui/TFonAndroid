package com.example.rui.mnist.tensorflow;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;

import com.example.rui.mnist.bean.Recognition;
import com.orhanobut.logger.Logger;

import org.tensorflow.Graph;
import org.tensorflow.Operation;
import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.StringTokenizer;


/**
 * Created by Rui on 2018/3/4.
 */

public class DetectBox {

    private static final String TAG = "DetectBox";
    private static final int MAX_RESULTS = Integer.MAX_VALUE;

    //private static final int MB_INPUT_SIZE = 224;

    private static final int MB_IMAGE_MEAN = 128;
    private static final float MB_IMAGE_STD = 128;
    private static final String MB_INPUT_NAME = "ResizeBilinear";
    private static final String MB_OUTPUT_LOCATIONS_NAME = "output_locations/Reshape";
    private static final String MB_OUTPUT_SCORES_NAME = "output_scores/Reshape";
    private static final String MB_MODEL_FILE = "file:///android_asset/multibox_model.pb";
    private static final String MB_LOCATION_FILE =
            "file:///android_asset/multibox_location_priors.txt";

    private int inputSize;
    private int[] intValues;
    private float[] floatValues;
    private float[] outputLocations;
    private float[] outputScores;
    private String[] outputNames;
    private int numLocations;
    private float[] boxPriors;


    private Graph graph;
    private Operation inputOp;
    private Operation outputOp;


    private TensorFlowInferenceInterface inferenceInterface;

    public DetectBox(AssetManager assetManager) {
        this.inferenceInterface = new TensorFlowInferenceInterface(assetManager, MB_MODEL_FILE);
        this.graph = inferenceInterface.graph();
        this.inputOp = graph.operation(MB_INPUT_NAME);
        this.outputOp = graph.operation(MB_OUTPUT_SCORES_NAME);
        inputSize = (int) inputOp.output(0).shape().size(1);
        numLocations = (int) outputOp.output(0).shape().size(1);
        boxPriors = new float[numLocations * 8];
        try {
            loadCoderOptions(assetManager, MB_LOCATION_FILE, boxPriors);
        } catch (final IOException e) {
            throw new RuntimeException("加载失败：" + MB_LOCATION_FILE);
        }
        outputNames = new String[]{MB_OUTPUT_LOCATIONS_NAME, MB_OUTPUT_SCORES_NAME};
        intValues = new int[inputSize * inputSize];
        floatValues = new float[inputSize * inputSize * 3];
        outputScores = new float[numLocations];
        outputLocations = new float[numLocations * 4];
    }

    private void loadCoderOptions(
            final AssetManager assetManager, final String locationFilename, final float[] boxPriors)
            throws IOException {
        final String assetPrefix = "file:///android_asset/";
        InputStream is;
        if (locationFilename.startsWith(assetPrefix)) {
            is = assetManager.open(locationFilename.split(assetPrefix)[1]);
        } else {
            is = new FileInputStream(locationFilename);
        }
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int priorIndex = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            final StringTokenizer st = new StringTokenizer(line, ", ");
            while (st.hasMoreTokens()) {
                final String token = st.nextToken();
                try {
                    final float number = Float.parseFloat(token);
                    boxPriors[priorIndex++] = number;
                } catch (final NumberFormatException e) {

                }
            }
        }
        if (priorIndex != boxPriors.length) {
            throw new RuntimeException(
                    "Box length:" + priorIndex + " , " + boxPriors.length);
        }
    }


    public List<Recognition> recognizeImage(final Bitmap bitmap) {

        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int i = 0; i < intValues.length; ++i) {
            floatValues[i * 3 + 0] = ((intValues[i] & 0xFF) - MB_IMAGE_MEAN) / MB_IMAGE_STD;
            floatValues[i * 3 + 1] = (((intValues[i] >> 8) & 0xFF) - MB_IMAGE_MEAN) / MB_IMAGE_STD;
            floatValues[i * 3 + 2] = (((intValues[i] >> 16) & 0xFF) - MB_IMAGE_MEAN) / MB_IMAGE_STD;
        }

        inferenceInterface.feed(MB_INPUT_NAME, floatValues, 1, inputSize, inputSize, 3);

        inferenceInterface.run(outputNames, false);

        final float[] outputScoresEncoding = new float[numLocations];
        final float[] outputLocationsEncoding = new float[numLocations * 4];
        inferenceInterface.fetch(outputNames[0], outputLocationsEncoding);
        inferenceInterface.fetch(outputNames[1], outputScoresEncoding);

        outputLocations = decodeLocationsEncoding(outputLocationsEncoding);
        outputScores = decodeScoresEncoding(outputScoresEncoding);

        final PriorityQueue<Recognition> pq =
                new PriorityQueue<Recognition>(
                        1,
                        new Comparator<Recognition>() {
                            @Override
                            public int compare(final Recognition lhs, final Recognition rhs) {
                                return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                            }
                        });


        for (int i = 0; i < outputScores.length; ++i) {
            final RectF detection =
                    new RectF(
                            outputLocations[4 * i] * inputSize,
                            outputLocations[4 * i + 1] * inputSize,
                            outputLocations[4 * i + 2] * inputSize,
                            outputLocations[4 * i + 3] * inputSize);
            pq.add(new Recognition("" + i, null, outputScores[i], detection));
        }

        final ArrayList<Recognition> recognitions = new ArrayList<Recognition>();
        for (int i = 0; i < Math.min(pq.size(), MAX_RESULTS); ++i) {
            recognitions.add(pq.poll());
        }
        Trace.endSection();
        return recognitions;
    }

    private float[] decodeLocationsEncoding(final float[] locationEncoding) {
        final float[] locations = new float[locationEncoding.length];
        boolean nonZero = false;
        for (int i = 0; i < numLocations; ++i) {
            for (int j = 0; j < 4; ++j) {
                final float currEncoding = locationEncoding[4 * i + j];
                nonZero = nonZero || currEncoding != 0.0f;
                final float mean = boxPriors[i * 8 + j * 2];
                final float stdDev = boxPriors[i * 8 + j * 2 + 1];
                float currentLocation = currEncoding * stdDev + mean;
                currentLocation = Math.max(currentLocation, 0.0f);
                currentLocation = Math.min(currentLocation, 1.0f);
                locations[4 * i + j] = currentLocation;
            }
        }
        if (!nonZero) {
            Log.d(TAG, "decodeLocationsEncoding:No non-zero  ");
        }
        return locations;
    }

    private float[] decodeScoresEncoding(final float[] scoresEncoding) {
        final float[] scores = new float[scoresEncoding.length];
        for (int i = 0; i < scoresEncoding.length; ++i) {
            scores[i] = 1 / ((float) (1 + Math.exp(-scoresEncoding[i])));
        }
        return scores;
    }
}
