package com.tcc.tccvioleta.Classes;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

import java.util.List;

public class Rota implements Comparable<Rota> {
    private JSONObject rota;
    private List<com.google.android.gms.maps.model.LatLng> cordenadas;
    private int pontuacao;

    public Rota() {
        pontuacao = 0;
    }

    public Rota(JSONObject rota) {
        this.rota = rota;

        pontuacao = 0;
    }

    public JSONObject getRota() {
        return rota;
    }

    public void setRota(JSONObject rota) {
        this.rota = rota;
    }

    public List<LatLng> getCordenadas() {
        return cordenadas;
    }

    public void setCordenadas(List<LatLng> cordenadas) {
        this.cordenadas = cordenadas;
    }

    public int getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(int pontuacao) {
        this.pontuacao = pontuacao;
    }

    @Override
    public int compareTo(Rota o) {
        return Integer.compare(o.getPontuacao(), this.pontuacao);
    }
}
