package br.com.fiap.restaurante.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

/**
 * Linha da tabela horario_funcionamento: um turno de um restaurante.
 */
@Entity
@Table(name = "horario_funcionamento")
@Getter
@Setter
@NoArgsConstructor
public class HorarioFuncionamentoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurante_id", nullable = false)
    private RestauranteEntity restaurante;

    @Column(name = "dia_semana", nullable = false, length = 10)
    private String diaSemana;

    @Column(nullable = false)
    private LocalTime abertura;

    @Column(nullable = false)
    private LocalTime fechamento;
}
