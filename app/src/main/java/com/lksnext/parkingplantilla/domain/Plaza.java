package com.lksnext.parkingplantilla.domain;

import java.util.HashMap;
import java.util.Map;

public class Plaza {

    public static final String TIPO_STANDARD = "Standard";
    public static final String TIPO_MOTORCYCLE = "Motorcycle";
    public static final String TIPO_CV_CHARGER = "CV Charger";
    public static final String TIPO_DISABLED = "Disabled";

    private static final Map<String, String> TIPO_IMAGES = new HashMap<>();
    static {
        TIPO_IMAGES.put(TIPO_STANDARD, "ic_car");
        TIPO_IMAGES.put(TIPO_MOTORCYCLE, "ic_motorcycle");
        TIPO_IMAGES.put(TIPO_CV_CHARGER, "ic_cv_charger");
        TIPO_IMAGES.put(TIPO_DISABLED, "ic_disabled");
    }
    private String id;  // Format like "A-12"
    private String tipo;

    public Plaza() {
    }

    public Plaza(String id, String tipo) {
        this.id = id;
        setTipo(tipo); // Use setter for validation
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        // Validate that the tipo is one of the allowed values
        if (TIPO_STANDARD.equals(tipo) ||
                TIPO_MOTORCYCLE.equals(tipo) ||
                TIPO_CV_CHARGER.equals(tipo) ||
                TIPO_DISABLED.equals(tipo)) {
            this.tipo = tipo;
        } else {
            throw new IllegalArgumentException("Invalid tipo value: " + tipo);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImageResource() {
        return TIPO_IMAGES.getOrDefault(tipo, "ic_car");
    }
}