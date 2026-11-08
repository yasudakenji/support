package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.PlanoManutencaoDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.PlanoManutencao;
import io.github.yasudakenji.support.model.enums.SituacaoHistoricoInicial;
import io.github.yasudakenji.support.model.enums.StatusRevisao;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.ManutencaoRepository;
import io.github.yasudakenji.support.repository.PlanoManutencaoRepository;
import io.github.yasudakenji.support.service.PlanoManutencaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlanoManutencaoServiceImpl implements PlanoManutencaoService {

    private static final BigDecimal LIMITE_ALERTA_HORAS = BigDecimal.valueOf(20);

    private final PlanoManutencaoRepository repository;
    private final EquipamentoRepository equipamentoRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Override
    public void salvarPlanoManutencao(PlanoManutencaoDTO dto) {
        Equipamento equipamento = equipamentoRepository.findById(dto.getEquipamentoId()).orElse(null);
        if (equipamento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado");
        }
        PlanoManutencao plano = dto.toEntity(equipamento);
        plano.setSituacaoHistoricoInicial(SituacaoHistoricoInicial.SEM_INFORMACAO);
        repository.save(plano);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public PlanoManutencaoDTO buscarPlanoManutencao(UUID id) {
        PlanoManutencao plano = buscarEntidade(id);
        return montarResposta(plano, buscarUltimosHorimetros(List.of(id)).get(id));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public List<PlanoManutencaoDTO> listarPlanoManutencao(UUID equipamentoId) {
        List<PlanoManutencao> planos;
        if (equipamentoId == null) {
            planos = repository.findAll();
        } else {
            planos = repository.findByEquipamento_Id(equipamentoId);
        }

        List<UUID> planoIds = new ArrayList<>();
        for (PlanoManutencao plano : planos) {
            planoIds.add(plano.getId());
        }
        Map<UUID, BigDecimal> ultimosHorimetros = buscarUltimosHorimetros(planoIds);

        List<PlanoManutencaoDTO> resposta = new ArrayList<>();
        for (PlanoManutencao plano : planos) {
            BigDecimal horimetroUltimaRevisao = ultimosHorimetros.get(plano.getId());
            resposta.add(montarResposta(plano, horimetroUltimaRevisao));
        }
        return resposta;
    }

    @Override
    public void atualizarPlanoManutencao(PlanoManutencaoDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do plano de manutenção");
        }

        PlanoManutencao plano = buscarEntidade(dto.getId());
        if (!plano.getEquipamento().getId().equals(dto.getEquipamentoId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O plano deve permanecer vinculado ao equipamento cadastrado");
        }

        plano.setNomePlano(dto.getNomePlano());
        plano.setIntervaloHoras(dto.getIntervaloHoras());
        plano.setDescricao(dto.getDescricao());
        repository.save(plano);
    }

    @Override
    public void deletarPlanoManutencao(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do plano de manutenção");
        }

        repository.delete(buscarEntidade(id));
    }

    private Map<UUID, BigDecimal> buscarUltimosHorimetros(List<UUID> planoIds) {
        if (planoIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, BigDecimal> ultimosHorimetros = new HashMap<>();
        for (ManutencaoRepository.UltimaRevisao revisao : manutencaoRepository.buscarUltimasRevisoes(planoIds)) {
            ultimosHorimetros.put(revisao.getPlanoId(), revisao.getHorimetro());
        }
        return ultimosHorimetros;
    }

    private PlanoManutencaoDTO montarResposta(PlanoManutencao plano, BigDecimal horimetroUltimaRevisao) {
        PlanoManutencaoDTO dto = new PlanoManutencaoDTO().toDTO(plano);
        dto.setHorimetroAtual(plano.getEquipamento().getHorimetroAtual());
        if (horimetroUltimaRevisao == null) {
            dto.setStatusRevisao(StatusRevisao.AVALIACAO_INICIAL_PENDENTE);
            return dto;
        }

        BigDecimal proximoHorimetro = horimetroUltimaRevisao.add(plano.getIntervaloHoras());
        BigDecimal horasRestantes = proximoHorimetro.subtract(dto.getHorimetroAtual());
        dto.setHorimetroUltimaRevisao(horimetroUltimaRevisao);
        dto.setProximoHorimetro(proximoHorimetro);
        dto.setHorasRestantes(horasRestantes);
        dto.setStatusRevisao(classificarRevisao(horasRestantes));
        return dto;
    }

    private StatusRevisao classificarRevisao(BigDecimal horasRestantes) {
        if (horasRestantes.compareTo(BigDecimal.ZERO) <= 0) {
            return StatusRevisao.VENCIDA;
        }
        if (horasRestantes.compareTo(LIMITE_ALERTA_HORAS) <= 0) {
            return StatusRevisao.PROXIMA;
        }
        return StatusRevisao.EM_DIA;
    }

    private PlanoManutencao buscarEntidade(UUID id) {
        PlanoManutencao plano = repository.findById(id).orElse(null);
        if (plano == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano de manutenção não encontrado");
        }
        return plano;
    }
}
