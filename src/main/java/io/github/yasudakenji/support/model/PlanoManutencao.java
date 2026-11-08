package io.github.yasudakenji.support.model;

import io.github.yasudakenji.support.model.enums.SituacaoHistoricoInicial;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "plano_manutencao")

public class PlanoManutencao {

    @Id
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    @Column(name = "nome_plano")
    private String nomePlano;

    @Column(name = "intervalo_horas")
    private BigDecimal intervaloHoras;

    @Column(name = "descricao")
    private String descricao;

    @Enumerated(EnumType.STRING)
    private SituacaoHistoricoInicial situacaoHistoricoInicial;

}
