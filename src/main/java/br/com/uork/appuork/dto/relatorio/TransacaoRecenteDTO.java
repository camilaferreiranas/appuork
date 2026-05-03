package br.com.uork.appuork.dto.relatorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransacaoRecenteDTO(String tituloDemanda, BigDecimal valor, LocalDateTime data) {
}
