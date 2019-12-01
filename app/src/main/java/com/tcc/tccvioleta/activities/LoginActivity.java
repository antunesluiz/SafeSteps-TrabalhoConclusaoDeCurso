package com.tcc.tccvioleta.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.tcc.tccvioleta.Classes.Usuario;
import com.tcc.tccvioleta.IndexActivity;
import com.tcc.tccvioleta.R;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private EditText campoEmail, campoSenha;

    private Usuario usuario;

    FirebaseAuth autenticacao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        autenticacao = FirebaseAuth.getInstance();

        campoEmail = findViewById(R.id.edtEmail1);
        campoSenha = findViewById(R.id.edtSenha1);

        Button btnLogin = findViewById(R.id.btnLogar);
        btnLogin.setOnClickListener(view -> {

            String textoSenha = campoSenha.getText().toString();
            String textoEmail = campoEmail.getText().toString();

            if (!textoEmail.isEmpty()) {
                if (!textoSenha.isEmpty()) {

                    usuario = new Usuario();
                    usuario.setEmail(textoEmail);
                    usuario.setSenha(textoSenha);

                    validarLogin();

                } else {
                    Toast.makeText(LoginActivity.this, "Preencha a senha!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Preencha o email!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void validarLogin() {


        autenticacao.signInWithEmailAndPassword(usuario.getEmail(), usuario.getSenha()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                abrirTelaPrincipal();
            } else {
                String excecao;
                try {
                    throw Objects.requireNonNull(task.getException());
                } catch (FirebaseAuthInvalidUserException e) {
                    excecao = "com.tcc.tccvioleta.Classes.Usuario não está cadastrado!";
                } catch (FirebaseAuthInvalidCredentialsException e) {
                    excecao = "Email e senha não correspodem a um usuario cadastrado";
                } catch (Exception e) {
                    excecao = "Erro ao cadastrar o usuário!" + e.getMessage();
                    e.printStackTrace();
                }
                Toast.makeText(LoginActivity.this, excecao, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirTelaPrincipal() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (autenticacao.getCurrentUser() != null) {
            System.out.println(autenticacao.getCurrentUser().getEmail());
            abrirTelaPrincipal();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        startActivity(new Intent(this, IndexActivity.class));
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
    }
}
