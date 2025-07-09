package com.jorge.meustitulos.util;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImagemSalva {

    // Salva um Bitmap na galeria do Android
    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap, String filename) {
        // Verifica se o nome do arquivo foi fornecido; caso contrário, gera um nome padrão
        if (filename == null || filename.isEmpty()) {
            filename = System.currentTimeMillis() + ".jpg";
        }

        // Prepara os valores para o MediaStore
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

        // Define o caminho relativo para Android 10 (API 29) e superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "MeusTitulos");
            values.put(MediaStore.Images.Media.IS_PENDING, 1); // Marca como pendente até o salvamento ser concluído
        } else {
            // Para versões anteriores, usa o diretório público de imagens
            File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MeusTitulos");
            if (!directory.exists()) {
                directory.mkdirs(); // Cria o diretório se não existir
            }
            File imageFile = new File(directory, filename);
            values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (uri != null) {
            try (OutputStream os = resolver.openOutputStream(uri)) {
                if (os != null) {
                    // Comprime o bitmap para JPEG e escreve no OutputStream
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, os);
                    os.flush();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear();
                        values.put(MediaStore.Images.Media.IS_PENDING, 0); // Remove a marca de pendente
                        resolver.update(uri, values, null, null); // Atualiza o registro no MediaStore
                    }
                    Toast.makeText(context, "Imagem salva na galeria!", Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (IOException e) {
                Toast.makeText(context, "Erro ao salvar imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        } else {
            Toast.makeText(context, "Erro: Não foi possível criar URI para salvar imagem.", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    // Verifica e solicita as permissões de armazenamento necessárias
    public static void checkAndRequestPermissions(Context context, ActivityResultLauncher<String> requestPermissionLauncher) {
        String permission;
        // Determina a permissão correta com base na versão do Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES; // Para Android 13+
        } else {
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE; // Para Android 12 e anteriores
        }

        // Verifica se a permissão já foi concedida
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Permissão de armazenamento já concedida.", Toast.LENGTH_SHORT).show();
        } else {
            // Solicita a permissão em tempo de execução
            requestPermissionLauncher.launch(permission);
        }
    }
}