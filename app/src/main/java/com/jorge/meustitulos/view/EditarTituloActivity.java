package com.jorge.meustitulos.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jorge.meustitulos.R;
import com.jorge.meustitulos.controller.TitulosController;
import com.jorge.meustitulos.model.Titulo;
import com.jorge.meustitulos.util.ImagemSalva;

import java.io.FileDescriptor;
import java.io.IOException;

public class EditarTituloActivity extends AppCompatActivity {

    private EditText tituloEditText, notaEditText, comentarioEditText;
    private Spinner spinnerTipo, spinnerGenero, spinnerStatus;
    private Button btnSalvarAlteracoes, btnSalvarCapaGaleria;
    private ImageView imageViewPreviewCapa;

    private TitulosController titulosController;
    private Titulo tituloAtual;
    private Uri selectedImageUri;
    private int currentUserId;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editartitulo);

        titulosController = new TitulosController(this);

        currentUserId = getIntent().getIntExtra("userId", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Erro: ID do usuário não fornecido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tituloEditText = findViewById(R.id.titulo);
        notaEditText = findViewById(R.id.nota);
        comentarioEditText = findViewById(R.id.comentario);
        btnSalvarAlteracoes = findViewById(R.id.btn_salvar_alteracoes);
        spinnerTipo = findViewById(R.id.spinner_tipo);
        spinnerGenero = findViewById(R.id.spinner_genero);
        spinnerStatus = findViewById(R.id.spinner_status);
        imageViewPreviewCapa = findViewById(R.id.imageView_preview_capa);
        btnSalvarCapaGaleria = findViewById(R.id.btn_salvar_capa_galeria);

        ArrayAdapter<CharSequence> adapterTipo = ArrayAdapter.createFromResource(this,
                R.array.tipos_titulos_array, R.layout.custom_spinner_item);
        adapterTipo.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapterTipo);

        ArrayAdapter<CharSequence> adapterGenero = ArrayAdapter.createFromResource(this,
                R.array.generos_array, R.layout.custom_spinner_item);
        adapterGenero.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerGenero.setAdapter(adapterGenero);

        ArrayAdapter<CharSequence> adapterStatus = ArrayAdapter.createFromResource(this,
                R.array.status_array, R.layout.custom_spinner_item);
        adapterStatus.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinnerStatus.setAdapter(adapterStatus);

        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openImagePicker();
                    } else {
                        Toast.makeText(this, "Permissão para acessar a galeria negada.", Toast.LENGTH_SHORT).show();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        imageViewPreviewCapa.setImageURI(selectedImageUri);
                        Toast.makeText(this, "Imagem selecionada!", Toast.LENGTH_SHORT).show();
                    } else {
                        selectedImageUri = null;
                        imageViewPreviewCapa.setImageResource(R.drawable.placeholder_capa);
                        Toast.makeText(this, "Nenhuma imagem selecionada.", Toast.LENGTH_SHORT).show();
                    }
                });

        imageViewPreviewCapa.setOnClickListener(v -> {
            String permission;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permission = Manifest.permission.READ_MEDIA_IMAGES;
            } else {
                permission = Manifest.permission.READ_EXTERNAL_STORAGE;
            }

            if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                openImagePicker();
            } else {
                requestPermissionLauncher.launch(permission);
            }
        });

        btnSalvarCapaGaleria.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                try {
                    ParcelFileDescriptor parcelFileDescriptor =
                            getContentResolver().openFileDescriptor(selectedImageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    Bitmap bitmapToSave = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();

                    if (bitmapToSave != null) {
                        String filename = "capa_titulo_editado_" + System.currentTimeMillis() + ".jpg";
                        ImagemSalva.saveBitmapToGallery(this, bitmapToSave, filename);
                    } else {
                        Toast.makeText(this, "Não foi possível carregar o bitmap da capa.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao processar imagem para salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nenhuma capa selecionada para salvar.", Toast.LENGTH_SHORT).show();
            }
        });


        int tituloId = getIntent().getIntExtra("titulo_id", -1);
        if (tituloId != -1) {
            tituloAtual = titulosController.buscarTituloPorId(tituloId);
            if (tituloAtual != null && tituloAtual.getUserId() == currentUserId) {
                preencherCampos(tituloAtual);
            } else {
                Toast.makeText(this, "Título não encontrado ou não pertence a este usuário.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "ID do título não fornecido.", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnSalvarAlteracoes.setOnClickListener(v -> {
            salvarAlteracoes();
        });
    }

    private void preencherCampos(Titulo titulo) {
        tituloEditText.setText(titulo.getTitulo());
        notaEditText.setText(String.valueOf(titulo.getNota()).replace(".", ","));
        comentarioEditText.setText(titulo.getComentario());

        ArrayAdapter<CharSequence> adapterTipo = (ArrayAdapter<CharSequence>) spinnerTipo.getAdapter();
        int tipoPosition = adapterTipo.getPosition(titulo.getTipo());
        if (tipoPosition != -1) spinnerTipo.setSelection(tipoPosition);

        ArrayAdapter<CharSequence> adapterGenero = (ArrayAdapter<CharSequence>) spinnerGenero.getAdapter();
        int generoPosition = adapterGenero.getPosition(titulo.getGenero());
        if (generoPosition != -1) spinnerGenero.setSelection(generoPosition);

        ArrayAdapter<CharSequence> adapterStatus = (ArrayAdapter<CharSequence>) spinnerStatus.getAdapter();
        int statusPosition = adapterStatus.getPosition(titulo.getStatus());
        if (statusPosition != -1) spinnerStatus.setSelection(statusPosition);

        if (titulo.getImagemUri() != null && !titulo.getImagemUri().isEmpty()) {
            selectedImageUri = Uri.parse(titulo.getImagemUri());
            imageViewPreviewCapa.setImageURI(selectedImageUri);
        } else {
            imageViewPreviewCapa.setImageResource(R.drawable.placeholder_capa);
        }
    }

    private void salvarAlteracoes() {
        String novoTituloStr = tituloEditText.getText().toString();
        String novoTipo = spinnerTipo.getSelectedItem().toString();
        String novoGenero = spinnerGenero.getSelectedItem().toString();
        String novaNotaStr = notaEditText.getText().toString();
        String novoStatus = spinnerStatus.getSelectedItem().toString();
        String novoComentario = comentarioEditText.getText().toString();

        if (novoTituloStr.isEmpty() ||
                novoTipo.equals("Selecione um Tipo") ||
                novoGenero.equals("Selecione um Gênero") ||
                novoStatus.equals("Selecione um Status") ||
                novaNotaStr.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios e selecione as opções.", Toast.LENGTH_SHORT).show();
            return;
        }

        double novaNota;
        try {
            novaNota = Double.parseDouble(novaNotaStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Por favor, insira uma nota válida (número).", Toast.LENGTH_SHORT).show();
            return;
        }

        if (novaNota < 0 || novaNota > 10) {
            Toast.makeText(this, "A nota deve ser entre 0 e 10.", Toast.LENGTH_SHORT).show();
            return;
        }

        tituloAtual.setTitulo(novoTituloStr);
        tituloAtual.setTipo(novoTipo);
        tituloAtual.setGenero(novoGenero);
        tituloAtual.setNota(novaNota);
        tituloAtual.setStatus(novoStatus);
        tituloAtual.setComentario(novoComentario);
        tituloAtual.setUserId(currentUserId);
        if (selectedImageUri != null) {
            tituloAtual.setImagemUri(selectedImageUri.toString());
            getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            tituloAtual.setImagemUri("");
        }

        boolean atualizado = titulosController.atualizarTitulo(tituloAtual);

        if (atualizado) {
            Toast.makeText(this, "Título atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar título.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }
}
