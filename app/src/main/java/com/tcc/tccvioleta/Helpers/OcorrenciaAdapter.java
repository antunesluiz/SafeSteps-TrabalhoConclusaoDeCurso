package com.tcc.tccvioleta.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tcc.tccvioleta.Classes.Ocorrencia;
import com.tcc.tccvioleta.R;

import java.util.ArrayList;

public class OcorrenciaAdapter extends ArrayAdapter<Ocorrencia> {

    public OcorrenciaAdapter(Context context, ArrayList<Ocorrencia> listaOcorrencias) {
        super(context, 0, listaOcorrencias);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listOcorrenciaView = convertView;

        if (listOcorrenciaView == null) {
            listOcorrenciaView = LayoutInflater.from(getContext()).inflate(R.layout.item_ocorrencia, parent, false);
        }

        Ocorrencia ocorrenciaAtual = getItem(position);

        TextView enderecoTextView = listOcorrenciaView.findViewById(R.id.endereco);
        if (ocorrenciaAtual != null) {
            enderecoTextView.setText(ocorrenciaAtual.getEndereco());
        }

        TextView tipoTextView = listOcorrenciaView.findViewById(R.id.tipo);
        if (ocorrenciaAtual != null) {
            tipoTextView.setText(ocorrenciaAtual.getTipo());
        }

        return listOcorrenciaView;
    }
}
