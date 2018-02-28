package com.example.rui.mnist.model;

import android.support.v4.app.Fragment;

/**
 * Created by Rui on 2018/2/27.
 */

public abstract class FragmentModel extends Fragment {
    protected abstract Fragment newInstance();
}
