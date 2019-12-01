package com.tcc.tccvioleta;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.tcc.tccvioleta.activities.CadastroActivity;
import com.tcc.tccvioleta.activities.LoginActivity;
import com.tcc.tccvioleta.activities.MainActivity;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;

public class IndexActivity extends AppCompatActivity {
    FirebaseAuth autenticacao;
    ImageView bgapp,clover;
    LinearLayout texthome;
    Animation bganim,frombottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);
    }

    @Override
    protected void onStart() {
        super.onStart();

        verificaUsuarioLogado();
    }

    public void btnCadastrar(View view){

        bgapp=(ImageView) findViewById(R.id.bgapp);
        clover=(ImageView) findViewById(R.id.imageView2);


        bgapp.animate().translationY(-1700).setDuration(800).setStartDelay(300);
        clover.animate().alpha(0).setDuration(800).setStartDelay(600);


        startActivity(new Intent(this, CadastroActivity.class));
        finish();
    }


    public void btnEntrar(View view){
        startActivity(new Intent(this, LoginActivity.class));
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void abrirTelaPrincipal(){
        startActivity(new Intent(this, MainActivity.class));
    }

    public void verificaUsuarioLogado(){
        autenticacao = ConfiguracaoFireBase.getFirebaseAutenticacao();

        if(autenticacao.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }
}
