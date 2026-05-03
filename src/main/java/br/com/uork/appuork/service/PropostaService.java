package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.demanda.DemandaResumoDTO;
import br.com.uork.appuork.dto.demanda.DetalheDemandaDTO;
import br.com.uork.appuork.dto.home.listaDemandaDRO;
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
import java.util.List;

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
        proposta.setTitulo(dto.titulo());
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

    public PropostaResponseDTO aceitarProposta(Long propostaId, String email) {

        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));

        if (!proposta.getPrestadorServico().getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Apenas o prestador pode aceitar a proposta");
        }

        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new RuntimeException("Somente propostas pendentes podem ser aceitas");
        }

        proposta.setStatus(StatusProposta.ACEITA);

        return montarResponse(propostaRepository.save(proposta));
    }

    public PropostaResponseDTO recusarProposta(Long propostaId, String email) {

        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));

        if (!proposta.getPrestadorServico().getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Apenas o prestador pode recusar a proposta");
        }

        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new RuntimeException("Somente propostas pendentes podem ser recusar");
        }

        proposta.setStatus(StatusProposta.RECUSADA);

        return montarResponse(propostaRepository.save(proposta));
    }

    public DetalheDemandaDTO buscarDetalheDemanda(Long propostaId) {

        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));

        return new DetalheDemandaDTO(
                proposta.getId(),
                proposta.getTitulo(),
                proposta.getUsuario().getNome(),
                proposta.getValor(),
                null, // distância será calculada futuramente
                proposta.getDescricao(),
                proposta.getPrestadorServico().getUsuario().getNome()
        );
    }

    public PropostaResponseDTO cancelarProposta(Long propostaId, String email) {

        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));

        if (!proposta.getUsuario().getEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Apenas o usuário que enviou a proposta pode cancelá-la");
        }

        if (proposta.getStatus() != StatusProposta.PENDENTE) {
            throw new RuntimeException("Somente propostas pendentes podem ser canceladas");
        }

        proposta.setStatus(StatusProposta.CANCELADA);

        Proposta propostaSalva = propostaRepository.save(proposta);

        return montarResponse(propostaSalva);
    }

    public PropostaResponseDTO finalizarProposta(Long propostaId) {
        Proposta proposta = propostaRepository.findById(propostaId)
                .orElseThrow(() -> new RuntimeException("Proposta não encontrada"));

        if (proposta.getStatus() != StatusProposta.ACEITA) {
            throw new RuntimeException("Somente propostas aceitas podem ser finalizadas");
        }

        proposta.setStatus(StatusProposta.FINALIZADA);

        Proposta propostaSalva = propostaRepository.save(proposta);

        return montarResponse(propostaSalva);
    }

    private PropostaResponseDTO montarResponse(Proposta proposta) {
        return new PropostaResponseDTO(
                proposta.getId(),
                proposta.getUsuario().getNome(),
                proposta.getPrestadorServico().getUsuario().getNome(),
                proposta.getDescricao(),
                proposta.getValor(),
                proposta.getStatus().name(),
                proposta.getDataCriacao()
        );
    }

    public listaDemandaDRO listaDemanda(Long prestadorId) {

        LocalDateTime dataInicio = LocalDateTime.now().minusDays(30);

        Integer novas = propostaRepository.contarNovasDemandas(prestadorId);
        Integer andamento = propostaRepository.contarEmAndamento(prestadorId);
        Integer concluido = propostaRepository.contarDemandasConcluida(prestadorId);
        BigDecimal faturamento = propostaRepository
                .faturamentoUltimos30Dias(prestadorId, dataInicio);


        List<Proposta> propostas = propostaRepository.buscarDemandasProximas(prestadorId);

        List<DemandaResumoDTO> demandas = propostas.stream()
                .map(p -> new DemandaResumoDTO(
                        p.getId(),
                        p.getTitulo(),
                        p.getStatus(),
                        p.getValor()
                ))
                .toList();

        return new listaDemandaDRO(
                novas,
                andamento,
                concluido,
                faturamento,
                demandas
        );
    }
}
