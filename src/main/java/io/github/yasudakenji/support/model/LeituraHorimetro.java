package io.github.yasudakenji.support.model;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "leitura_horimetro")
public class LeituraHorimetro {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "equipamento_id")
    private Equipamento equipamento;

    @Column(name = "data_leitura")
    private LocalDateTime dataHoraLeitura;

    @Column(name = "valor_horimetro")
    private BigDecimal valorHorimetro;
}
