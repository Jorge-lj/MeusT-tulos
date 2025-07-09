package com.jorge.meustitulos.view;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jorge.meustitulos.R;
import com.jorge.meustitulos.controller.UsuarioController;
import com.jorge.meustitulos.model.Usuario;

public class LoginActivity extends AppCompatActivity {

    EditText nomeUsuarioEditText, senhaEditText;
    Button btnLogin;
    TextView textCadastro, btnLimparLogin;

    public static final String PREFS_NAME = "LoginPrefs";

    static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_LOGGED_IN_USERNAME = "loggedInUsername";
    private static final String KEY_LOGGED_IN_USER_ID = "loggedInUserId";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        nomeUsuarioEditText = findViewById(R.id.nome_usuario_login);
        senhaEditText = findViewById(R.id.senha_login);
        btnLogin = findViewById(R.id.btn_login);
        textCadastro = findViewById(R.id.text_cadastro);
        btnLimparLogin = findViewById(R.id.btn_limpar_login);

        loadLoginCredentials();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeUsuario = nomeUsuarioEditText.getText().toString();
                String senha = senhaEditText.getText().toString();

                if (nomeUsuario.isEmpty() || senha.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                UsuarioController controller = new UsuarioController(LoginActivity.this);
                boolean autenticado = controller.autenticarUsuario(nomeUsuario, senha);

                if (autenticado) {
                    Toast.makeText(LoginActivity.this, "Login bem-sucedido!", Toast.LENGTH_SHORT).show();
                    saveLoginCredentials(nomeUsuario, senha);

                    // Buscar o objeto Usuario completo para obter o ID
                    Usuario usuarioLogado = controller.buscarUsuarioPorNomeUsuario(nomeUsuario);
                    if (usuarioLogado != null) {
                        saveLoggedInUsername(nomeUsuario);
                        saveLoggedInUserId(usuarioLogado.getId()); // Salva o ID do usuário logado
                    }

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Nome de usuário ou senha incorretos.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
                startActivity(intent);
            }
        });

        btnLimparLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearLoginCredentials();
                Toast.makeText(LoginActivity.this, "Dados de login limpos.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveLoginCredentials(String username, String password) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_PASSWORD, password);
        editor.apply();
    }

    private void loadLoginCredentials() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String savedUsername = settings.getString(KEY_USERNAME, "");
        String savedPassword = settings.getString(KEY_PASSWORD, "");

        nomeUsuarioEditText.setText(savedUsername);
        senhaEditText.setText(savedPassword);
    }

    private void clearLoginCredentials() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(KEY_USERNAME);
        editor.remove(KEY_PASSWORD);
        editor.remove(KEY_LOGGED_IN_USERNAME);
        editor.remove(KEY_LOGGED_IN_USER_ID); // Remove o ID do usuário também
        editor.apply();

        nomeUsuarioEditText.setText("");
        senhaEditText.setText("");
    }

    private void saveLoggedInUsername(String username) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(KEY_LOGGED_IN_USERNAME, username);
        editor.apply();
    }

    private void saveLoggedInUserId(int userId) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(KEY_LOGGED_IN_USER_ID, userId);
        editor.apply();
    }
}
