package io.github.yasudakenji.support.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table (name = "equipamento")
public class Equipamento {

    @Id
    @Column(name = "id")
    @GeneratedValue (strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "nome")
    private String nome;

    @Column(name = "modelo")
    private String modelo;

    @Column(name = "data_cadastro")
    private LocalDate dataCadastro;

    @Column(name = "horimetro_atual")
    private BigDecimal horimetroAtual;

    @ManyToOne
    @JoinColumn(name = "setor_id")
    private Setor setor;

}
