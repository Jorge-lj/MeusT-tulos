package com.jorge.meustitulos.view;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.jorge.meustitulos.R;
import com.jorge.meustitulos.controller.UsuarioController;
import com.jorge.meustitulos.model.Usuario;
import com.jorge.meustitulos.util.ImagemSalva;

import java.io.FileDescriptor;
import java.io.IOException;

public class PerfilUsuarioActivity extends AppCompatActivity {

    private static final String TAG = "PerfilDebug";

    private EditText nomeCompletoEditText, emailEditText, nomeUsuarioEditText, senhaEditText;
    private Button btnSalvarAlteracoes, btnExcluirConta, btnSalvarFotoPerfilGaleria; // Removido btnSelecionarFotoPerfil
    private ImageView imageViewFotoPerfil; // Agora é clicável

    private UsuarioController usuariosController;
    private Usuario usuarioAtual;
    private Uri selectedImageUri;

    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_LOGGED_IN_USERNAME = "loggedInUsername";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN_USER_ID = "loggedInUserId";

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<String> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_usuario);

        usuariosController = new UsuarioController(this);

        nomeCompletoEditText = findViewById(R.id.nome_completo_perfil);
        emailEditText = findViewById(R.id.email_perfil);
        nomeUsuarioEditText = findViewById(R.id.nome_usuario_perfil);
        senhaEditText = findViewById(R.id.senha_perfil);
        btnSalvarAlteracoes = findViewById(R.id.btn_salvar_alteracoes_perfil);
        btnExcluirConta = findViewById(R.id.btn_excluir_conta);
        imageViewFotoPerfil = findViewById(R.id.imageView_foto_perfil); // Referência à ImageView
        btnSalvarFotoPerfilGaleria = findViewById(R.id.btn_salvar_foto_perfil_galeria);

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
                        imageViewFotoPerfil.setImageURI(selectedImageUri);
                        Toast.makeText(this, "Foto de perfil selecionada!", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Imagem selecionada: " + selectedImageUri.toString());
                    } else {
                        selectedImageUri = null;
                        imageViewFotoPerfil.setImageResource(R.drawable.placeholder_profile_picture);
                        Toast.makeText(this, "Nenhuma foto selecionada.", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "Nenhuma imagem selecionada.");
                    }
                });

        imageViewFotoPerfil.setOnClickListener(v -> {
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

        btnSalvarFotoPerfilGaleria.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                try {
                    ParcelFileDescriptor parcelFileDescriptor =
                            getContentResolver().openFileDescriptor(selectedImageUri, "r");
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                    Bitmap bitmapToSave = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();

                    if (bitmapToSave != null) {
                        String filename = "perfil_" + System.currentTimeMillis() + ".jpg";
                        ImagemSalva.saveBitmapToGallery(this, bitmapToSave, filename);
                    } else {
                        Toast.makeText(this, "Não foi possível carregar o bitmap da foto de perfil.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao processar imagem para salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Nenhuma foto de perfil selecionada para salvar.", Toast.LENGTH_SHORT).show();
            }
        });


        String loggedInUsername = getLoggedInUsername();
        Log.d(TAG, "onCreate: Nome de usuário logado recuperado: " + loggedInUsername);

        if (loggedInUsername != null && !loggedInUsername.isEmpty()) {
            usuarioAtual = usuariosController.buscarUsuarioPorNomeUsuario(loggedInUsername);
            if (usuarioAtual != null) {
                Log.d(TAG, "onCreate: Usuário atual encontrado. ID: " + usuarioAtual.getId() + ", Nome: " + usuarioAtual.getNomeUsuario());
                preencherCampos(usuarioAtual);
            } else {
                Log.e(TAG, "onCreate: Erro: Usuário logado não encontrado no banco de dados para o nome: " + loggedInUsername);
                Toast.makeText(this, "Erro: Usuário logado não encontrado.", Toast.LENGTH_LONG).show();
                clearLoginCredentialsAndRedirectToLogin();
            }
        } else {
            Log.e(TAG, "onCreate: Nenhum usuário logado encontrado nas SharedPreferences.");
            Toast.makeText(this, "Nenhum usuário logado.", Toast.LENGTH_LONG).show();
            clearLoginCredentialsAndRedirectToLogin();
        }

        btnSalvarAlteracoes.setOnClickListener(v -> {
            salvarAlteracoes();
        });

        btnExcluirConta.setOnClickListener(v -> {
            confirmarExclusaoConta();
        });
    }

    private String getLoggedInUsername() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return settings.getString(KEY_LOGGED_IN_USERNAME, null);
    }

    private void preencherCampos(Usuario usuario) {
        nomeCompletoEditText.setText(usuario.getNomeCompleto());
        emailEditText.setText(usuario.getEmail());
        nomeUsuarioEditText.setText(usuario.getNomeUsuario());
        senhaEditText.setText(""); // Não preenche a senha por segurança

        Log.d(TAG, "preencherCampos: Foto de perfil URI: " + usuario.getFotoPerfilUri());
        if (usuario.getFotoPerfilUri() != null && !usuario.getFotoPerfilUri().isEmpty()) {
            try {
                selectedImageUri = Uri.parse(usuario.getFotoPerfilUri());
                imageViewFotoPerfil.setImageURI(selectedImageUri);
            } catch (Exception e) {
                Log.e(TAG, "preencherCampos: Erro ao carregar URI da imagem: " + e.getMessage());
                imageViewFotoPerfil.setImageResource(R.drawable.placeholder_profile_picture);
            }
        } else {
            imageViewFotoPerfil.setImageResource(R.drawable.placeholder_profile_picture);
        }
    }

    private void salvarAlteracoes() {
        Log.d(TAG, "salvarAlteracoes: Iniciando processo de salvamento.");

        String novoNomeCompleto = nomeCompletoEditText.getText().toString();
        String novoEmail = emailEditText.getText().toString();
        String novoNomeUsuario = nomeUsuarioEditText.getText().toString();
        String novaSenha = senhaEditText.getText().toString();

        if (novoNomeCompleto.isEmpty() || novoEmail.isEmpty() || novoNomeUsuario.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos obrigatórios.", Toast.LENGTH_SHORT).show();
            Log.w(TAG, "salvarAlteracoes: Campos obrigatórios vazios.");
            return;
        }

        if (usuarioAtual == null) {
            Toast.makeText(this, "Erro: Usuário não carregado para atualização.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "salvarAlteracoes: usuarioAtual é null. Não é possível atualizar.");
            return;
        }

        usuarioAtual.setNomeCompleto(novoNomeCompleto);
        usuarioAtual.setEmail(novoEmail);
        usuarioAtual.setNomeUsuario(novoNomeUsuario);

        if (!novaSenha.isEmpty()) {
            usuarioAtual.setSenha(novaSenha);
            Log.d(TAG, "salvarAlteracoes: Senha será atualizada.");
        } else {
            Log.d(TAG, "salvarAlteracoes: Senha não será alterada (campo vazio).");
        }

        Log.d(TAG, "salvarAlteracoes: Nova URI da imagem: " + (selectedImageUri != null ? selectedImageUri.toString() : "null"));
        if (selectedImageUri != null) {
            usuarioAtual.setFotoPerfilUri(selectedImageUri.toString());
            try {
                getContentResolver().takePersistableUriPermission(selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Log.d(TAG, "salvarAlteracoes: Permissão da URI persistida com sucesso.");
            } catch (SecurityException e) {
                Log.e(TAG, "salvarAlteracoes: Falha ao persistir permissão da URI: " + e.getMessage());
                Toast.makeText(this, "Erro ao persistir permissão da imagem. Tente novamente.", Toast.LENGTH_LONG).show();
            }
        } else {
            usuarioAtual.setFotoPerfilUri("");
            Log.d(TAG, "salvarAlteracoes: URI da imagem definida como vazia.");
        }

        Log.d(TAG, "salvarAlteracoes: Tentando atualizar usuário no DB. ID: " + usuarioAtual.getId() + ", Nome Usuário: " + usuarioAtual.getNomeUsuario());
        boolean atualizado = usuariosController.atualizarUsuario(usuarioAtual);

        if (atualizado) {
            Toast.makeText(this, "Perfil atualizado com sucesso!", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "salvarAlteracoes: Perfil atualizado com sucesso no DB.");

            if (!usuarioAtual.getNomeUsuario().equals(getLoggedInUsername())) {
                saveLoggedInUsername(usuarioAtual.getNomeUsuario());
                Log.d(TAG, "salvarAlteracoes: Nome de usuário logado atualizado nas SharedPreferences.");
            }
            finish();
        } else {
            Toast.makeText(this, "Erro ao atualizar perfil.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "salvarAlteracoes: Falha ao atualizar perfil no DB.");
        }
    }

    private void confirmarExclusaoConta() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão de Conta")
                .setMessage("Tem certeza de que deseja excluir sua conta? Esta ação é irreversível e todos os seus títulos serão perdidos.")
                .setPositiveButton("Sim, Excluir", (dialog, which) -> {
                    excluirConta();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void excluirConta() {
        if (usuarioAtual != null) {
            Log.d(TAG, "excluirConta: Tentando remover usuário com ID: " + usuarioAtual.getId());
            boolean removido = usuariosController.removerUsuario(usuarioAtual.getId());
            if (removido) {
                Toast.makeText(this, "Conta excluída com sucesso!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "excluirConta: Conta removida do DB.");
                clearLoginCredentialsAndRedirectToLogin();
            } else {
                Toast.makeText(this, "Erro ao excluir conta.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "excluirConta: Falha ao remover conta do DB.");
            }
        } else {
            Log.e(TAG, "excluirConta: usuarioAtual é null. Não é possível excluir.");
        }
    }

    private void clearLoginCredentialsAndRedirectToLogin() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_LOGGED_IN_USERNAME);
        editor.remove(KEY_LOGGED_IN_USER_ID);
        editor.apply();
        Log.d(TAG, "clearLoginCredentialsAndRedirectToLogin: Credenciais de login limpas e redirecionando para LoginActivity.");

        Intent intent = new Intent(PerfilUsuarioActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void saveLoggedInUsername(String username) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_LOGGED_IN_USERNAME, username);
        editor.apply();
        Log.d(TAG, "saveLoggedInUsername: Nome de usuário logado salvo: " + username);
    }

    private void openImagePicker() {
        pickImageLauncher.launch("image/*");
    }
}
