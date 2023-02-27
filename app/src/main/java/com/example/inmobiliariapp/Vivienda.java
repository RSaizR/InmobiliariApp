package com.example.inmobiliariapp;

public class Vivienda {
    String latitud;
    String longitud;
    String direccio;
    String información;
    String url;


    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }

    public String getDireccio() { return direccio; }

    public void setDireccio(String direccio) {
        this.direccio = direccio;
    }

    public String getInformación() { return información; }

    public void setInformación(String información) {
        this.información = información;
    }

    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    public Vivienda(String latitud, String longitud, String direccio, String información, String url) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccio = direccio;
        this.información = información;
        this.url = url;
    }
    public Vivienda() {
    }
}
