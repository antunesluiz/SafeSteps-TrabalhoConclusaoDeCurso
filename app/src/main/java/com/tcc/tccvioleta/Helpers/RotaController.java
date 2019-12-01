package com.tcc.tccvioleta.Helpers;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tcc.tccvioleta.activities.RotasActivity;
import com.tcc.tccvioleta.Classes.Rota;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RotaController {
    private JSONArray rotas;
    private ArrayList<Rota> rotaArrayList;

    private int size;
    private int size2;

    public RotaController(JSONArray rotas) throws JSONException {
        this.rotas = rotas;

        inserirRotasArrayList();
        buscarCordenadasDasRotas();

        for (int i = 0; i < rotaArrayList.size(); i++) {
            for (int j = 0; j < rotaArrayList.get(i).getCordenadas().size(); j = j + 10) {
                size++;
            }
        }

        for (int i = 0; i < rotaArrayList.size(); i++) {
            for (int j = 0; j < rotaArrayList.get(i).getCordenadas().size(); j = j + 10) {
                calculaNearbyPlace(rotaArrayList.get(i).getCordenadas().get(j), i);
            }
        }
    }

    private void calculaNearbyPlace(LatLng latLng, int id) {
        String url = montaURLNearbyPlace(latLng);

        RotaController.MyAsyncTask tarefa = new RotaController.MyAsyncTask();

        //Executa a tarefa passando a URL recuperada
        tarefa.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url + ";" + id);
    }

    private String montaURLNearbyPlace(LatLng latLng) {
        String key = "AIzaSyCQiUn-L90dUEJo18ZNrTFRrVbKk60cTpQ";
        String latitude = String.valueOf(latLng.latitude);
        String longitude = String.valueOf(latLng.longitude);

        List<Place.Type> fields = Arrays.asList(
                Place.Type.AIRPORT,
                Place.Type.BAKERY,
                Place.Type.BANK,
                Place.Type.BAR,
                Place.Type.BEAUTY_SALON,
                Place.Type.CITY_HALL,
                Place.Type.DENTIST,
                Place.Type.CAR_WASH,
                Place.Type.GYM,
                Place.Type.HAIR_CARE,
                Place.Type.FIRE_STATION,
                Place.Type.POLICE,
                Place.Type.POLITICAL,
                Place.Type.SCHOOL,
                Place.Type.ZOO,
                Place.Type.VETERINARY_CARE,
                Place.Type.HOSPITAL,
                Place.Type.UNIVERSITY,
                Place.Type.TRANSIT_STATION,
                Place.Type.TAXI_STAND,
                Place.Type.ATM,
                Place.Type.SUPERMARKET,
                Place.Type.SUBWAY_STATION,
                Place.Type.STORE,
                Place.Type.STADIUM,
                Place.Type.SPA,
                Place.Type.SHOPPING_MALL,
                Place.Type.SHOE_STORE,
                Place.Type.RESTAURANT,
                Place.Type.POST_OFFICE,
                Place.Type.PHARMACY,
                Place.Type.PET_STORE,
                Place.Type.MUSEUM,
                Place.Type.MOVIE_THEATER,
                Place.Type.LIBRARY,
                Place.Type.GAS_STATION,
                Place.Type.FURNITURE_STORE,
                Place.Type.FIRE_STATION,
                Place.Type.ELECTRONICS_STORE,
                Place.Type.ELECTRICIAN,
                Place.Type.DOCTOR,
                Place.Type.DEPARTMENT_STORE,
                Place.Type.CONVENIENCE_STORE,
                Place.Type.CHURCH,
                Place.Type.CAFE);

        return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key=" + key + "&location=" + latitude + "," + longitude + "&radius=20&type=" + fields + "&opennow=true";
    }

    private void buscarCordenadasDasRotas() throws JSONException {
        for (int i = 0; i < rotaArrayList.size(); i++) {
            String pontos = rotaArrayList.get(i).getRota().getJSONObject("overview_polyline").getString("points");
            ArrayList<LatLng> listaCordenadas = extrairLatLngDaRota(pontos);
            rotaArrayList.get(i).setCordenadas(listaCordenadas);
        }
    }

    private void inserirRotasArrayList() throws JSONException {
        rotaArrayList = new ArrayList<>();

        //Criando objetos das rotas
        for (int i = 0; i < rotas.length(); i++) {
            rotaArrayList.add(new Rota(rotas.getJSONObject(i)));
        }
    }

    private ArrayList<LatLng> extrairLatLngDaRota(String pontosPintar) {
        ArrayList<LatLng> listaResult = new ArrayList<>();
        int index = 0, len = pontosPintar.length();
        int lat = 0, lng = 0;

        while (index < len) {

            int b, shift = 0, result = 0;
            do {
                b = pontosPintar.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = pontosPintar.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);

            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            listaResult.add(p);
        }

        return listaResult;
    }

    private String requisicaoHTTP(String url) {
        String body = null;

        String[] urlCerta = url.split(";");

        try {
            OkHttpClient httpClient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(urlCerta[0])
                    .build();
            Response response = httpClient.newCall(request).execute();

            if (response.code() != 200) {
                Log.e("MERDAS", "DEU MERDA HTTP NEARBY");
                return null;
            } else {

                body = response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return body + ";" + urlCerta[1];
    }

    private class MyAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            return requisicaoHTTP(strings[0]);
        }

        @Override
        protected void onPostExecute(String string) {
            super.onPostExecute(string);

            size2++;

            if (size == size2) {
                RotasActivity.pintaOCaminho(rotaArrayList);
            }

            String[] retornos = string.split(";");

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(retornos[0]);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                int quantNearbyPlaces = jsonObject.getJSONArray("results").length();
                rotaArrayList.get(Integer.parseInt(retornos[1])).setPontuacao(rotaArrayList.get(Integer.parseInt(retornos[1])).getPontuacao() + quantNearbyPlaces);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
