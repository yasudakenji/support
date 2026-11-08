package io.github.yasudakenji.support.service;

import io.github.yasudakenji.support.dto.EquipamentoDTO;
import io.github.yasudakenji.support.dto.LeituraHorimetroDTO;
import io.github.yasudakenji.support.dto.ManutencaoDTO;
import io.github.yasudakenji.support.dto.PlanoManutencaoDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.PlanoManutencao;
import io.github.yasudakenji.support.model.enums.SituacaoHistoricoInicial;
import io.github.yasudakenji.support.model.enums.StatusRevisao;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.PlanoManutencaoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StatusRevisaoIntegrationTest {

    @Autowired
    private EquipamentoRepository equipamentoRepository;

    @Autowired
    private PlanoManutencaoRepository planoManutencaoRepository;

    @Autowired
    private ManutencaoService manutencaoService;

    @Autowired
    private PlanoManutencaoService planoManutencaoService;

    @Autowired
    private EquipamentoService equipamentoService;

    @Autowired
    private LeituraHorimetroService leituraHorimetroService;

    private UUID equipamentoId;
    private UUID planoId;
    private LocalDate hoje;

    @BeforeEach
    void prepararEquipamentoEPlano() {
        hoje = LocalDate.now();
        Equipamento equipamento = new Equipamento();
        equipamento.setNome("Equipamento de teste");
        equipamento.setModelo("TESTE");
        equipamento.setDataCadastro(hoje);
        equipamento.setHorimetroAtual(new BigDecimal("130.00"));
        equipamentoId = equipamentoRepository.save(equipamento).getId();

        PlanoManutencao plano = new PlanoManutencao();
        plano.setEquipamento(equipamento);
        plano.setNomePlano("Troca de óleo de teste");
        plano.setDescricao("Registrar troca no horímetro.");
        plano.setIntervaloHoras(new BigDecimal("40.00"));
        plano.setSituacaoHistoricoInicial(SituacaoHistoricoInicial.SEM_INFORMACAO);
        planoId = planoManutencaoRepository.save(plano).getId();
    }

    @Test
    void cadastroDePlanoPreencheHistoricoInicialNoBackend() {
        planoManutencaoRepository.deleteById(planoId);
        PlanoManutencaoDTO dto = new PlanoManutencaoDTO();
        dto.setEquipamentoId(equipamentoId);
        dto.setNomePlano("Inspeção de teste");
        dto.setDescricao("Verificar o equipamento.");
        dto.setIntervaloHoras(new BigDecimal("50.00"));

        planoManutencaoService.salvarPlanoManutencao(dto);

        PlanoManutencaoDTO cadastrado = planoManutencaoService.listarPlanoManutencao(equipamentoId).getFirst();
        assertEquals(SituacaoHistoricoInicial.SEM_INFORMACAO, cadastrado.getSituacaoHistoricoInicial());
        assertEquals(StatusRevisao.AVALIACAO_INICIAL_PENDENTE, cadastrado.getStatusRevisao());
    }

    @Test
    void edicaoDePlanoPreservaHistoricoInicialJaGravado() {
        PlanoManutencao plano = planoManutencaoRepository.findById(planoId).orElseThrow();
        plano.setSituacaoHistoricoInicial(SituacaoHistoricoInicial.REALIZADA);
        PlanoManutencaoDTO dto = planoManutencaoService.buscarPlanoManutencao(planoId);
        dto.setSituacaoHistoricoInicial(null);
        dto.setNomePlano("Plano renomeado");

        planoManutencaoService.atualizarPlanoManutencao(dto);

        PlanoManutencaoDTO atualizado = planoManutencaoService.buscarPlanoManutencao(planoId);
        assertEquals("Plano renomeado", atualizado.getNomePlano());
        assertEquals(SituacaoHistoricoInicial.REALIZADA, atualizado.getSituacaoHistoricoInicial());
        assertEquals(StatusRevisao.AVALIACAO_INICIAL_PENDENTE, atualizado.getStatusRevisao());
    }

    @Test
    void registrarRevisaoAtualizaOVencimentoNaListagem() {
        PlanoManutencaoDTO antes = planoManutencaoService.buscarPlanoManutencao(planoId);
        assertEquals(StatusRevisao.AVALIACAO_INICIAL_PENDENTE, antes.getStatusRevisao());

        registrarRevisao(planoId, hoje, "90.00");
        PlanoManutencaoDTO noLimite = planoManutencaoService.buscarPlanoManutencao(planoId);
        assertEquals(StatusRevisao.VENCIDA, noLimite.getStatusRevisao());
        assertEquals(0, noLimite.getHorasRestantes().compareTo(BigDecimal.ZERO));

        registrarRevisao(planoId, hoje, "130.00");
        PlanoManutencaoDTO emDia = planoManutencaoService
                .listarPlanoManutencao(equipamentoId).getFirst();

        assertEquals(StatusRevisao.EM_DIA, emDia.getStatusRevisao());
        assertEquals(0, emDia.getHorimetroUltimaRevisao().compareTo(new BigDecimal("130")));
        assertEquals(0, emDia.getProximoHorimetro().compareTo(new BigDecimal("170")));
        assertEquals(0, emDia.getHorasRestantes().compareTo(new BigDecimal("40")));
    }

    @Test
    void rejeitaCorrecaoAbaixoDeUmaLeitura() {
        LeituraHorimetroDTO leitura = new LeituraHorimetroDTO();
        leitura.setEquipamentoId(equipamentoId);
        leitura.setDataHoraLeitura(LocalDateTime.now().minusHours(1));
        leitura.setValorHorimetro(new BigDecimal("140.00"));
        leituraHorimetroService.salvarLeituraHorimetro(leitura);

        EquipamentoDTO dto = equipamentoService.buscarEquipamento(equipamentoId);
        dto.setHorimetroAtual(new BigDecimal("139.99"));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> equipamentoService.atualizarEquipamento(dto));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
        assertEquals(new BigDecimal("140.00"), equipamentoService.buscarEquipamento(equipamentoId).getHorimetroAtual());
    }

    @Test
    void rejeitaCorrecaoAbaixoDeUmaRevisao() {
        registrarRevisao(planoId, hoje, "120.00");
        EquipamentoDTO dto = equipamentoService.buscarEquipamento(equipamentoId);
        dto.setHorimetroAtual(new BigDecimal("119.99"));

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> equipamentoService.atualizarEquipamento(dto));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void permiteCorrecaoIgualAoMaiorHorimetroDoHistorico() {
        registrarRevisao(planoId, hoje, "120.00");
        EquipamentoDTO dto = equipamentoService.buscarEquipamento(equipamentoId);
        dto.setHorimetroAtual(new BigDecimal("120.00"));

        equipamentoService.atualizarEquipamento(dto);

        assertEquals(new BigDecimal("120.00"), equipamentoService.buscarEquipamento(equipamentoId).getHorimetroAtual());
    }

    @Test
    void rejeitaRevisaoComDataAnteriorAUltima() {
        registrarRevisao(planoId, hoje, "90.00");

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> registrarRevisao(planoId, hoje.minusDays(1), "100.00"));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    @Test
    void rejeitaRevisaoComHorimetroMenorMesmoEmDataPosterior() {
        registrarRevisao(planoId, hoje.minusDays(1), "100.00");

        ResponseStatusException erro = assertThrows(ResponseStatusException.class,
                () -> registrarRevisao(planoId, hoje, "90.00"));
        assertEquals(HttpStatus.BAD_REQUEST, erro.getStatusCode());
    }

    private void registrarRevisao(UUID planoId, LocalDate data, String horimetro) {
        ManutencaoDTO revisao = new ManutencaoDTO();
        revisao.setPlanoManutencaoId(planoId);
        revisao.setDataManutencao(data);
        revisao.setHorimetroNaExecucao(new BigDecimal(horimetro));
        manutencaoService.salvarManutencao(revisao);
    }
}
