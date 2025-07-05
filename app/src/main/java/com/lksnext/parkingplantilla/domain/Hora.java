package com.lksnext.parkingplantilla.domain;

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
        int inicioHoras = (int) (horaInicio / 3600000);
        int inicioMin = (int) ((horaInicio % 3600000) / 60000);
        int finHoras = (int) (horaFin / 3600000);
        int finMin = (int) ((horaFin % 3600000) / 60000);
        return String.format(java.util.Locale.getDefault(), "%02d:%02d - %02d:%02d", inicioHoras, inicioMin, finHoras, finMin);
    }
}
