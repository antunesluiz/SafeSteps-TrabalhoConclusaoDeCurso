package com.tcc.tccvioleta.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;

import com.tcc.tccvioleta.Classes.Contato;
import com.tcc.tccvioleta.Helpers.ConfiguracaoFireBase;
import com.tcc.tccvioleta.R;

import java.util.Objects;

public class AddContatoActivity extends AppCompatActivity {
    private static final int RESULT_PICK_CONTACT = 1001;

    private EditText tvContactName, tvContactNumber;

    private Contato contato;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contato);

        tvContactName = findViewById(R.id.nome_add_et);
        tvContactNumber = findViewById(R.id.numero_add_et);

        findViewById(R.id.button_addContato_ed).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contato.salvarDados(Objects.requireNonNull(ConfiguracaoFireBase.getFirebaseAutenticacao().getCurrentUser()).getUid());

                finish();
            }
        });
    }

    public void onClickEditText(View v) {

        Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RESULT_PICK_CONTACT:
                    Cursor cursor = null;

                    try {
                        String contactNumber = null;
                        String contactName = null;

                        Uri uri = null;
                        if (data != null) {
                            uri = data.getData();
                        }
                        if (uri != null) {
                            cursor = getContentResolver().query(uri, null, null, null, null);
                        }
                        if (cursor != null) {
                            cursor.moveToFirst();
                        }

                        int phoneIndex = 0;
                        if (cursor != null) {
                            phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        }
                        int nameIndex = 0;
                        if (cursor != null) {
                            nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        }

                        contactNumber = cursor.getString(phoneIndex);
                        contactName = cursor.getString(nameIndex);


                        contato = new Contato(contactName, contactNumber);

                        tvContactNumber.setText(contactNumber);
                        tvContactName.setText(contactName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }
}
