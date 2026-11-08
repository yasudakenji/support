package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.PlanoManutencaoDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.PlanoManutencao;
import io.github.yasudakenji.support.model.enums.StatusRevisao;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.ManutencaoRepository;
import io.github.yasudakenji.support.repository.PlanoManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanoManutencaoServiceImplTest {

    @Mock
    private PlanoManutencaoRepository repository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private ManutencaoRepository manutencaoRepository;

    @Mock
    private ManutencaoRepository.UltimaRevisao ultimaRevisao;

    @InjectMocks
    private PlanoManutencaoServiceImpl service;

    private final UUID planoId = UUID.randomUUID();
    private final Equipamento equipamento = new Equipamento();

    @BeforeEach
    void prepararPlano() {
        equipamento.setId(UUID.randomUUID());
        equipamento.setHorimetroAtual(new BigDecimal("1230.00"));

        PlanoManutencao plano = new PlanoManutencao();
        plano.setId(planoId);
        plano.setEquipamento(equipamento);
        plano.setIntervaloHoras(new BigDecimal("250.00"));

        when(repository.findById(planoId)).thenReturn(Optional.of(plano));
    }

    @ParameterizedTest
    @CsvSource({
            "1229.99, 20.01, EM_DIA",
            "1230.00, 20.00, PROXIMA",
            "1249.99, 0.01, PROXIMA",
            "1250.00, 0.00, VENCIDA",
            "1250.01, -0.01, VENCIDA"
    })
    void classificaAlertaNosLimites(String horimetroAtual, String horasRestantes, StatusRevisao status) {
        equipamento.setHorimetroAtual(new BigDecimal(horimetroAtual));
        when(ultimaRevisao.getPlanoId()).thenReturn(planoId);
        when(ultimaRevisao.getHorimetro()).thenReturn(new BigDecimal("1000.00"));
        when(manutencaoRepository.buscarUltimasRevisoes(List.of(planoId))).thenReturn(List.of(ultimaRevisao));

        PlanoManutencaoDTO resposta = service.buscarPlanoManutencao(planoId);

        assertEquals(new BigDecimal("1250.00"), resposta.getProximoHorimetro());
        assertEquals(new BigDecimal(horasRestantes), resposta.getHorasRestantes());
        assertEquals(status, resposta.getStatusRevisao());
    }

    @Test
    void indicaAvaliacaoInicialQuandoNaoExisteRevisao() {
        when(manutencaoRepository.buscarUltimasRevisoes(List.of(planoId))).thenReturn(List.of());

        PlanoManutencaoDTO resposta = service.buscarPlanoManutencao(planoId);

        assertEquals(StatusRevisao.AVALIACAO_INICIAL_PENDENTE, resposta.getStatusRevisao());
        assertNull(resposta.getHorimetroUltimaRevisao());
        assertNull(resposta.getProximoHorimetro());
        assertNull(resposta.getHorasRestantes());
    }
}
