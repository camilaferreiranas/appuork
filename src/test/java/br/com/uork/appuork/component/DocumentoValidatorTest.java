package br.com.uork.appuork.component;

import br.com.uork.appuork.exception.DocumentoInvalidoException;
import br.com.uork.appuork.models.enuns.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DocumentoValidatorTest {

    @Test
    @DisplayName("valid CPF does not throw")
    void validCpf() {
        assertDoesNotThrow(() -> DocumentoValidator.validar(TipoPessoa.CPF, "52998224725"));
    }

    @Test
    @DisplayName("valid CPF with punctuation does not throw")
    void validCpfWithPunctuation() {
        assertDoesNotThrow(() -> DocumentoValidator.validar(TipoPessoa.CPF, "529.982.247-25"));
    }

    @Test
    @DisplayName("invalid CPF throws")
    void invalidCpf() {
        assertThrows(DocumentoInvalidoException.class,
                () -> DocumentoValidator.validar(TipoPessoa.CPF, "123"));
    }

    @Test
    @DisplayName("null CPF throws")
    void nullCpf() {
        assertThrows(DocumentoInvalidoException.class,
                () -> DocumentoValidator.validar(TipoPessoa.CPF, null));
    }

    @Test
    @DisplayName("valid CNPJ does not throw")
    void validCnpj() {
        assertDoesNotThrow(() -> DocumentoValidator.validar(TipoPessoa.CNPJ, "11222333000181"));
    }

    @Test
    @DisplayName("invalid CNPJ throws")
    void invalidCnpj() {
        assertThrows(DocumentoInvalidoException.class,
                () -> DocumentoValidator.validar(TipoPessoa.CNPJ, "1234"));
    }
}