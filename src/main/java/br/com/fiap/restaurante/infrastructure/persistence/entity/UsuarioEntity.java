package br.com.fiap.restaurante.infrastructure.persistence.entity;

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
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Linha da tabela usuario. Os campos pessoais aceitam nulo só por causa da
 * anonimização; para usuários ativos, uma regra CHECK no banco exige todos.
 */
@Entity
@Table(name = "usuario")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class UsuarioEntity {

    public static final String NOME_ANONIMIZADO = "Usuário removido";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 255)
    private String email;

    @Column(length = 50)
    private String login;

    @Column(length = 100)
    private String senha;

    @Column(length = 14)
    private String documento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_usuario_id")
    private TipoUsuarioEntity tipo;

    @Embedded
    private EnderecoEmbeddable endereco;

    @CreatedDate
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @LastModifiedDate
    @Column(name = "data_ultima_alteracao", nullable = false)
    private LocalDateTime dataUltimaAlteracao;

    @Column(name = "removido_em")
    private LocalDateTime removidoEm;

    /** Apaga os dados pessoais e marca a remoção. Id e datas ficam. */
    public void anonimizar(LocalDateTime momento) {
        nome = NOME_ANONIMIZADO;
        email = null;
        login = null;
        senha = null;
        documento = null;
        tipo = null;
        endereco = null;
        removidoEm = momento;
    }
}
