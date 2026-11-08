package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.EquipamentoDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.Setor;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.LeituraHorimetroRepository;
import io.github.yasudakenji.support.repository.ManutencaoRepository;
import io.github.yasudakenji.support.repository.SetorRepository;
import io.github.yasudakenji.support.service.EquipamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EquipamentoServiceImpl implements EquipamentoService {

    private final EquipamentoRepository repository;
    private final SetorRepository setorRepository;
    private final LeituraHorimetroRepository leituraHorimetroRepository;
    private final ManutencaoRepository manutencaoRepository;

    @Override
    public void salvarEquipamento(EquipamentoDTO equipamento) {
        Setor setor = buscarSetor(equipamento.getSetorId());
        repository.save(equipamento.toEntity(setor));
    }

    @Override
    public EquipamentoDTO buscarEquipamento(UUID id) {
        EquipamentoDTO dto = new EquipamentoDTO();
        return dto.toDTO(buscarEntidade(id));
    }

    @Override
    public void deletarEquipamento(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do equipamento");
        }

        Equipamento equipamento = buscarEntidade(id);
        try {
            repository.delete(equipamento);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível excluir o equipamento por restrições de integridade", exception);
        }
    }

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void atualizarEquipamento(EquipamentoDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do equipamento");
        }

        Equipamento equipamento = buscarEntidade(dto.getId());
        if (dto.getHorimetroAtual().compareTo(equipamento.getHorimetroAtual()) != 0) {
            validarHorimetro(dto.getId(), dto.getHorimetroAtual());
        }
        equipamento.setNome(dto.getNome());
        equipamento.setModelo(dto.getModelo());
        equipamento.setHorimetroAtual(dto.getHorimetroAtual());
        equipamento.setSetor(buscarSetor(dto.getSetorId()));

        repository.save(equipamento);
    }

    @Override
    public List<EquipamentoDTO> listarEquipamento(UUID setorId) {
        List<Equipamento> equipamentos;
        if (setorId == null) {
            equipamentos = repository.findAll();
        } else {
            equipamentos = repository.findBySetor_Id(setorId);
        }

        List<EquipamentoDTO> resposta = new ArrayList<>();
        EquipamentoDTO dto = new EquipamentoDTO();
        for (Equipamento equipamento : equipamentos) {
            resposta.add(dto.toDTO(equipamento));
        }
        return resposta;
    }

    private Equipamento buscarEntidade(UUID id) {
        Equipamento equipamento = repository.findById(id).orElse(null);
        if (equipamento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado");
        }
        return equipamento;
    }

    private void validarHorimetro(UUID equipamentoId, BigDecimal novoHorimetro) {
        BigDecimal maiorLeitura = leituraHorimetroRepository.buscarMaiorHorimetro(equipamentoId)
                .orElse(BigDecimal.ZERO);
        BigDecimal maiorRevisao = manutencaoRepository.buscarMaiorHorimetro(equipamentoId)
                .orElse(BigDecimal.ZERO);
        BigDecimal minimo = maiorLeitura.max(maiorRevisao);

        if (novoHorimetro.compareTo(minimo) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O horímetro não pode ficar abaixo do maior valor no histórico: " + minimo.toPlainString() + " h");
        }
    }

    private Setor buscarSetor(UUID id) {
        if (id == null) {
            return null;
        }

        Setor setor = setorRepository.findById(id).orElse(null);
        if (setor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Setor não encontrado");
        }
        return setor;
    }
}
