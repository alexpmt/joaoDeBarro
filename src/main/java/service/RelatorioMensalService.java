package service;

import enums.StatusChamado;
import enums.StatusCobranca;
import enums.StatusReserva;
import enums.TipoCobranca;
import model.ChamadoManutencao;
import model.Cobranca;
import model.RegistroVisita;
import model.RelatorioMensal;
import model.Reserva;
import repository.CobrancaRepository;
import repository.ManutencaoRepository;
import repository.RegistroVisitaRepository;
import repository.ReservaRepository;
import util.RelatorioPdfGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RelatorioMensalService {

    private final CobrancaRepository cobrancaRepository;
    private final ManutencaoRepository manutencaoRepository;
    private final ReservaRepository reservaRepository;
    private final RegistroVisitaRepository registroVisitaRepository;

    public RelatorioMensalService(
            CobrancaRepository cobrancaRepository,
            ManutencaoRepository manutencaoRepository,
            ReservaRepository reservaRepository,
            RegistroVisitaRepository registroVisitaRepository) {

        this.cobrancaRepository = Objects.requireNonNull(cobrancaRepository);
        this.manutencaoRepository = Objects.requireNonNull(manutencaoRepository);
        this.reservaRepository = Objects.requireNonNull(reservaRepository);
        this.registroVisitaRepository = Objects.requireNonNull(registroVisitaRepository);
    }

    public RelatorioMensal gerarRelatorioMensal(YearMonth mes) {
        if (mes == null) {
            throw new IllegalArgumentException("Informe o mês do relatório.");
        }

        LocalDate inicio = mes.atDay(1);
        LocalDate fim = mes.atEndOfMonth();

        List<Cobranca> cobrancas = cobrancaRepository.listarTudo();
        List<Cobranca> pagas = cobrancas.stream()
                .filter(c -> c.getStatus() == StatusCobranca.PAGO)
                .filter(c -> c.getDataPagamento() != null)
                .filter(c -> dentro(c.getDataPagamento(), inicio, fim))
                .toList();

        Map<TipoCobranca, BigDecimal> porTipo = new EnumMap<>(TipoCobranca.class);
        BigDecimal arrecadacao = BigDecimal.ZERO;
        for (Cobranca c : pagas) {
            BigDecimal recebido = c.getValorPago() != null ? c.getValorPago() : c.getValor();
            arrecadacao = arrecadacao.add(recebido);
            porTipo.merge(c.getTipo(), recebido, BigDecimal::add);
        }

        List<Cobranca> emAtraso = cobrancas.stream()
                .filter(c -> c.estaAtrasada(fim))
                .toList();

        BigDecimal inadimplencia = emAtraso.stream()
                .map(c -> c.valorComEncargos(fim))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<ChamadoManutencao> chamados = manutencaoRepository.listarTudo();

        List<ChamadoManutencao> concluidos = chamados.stream()
                .filter(c -> c.getStatus() == StatusChamado.CONCLUIDO)
                .filter(c -> c.getDataConclusao() != null)
                .filter(c -> dentro(c.getDataConclusao(), inicio, fim))
                .toList();

        BigDecimal gastos = concluidos.stream()
                .map(ChamadoManutencao::getCusto)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int abertos = (int) chamados.stream()
                .filter(c -> dentro(c.getDataAbertura(), inicio, fim))
                .count();

        List<Reserva> reservas = reservaRepository.listarTudo().stream()
                .filter(r -> dentro(r.getData(), inicio, fim))
                .toList();

        Map<StatusReserva, Long> reservasPorStatus = new EnumMap<>(StatusReserva.class);
        for (Reserva r : reservas) {
            reservasPorStatus.merge(r.getStatus(), 1L, Long::sum);
        }

        int visitas = (int) registroVisitaRepository.listarTudo().stream()
                .map(RegistroVisita::getEntrada)
                .filter(Objects::nonNull)
                .filter(e -> dentro(e.toLocalDate(), inicio, fim))
                .count();

        return new RelatorioMensal(
                mes,
                arrecadacao,
                porTipo,
                pagas.size(),
                inadimplencia,
                emAtraso.size(),
                gastos,
                concluidos,
                abertos,
                reservasPorStatus,
                reservas.size(),
                visitas
        );
    }

    public void transformarRelatorioEmPdf(RelatorioMensal relatorio) {
        new RelatorioPdfGenerator().gerar(relatorio);
    }

    private boolean dentro(LocalDate data, LocalDate inicio, LocalDate fim) {
        return !data.isBefore(inicio) && !data.isAfter(fim);
    }
}