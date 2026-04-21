package br.com.uork.appuork.config;

import br.com.uork.appuork.dto.common.ApiErros;
import br.com.uork.appuork.exception.DocumentoInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalControllerAdvice {


    @ExceptionHandler(DocumentoInvalidoException.class)
    public ResponseEntity<ApiErros> handleDocumentoInvalido(RuntimeException e) {
        var errors = new ApiErros(List.of(e.getMessage()), HttpStatus.BAD_REQUEST.value(), LocalDateTime.now());
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
