package br.com.fiap.restaurante.infrastructure.security;

import br.com.fiap.restaurante.application.port.IPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementação da porta de senha com BCrypt (spring-security-crypto).
 */
@Component
public class CodificadorDeSenhaBCrypt implements IPasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    // Gerado com o mesmo custo dos hashes reais, para a comparação levar o mesmo tempo
    private final String hashDeDescarte = bcrypt.encode("hash-de-descarte");

    @Override
    public String codificar(String senha) {
        return bcrypt.encode(senha);
    }

    @Override
    public boolean confere(String senha, String senhaCodificada) {
        // Sem hash, o BCrypt devolveria false na hora, e a resposta rápida
        // denunciaria que o login não existe.
        if (senhaCodificada == null) {
            bcrypt.matches(senha, hashDeDescarte);
            return false;
        }
        return bcrypt.matches(senha, senhaCodificada);
    }
}
