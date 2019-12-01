package com.tcc.tccvioleta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.tcc.tccvioleta.Classes.Usuario;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.IndexActivity;
import com.tcc.tccvioleta.R;

import java.util.Objects;

public class CadastroActivity extends AppCompatActivity {

    private EditText campoEmail, campoSenha;
    private Usuario usuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        campoEmail = findViewById(R.id.edtEmail);
        campoSenha = findViewById(R.id.edtSenha);

        Button btnCad = findViewById(R.id.btnCadastrar);

        btnCad.setOnClickListener(view -> {

            String textoSenha = campoSenha.getText().toString().trim();
            String textoEmail = campoEmail.getText().toString().trim();

            //validar se os campos foram preenchidos
            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {

                    usuario = new Usuario("", textoEmail, textoSenha);

                    cadastrarUsuario();
                } else {
                    Toast.makeText(CadastroActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(CadastroActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void cadastrarUsuario() {
        FirebaseAuth autenticacao = ConfiguracaoFireBase.getFirebaseAutenticacao();

        autenticacao.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                startActivity(new Intent(this, ProfileActivity.class));
                finish();
            } else {
                String excecao;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthWeakPasswordException e) {
                    excecao = "Digite uma senha mais forte!";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    excecao = "Digite um email valido!";
                } catch (FirebaseAuthUserCollisionException e) {
                    excecao = "Essa conta jรก foi cadastrada!";
                } catch (Exception e) {
                    excecao = "Erro ao cadastrar o usuario!" + e.getMessage();
                    e.printStackTrace();
                }

                Toast.makeText(CadastroActivity.this, excecao, Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, IndexActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
