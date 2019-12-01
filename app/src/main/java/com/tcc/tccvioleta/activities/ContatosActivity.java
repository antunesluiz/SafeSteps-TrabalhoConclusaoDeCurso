package com.tcc.tccvioleta.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tcc.tccvioleta.Classes.Contato;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.Helpers.ContatoAdapter;
import com.tcc.tccvioleta.R;

import java.util.ArrayList;
import java.util.Objects;

public class ContatosActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    ListView listView;
    ArrayList<Contato> contatos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);

        contatos = new ArrayList<>();

        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("Usuarios");

        ValueEventListener valueEventListener = databaseReference.child(Objects.requireNonNull(user).getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                contatos.clear();
                for (DataSnapshot ds : dataSnapshot.child("Contatos").getChildren()) {
                    Contato contato = ds.getValue(Contato.class);

                    contatos.add(contato);
                }

                ContatoAdapter contatoAdapter = new ContatoAdapter(getApplicationContext(), contatos);

                listView = findViewById(R.id.lista_contatos);
                listView.setAdapter(contatoAdapter);

                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        contatos.remove(position);

                        contatoAdapter.notifyDataSetChanged();

                        return false;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        databaseReference.removeEventListener(valueEventListener);

        Button buttonAdd = findViewById(R.id.button_addContato);
        buttonAdd.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), AddContatoActivity.class));
            finish();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
        FirebaseUser user = firebaseAuth.getCurrentUser();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Usuarios");

        if (user != null) {
            myRef.child(user.getUid()).child("Contatos").removeValue();
        }

        for (Contato contato : contatos) {
            if (user != null) {
                myRef.child(user.getUid()).child("Contatos").push().setValue(contato);
            }
        }
    }
}
