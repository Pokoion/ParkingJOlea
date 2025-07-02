package com.lksnext.parkingplantilla.domain;

import com.google.firebase.firestore.Exclude;

public class Reserva {
    public enum Estado {
        ACTIVA,
        FINALIZADA,
        CANCELADA
    }

    String fecha;
    String usuario;
    String id;
    Plaza plaza;
    Hora hora;
    Estado estado;

    public Reserva() {

    }

    public Reserva(String fecha, String usuario, String id, Plaza plaza, Hora hora) {
        this.fecha = fecha;
        this.usuario = usuario;
        this.plaza = plaza;
        this.hora = hora;
        this.id = id;
        this.estado = Estado.ACTIVA;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Plaza getPlaza() {
        return plaza;
    }

    public void setPlaza(Plaza plaza) {
        this.plaza = plaza;
    }

    public Hora getHora() {
        return hora;
    }

    public void setHora(Hora hora) {
        this.hora = hora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Exclude
    public String getEstadoString() {
        if (estado == null) return "";
        String name = estado.name().toLowerCase();
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

}
