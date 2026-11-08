package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.LeituraHorimetroDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.LeituraHorimetro;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.LeituraHorimetroRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeituraHorimetroServiceImplTest {

    @Mock
    private LeituraHorimetroRepository repository;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private LeituraHorimetroServiceImpl service;

    private final UUID equipamentoId = UUID.randomUUID();
    private final Equipamento equipamento = new Equipamento();
    private final LocalDateTime dataAnterior = LocalDateTime.of(2026, 1, 10, 9, 0);

    @BeforeEach
    void prepararEquipamento() {
        equipamento.setId(equipamentoId);
        equipamento.setHorimetroAtual(new BigDecimal("100.00"));
        when(equipamentoRepository.findById(equipamentoId)).thenReturn(Optional.of(equipamento));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    void rejeitaDataAnteriorOuIgualAUltimaLeitura(int horasDepois) {
        prepararUltimaLeitura("100.00");
        LeituraHorimetroDTO dto = criarLeitura(dataAnterior.plusHours(horasDepois), "120.00");

        verificarRejeicaoSemGravar(dto);
    }

    @Test
    void rejeitaValorAbaixoDoHorimetroAtual() {
        when(repository.findFirstByEquipamento_IdOrderByDataHoraLeituraDesc(equipamentoId))
                .thenReturn(Optional.empty());
        LeituraHorimetroDTO dto = criarLeitura(dataAnterior.plusHours(1), "99.99");

        verificarRejeicaoSemGravar(dto);
        assertEquals(new BigDecimal("100.00"), equipamento.getHorimetroAtual());
    }

    @Test
    void rejeitaValorAbaixoDaUltimaLeituraMesmoAcimaDoHorimetroAtual() {
        prepararUltimaLeitura("140.00");
        LeituraHorimetroDTO dto = criarLeitura(dataAnterior.plusHours(1), "130.00");

        verificarRejeicaoSemGravar(dto);
        assertEquals(new BigDecimal("100.00"), equipamento.getHorimetroAtual());
    }

    @ParameterizedTest
    @ValueSource(strings = {"100.00", "120.50"})
    void gravaLeituraPosteriorComValorIgualOuMaior(String valor) {
        prepararUltimaLeitura("100.00");
        LeituraHorimetroDTO dto = criarLeitura(dataAnterior.plusHours(1), valor);

        service.salvarLeituraHorimetro(dto);

        ArgumentCaptor<LeituraHorimetro> registro = ArgumentCaptor.forClass(LeituraHorimetro.class);
        verify(repository).save(registro.capture());
        verify(equipamentoRepository).save(equipamento);
        assertSame(equipamento, registro.getValue().getEquipamento());
        assertEquals(dto.getDataHoraLeitura(), registro.getValue().getDataHoraLeitura());
        assertEquals(dto.getValorHorimetro(), registro.getValue().getValorHorimetro());
        assertEquals(dto.getValorHorimetro(), equipamento.getHorimetroAtual());
    }

    private void verificarRejeicaoSemGravar(LeituraHorimetroDTO dto) {
        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> service.salvarLeituraHorimetro(dto));

        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        verify(repository, never()).save(any());
        verify(equipamentoRepository, never()).save(any());
    }

    private void prepararUltimaLeitura(String valor) {
        LeituraHorimetro anterior = new LeituraHorimetro();
        anterior.setDataHoraLeitura(dataAnterior);
        anterior.setValorHorimetro(new BigDecimal(valor));
        when(repository.findFirstByEquipamento_IdOrderByDataHoraLeituraDesc(equipamentoId))
                .thenReturn(Optional.of(anterior));
    }

    private LeituraHorimetroDTO criarLeitura(LocalDateTime data, String valor) {
        LeituraHorimetroDTO dto = new LeituraHorimetroDTO();
        dto.setEquipamentoId(equipamentoId);
        dto.setDataHoraLeitura(data);
        dto.setValorHorimetro(new BigDecimal(valor));
        return dto;
    }
}
