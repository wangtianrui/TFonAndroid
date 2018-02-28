package com.example.rui.mnist.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rui.mnist.R;
import com.example.rui.mnist.model.FragmentModel;

/**
 * Created by Rui on 2018/2/27.
 */

public class LongDistanceFragment extends FragmentModel {
    @Override
    protected Fragment newInstance() {
        return new LongDistanceFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.long_distance_fragment, container, false);
        return v;
    }
}
