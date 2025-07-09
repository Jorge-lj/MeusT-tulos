package com.jorge.meustitulos.view;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jorge.meustitulos.R;
import com.jorge.meustitulos.controller.UsuarioController;
import com.jorge.meustitulos.model.Usuario;

public class CadastroActivity extends AppCompatActivity {

    EditText nomeCompletoEditText, emailEditText, nomeUsuarioEditText, senhaEditText, confirmarSenhaEditText;
    Button btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        nomeCompletoEditText = findViewById(R.id.nome_completo_cadastro);
        emailEditText = findViewById(R.id.email_cadastro);
        nomeUsuarioEditText = findViewById(R.id.nome_usuario_cadastro);
        senhaEditText = findViewById(R.id.senha_cadastro);
        confirmarSenhaEditText = findViewById(R.id.confirmar_senha_cadastro);
        btnCadastrar = findViewById(R.id.btn_cadastrar);

        btnCadastrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nomeCompleto = nomeCompletoEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String nomeUsuario = nomeUsuarioEditText.getText().toString();
                String senha = senhaEditText.getText().toString();
                String confirmarSenha = confirmarSenhaEditText.getText().toString();

                if (nomeCompleto.isEmpty() || email.isEmpty() || nomeUsuario.isEmpty() || senha.isEmpty() || confirmarSenha.isEmpty()) {
                    Toast.makeText(CadastroActivity.this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!senha.equals(confirmarSenha)) {
                    Toast.makeText(CadastroActivity.this, "As senhas não coincidem.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Usuario novoUsuario = new Usuario();
                novoUsuario.setNomeCompleto(nomeCompleto);
                novoUsuario.setEmail(email);
                novoUsuario.setNomeUsuario(nomeUsuario);
                novoUsuario.setSenha(senha);

                UsuarioController controller = new UsuarioController(CadastroActivity.this);
                long result = controller.salvarUsuario(novoUsuario);

                if (result > -1) {
                    Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(CadastroActivity.this, "Erro ao cadastrar usuário.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
