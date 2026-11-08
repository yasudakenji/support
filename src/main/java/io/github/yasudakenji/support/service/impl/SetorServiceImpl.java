package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.SetorDTO;
import io.github.yasudakenji.support.model.Setor;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.SetorRepository;
import io.github.yasudakenji.support.service.SetorService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SetorServiceImpl implements SetorService {

    private final SetorRepository repository;
    private final EquipamentoRepository equipamentoRepository;

    @Override
    public void salvarSetor(SetorDTO dto) {
        repository.save(dto.toEntity());
    }

    @Override
    public SetorDTO buscarSetor(UUID id) {
        SetorDTO dto = new SetorDTO();
        Setor setor = buscarEntidade(id);
        return dto.toDTO(setor, equipamentoRepository.countBySetor_Id(id));
    }

    @Override
    public List<SetorDTO> listarSetor() {
        List<Setor> setores = repository.findAll();
        List<SetorDTO> resposta = new ArrayList<>();
        SetorDTO dto = new SetorDTO();
        for (Setor setor : setores) {
            long totalEquipamentos = equipamentoRepository.countBySetor_Id(setor.getId());
            resposta.add(dto.toDTO(setor, totalEquipamentos));
        }
        return resposta;
    }

    @Override
    public void atualizarSetor(SetorDTO dto) {
        if (dto.getId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do setor");
        }

        Setor setor = buscarEntidade(dto.getId());
        setor.setNome(dto.getNome());
        repository.save(setor);
    }

    @Override
    public void deletarSetor(UUID id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o ID do setor");
        }

        Setor setor = buscarEntidade(id);
        try {
            repository.delete(setor);
        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Não foi possível excluir o setor por restrições de integridade", exception);
        }
    }

    private Setor buscarEntidade(UUID id) {
        Setor setor = repository.findById(id).orElse(null);
        if (setor == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Setor não encontrado");
        }
        return setor;
    }
}
