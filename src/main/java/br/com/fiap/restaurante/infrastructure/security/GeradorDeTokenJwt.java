package br.com.fiap.restaurante.infrastructure.security;

import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.application.port.ITokenGenerator;
import br.com.fiap.restaurante.domain.entity.Usuario;
import br.com.fiap.restaurante.infrastructure.config.TokenProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Emite um JWT assinado com HMAC-SHA256 (biblioteca nimbus-jose-jwt). O token
 * leva o id do usuário no {@code sub} e o código do tipo. Nenhum endpoint o
 * exige nesta fase.
 */
@Component
public class GeradorDeTokenJwt implements ITokenGenerator {

    private final JWSSigner assinador;
    private final Duration validade;

    public GeradorDeTokenJwt(TokenProperties propriedades) {
        try {
            this.assinador = new MACSigner(propriedades.chave().getBytes(StandardCharsets.UTF_8));
        } catch (JOSEException e) {
            throw new IllegalStateException("A chave do token é curta demais para o HS256.", e);
        }
        this.validade = propriedades.validade();
    }

    @Override
    public TokenDeAcesso gerar(Usuario usuario) {
        // O JWT guarda as datas em segundos inteiros
        Instant agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiracao = agora.plus(validade);
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject(String.valueOf(usuario.getId()))
                .claim("tipo", usuario.getTipo().getCodigo())
                .issueTime(Date.from(agora))
                .expirationTime(Date.from(expiracao))
                .build();

        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        try {
            jwt.sign(assinador);
        } catch (JOSEException e) {
            throw new IllegalStateException("Não foi possível assinar o token.", e);
        }
        return new TokenDeAcesso(jwt.serialize(), LocalDateTime.ofInstant(expiracao, ZoneId.systemDefault()));
    }
}
