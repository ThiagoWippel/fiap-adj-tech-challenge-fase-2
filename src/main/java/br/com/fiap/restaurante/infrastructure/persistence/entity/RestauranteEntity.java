package br.com.fiap.restaurante.infrastructure.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Linha da tabela restaurante. O tipo de cozinha fica como texto: mapeado como
 * enum, o Hibernate esperaria uma coluna ENUM do MySQL.
 */
@Entity
@Table(name = "restaurante")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class RestauranteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(name = "tipo_cozinha", nullable = false, length = 30)
    private String tipoCozinha;

    @Embedded
    private EnderecoEmbeddable endereco;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dono_id", nullable = false)
    private UsuarioEntity dono;

    // Numa listagem, os turnos de todos os restaurantes da página vêm numa consulta só
    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 50)
    @Setter(AccessLevel.NONE)
    private List<HorarioFuncionamentoEntity> horarios = new ArrayList<>();

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_ultima_alteracao", nullable = false)
    private LocalDateTime dataUltimaAlteracao;

    @Column(name = "removido_em")
    private LocalDateTime removidoEm;

    /**
     * Troca todos os turnos. A lista é a mesma instância: o orphanRemoval apaga do
     * banco os turnos que saíram.
     */
    public void substituirHorarios(List<HorarioFuncionamentoEntity> novos) {
        horarios.clear();
        novos.forEach(horario -> {
            horario.setRestaurante(this);
            horarios.add(horario);
        });
    }
}
