package com.example.jwtchallenge;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@SpringBootApplication
@RestController
public class JwtChallengeApplication {

    private SecretKey secretKey;
    private Map<String, String> userPasswordMap = new HashMap<>();
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JwtChallengeApplication(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static void main(String[] args) {
        SpringApplication.run(JwtChallengeApplication.class, args);
    }

    @PostConstruct
    public void init() throws Exception {
        secretKey = Keys.hmacShaKeyFor("*mySuperSecretKeyForJWTs1234567890!".getBytes(StandardCharsets.UTF_8));

        BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource("users.csv").getInputStream(), StandardCharsets.UTF_8));
        reader.lines().forEach(line -> {
            String[] parts = line.split(",");
            if (parts.length == 2) {
                userPasswordMap.put(parts[0].trim(), parts[1].trim());
            }
        });
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (userPasswordMap.containsKey(username) && userPasswordMap.get(username).equals(password)) {
            Map<String, Object> claims = new HashMap<>();
            claims.put("user", username);
            claims.put("role", "user");
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .signWith(secretKey, SignatureAlgorithm.HS256)
                    .compact();
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(403).body(Map.of("error", "Credenciales Inválidas"));
        }
    }

    @GetMapping("/exam/part1")
    public ResponseEntity<?> examPart1(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        String role = extractRole(authHeader);
        if (role == null || !role.equals("admin")) {
            return ResponseEntity.status(403).body(Map.of("error", "Se requieren privilegios de admin"));
        }
        return ResponseEntity.ok(Map.of("passwordExamen", "Si_Se_Pudo!"));
    }

    @GetMapping("/search-student")
    public ResponseEntity<?> searchStudent(@RequestParam String name) {
        // vulnerable query (SQL injection)
        String sql = "SELECT name FROM students WHERE name LIKE '" + name + "%'";
        try {
            if(name!=null && name.length()>0) {
                List<String> results = jdbcTemplate.queryForList(sql, String.class);
                return ResponseEntity.ok(Map.of("results", results));
            }else{
                return ResponseEntity.status(500).body(Map.of("error", "null"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private String extractRole(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        String token = authHeader.replace("Bearer ", "");
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);
            return claimsJws.getBody().get("role", String.class);
        } catch (JwtException e) {
            return null;
        }
    }
} 
