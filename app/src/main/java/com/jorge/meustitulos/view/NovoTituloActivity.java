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

public class NovoTituloActivity extends AppCompatActivity {

    EditText tituloEditText, notaEditText, comentarioEditText;
    Spinner spinnerTipo, spinnerGenero, spinnerStatus;
    Button btnAdicionar, btnSalvarCapaGaleria; // Removido btnSelecionarImagem
    ImageView imageViewPreviewCapa; // Agora é clicável

    private Uri selectedImageUri;
    private int currentUserId;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_novotitulo);

        currentUserId = getIntent().getIntExtra("userId", -1);
        if (currentUserId == -1) {
            Toast.makeText(this, "Erro: ID do usuário não fornecido.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        tituloEditText = findViewById(R.id.titulo);
        notaEditText = findViewById(R.id.nota);
        comentarioEditText = findViewById(R.id.comentario);
        btnAdicionar = findViewById(R.id.btn_adicionar_titulo);

        spinnerTipo = findViewById(R.id.spinner_tipo);
        spinnerGenero = findViewById(R.id.spinner_genero);
        spinnerStatus = findViewById(R.id.spinner_status);

        imageViewPreviewCapa = findViewById(R.id.imageView_preview_capa); // Referência à ImageView
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

        // Adicionado OnClickListener diretamente na ImageView
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
                        String filename = "capa_titulo_" + System.currentTimeMillis() + ".jpg";
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


        btnAdicionar.setOnClickListener(v -> {
            String selectedTipo = spinnerTipo.getSelectedItem().toString();
            String selectedGenero = spinnerGenero.getSelectedItem().toString();
            String selectedStatus = spinnerStatus.getSelectedItem().toString();

            if (tituloEditText.getText().toString().isEmpty() ||
                    selectedTipo.equals("Selecione um Tipo") ||
                    selectedGenero.equals("Selecione um Gênero") ||
                    selectedStatus.equals("Selecione um Status") ||
                    notaEditText.getText().toString().isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios e selecione as opções.", Toast.LENGTH_SHORT).show();
                return;
            }

            double notaValue;
            try {
                notaValue = Double.parseDouble(notaEditText.getText().toString().replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Por favor, insira uma nota válida (número).", Toast.LENGTH_SHORT).show();
                return;
            }

            if (notaValue < 0 || notaValue > 10) {
                Toast.makeText(this, "A nota deve ser entre 0 e 10.", Toast.LENGTH_SHORT).show();
                return;
            }

            Titulo novo = new Titulo();
            novo.setTitulo(tituloEditText.getText().toString());
            novo.setTipo(selectedTipo);
            novo.setGenero(selectedGenero);
            novo.setNota(notaValue);
            novo.setStatus(selectedStatus);
            novo.setComentario(comentarioEditText.getText().toString());
            novo.setUserId(currentUserId);

            if (selectedImageUri != null) {
                novo.setImagemUri(selectedImageUri.toString());
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                novo.setImagemUri("");
            }

            TitulosController controller = new TitulosController(this);
            long result = controller.salvarTitulo(novo);

            if (result > -1) {
                Toast.makeText(this, "Título salvo com sucesso!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erro ao salvar título.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }
}
