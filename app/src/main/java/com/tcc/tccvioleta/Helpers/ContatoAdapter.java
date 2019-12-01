package com.tcc.tccvioleta.Helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.tcc.tccvioleta.Classes.Contato;
import com.tcc.tccvioleta.Classes.Ocorrencia;
import com.tcc.tccvioleta.R;

import java.util.ArrayList;

public class ContatoAdapter extends ArrayAdapter<Contato> {

    public ContatoAdapter(@NonNull Context context, ArrayList<Contato> listaContatos) {
        super(context, 0, listaContatos);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listContatoView = convertView;

        if (listContatoView == null) {
            listContatoView = LayoutInflater.from(getContext()).inflate(R.layout.item_contato, parent, false);
        }

        Contato contatoAtual = getItem(position);

        TextView nomeTextView = listContatoView.findViewById(R.id.nome_contato);
        if (contatoAtual != null) {
            nomeTextView.setText(contatoAtual.getNome());
        }

        TextView telefoneTextView = listContatoView.findViewById(R.id.telefone_contato);
        if (contatoAtual != null) {
            telefoneTextView.setText(contatoAtual.getTelefone());
        }

        return listContatoView;
    }
}
