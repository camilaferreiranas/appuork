package br.com.uork.appuork.dto.usuario;

public record EnderecoResponseDTO(
        String rua,
        String numero,
        String bairro,
        String cidade,
        String estado,
        String cep,
        Double latitude,
        Double longitude
) {
    public EnderecoResponseDTO(
            String rua,
            String numero,
            String bairro,
            String cidade,
            String estado,
            String cep) {
        this(rua, numero, bairro, cidade, estado, cep, null, null);
    }
}
