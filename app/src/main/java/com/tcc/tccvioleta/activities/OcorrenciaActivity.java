package com.tcc.tccvioleta.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.tcc.tccvioleta.Classes.Ocorrencia;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class OcorrenciaActivity extends AppCompatActivity {
    Calendar myCalendar = Calendar.getInstance();
    DatePickerDialog.OnDateSetListener date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocorrencia);

        EditText nome = findViewById(R.id.endereco_editText);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {
            String t = (String) bundle.get("Endereco");
            String t2 = "";

            for (int i = 0; i < Objects.requireNonNull(t).length(); i++) {
                if (t.charAt(i) != '.') {
                    t2 += t.charAt(i);
                }
            }

            nome.setText(t2);
        }

        date = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabelCalendario();
        };

        EditText editText_data = findViewById(R.id.dataOcorrencia);
        editText_data.performClick();
        editText_data.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                new DatePickerDialog(OcorrenciaActivity.this, date, myCalendar.get(Calendar.YEAR), myCalendar.get(Calendar.MONTH), myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        EditText editText_time = findViewById(R.id.timeOcorrencia);
        editText_time.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(OcorrenciaActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @SuppressLint("DefaultLocale")
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                    EditText editText_time1 = findViewById(R.id.timeOcorrencia);
                    editText_time1.setText(String.format("%02d:%02d", hourOfDay, minutes));
                }
            }, currentHour, currentMinute, true);

            timePickerDialog.show();
        });
    }

    private void updateLabelCalendario() {
        EditText editText_data = findViewById(R.id.dataOcorrencia);

        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, new Locale("pt", "BR"));

        editText_data.setText(sdf.format(myCalendar.getTime()));
    }

    public void submit(View v) {
        RadioGroup rg = findViewById(R.id.ocorrencia);
        EditText editText_data = findViewById(R.id.dataOcorrencia);
        EditText editText_time = findViewById(R.id.timeOcorrencia);
        EditText editText_endereco = findViewById(R.id.endereco_editText);

        if (rg.getCheckedRadioButtonId() == -1) {
            msg();
            rg.requestFocus();
        } else {
            if (editText_data.getText().toString().equals("DD/MM/AAAA")) {
                msg();
                editText_data.requestFocus();
            } else {
                if (editText_time.getText().toString().equals("00:00")) {
                    msg();
                    editText_time.requestFocus();
                } else {
                    if (editText_endereco.getText().toString().equals("")) {
                        msg();
                        editText_endereco.requestFocus();
                    } else {
                        int radioButtonId = rg.getCheckedRadioButtonId();
                        View radioButton = rg.findViewById(radioButtonId);
                        int idx = rg.indexOfChild(radioButton);

                        RadioButton rb = (RadioButton) rg.getChildAt(idx);
                        String selectedText = rb.getText().toString();

                        Ocorrencia ocorrencia = new Ocorrencia(editText_endereco.getText().toString(), editText_time.getText().toString(), editText_data.getText().toString(), selectedText);

                        FirebaseAuth firebaseAuth = ConfiguracaoFireBase.getFirebaseAutenticacao();
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("Usuarios");
                        if (user != null) {
                            myRef.child(user.getUid()).child("Ocorrencias").push().setValue(ocorrencia);
                        }

                        finishAndRemoveTask();
                    }
                }
            }
        }
    }

    private void msg() {
        new AlertDialog.Builder(this).setTitle("Alerta").setMessage("Preencha todos os dados corretamentes!").setNeutralButton("OK", null).show();
    }
}
