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

    @Override
    public String codificar(String senha) {
        return bcrypt.encode(senha);
    }

    @Override
    public boolean confere(String senha, String senhaCodificada) {
        return bcrypt.matches(senha, senhaCodificada);
    }
}
