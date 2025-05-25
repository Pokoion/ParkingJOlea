package com.lksnext.parkingplantilla.domain;

import com.lksnext.parkingplantilla.utils.DateUtils;

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
        return DateUtils.formatTimeFromMs(horaInicio) + " - " + DateUtils.formatTimeFromMs(horaFin);
    }
}
