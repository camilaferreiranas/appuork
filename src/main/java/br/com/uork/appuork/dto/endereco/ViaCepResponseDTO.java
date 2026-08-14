package br.com.uork.appuork.dto.endereco;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ViaCepResponseDTO(
        String cep,
        String logradouro,
        String bairro,
        @JsonProperty("localidade") String cidade,
        @JsonProperty("uf") String estado,
        Boolean erro
) {}
