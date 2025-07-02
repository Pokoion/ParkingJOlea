package com.lksnext.parkingplantilla.utils;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Helper class that observes for a pre-specified amount of time a livedata value to be changed.
 */
public class LiveDataTestUtil {

    private LiveDataTestUtil() {

    }

    /**
     * Function that waits for a LiveData value to be changed
     * @param liveData The LiveData variable to observe
     * @param <T> The LiveData value type
     * @return The new value of the LiveData
     * @throws InterruptedException Thrown when the latch wait is interrupted by the timeout
     */
    public static <T> T getValue(final LiveData<T> liveData) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        //Create a observer to observe for LiveData value change
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                data[0] = o;
                latch.countDown();
                liveData.removeObserver(this);
            }
        };
        //Observe the LiveData forever. Latch timeout will trigger.
        liveData.observeForever(observer);
        boolean completed = latch.await(10, TimeUnit.SECONDS); // Timeout fijo de 10 segundos
        if (!completed) {
            throw new InterruptedException("Timeout esperando el valor de LiveData");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    /**
     * Espera hasta que el valor de LiveData sea igual al esperado o se agote el timeout.
     */
    public static <T> T getOrAwaitValue(final LiveData<T> liveData, T expected) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                if ((expected == null && o == null) || (expected != null && expected.equals(o))) {
                    data[0] = o;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            }
        };
        liveData.observeForever(observer);
        boolean completed = latch.await(10, TimeUnit.SECONDS); // Timeout fijo de 10 segundos
        if (!completed) {
            throw new InterruptedException("Timeout esperando el valor de LiveData");
        }
        //noinspection unchecked
        return (T) data[0];
    }

    /**
     * Espera hasta que el valor de LiveData cumpla el predicado dado o se agote el timeout.
     */
    public static <T> T getOrAwaitValue(final LiveData<T> liveData, Predicate<T> predicate) throws InterruptedException {
        final Object[] data = new Object[1];
        final CountDownLatch latch = new CountDownLatch(1);
        Observer<T> observer = new Observer<T>() {
            @Override
            public void onChanged(@Nullable T o) {
                if (predicate.test(o)) {
                    data[0] = o;
                    latch.countDown();
                    liveData.removeObserver(this);
                }
            }
        };
        liveData.observeForever(observer);
        boolean completed = latch.await(10, TimeUnit.SECONDS); // Timeout fijo de 10 segundos
        if (!completed) {
            throw new InterruptedException("Timeout esperando el valor de LiveData");
        }
        //noinspection unchecked
        return (T) data[0];
    }
}
