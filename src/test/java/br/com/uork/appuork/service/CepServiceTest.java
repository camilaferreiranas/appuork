package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.endereco.ConsultaCepResponseDTO;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CepServiceTest {

    private HttpServer server;
    private CepService cepService;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ws/01001000/json/", exchange -> responder(
                exchange,
                200,
                """
                {
                  "cep": "01001-000",
                  "logradouro": "Praça da Sé",
                  "bairro": "Sé",
                  "localidade": "São Paulo",
                  "uf": "SP"
                }
                """
        ));
        server.createContext("/ws/99999999/json/", exchange -> responder(
                exchange,
                200,
                "{\"erro\": true}"
        ));
        server.start();

        cepService = new CepService(
                "http://localhost:" + server.getAddress().getPort()
        );
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void deveConsultarENormalizarCep() {
        ConsultaCepResponseDTO endereco = cepService.consultar("01001-000");

        assertEquals("01001000", endereco.cep());
        assertEquals("Praça da Sé", endereco.rua());
        assertEquals("Sé", endereco.bairro());
        assertEquals("São Paulo", endereco.cidade());
        assertEquals("SP", endereco.estado());
    }

    @Test
    void deveRejeitarCepInexistente() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.consultar("99999999")
        );

        assertEquals("CEP não encontrado", exception.getMessage());
    }

    @Test
    void deveRejeitarFormatoInvalidoSemConsultarServicoExterno() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cepService.consultar("1234")
        );

        assertEquals(
                "O CEP deve conter exatamente 8 números",
                exception.getMessage()
        );
    }

    private void responder(HttpExchange exchange, int status, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
