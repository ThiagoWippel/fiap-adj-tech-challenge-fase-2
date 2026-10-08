package br.com.fiap.restaurante.interfaceadapter.datasource;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Restaurante no formato trocado com a origem de dados, com o dono completo. O
 * tipo de cozinha vai como o nome da constante. As datas são preenchidas pela
 * origem de dados e ignoradas na gravação.
 */
public record DadosRestaurante(Long id, String nome, DadosEndereco endereco, String tipoCozinha,
                               List<DadosTurno> horarios, DadosUsuario dono, LocalDateTime dataCriacao,
                               LocalDateTime dataUltimaAlteracao) {
}
