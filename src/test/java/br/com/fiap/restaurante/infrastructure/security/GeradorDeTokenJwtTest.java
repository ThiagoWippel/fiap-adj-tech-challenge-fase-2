package br.com.fiap.restaurante.infrastructure.security;

import br.com.fiap.restaurante.application.dto.TokenDeAcesso;
import br.com.fiap.restaurante.infrastructure.config.TokenProperties;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

import static br.com.fiap.restaurante.suporte.Exemplos.maria;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Gerador de token JWT")
class GeradorDeTokenJwtTest {

    private static final String CHAVE = "chave-de-teste-com-pelo-menos-32-caracteres";

    private final GeradorDeTokenJwt gerador = new GeradorDeTokenJwt(new TokenProperties(CHAVE, Duration.ofMinutes(45)));

    @Test
    @DisplayName("TOK-01 · o token é um JWT HS256 com o id no sub, o código do tipo e a validade da configuração")
    void deveGerarTokenAssinadoComOsDadosDoUsuario() throws Exception {
        /* act */
        TokenDeAcesso token = gerador.gerar(maria());

        /* assert */
        SignedJWT jwt = SignedJWT.parse(token.valor());
        JWTClaimsSet claims = jwt.getJWTClaimsSet();
        assertThat(jwt.getHeader().getAlgorithm()).isEqualTo(JWSAlgorithm.HS256);
        assertThat(jwt.verify(new MACVerifier(CHAVE.getBytes(StandardCharsets.UTF_8)))).isTrue();
        assertThat(claims.getSubject()).isEqualTo("7");
        assertThat(claims.getStringClaim("tipo")).isEqualTo("CLIENTE");
        assertThat(Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()))
                .isEqualTo(Duration.ofMinutes(45));
        assertThat(token.expiraEm())
                .isEqualTo(LocalDateTime.ofInstant(claims.getExpirationTime().toInstant(), ZoneId.systemDefault()));
    }

    @Test
    @DisplayName("TOK-02 · um token com o conteúdo adulterado não passa na verificação da assinatura")
    void deveRecusarTokenAdulterado() throws Exception {
        /* arrange */
        String[] partes = gerador.gerar(maria()).valor().split("\\.");
        String conteudo = new String(Base64.getUrlDecoder().decode(partes[1]), StandardCharsets.UTF_8);
        String conteudoAdulterado = conteudo.replace("\"sub\":\"7\"", "\"sub\":\"8\"");
        String adulterado = partes[0] + "."
                + Base64.getUrlEncoder().withoutPadding().encodeToString(conteudoAdulterado.getBytes(StandardCharsets.UTF_8))
                + "." + partes[2];

        /* act */
        boolean assinaturaConfere = SignedJWT.parse(adulterado).verify(new MACVerifier(CHAVE.getBytes(StandardCharsets.UTF_8)));

        /* assert */
        assertThat(conteudoAdulterado).isNotEqualTo(conteudo);
        assertThat(assinaturaConfere).isFalse();
    }

    @Test
    @DisplayName("TOK-02 · um token assinado com outra chave não passa na verificação")
    void deveRecusarTokenDeOutraChave() throws Exception {
        /* arrange */
        GeradorDeTokenJwt outro = new GeradorDeTokenJwt(
                new TokenProperties("outra-chave-qualquer-com-mais-de-32-caracteres", Duration.ofMinutes(45)));

        /* act */
        boolean assinaturaConfere = SignedJWT.parse(outro.gerar(maria()).valor())
                .verify(new MACVerifier(CHAVE.getBytes(StandardCharsets.UTF_8)));

        /* assert */
        assertThat(assinaturaConfere).isFalse();
    }
}
