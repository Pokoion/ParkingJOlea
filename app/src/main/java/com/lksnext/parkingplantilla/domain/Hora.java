package com.lksnext.parkingplantilla.domain;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.annotation.NonNull;

public class Hora {

    long horaInicio;
    long horaFin;

    public Hora() {

    }

    public Hora(long horaInicio, long horaFin) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    public long getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(long horaInicio) {
        this.horaInicio = horaInicio;
    }

    public long getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(long horaFin) {
        this.horaFin = horaFin;
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date startDate = new Date(horaInicio);
        Date endDate = new Date(horaFin);
        return sdf.format(startDate) + " - " + sdf.format(endDate);
    }
}
