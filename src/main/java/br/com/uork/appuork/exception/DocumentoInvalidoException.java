package br.com.uork.appuork.exception;

public class DocumentoInvalidoException extends RuntimeException {


    public DocumentoInvalidoException() {
        super("Documento inválido");
    }

    public DocumentoInvalidoException(String message) {
        super(message);
    }
}
