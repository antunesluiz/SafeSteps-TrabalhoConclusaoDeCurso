package com.tcc.tccvioleta.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tccvioleta.Classes.Ocorrencia;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.Helpers.OcorrenciaAdapter;
import com.tcc.tccvioleta.R;

import java.util.ArrayList;
import java.util.Objects;

public class MinhasOcorrencias extends AppCompatActivity {
    DatabaseReference databaseReference;
    ListView listView;
    ArrayList<Ocorrencia> arrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minhas_ocorrencias);

        arrayList = new ArrayList<>();

        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        ValueEventListener valueEventListener = databaseReference.child(Objects.requireNonNull(user).getUid()).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.child("Ocorrencias").getChildren()) {
                    Ocorrencia ocorrencia = ds.getValue(Ocorrencia.class);

                    arrayList.add(ocorrencia);
                }

                OcorrenciaAdapter ocorrenciaAdapter = new OcorrenciaAdapter(getApplicationContext(), arrayList);

                listView = findViewById(R.id.lista_ocorrencias);
                listView.setAdapter(ocorrenciaAdapter);

                listView.setOnItemLongClickListener((parent, view, position, id) -> {
                    arrayList.remove(position);

                    ocorrenciaAdapter.notifyDataSetChanged();

                    return false;
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.removeEventListener(valueEventListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Usuarios");

        if (user != null) {
            myRef.child(user.getUid()).child("Ocorrencias").removeValue();
        }

        for (Ocorrencia ocorrencia : arrayList) {
            if (user != null) {
                myRef.child(user.getUid()).child("Ocorrencias").push().setValue(ocorrencia);
            }
        }
    }
}
