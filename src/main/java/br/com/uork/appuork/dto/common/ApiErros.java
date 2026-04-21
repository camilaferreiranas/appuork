package br.com.uork.appuork.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErros(List<String> erros, Integer httpStatus, @JsonFormat(pattern = "dd-MM-yyyy HH:mm:ss") LocalDateTime timestamp) {
}
