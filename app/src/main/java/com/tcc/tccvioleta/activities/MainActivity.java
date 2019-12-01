package com.tcc.tccvioleta.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tccvioleta.Classes.Contato;
import com.tcc.tccvioleta.Classes.Ocorrencia;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.IndexActivity;
import com.tcc.tccvioleta.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends FragmentActivity implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMapClickListener {
    static private final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 1;
    static private final int AUTOCOMPLETE_REQUEST_CODE_ORIGEM = 1;

    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private Place place1;
    private Marker[] marker;

    private GoogleMap mMap;

    private ArrayList<LatLng> listLatLngOcorrencias;
    private ArrayList<Contato> contatoArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listLatLngOcorrencias = new ArrayList<>();

        Toolbar toolbar = findViewById(R.id.toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        Objects.requireNonNull(mapFragment).getMapAsync(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyCQiUn-L90dUEJo18ZNrTFRrVbKk60cTpQ");
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        localizacaoAtual();

        EditText editTextOrigem = findViewById(R.id.edit_text_origem);

        editTextOrigem.setOnClickListener(v -> {
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

            Intent intent = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN, fields)
                    .setCountry("br")
                    .build(getApplicationContext());
            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE_ORIGEM);
        });

        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        contatoArrayList = new ArrayList<>();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");
        if (user != null) {
            databaseReference.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    contatoArrayList.clear();
                    for (DataSnapshot snapshot : dataSnapshot.child("Contatos").getChildren()) {
                        Contato contato = snapshot.getValue(Contato.class);

                        contatoArrayList.add(contato);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        findViewById(R.id.emergencia).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    Activity#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for Activity#requestPermissions for more details.
                    return;
                }
                Task<Location> task = fusedLocationProviderClient.getLastLocation();
                task.addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;

                        Toast.makeText(getApplicationContext(), "Enviando mensagem de texto...", Toast.LENGTH_LONG).show();

                        String endereco = getAddressFromLatLng(getApplicationContext(), currentLocation.getLatitude(), currentLocation.getLongitude());

                        for (Contato contato : contatoArrayList) {
                            System.out.println(contato.getNome());
                            try {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(contato.getTelefone(), null, "Estou precisando de ajuda! \n\n" + endereco, null, null);
                                Toast.makeText(MainActivity.this, "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "SMS Failed to Send, Please try again", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });


            }
        });

    }

    private void localizacaoAtual() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_FINE_LOCATION);
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(location -> {
            if (location != null) {
                currentLocation = location;
                Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                if (mapFragment != null) {
                    mapFragment.getMapAsync(MainActivity.this);
                }
            }

            moveCameraLocalizacaoAtual();
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng latlngtemp = new LatLng(0, 0);
        final Marker marker = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        final Marker marker1 = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        marker.setVisible(false);
        marker1.setVisible(false);


        mMap.setOnMapClickListener(latLng -> {

            if (latLng != null) {
                Double latitude = latLng.latitude;
                Double longitude = latLng.longitude;

                String address = getAddressFromLatLng(getApplicationContext(), latitude, longitude);

                Intent intent = new Intent(getApplicationContext(), OcorrenciaActivity.class);
                intent.putExtra("Endereco", address);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        EditText editTextOrigem = findViewById(R.id.edit_text_origem);

        LatLng latlngtemp = new LatLng(0, 0);
        final Marker marker = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        final Marker marker1 = mMap.addMarker(new MarkerOptions().position(latlngtemp).title("Marker in local position"));
        marker.setVisible(false);
        marker1.setVisible(false);

        if (data != null) {
            switch (requestCode) {
                case AUTOCOMPLETE_REQUEST_CODE_ORIGEM:
                    if (resultCode == RESULT_OK) {
                        place1 = Autocomplete.getPlaceFromIntent(data);
                        editTextOrigem.setText(place1.getName());
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(place1.getLatLng()));
                        mMap.clear();
                        exibirOcorrencias(place1.getLatLng());
                    }
                    break;
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    localizacaoAtual();
                }
                break;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    public void myLocation(View view) {
        moveCameraLocalizacaoAtual();
        EditText editText_search = findViewById(R.id.edit_text_origem);
        editText_search.setText("");
        editText_search.setHint(" Search here");
        exibirOcorrencias(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        place1 = null;
    }

    private void moveCameraLocalizacaoAtual() {
        mMap.clear();
        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude())));
        marker.setVisible(true);
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        exibirOcorrencias(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
    }

    private void exibirOcorrencias(LatLng latlng) {
        LatLng latlngtemp = new LatLng(0, 0);

        marker = new Marker[1];
        marker[0] = mMap.addMarker(new MarkerOptions().position(latlngtemp));

        marker[0].setVisible(false);
        marker[0].remove();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Usuarios");

        double raio = 0.009;

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mMap.clear();
                listLatLngOcorrencias.clear();

                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date dataAtual = new Date();
                String horaAtual = dateFormat.format(dataAtual);
                String[] hora = horaAtual.split(":");

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    for (DataSnapshot ds3 : ds.child("Ocorrencias").getChildren()) {
                        Ocorrencia ocorrencias = ds3.getValue(Ocorrencia.class);

                        Geocoder geocoder = new Geocoder(getApplicationContext());
                        try {
                            if (ocorrencias != null) {
                                String[] horaOcorrencia = ocorrencias.getHora().split(":");

                                if (Integer.parseInt(hora[0]) >= 18) {
                                    if (Integer.parseInt(horaOcorrencia[0]) >= 18 || Integer.parseInt(horaOcorrencia[0]) <= 6) {
                                        List<Address> enderecos = geocoder.getFromLocationName(ocorrencias.getEndereco(), 1);

                                        if (enderecos.size() > 0) {

                                            LatLng latLng1 = new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude());
                                            listLatLngOcorrencias.add(latLng1);

                                            if (calcularDistanciaEntreDoisPontos(latLng1, latlng) < raio) {
                                                if (ocorrencias.getTipo().equals("Ausência de iluminação")) {
                                                    marker[0] = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                            .title("Rua mal iluminada")
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                    );
                                                    marker[0].setVisible(true);
                                                } else {
                                                    if (ocorrencias.getTipo().equals("Rua pouco movimentada")) {
                                                        marker[0] = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                .title("Rua pouco movimentada")
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                                        );
                                                        marker[0].setVisible(true);
                                                    } else {
                                                        if (ocorrencias.getTipo().equals("Assédio costumeiro")) {
                                                            marker[0] = mMap.addMarker(new MarkerOptions()
                                                                    .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                    .title("Assédio costumeiro")
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                                            );
                                                            marker[0].setVisible(true);
                                                        } else {
                                                            marker[0] = mMap.addMarker(new MarkerOptions()
                                                                    .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                    .title("Área de risco")
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                            );
                                                            marker[0].setVisible(true);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                    }
                                } else {
                                    if (Integer.parseInt(horaOcorrencia[0]) < 18 || Integer.parseInt(horaOcorrencia[0]) > 6) {
                                        List<Address> enderecos = geocoder.getFromLocationName(ocorrencias.getEndereco(), 1);

                                        if (enderecos.size() > 0) {

                                            LatLng latLng1 = new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude());
                                            listLatLngOcorrencias.add(latLng1);

                                            if (calcularDistanciaEntreDoisPontos(latLng1, latlng) < raio) {
                                                if (ocorrencias.getTipo().equals("Ausência de iluminação")) {
                                                    marker[0] = mMap.addMarker(new MarkerOptions()
                                                            .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                            .title("Rua mal iluminada")
                                                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                                                    );
                                                    marker[0].setVisible(true);
                                                } else {
                                                    if (ocorrencias.getTipo().equals("Rua pouco movimentada")) {
                                                        marker[0] = mMap.addMarker(new MarkerOptions()
                                                                .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                .title("Rua pouco movimentada")
                                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                                                        );
                                                        marker[0].setVisible(true);
                                                    } else {
                                                        if (ocorrencias.getTipo().equals("Assédio costumeiro")) {
                                                            marker[0] = mMap.addMarker(new MarkerOptions()
                                                                    .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                    .title("Assédio costumeiro")
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                                                            );
                                                            marker[0].setVisible(true);
                                                        } else {
                                                            marker[0] = mMap.addMarker(new MarkerOptions()
                                                                    .position(new LatLng(enderecos.get(0).getLatitude(), enderecos.get(0).getLongitude()))
                                                                    .title("Área de risco")
                                                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                                                            );
                                                            marker[0].setVisible(true);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            private double calcularDistanciaEntreDoisPontos(LatLng ponto1, LatLng ponto2) {
                double distancia;

                distancia = Math.sqrt((Math.pow((ponto1.latitude - ponto2.latitude), 2) + Math.pow((ponto1.longitude - ponto2.longitude), 2)));

                System.out.println(distancia);

                return distancia;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (latLng != null) {
            Double latitude = latLng.latitude;
            Double longitude = latLng.longitude;

            String address = getAddressFromLatLng(getApplicationContext(), latitude, longitude);

            Intent intent = new Intent(getApplicationContext(), OcorrenciaActivity.class);
            intent.putExtra("Endereco", address);
            startActivity(intent);
        }
    }

    private String getAddressFromLatLng(Context context, double latitude, double longitude) {
        String fullEndereco = "";

        try {
            Geocoder geocoder = new Geocoder(context, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses.size() > 0) {
                Address address = addresses.get(0);
                fullEndereco = address.getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fullEndereco;
    }

    public void rotas(View view) {
        Intent intent = new Intent(getApplicationContext(), RotasActivity.class);
        intent.putExtra("Latitude", currentLocation.getLatitude());
        intent.putExtra("Longitude", currentLocation.getLongitude());
        intent.putExtra("LatitudeSearch", place1);

        intent.putExtra("listaOcorrencias", listLatLngOcorrencias);

        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.rotasFavoritas) {
            // Handle the camera action
        } else if (id == R.id.minhasOcorrencias) {
            startActivity(new Intent(this, MinhasOcorrencias.class));

        } else if (id == R.id.contatos_ajuda) {
            startActivity(new Intent(this, ContatosActivity.class));

        } else if (id == R.id.logout) {

            FirebaseAuth autenticacao = ConfiguracaoFireBase.getFirebaseAutenticacao();
            autenticacao.signOut();

            startActivity(new Intent(this, IndexActivity.class));
            finish();

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
