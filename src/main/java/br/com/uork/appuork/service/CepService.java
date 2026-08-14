package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.endereco.ConsultaCepResponseDTO;
import br.com.uork.appuork.dto.endereco.ViaCepResponseDTO;
import br.com.uork.appuork.exception.CepServiceIndisponivelException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;

@Service
public class CepService {

    private final RestClient restClient;

    public CepService(@Value("${viacep.base-url:https://viacep.com.br}") String viaCepBaseUrl) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory =
                new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(viaCepBaseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    public ConsultaCepResponseDTO consultar(String cepInformado) {
        String cep = normalizarCep(cepInformado);

        try {
            ViaCepResponseDTO response = restClient
                    .get()
                    .uri("/ws/{cep}/json/", cep)
                    .retrieve()
                    .body(ViaCepResponseDTO.class);

            if (response == null || Boolean.TRUE.equals(response.erro())) {
                throw new IllegalArgumentException("CEP não encontrado");
            }

            return new ConsultaCepResponseDTO(
                    normalizarCep(response.cep()),
                    valorOuVazio(response.logradouro()),
                    valorOuVazio(response.bairro()),
                    valorOuVazio(response.cidade()),
                    valorOuVazio(response.estado())
            );
        } catch (IllegalArgumentException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new CepServiceIndisponivelException(
                    "O serviço de consulta de CEP está indisponível",
                    exception
            );
        }
    }

    private String normalizarCep(String cepInformado) {
        if (cepInformado == null) {
            throw new IllegalArgumentException("O CEP deve conter exatamente 8 números");
        }

        String cep = cepInformado.replaceAll("\\D", "");

        if (!cep.matches("\\d{8}")) {
            throw new IllegalArgumentException("O CEP deve conter exatamente 8 números");
        }

        return cep;
    }

    private String valorOuVazio(String valor) {
        return valor == null ? "" : valor;
    }
}
