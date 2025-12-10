package com.example.jwtchallenge;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class DictionaryController {

    private static final String CSV_PATH = "users.csv";
    private static final String OUTPUT_FILENAME = "dictionary.txt";
    private static final int TOTAL_ENTRIES = 5000;
    private static final int PASSWORD_LENGTH = 16;
    private static final String BASE64_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private final SecureRandom random = new SecureRandom();

    @GetMapping("/dictionary")
    public ResponseEntity<Resource> downloadDictionary() throws IOException {

        File file = new File(OUTPUT_FILENAME);

        // 1. Lógica Lazy: Si no existe, lo generamos en ese momento
        if (!file.exists()) {
            System.out.println("El diccionario no existe. Generando uno nuevo...");
            generateDictionaryFile(file);
        }

        // 2. Preparamos el recurso para la descarga
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        // 3. Devolvemos la respuesta con headers de descarga
        return ResponseEntity.ok()
                // Header clave para forzar la descarga con nombre
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate")
                .contentLength(file.length())
                .contentType(MediaType.TEXT_PLAIN)
                .body(resource);
    }

    /**
     * Lógica encapsulada para crear el archivo mezclando datos reales y falsos
     */
    private void generateDictionaryFile(File targetFile) throws IOException {
        List<String> passwords = new ArrayList<>();

        // A. Leer reales
        ClassPathResource csvResource = new ClassPathResource(CSV_PATH);
        if (csvResource.exists()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        passwords.add(parts[1].trim());
                    }
                }
            }
        } else {
            System.err.println("ADVERTENCIA: No se encontró users.csv, generando solo datos falsos.");
        }

        // B. Rellenar con falsos
        int decoysNeeded = TOTAL_ENTRIES - passwords.size();
        for (int i = 0; i < decoysNeeded; i++) {
            passwords.add(generateFakeBase64(PASSWORD_LENGTH));
        }

        // C. Mezclar
        Collections.shuffle(passwords);

        // D. Escribir al disco
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
            for (String pwd : passwords) {
                writer.write(pwd);
                writer.newLine();
            }
        }
    }

    private String generateFakeBase64(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(BASE64_CHARS.length());
            sb.append(BASE64_CHARS.charAt(randomIndex));
        }
        return sb.toString();
    }
}