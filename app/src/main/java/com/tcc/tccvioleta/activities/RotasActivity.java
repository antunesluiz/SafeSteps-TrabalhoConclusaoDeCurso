package com.tcc.tccvioleta.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.tcc.tccvioleta.Classes.Rota;
import com.tcc.tccvioleta.Helpers.RotaController;
import com.tcc.tccvioleta.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RotasActivity extends AppCompatActivity implements OnMapReadyCallback {
    static private final int AUTOCOMPLETE_REQUEST_CODE_ORIGEM = 1;
    static private final int AUTOCOMPLETE_REQUEST_CODE_DESTINO = 2;

    private static GoogleMap mMap;

    private LatLng latlngOrigem;
    private Place latLngDestino;

    private static ArrayList<LatLng> listLatLngOcorrencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rotas);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_rota);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        Bundle bundle = getIntent().getExtras();

        Double latitudeOrigem = null;
        Double longitudeOrigem = null;

        if (bundle != null) {
            latitudeOrigem = (Double) bundle.get("Latitude");
            longitudeOrigem = (Double) bundle.get("Longitude");

            latLngDestino = (Place) bundle.get("LatitudeSearch");

            //noinspection unchecked
            listLatLngOcorrencias = (ArrayList<LatLng>) bundle.get("listaOcorrencias");
        }

        for (int i = 0; i < listLatLngOcorrencias.size(); i++) {
            System.out.println(listLatLngOcorrencias.get(i).latitude);
        }

        latlngOrigem = new LatLng(Objects.requireNonNull(latitudeOrigem), Objects.requireNonNull(longitudeOrigem));

        EditText edit_origem = findViewById(R.id.edit_origem);
        EditText edit_destino = findViewById(R.id.edit_destino);

        if (latLngDestino != null) {
            edit_destino.setHint(latLngDestino.getName());
        }
        edit_origem.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .setCountry("br")
                    .build(getApplicationContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGEM);
        });

        edit_destino.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .setCountry("br")
                    .build(getApplicationContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_DESTINO);
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EditText edit_origem = findViewById(R.id.edit_origem);
        EditText edit_destino = findViewById(R.id.edit_destino);

        LatLng latlngtemp = new LatLng(0, 0);
        final Marker marker = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        final Marker marker1 = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        marker.setVisible(false);
        marker1.setVisible(false);

        if (data != null) {
            switch (requestCode) {
                case AUTOCOMPLETE_REQUEST_CODE_ORIGEM:
                    if (resultCode == RESULT_OK) {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        edit_origem.setText(String.format(" %s", place.getName()));
                        marker.setPosition(Objects.requireNonNull(place.getLatLng()));
                        marker.setVisible(true);
                        marker.setTitle(place.getName());
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place.getLatLng()));


                        latlngOrigem = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                        calcularRota();
                    }
                    break;
                case AUTOCOMPLETE_REQUEST_CODE_DESTINO:
                    if (resultCode == RESULT_OK) {
                        latLngDestino = Autocomplete.getPlaceFromIntent(data);
                        edit_destino.setText(String.format(" %s", latLngDestino.getName()));
                        marker.setPosition(Objects.requireNonNull(latLngDestino.getLatLng()));
                        marker.setVisible(true);
                        marker.setTitle(latLngDestino.getName());
                       // mMap.animateCamera(CameraUpdateFactory.newLatLng(latLngDestino.getLatLng()));

                        calcularRota();
                    }
                    break;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.animateCamera(CameraUpdateFactory.zoomBy(10));

        moveCameraLocalizacaoAtual();

        calcularRota();
    }

    public void calcularRota() {
        String url = montaURLRotaMapa();

        if (url != null) {
            MinhaAsyncTask tarefa = new MinhaAsyncTask();
            mMap.animateCamera(CameraUpdateFactory.zoomBy(-2));

            //Executa a tarefa passando a URL recuperada
            tarefa.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
        }
    }

    private String montaURLRotaMapa() {
        if (latlngOrigem != null) {
            if (latLngDestino != null) {
                String str_origin = String.format("origin=%s,%s", latlngOrigem.latitude, latlngOrigem.longitude);
                String str_dest = String.format("destination=%s,%s", Objects.requireNonNull(latLngDestino.getLatLng()).latitude, latLngDestino.getLatLng().longitude);

                String mode = "mode=" + "walking";
                String alternative = "alternatives=true";

                String parameters = str_origin + "&" + str_dest + "&" + mode + "&" + alternative;
                String output = "json";

                return String.format("https://maps.googleapis.com/maps/api/directions/%s?%s&key=%s", output, parameters, getString(R.string.google_maps_key));
            }
        }

        return null;
    }

    public JSONObject requisicaoHTTP(String url) {
        JSONObject resultado = null;

        try {
            OkHttpClient httpClient = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Response response = httpClient.newCall(request).execute();

            if (response.code() != 200) {
                Log.e("MERDAS", "DEU MERDA HTTP");
            } else {
                String body = response.body().string();
                resultado = new JSONObject(body);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return resultado;
    }

    private void moveCameraLocalizacaoAtual() {
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlngOrigem, 16));
        Marker marker = mMap.addMarker(new MarkerOptions().position(latlngOrigem));
        marker.setVisible(true);
        //mMap.animateCamera(CameraUpdateFactory.zoomBy(-16));
    }

    public void pintarCaminho(JSONObject json) {
        try {
            JSONArray listaRotas = json.getJSONArray("routes");
            new RotaController(listaRotas);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void pintaOCaminho(ArrayList<Rota> rotaArrayList) {

        for (int i = 0; i < rotaArrayList.size(); i++) {
            Log.e("MELHOR ROTA",  i+1 + " - " + rotaArrayList.get(i).getPontuacao());
        }
        //Collections.sort(rotaArrayList);

        List<LatLng> listaCordenadas = melhorRota(rotaArrayList);

        if (listaCordenadas != null) {
            for (int ponto = 0; ponto < listaCordenadas.size() - 1; ponto += 1) {
                LatLng pontoOrigem = listaCordenadas.get(ponto);
                LatLng pontoDestino = listaCordenadas.get(ponto + 1);

                PolylineOptions opcoesLinha = new PolylineOptions();

                opcoesLinha.add(new LatLng(pontoOrigem.latitude, pontoOrigem.longitude),
                        new LatLng(pontoDestino.latitude, pontoDestino.longitude));
                Polyline line = mMap.addPolyline(opcoesLinha);
                line.setWidth(12);
                line.setColor(Color.BLACK);
                line.setGeodesic(true);
            }
        } else {
            System.out.println("Cordenadas Nulas");
        }
    }

    private static List<LatLng> melhorRota(ArrayList<Rota> listaRotas) {
        if (listaRotas != null) {
            for (int i = 0; i < listaRotas.size(); i++) {
                for (int j = 0; j < listaRotas.get(i).getCordenadas().size(); j++) {
                    for (int k = 0; k < listLatLngOcorrencias.size(); k++) {
                        LatLng ponto1 = listLatLngOcorrencias.get(k);
                        LatLng ponto2 = listaRotas.get(i).getCordenadas().get(j);

                        if (calcularDistanciaEntreDoisPontos(ponto1, ponto2) <= 0.0005) {
                            listaRotas.get(i).setPontuacao(listaRotas.get(i).getPontuacao() - 1);
                        }
                    }
                }
            }

            for (int i = 0; i < listaRotas.size(); i++) {
                Log.e("MELHOR ROTA OCORRENCIAS",  i + 1 + " - " + listaRotas.get(i).getPontuacao());
            }

            Collections.sort(listaRotas);

            return listaRotas.get(0).getCordenadas();
        }

        return null;
    }

    private static double calcularDistanciaEntreDoisPontos(@org.jetbrains.annotations.NotNull LatLng ponto1, LatLng ponto2) {
        double distancia;

        distancia = Math.sqrt((Math.pow((ponto1.latitude - ponto2.latitude), 2) + Math.pow((ponto1.longitude - ponto2.longitude), 2)));

        // System.out.println("teste: " + distancia);

        return distancia;
    }

    private class MinhaAsyncTask extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {

            return requisicaoHTTP(strings[0]);
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);

            if (jsonObject != null) {
                pintarCaminho(jsonObject);
            }
        }
    }
}
