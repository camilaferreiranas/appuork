package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.localizacao.LocalizacaoPrestadorDTO;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PrestadorLocalizacaoCacheService {

    private final ConcurrentMap<Long, LocalizacaoPrestadorDTO> localizacoes =
            new ConcurrentHashMap<>();

    public LocalizacaoPrestadorDTO salvar(
            Long prestadorId,
            Double latitude,
            Double longitude) {
        if (prestadorId == null) {
            throw new IllegalArgumentException("Prestador é obrigatório");
        }

        validarCoordenadas(latitude, longitude);

        LocalizacaoPrestadorDTO localizacao = new LocalizacaoPrestadorDTO(
                latitude,
                longitude,
                Instant.now()
        );
        localizacoes.put(prestadorId, localizacao);
        return localizacao;
    }

    public Optional<LocalizacaoPrestadorDTO> buscar(Long prestadorId) {
        if (prestadorId == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(localizacoes.get(prestadorId));
    }

    private void validarCoordenadas(Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            throw new IllegalArgumentException(
                    "Latitude e longitude devem ser informadas juntas"
            );
        }

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude deve estar entre -90 e 90");
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude deve estar entre -180 e 180");
        }
    }
}
