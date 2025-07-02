package com.lksnext.parkingplantilla.utils;

import com.lksnext.parkingplantilla.data.DataRepository;
import com.lksnext.parkingplantilla.domain.Plaza;

public class FirestoreInitUtils {

    private FirestoreInitUtils() {
        // Constructor privado para evitar instanciación
    }

    public static void initializeDB(DataRepository repository) {
        // Estándar: A,B,C,D del 1-50
        for (char row : new char[]{'A', 'B', 'C', 'D'}) {
            for (int num = 1; num <= 50; num++) {
                String id = row + "-" + num;
                Plaza plaza = new Plaza(id, Plaza.TIPO_STANDARD);
                repository.addPlaza(plaza, new com.lksnext.parkingplantilla.domain.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Vacío a propósito
                    }
                    @Override
                    public void onFailure(Exception e) {
                        // Vacío a propósito
                    }
                });
            }
        }
        // Moto: E 1-50
        for (int num = 1; num <= 50; num++) {
            String id = "E-" + num;
            Plaza plaza = new Plaza(id, Plaza.TIPO_MOTORCYCLE);
            repository.addPlaza(plaza, new com.lksnext.parkingplantilla.domain.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    // Vacío a propósito
                }
                @Override
                public void onFailure(Exception e) {
                    // Vacío a propósito
                }
            });
        }
        // Disabled: F 1-30
        for (int num = 1; num <= 30; num++) {
            String id = "F-" + num;
            Plaza plaza = new Plaza(id, Plaza.TIPO_DISABLED);
            repository.addPlaza(plaza, new com.lksnext.parkingplantilla.domain.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                    // Vacío a propósito
                }
                @Override
                public void onFailure(Exception e) {
                    // Vacío a propósito
                }
            });
        }
        // CV: G,H del 1-50
        for (char row : new char[]{'G', 'H'}) {
            for (int num = 1; num <= 50; num++) {
                String id = row + "-" + num;
                Plaza plaza = new Plaza(id, Plaza.TIPO_CV_CHARGER);
                repository.addPlaza(plaza, new com.lksnext.parkingplantilla.domain.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        // Vacío a propósito
                    }
                    @Override
                    public void onFailure(Exception e) {
                        // Vacío a propósito
                    }
                });
            }
        }
    }
}
