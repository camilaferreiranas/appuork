package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.prestadorServico.*;
import br.com.uork.appuork.dto.servico.ServicoOferecidoDTO;
import br.com.uork.appuork.models.Categoria;
import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.CategoriaRepository;
import br.com.uork.appuork.repository.PrestadorServicoRepository;
import br.com.uork.appuork.repository.PropostaRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class PrestadorServicoService {

    private final PrestadorServicoRepository prestadorServicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PropostaRepository propostaRepository;

    public PrestadorServicoService(PrestadorServicoRepository prestadorServicoRepository,
                                   UsuarioRepository usuarioRepository,
                                   CategoriaRepository categoriaRepository,
                                   PropostaRepository propostaRepository) {
        this.prestadorServicoRepository = prestadorServicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.propostaRepository = propostaRepository;
    }

    public PrestadorResponseDTO criarPrestador(String email, PrestadorCreateDTO dto) {

        // 1. Buscar usuário
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        // 2. Verificar se já é prestador
        if (prestadorServicoRepository.existsByUsuario(usuario)) {
            throw new RuntimeException("Usuário já está cadastrado como prestador");
        }
        // 3. Validar campo de descrição
        if (dto.descricao() == null || dto.descricao().isBlank()) {
            throw new RuntimeException("Descrição é obrigatória e não pode ser vazia");
        }

        // 4. Validar lista de categorias
        if (dto.categoriasIds() == null || dto.categoriasIds().isEmpty()) {
            throw new RuntimeException("É necessário informar ao menos uma categoria");
        }

        // 5. Buscar categorias
        List<Categoria> categorias = categoriaRepository.findAllById(dto.categoriasIds());

        // 6. Validar se todas existem
        if (categorias.size() != dto.categoriasIds().size()) {
            throw new RuntimeException("Uma ou mais categorias informadas não existem");
        }

        // 6. Criar prestador
        PrestadorServico prestador = new PrestadorServico();
        prestador.setUsuario(usuario);
        prestador.setCategorias(categorias);
        prestador.setDescricao(dto.descricao());
        prestador.setMediaAvaliacoes(0.0);
        prestador.setTotalAvaliacoes(0);
        prestador.setAtivo(true);

        // 7. Salvar
        PrestadorServico prestadorSalvo = prestadorServicoRepository.save(prestador);

        // 8. Converter categorias para nome
        List<String> nomesCategorias = prestadorSalvo.getCategorias()
                .stream()
                .map(Categoria::getNome)
                .toList();

        // 9. Retornar DTO
        return new PrestadorResponseDTO(
                prestadorSalvo.getId(),
                prestadorSalvo.getUsuario().getNome(),
                prestadorSalvo.getDescricao(),
                nomesCategorias,
                prestadorSalvo.getAtivo()
        );
    }

    public Page<PrestadorListDTO> listarPrestadores(Pageable pageable, Long categoriaId) {

        Page<PrestadorServico> pagina =
                prestadorServicoRepository.buscarPorCategoria(categoriaId, pageable);

        return pagina.map(prestador -> {

            List<String> categorias = prestador.getCategorias()
                    .stream()
                    .map(Categoria::getNome)
                    .toList();

            return new PrestadorListDTO(
                    prestador.getId(),
                    prestador.getUsuario().getNome(),
                    categorias,
                    prestador.getMediaAvaliacoes()
            );
        });
    }

    public PrestadorDetalheDTO buscarPrestadorPorId(Long id) {

        PrestadorServico prestador = prestadorServicoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prestador de serviço não encontrado"));

        List<String> categorias = prestador.getCategorias()
                .stream()
                .map(Categoria::getNome)
                .toList();

        return new PrestadorDetalheDTO(
                prestador.getId(),
                prestador.getUsuario().getNome(),
                prestador.getDescricao(),
                categorias,
                prestador.getMediaAvaliacoes(),
                prestador.getTotalAvaliacoes(),
                prestador.getAtivo()
        );
    }

    public PerfilPrestadorDTO buscarPerfil(Long prestadorId) {

        PrestadorServico prestador = prestadorServicoRepository.findById(prestadorId)
                .orElseThrow(() -> new RuntimeException("Prestador não encontrado"));

        BigDecimal totalGanho = propostaRepository.totalGanho(prestadorId);

        Long concluidas = propostaRepository.totalConcluidas(prestadorId);
        Long totalDemandas = propostaRepository.totalDemandas(prestadorId);

        String dataCriacao = DateTimeFormatter.ofPattern("MMMM 'de' yyyy")
                .withLocale(new Locale("pt", "BR"))
                .format(
                        prestador.getUsuario()
                                .getDataCriacao()
                                .atZone(ZoneId.systemDefault())
                );

        double percentualConclusao = 0.0;

        if (totalDemandas > 0) {
            percentualConclusao =
                    ((double) concluidas / totalDemandas) * 100;
        }

        List<ServicoOferecidoDTO> servicos = prestador.getCategorias()
                .stream()
                .map(categoria -> new ServicoOferecidoDTO(
                        categoria.getNome(),
                        "Serviço especializado em " + categoria.getNome(),
                        BigDecimal.ZERO,
                        prestador.getMediaAvaliacoes()
                ))
                .toList();

        return new PerfilPrestadorDTO(
                prestador.getId(),
                prestador.getUsuario().getNome(),
                prestador.getDescricao(),
                prestador.getUsuario().getEndereco().getCidade(),
                prestador.getUsuario().getEndereco().getEstado(),
                dataCriacao,

                prestador.getMediaAvaliacoes(),
                prestador.getTotalAvaliacoes(),
                percentualConclusao,
                totalGanho,

                prestador.getUsuario().getTelefone(),
                prestador.getUsuario().getEmail(),

                servicos
        );
    }

}
