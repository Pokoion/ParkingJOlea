package com.lksnext.parkingplantilla.domain;

public interface DataCallback<T> {
    void onSuccess(T result);
    void onError(Exception e);
}