package com.example.rui.mnist.tensorflow;

import android.content.res.AssetManager;
import android.os.Trace;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;


/**
 * Created by Rui on 2018/2/28.
 */


public class MyTSF {
    private static final String MODEL_FILE = "file:///android_asset/mnist.pb"; //模型存放路径

    //数据的维度
    private static final int HEIGHT = 28;
    private static final int WIDTH = 28;

    //模型中输出变量的名称
    private static final String inputName = "input";
    //用于存储的模型输入数据
    private float[] inputs = new float[HEIGHT * WIDTH];

    //模型中输出变量的名称
    private static final String outputName = "output";
    //用于存储模型的输出数据
    private float[] outputs = new float[HEIGHT * WIDTH];


    TensorFlowInferenceInterface inferenceInterface;


    static {
        //加载库文件
        System.loadLibrary("tensorflow_inference");
    }

    public MyTSF(AssetManager assetManager) {
        //接口定义
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, MODEL_FILE);
    }

    public float[] getResult(float[] inputs) {
        this.inputs = inputs;

        //将数据feed给模型
        Trace.beginSection("feed");
        inferenceInterface.feed(inputName, inputs, WIDTH, HEIGHT);
        Trace.endSection();

        //运行识别操作
        Trace.beginSection("run");
        String[] outputNames = new String[]{outputName};
        inferenceInterface.run(outputNames);
        Trace.endSection();

        //将输出存放到outputs中
        Trace.beginSection("fetch");
        inferenceInterface.fetch(outputName, outputs);
        Trace.endSection();
        return outputs;
    }


}