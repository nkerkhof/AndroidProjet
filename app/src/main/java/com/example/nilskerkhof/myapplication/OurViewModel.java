package com.example.nilskerkhof.myapplication;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class OurViewModel extends ViewModel {
    private MutableLiveData<Integer> mCurrentPosition;

    public MutableLiveData<Integer> getmCurrentPosition() {
        if (mCurrentPosition == null) {
            mCurrentPosition = new MutableLiveData<Integer>();
        }
        return mCurrentPosition;
    }
}
