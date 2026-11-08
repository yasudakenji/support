package io.github.yasudakenji.support.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "manutencao")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column (name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "plano_manutencao_id")
    private PlanoManutencao planoManutencao;

    @Column(name = "data_manutencao")
    private LocalDate dataManutencao;

    @Column (name = "horimetro_execucao")
    private BigDecimal horimetroNaExecucao;

    @Column(name = "descricao_execucao", length = 1000)
    private String descricaoExecucao;
}
