package br.com.uork.appuork.service;

import br.com.uork.appuork.dto.relatorio.RelatorioResponseDTO;
import br.com.uork.appuork.dto.relatorio.TransacaoRecenteDTO;
import br.com.uork.appuork.models.PrestadorServico;
import br.com.uork.appuork.models.Usuario;
import br.com.uork.appuork.models.enuns.StatusProposta;
import br.com.uork.appuork.repository.PrestadorServicoRepository;
import br.com.uork.appuork.repository.PropostaRepository;
import br.com.uork.appuork.repository.UsuarioRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final PropostaRepository propostaRepository;
    private final PrestadorServicoRepository prestadorServicoRepository;
    private final UsuarioRepository usuarioRepository;

    public RelatorioService(PropostaRepository propostaRepository,
                            PrestadorServicoRepository prestadorServicoRepository,
                            UsuarioRepository usuarioRepository) {
        this.propostaRepository = propostaRepository;
        this.prestadorServicoRepository = prestadorServicoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public RelatorioResponseDTO gerarRelatorio(String email) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        PrestadorServico prestador = prestadorServicoRepository.findByUsuario(usuario)
                .orElseThrow(() -> new RuntimeException("Prestador de serviço não encontrado"));

        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioTresMeses = LocalDate.now().minusMonths(3).atStartOfDay();

        BigDecimal totalBD = propostaRepository.somarValorPorStatus(prestador, StatusProposta.FINALIZADA);
        BigDecimal mesBD = propostaRepository.somarValorPorStatusEPeriodo(prestador, StatusProposta.FINALIZADA, inicioMes);

        Double totalRecebido = totalBD != null ? totalBD.doubleValue() : 0.0;
        Double mesAtual = mesBD != null ? mesBD.doubleValue() : 0.0;

        Integer propostasConcluidas = propostaRepository.contarPorStatus(prestador, StatusProposta.FINALIZADA);
        Integer propostasEmAndamento = propostaRepository.contarPorStatus(prestador, StatusProposta.PENDENTE);

        Float avaliacao = prestador.getMediaAvaliacoes() != null ? prestador.getMediaAvaliacoes().floatValue() : 0f;

        Map<String, Double> evolucaoMensal = buildEvolucaoMensal(
                propostaRepository.evolucaoMensal(prestador, StatusProposta.FINALIZADA)
        );

        Integer quantidadeDemandas = propostaRepository.contarDemandas(prestador, inicioTresMeses);

        List<TransacaoRecenteDTO> transacoesRecentes = propostaRepository
                .transacoesRecentes(prestador, StatusProposta.FINALIZADA, PageRequest.of(0, 10))
                .stream()
                .map(p -> new TransacaoRecenteDTO(p.getDescricao(), p.getValor(), p.getDataCriacao()))
                .toList();

        return new RelatorioResponseDTO(
                totalRecebido,
                mesAtual,
                propostasConcluidas,
                propostasEmAndamento,
                avaliacao,
                evolucaoMensal,
                quantidadeDemandas,
                transacoesRecentes
        );
    }

    private Map<String, Double> buildEvolucaoMensal(List<Object[]> rows) {
        Map<String, Double> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            BigDecimal valor = (BigDecimal) row[2];
            String key = String.format("%04d-%02d", year, month);
            result.put(key, valor != null ? valor.doubleValue() : 0.0);
        }
        return result;
    }
}
