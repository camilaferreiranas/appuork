package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.localizacao.LocalizacaoPrestadorDTO;
import br.com.uork.appuork.dto.localizacao.LocalizacaoRequestDTO;
import br.com.uork.appuork.dto.prestadorServico.*;
import br.com.uork.appuork.dto.servico.ServicoOferecidoDTO;
import br.com.uork.appuork.dto.usuario.EnderecoResponseDTO;
import br.com.uork.appuork.models.Categoria;
import br.com.uork.appuork.models.Endereco;
import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.repository.CategoriaRepository;
import br.com.uork.appuork.repository.PrestadorServicoRepository;
import br.com.uork.appuork.repository.PropostaRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class PrestadorServicoService {

    private final PrestadorServicoRepository prestadorServicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final PropostaRepository propostaRepository;
    private final PrestadorLocalizacaoCacheService localizacaoCacheService;
    private final FotoPerfilStorageService fotoPerfilStorageService;

    public PrestadorServicoService(PrestadorServicoRepository prestadorServicoRepository,
                                   UsuarioRepository usuarioRepository,
                                   CategoriaRepository categoriaRepository,
                                   PropostaRepository propostaRepository,
                                   PrestadorLocalizacaoCacheService localizacaoCacheService,
                                   FotoPerfilStorageService fotoPerfilStorageService) {
        this.prestadorServicoRepository = prestadorServicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.categoriaRepository = categoriaRepository;
        this.propostaRepository = propostaRepository;
        this.localizacaoCacheService = localizacaoCacheService;
        this.fotoPerfilStorageService = fotoPerfilStorageService;
    }

    @Transactional
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

        Endereco endereco = criarEndereco(dto.endereco());

        // 6. Criar prestador
        PrestadorServico prestador = new PrestadorServico();
        prestador.setUsuario(usuario);
        prestador.setCategorias(categorias);
        prestador.setDescricao(dto.descricao().trim());
        prestador.setMediaAvaliacoes(0.0);
        prestador.setTotalAvaliacoes(0);
        prestador.setAtivo(true);

        usuario.setEndereco(endereco);
        usuarioRepository.save(usuario);

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

    @Transactional(readOnly = true)
    public Page<PrestadorListDTO> listarPrestadores(
            Pageable pageable,
            Long categoriaId,
            String busca,
            Double latitude,
            Double longitude,
            String emailUsuarioAtual) {

        validarCoordenadas(latitude, longitude);

        Usuario usuarioAtual = usuarioRepository
                .findByEmailIgnoreCase(emailUsuarioAtual)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        Long usuarioIdExcluir = usuarioAtual.getId();
        String termoBusca = normalizarTermoBusca(busca);

        if (latitude == null) {
            Page<PrestadorServico> pagina =
                    prestadorServicoRepository.buscarPorCategoria(
                            categoriaId,
                            usuarioIdExcluir,
                            termoBusca,
                            pageable
                    );

            return pagina.map(prestador -> montarPrestadorListDTO(prestador, null, null));
        }

        List<PrestadorListDTO> prestadores = prestadorServicoRepository
                .buscarPorCategoria(categoriaId, usuarioIdExcluir, termoBusca)
                .stream()
                .map(prestador -> montarPrestadorListDTO(prestador, latitude, longitude))
                .sorted(Comparator.comparing(
                        PrestadorListDTO::distanciaKm,
                        Comparator.nullsLast(Double::compareTo)
                ))
                .toList();

        int inicio = Math.toIntExact(pageable.getOffset());
        if (inicio >= prestadores.size()) {
            return new PageImpl<>(List.of(), pageable, prestadores.size());
        }

        int fim = Math.min(inicio + pageable.getPageSize(), prestadores.size());
        return new PageImpl<>(prestadores.subList(inicio, fim), pageable, prestadores.size());
    }

    private String normalizarTermoBusca(String busca) {
        if (busca == null || busca.isBlank()) {
            return null;
        }

        return Normalizer.normalize(busca.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("\\s+", " ");
    }

    public LocalizacaoPrestadorDTO atualizarLocalizacao(
            String email,
            LocalizacaoRequestDTO localizacaoRequest) {
        if (localizacaoRequest == null) {
            throw new IllegalArgumentException("Localização é obrigatória");
        }

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        PrestadorServico prestador = prestadorServicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException(
                        "Usuário não está cadastrado como prestador"
                ));

        return localizacaoCacheService.salvar(
                prestador.getId(),
                localizacaoRequest.latitude(),
                localizacaoRequest.longitude()
        );
    }

    private Endereco criarEndereco(EnderecoResponseDTO dto) {
        if (dto == null) {
            throw new RuntimeException("Endereço é obrigatório");
        }

        String cep = somenteDigitos(dto.cep());
        if (cep.length() != 8) {
            throw new RuntimeException("CEP deve conter 8 números");
        }

        String rua = campoObrigatorio(dto.rua(), "Rua");
        String numero = campoObrigatorio(dto.numero(), "Número");
        String bairro = campoObrigatorio(dto.bairro(), "Bairro");
        String cidade = campoObrigatorio(dto.cidade(), "Cidade");
        String estado = campoObrigatorio(dto.estado(), "Estado").toUpperCase(Locale.ROOT);
        if (estado.length() != 2) {
            throw new RuntimeException("Estado deve conter a sigla com 2 letras");
        }

        Endereco endereco = new Endereco();
        endereco.setCep(cep);
        endereco.setRua(rua);
        endereco.setNumero(numero);
        endereco.setBairro(bairro);
        endereco.setCidade(cidade);
        endereco.setEstado(estado);
        return endereco;
    }

    private String campoObrigatorio(String valor, String nomeCampo) {
        if (valor == null || valor.isBlank()) {
            throw new RuntimeException(nomeCampo + " é obrigatório");
        }
        return valor.trim();
    }

    private String somenteDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    @Transactional(readOnly = true)
    public boolean usuarioEstaCadastradoComoPrestador(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        return prestadorServicoRepository.existsByUsuario(usuario);
    }

    private PrestadorListDTO montarPrestadorListDTO(
            PrestadorServico prestador,
            Double latitude,
            Double longitude) {

        List<String> categorias = prestador.getCategorias()
                .stream()
                .map(Categoria::getNome)
                .toList();

        return new PrestadorListDTO(
                prestador.getId(),
                prestador.getUsuario().getNome(),
                fotoPerfilStorageService.gerarUrlLeitura(
                        prestador.getUsuario().getFotoPerfilKey()
                ),
                categorias,
                prestador.getMediaAvaliacoes(),
                calcularDistancia(prestador.getId(), latitude, longitude)
        );
    }

    private void validarCoordenadas(Double latitude, Double longitude) {
        if ((latitude == null) != (longitude == null)) {
            throw new IllegalArgumentException("Latitude e longitude devem ser informadas juntas");
        }

        if (latitude == null) {
            return;
        }

        if (!Double.isFinite(latitude) || latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude deve estar entre -90 e 90");
        }

        if (!Double.isFinite(longitude) || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude deve estar entre -180 e 180");
        }
    }

    private Double calcularDistancia(
            Long prestadorId,
            Double latitudeCliente,
            Double longitudeCliente) {

        if (latitudeCliente == null) {
            return null;
        }

        LocalizacaoPrestadorDTO localizacaoPrestador = localizacaoCacheService
                .buscar(prestadorId)
                .orElse(null);

        if (localizacaoPrestador == null) {
            return null;
        }

        final double raioTerraKm = 6371.0088;
        double latitudeOrigem = Math.toRadians(latitudeCliente);
        double latitudeDestino = Math.toRadians(localizacaoPrestador.latitude());
        double diferencaLatitude = latitudeDestino - latitudeOrigem;
        double diferencaLongitude = Math.toRadians(
                localizacaoPrestador.longitude() - longitudeCliente
        );

        double haversine = Math.pow(Math.sin(diferencaLatitude / 2), 2)
                + Math.cos(latitudeOrigem)
                * Math.cos(latitudeDestino)
                * Math.pow(Math.sin(diferencaLongitude / 2), 2);

        double haversineNormalizado = Math.min(1.0, Math.max(0.0, haversine));
        double distancia = 2 * raioTerraKm * Math.asin(Math.sqrt(haversineNormalizado));
        return Math.round(distancia * 100.0) / 100.0;
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
                fotoPerfilStorageService.gerarUrlLeitura(
                        prestador.getUsuario().getFotoPerfilKey()
                ),
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
                fotoPerfilStorageService.gerarUrlLeitura(
                        prestador.getUsuario().getFotoPerfilKey()
                ),
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
