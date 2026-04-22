package br.com.uork.appuork.component;

import br.com.uork.appuork.models.enuns.TipoPessoa;
import org.springframework.stereotype.Component;

@Component
public class DocumentoValidator {

    public void validar(TipoPessoa tipo, String documento) {

        if (tipo == TipoPessoa.CPF) {
            validarCPF(documento);
        } else {
            validarCNPJ(documento);
        }
    }

    private void validarCPF(String cpf) {
        // lógica
    }

    private void validarCNPJ(String cnpj) {
        // lógica
    }
}