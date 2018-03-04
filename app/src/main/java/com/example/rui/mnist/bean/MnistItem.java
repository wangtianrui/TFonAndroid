package com.example.rui.mnist.bean;

import android.support.annotation.NonNull;

/**
 * Created by Rui on 2018/3/4.
 */

public class MnistItem implements Comparable<MnistItem> {
    float value;
    float index;

    public float getValue() {
        return value;
    }

    public float getIndex() {
        return index;
    }

    public MnistItem(float value, float index) {
        this.value = value;
        this.index = index;
    }

    @Override
    public int compareTo(@NonNull MnistItem o) {
        return value < o.value ? 1 : -1;
    }
}
