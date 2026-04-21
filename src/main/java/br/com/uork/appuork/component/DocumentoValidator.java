package br.com.uork.appuork.component;

import br.com.uork.appuork.exception.DocumentoInvalidoException;
import br.com.uork.appuork.models.TipoPessoa;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DocumentoValidator {

    private static final Pattern CPF_PATTERN =
            Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern CNPJ_PATTERN =
            Pattern.compile("\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}");


    public static void validar(TipoPessoa tipo, String documento) {

        if (tipo == TipoPessoa.CPF) {
            validarCPF(documento);
        } else {
            validarCNPJ(documento);
        }
    }

    private static boolean validarCPF(String cpf) {
        if( cpf == null) throw new DocumentoInvalidoException("Cpf inválido");
        return CPF_PATTERN.matcher(cpf).matches();
    }


    private static boolean validarCNPJ(String cnpj) {
        if (cnpj == null) throw  new DocumentoInvalidoException("Cnpj inválido");
        return CNPJ_PATTERN.matcher(cnpj).matches();
    }
}