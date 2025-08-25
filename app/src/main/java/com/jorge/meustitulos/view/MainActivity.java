package com.jorge.meustitulos.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jorge.meustitulos.R;
import com.jorge.meustitulos.controller.TitulosController;
import com.jorge.meustitulos.model.Titulo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TituloAdapter.OnItemClickListener {

    private RecyclerView recyclerViewTitulos;
    private TituloAdapter tituloAdapter;
    private List<Titulo> titulosList;
    private FloatingActionButton fabAddTitulo;
    private FloatingActionButton fabAPerfilUsuario;
    private TitulosController titulosController;
    private int currentUserId; // Variável para armazenar o ID do usuário logado

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        titulosController = new TitulosController(this);

        // Obter o ID do usuário logado
        currentUserId = getLoggedInUserId();
        if (currentUserId == -1) {
            Toast.makeText(this, "Erro: ID do usuário não encontrado. Faça login novamente.", Toast.LENGTH_LONG).show();
            // Redirecionar para a tela de login se o ID do usuário não for encontrado
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return; // Interrompe a execução do onCreate
        }

        recyclerViewTitulos = findViewById(R.id.recyclerViewTitulos);
        recyclerViewTitulos.setLayoutManager(new LinearLayoutManager(this));

        titulosList = new ArrayList<>();
        tituloAdapter = new TituloAdapter(titulosList);
        tituloAdapter.setOnItemClickListener(this);
        recyclerViewTitulos.setAdapter(tituloAdapter);

        fabAddTitulo = findViewById(R.id.fabAddTitulo);
        fabAddTitulo.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, NovoTituloActivity.class);
            intent.putExtra("userId", currentUserId); // Passa o userId para NovoTituloActivity
            startActivity(intent);
        });

        fabAPerfilUsuario = findViewById(R.id.fabAPerfilUsuario);
        fabAPerfilUsuario.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, PerfilUsuarioActivity.class);
            startActivity(intent);
        });

        carregarTitulos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarTitulos();
    }

    private void carregarTitulos() {
        // Buscar apenas os títulos do usuário logado
        List<Titulo> novosTitulos = titulosController.buscarTitulosDoUsuario(currentUserId);

        if (novosTitulos != null) {
            tituloAdapter.setTitulos(novosTitulos);
            titulosList.clear();
            titulosList.addAll(novosTitulos);

            if (novosTitulos.isEmpty()) {
                Toast.makeText(this, "Nenhum título encontrado. Adicione um novo!", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Títulos carregados: " + novosTitulos.size(), Toast.LENGTH_SHORT).show();
            }
        } else {
            tituloAdapter.setTitulos(new ArrayList<>());
            titulosList.clear();
            Toast.makeText(this, "Erro ao carregar títulos. Nenhuma lista retornada.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(int position) {
        if (position != RecyclerView.NO_POSITION && position < titulosList.size()) {
            Titulo tituloSelecionado = titulosList.get(position);
            Intent intent = new Intent(MainActivity.this, EditarTituloActivity.class);
            intent.putExtra("titulo_id", tituloSelecionado.getId());
            intent.putExtra("userId", currentUserId);
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(int position) {
        if (position != RecyclerView.NO_POSITION && position < titulosList.size()) {
            Titulo tituloParaRemover = titulosList.get(position);

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar Exclusão")
                    .setMessage("Deseja realmente remover o título: \"" + tituloParaRemover.getTitulo() + "\"?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        boolean removidoDB = titulosController.removerTitulo(tituloParaRemover.getId(), currentUserId);

                        if (removidoDB) {
                            tituloAdapter.removerItem(position);
                            titulosList.remove(position);
                            Toast.makeText(MainActivity.this, "Título \"" + tituloParaRemover.getTitulo() + "\" removido!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Falha ao remover título do banco de dados.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }
    }

    private void showEditStatusDialog(Titulo titulo, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_status, null);
        builder.setView(dialogView);

        final Spinner spinnerStatusDialog = dialogView.findViewById(R.id.spinner_status_dialog);

        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(this,
                R.array.status_array, R.layout.custom_spinner_item);
        adapterStatus.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerStatusDialog.setAdapter(adapterStatus);

        int currentStatusPosition = adapterStatus.getPosition(titulo.getStatus());
        if (currentStatusPosition != -1) {
            spinnerStatusDialog.setSelection(currentStatusPosition);
        }

        builder.setTitle("Editar Status de: " + titulo.getTitulo())
                .setPositiveButton("Salvar", (dialog, id) -> {
                    String novoStatus = spinnerStatusDialog.getSelectedItem().toString();
                    if (novoStatus.equals("Selecione um Status")) {
                        Toast.makeText(this, "Por favor, selecione um status válido.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean atualizadoDB = titulosController.atualizarTituloStatus(titulo.getId(), novoStatus, currentUserId);

                    if (atualizadoDB) {
                        titulo.setStatus(novoStatus);
                        tituloAdapter.notifyItemChanged(position);
                        Toast.makeText(this, "Status de \"" + titulo.getTitulo() + "\" atualizado para: " + novoStatus, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Falha ao atualizar status no banco de dados.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Método para obter o ID do usuário logado das SharedPreferences
    private int getLoggedInUserId() {
        SharedPreferences settings = getSharedPreferences(LoginActivity.PREFS_NAME, Context.MODE_PRIVATE);
        // Retorna -1 se a chave não for encontrada, indicando que o usuário não está logado
        return settings.getInt("loggedInUserId", -1);
    }
}