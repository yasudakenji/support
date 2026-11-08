package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.ManutencaoDTO;
import io.github.yasudakenji.support.model.Manutencao;
import io.github.yasudakenji.support.model.PlanoManutencao;
import io.github.yasudakenji.support.repository.ManutencaoRepository;
import io.github.yasudakenji.support.repository.PlanoManutencaoRepository;
import io.github.yasudakenji.support.service.ManutencaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ManutencaoServiceImpl implements ManutencaoService {

    private final ManutencaoRepository repository;
    private final PlanoManutencaoRepository planoManutencaoRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void salvarManutencao(ManutencaoDTO dto) {
        PlanoManutencao plano = planoManutencaoRepository.findById(dto.getPlanoManutencaoId()).orElse(null);
        if (plano == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Plano de manutenção não encontrado");
        }

        if (dto.getHorimetroNaExecucao().compareTo(plano.getEquipamento().getHorimetroAtual()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horímetro da revisão não pode superar o horímetro atual");
        }

        validarOrdemDaRevisao(dto);
        repository.save(dto.toEntity(plano));
    }

    @Override
    public ManutencaoDTO buscarManutencao(UUID id) {
        ManutencaoDTO dto = new ManutencaoDTO();
        return dto.toDTO(buscarEntidade(id));
    }

    @Override
    @Transactional
    public void deletarManutencao(UUID id) {
        repository.delete(buscarEntidade(id));
    }

    @Override
    public List<ManutencaoDTO> listarManutencao(UUID planoManutencaoId) {
        List<Manutencao> manutencoes;
        if (planoManutencaoId == null) {
            manutencoes = repository.findAll();
        } else {
            manutencoes = repository.findByPlanoManutencao_Id(planoManutencaoId);
        }

        List<ManutencaoDTO> resposta = new ArrayList<>();
        ManutencaoDTO dto = new ManutencaoDTO();
        for (Manutencao manutencao : manutencoes) {
            resposta.add(dto.toDTO(manutencao));
        }
        return resposta;
    }

    private void validarOrdemDaRevisao(ManutencaoDTO dto) {
        Manutencao ultimaRevisao = repository
                .findFirstByPlanoManutencao_IdOrderByDataManutencaoDescHorimetroNaExecucaoDesc(dto.getPlanoManutencaoId())
                .orElse(null);
        if (ultimaRevisao == null) {
            return;
        }

        if (dto.getDataManutencao().isBefore(ultimaRevisao.getDataManutencao())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data não pode ser anterior à última revisão do plano");
        }
        if (dto.getHorimetroNaExecucao().compareTo(ultimaRevisao.getHorimetroNaExecucao()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O horímetro não pode ser menor que o da última revisão do plano");
        }
    }

    private Manutencao buscarEntidade(UUID id) {
        Manutencao manutencao = repository.findById(id).orElse(null);
        if (manutencao == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manutenção não encontrada");
        }
        return manutencao;
    }
}
