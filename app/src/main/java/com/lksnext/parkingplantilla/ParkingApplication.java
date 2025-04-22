package com.lksnext.parkingplantilla;

import android.app.Application;
import android.content.Context;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.data.local.LocalDataSource;
import com.lksnext.parkingplantilla.data.repository.DataSource;

public class ParkingApplication extends Application {

    private static DataRepository repository;
    private Context context;

    @Override
    public void onCreate() {
        super.onCreate();

        this.context = getApplicationContext();

        // Initialize the data source
        DataSource dataSource = new LocalDataSource();

        // Initialize repository with the data source
        repository = DataRepository.getInstance(dataSource, context);
    }

    public static DataRepository getRepository() {
        return repository;
    }

}