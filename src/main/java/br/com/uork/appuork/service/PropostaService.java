package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.proposta.PropostaCreateDTO;
import br.com.uork.appuork.dto.proposta.PropostaResponseDTO;
import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Proposta;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.models.enuns.StatusProposta;
import br.com.uork.appuork.repository.PrestadorServicoRepository;
import br.com.uork.appuork.repository.PropostaRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PropostaService {

    private final PropostaRepository propostaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrestadorServicoRepository prestadorServicoRepository;

    public PropostaService(PropostaRepository propostaRepository,
                           UsuarioRepository usuarioRepository,
                           PrestadorServicoRepository prestadorServicoRepository) {
        this.propostaRepository = propostaRepository;
        this.usuarioRepository = usuarioRepository;
        this.prestadorServicoRepository = prestadorServicoRepository;
    }

    public PropostaResponseDTO criarProposta(PropostaCreateDTO dto) {

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PrestadorServico prestador = prestadorServicoRepository.findById(dto.prestadorId())
                .orElseThrow(() -> new RuntimeException("Prestador de serviço não encontrado"));

        if (dto.descricao() == null || dto.descricao().isBlank()) {
            throw new RuntimeException("Descrição da proposta é obrigatória");
        }

        if (dto.descricao().length() < 10) {
            throw new RuntimeException("Descrição da proposta deve ter no mínimo 10 caracteres");
        }

        if (dto.valor() == null) {
            throw new RuntimeException("Valor da proposta é obrigatório");
        }

        if (dto.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Valor da proposta deve ser maior que zero");
        }

        if (Boolean.FALSE.equals(prestador.getAtivo())) {
            throw new RuntimeException("Prestador está inativo");
        }

        if (prestador.getUsuario().getId().equals(usuario.getId())) {
            throw new RuntimeException("Usuário não pode enviar proposta para si mesmo");
        }

        Proposta proposta = new Proposta();
        proposta.setUsuario(usuario);
        proposta.setPrestadorServico(prestador);
        proposta.setDescricao(dto.descricao());
        proposta.setValor(dto.valor());
        proposta.setStatus(StatusProposta.PENDENTE);
        proposta.setDataCriacao(LocalDateTime.now());

        Proposta propostaSalva = propostaRepository.save(proposta);

        return new PropostaResponseDTO(
                propostaSalva.getId(),
                propostaSalva.getUsuario().getNome(),
                propostaSalva.getPrestadorServico().getUsuario().getNome(),
                propostaSalva.getDescricao(),
                propostaSalva.getValor(),
                propostaSalva.getStatus().name(),
                propostaSalva.getDataCriacao()
        );
    }
}
