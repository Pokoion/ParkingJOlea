package com.lksnext.parkingplantilla.viewmodel;

import com.lksnext.parkingplantilla.domain.DataCallback;
import java.util.concurrent.CountDownLatch;

public class LatchCallback<T> implements DataCallback<T> {
    private final CountDownLatch latch;

    public LatchCallback(CountDownLatch latch) {
        this.latch = latch;
    }

    @Override
    public void onSuccess(T result) {
        latch.countDown();
    }

    @Override
    public void onFailure(Exception e) {
        latch.countDown();
    }
}

