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

    //Salva um Bitmap na galeria do Android
    public static boolean saveBitmapToGallery(Context context, Bitmap bitmap, String filename) {
        if (filename == null || filename.isEmpty()) {
            filename = System.currentTimeMillis() + ".jpg";
        }

        ContentResolver resolver = context.getContentResolver();
        Uri uri = null;
        OutputStream os = null;

        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ → usa MediaStore com caminho relativo
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/MeusTitulos");
                values.put(MediaStore.Images.Media.IS_PENDING, 1);
            } else {
                // Android 9 ou menor → usa diretório público
                File directory = new File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                        "MeusTitulos"
                );
                if (!directory.exists()) directory.mkdirs();
                File imageFile = new File(directory, filename);
                values.put(MediaStore.Images.Media.DATA, imageFile.getAbsolutePath());
            }

            uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri == null) {
                Toast.makeText(context, "Erro: URI não criada", Toast.LENGTH_LONG).show();
                return false;
            }

            os = resolver.openOutputStream(uri);
            if (os == null) {
                Toast.makeText(context, "Erro: não foi possível abrir OutputStream", Toast.LENGTH_LONG).show();
                return false;
            }

            boolean saved = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();

            if (!saved) {
                Toast.makeText(context, "Erro ao comprimir imagem.", Toast.LENGTH_LONG).show();
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Finaliza salvamento removendo IS_PENDING
                ContentValues updateValues = new ContentValues();
                updateValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                resolver.update(uri, updateValues, null, null);
            } else {
                // Força a galeria a escanear a imagem
                context.sendBroadcast(
                        new android.content.Intent(
                                android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                                uri
                        )
                );
            }

            Toast.makeText(context,
                    "Imagem salva em Pictures/MeusTitulos!",
                    Toast.LENGTH_SHORT).show();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Erro ao salvar imagem: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
            return false;

        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    // Verifica e solicita as permissões necessárias para acessar imagens
    public static void checkAndRequestPermissions(Context context,
                                                  ActivityResultLauncher<String> requestPermissionLauncher) {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_IMAGES; // Android 13+
        } else {
            permission = Manifest.permission.WRITE_EXTERNAL_STORAGE; // Android <= 12
        }

        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context,
                    "Permissão de armazenamento já concedida.",
                    Toast.LENGTH_SHORT).show();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }
}
