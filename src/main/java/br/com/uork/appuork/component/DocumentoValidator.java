package br.com.uork.appuork.component;

import br.com.uork.appuork.exception.DocumentoInvalidoException;
import br.com.uork.appuork.models.enuns.TipoPessoa;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class DocumentoValidator {


    private static final Pattern CPF_PATTERN =
            Pattern.compile("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d{2}");
    private static final Pattern CNPJ_PATTERN =
            Pattern.compile("\\d{2}\\.?\\d{3}\\.?\\d{3}/?\\d{4}-?\\d{2}");


    public static void validar(TipoPessoa tipo, String documento) {

        boolean valido = (tipo == TipoPessoa.CPF)
                ? validarCPF(documento)
                : validarCNPJ(documento);

        if (!valido) {
            String mensagem = (tipo == TipoPessoa.CPF) ? "Cpf inválido" : "Cnpj inválido";
            throw new DocumentoInvalidoException(mensagem);
        }
    }

    private static boolean validarCPF(String cpf) {
        if (cpf == null) return false;
        return CPF_PATTERN.matcher(cpf).matches();
    }

    private static boolean validarCNPJ(String cnpj) {
        if (cnpj == null) return false;
        return CNPJ_PATTERN.matcher(cnpj).matches();
    }
}