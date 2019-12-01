package com.tcc.tccvioleta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.tcc.tccvioleta.Classes.Usuario;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.R;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Button button = findViewById(R.id.Button_prosseguir);
        button.setOnClickListener(v -> {
            EditText editText_nome = findViewById(R.id.nome_edit_text);
            EditText editText_telefone = findViewById(R.id.telefone_edit_text);

            FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
            FirebaseUser user = firebaseAuth.getCurrentUser();

            if (user != null) {
                System.out.println(user.getUid());
            }

            Usuario usuario = new Usuario();
            if (user != null) {
                usuario.setId(user.getUid());
            }
            if (user != null) {
                usuario.setEmail(user.getEmail());
            }
            usuario.setNome(editText_nome.getText().toString());
            usuario.setTelefone(editText_telefone.getText().toString());

            usuario.salvarDados();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

    }
}
