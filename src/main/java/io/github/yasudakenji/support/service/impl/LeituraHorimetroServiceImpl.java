package io.github.yasudakenji.support.service.impl;

import io.github.yasudakenji.support.dto.LeituraHorimetroDTO;
import io.github.yasudakenji.support.model.Equipamento;
import io.github.yasudakenji.support.model.LeituraHorimetro;
import io.github.yasudakenji.support.repository.EquipamentoRepository;
import io.github.yasudakenji.support.repository.LeituraHorimetroRepository;
import io.github.yasudakenji.support.service.LeituraHorimetroService;
import lombok.RequiredArgsConstructor;
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
public class LeituraHorimetroServiceImpl implements LeituraHorimetroService {

    private final LeituraHorimetroRepository repository;
    private final EquipamentoRepository equipamentoRepository;

    @Override
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void salvarLeituraHorimetro(LeituraHorimetroDTO dto) {
        Equipamento equipamento = equipamentoRepository.findById(dto.getEquipamentoId()).orElse(null);
        if (equipamento == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipamento não encontrado");
        }

        LeituraHorimetro ultimaLeitura = repository
                .findFirstByEquipamento_IdOrderByDataHoraLeituraDesc(equipamento.getId())
                .orElse(null);

        BigDecimal valorMinimo = equipamento.getHorimetroAtual();
        if (ultimaLeitura != null) {
            if (!dto.getDataHoraLeitura().isAfter(ultimaLeitura.getDataHoraLeitura())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data e hora devem ser posteriores à última leitura");
            }
            valorMinimo = valorMinimo.max(ultimaLeitura.getValorHorimetro());
        }

        if (dto.getValorHorimetro().compareTo(valorMinimo) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O valor deve ser igual ou maior que o horímetro atual e a última leitura");
        }

        repository.save(dto.toEntity(equipamento));
        equipamento.setHorimetroAtual(dto.getValorHorimetro());
        equipamentoRepository.save(equipamento);
    }

    @Override
    public LeituraHorimetroDTO buscarLeituraHorimetro(UUID id) {
        LeituraHorimetroDTO dto = new LeituraHorimetroDTO();
        return dto.toDTO(buscarEntidade(id));
    }

    @Override
    @Transactional
    public void deletarLeituraHorimetro(UUID id) {
        repository.delete(buscarEntidade(id));
    }

    @Override
    public List<LeituraHorimetroDTO> listarLeituraHorimetro(UUID equipamentoId) {
        List<LeituraHorimetro> leituras;
        if (equipamentoId == null) {
            leituras = repository.findAll();
        } else {
            leituras = repository.findByEquipamento_IdOrderByDataHoraLeituraDesc(equipamentoId);
        }

        List<LeituraHorimetroDTO> resposta = new ArrayList<>();
        LeituraHorimetroDTO dto = new LeituraHorimetroDTO();
        for (LeituraHorimetro leitura : leituras) {
            resposta.add(dto.toDTO(leitura));
        }
        return resposta;
    }

    private LeituraHorimetro buscarEntidade(UUID id) {
        LeituraHorimetro leitura = repository.findById(id).orElse(null);
        if (leitura == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Leitura de horímetro não encontrada");
        }
        return leitura;
    }
}
