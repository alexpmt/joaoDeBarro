package model;

import enums.StatusReserva;
import enums.TipoCobranca;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public record RelatorioMensal(
        YearMonth mes,
        BigDecimal arrecadacao,
        Map<TipoCobranca, BigDecimal> arrecadacaoPorTipo,
        int cobrancasPagas,
        BigDecimal inadimplencia,
        int cobrancasEmAtraso,
        BigDecimal gastos,
        List<ChamadoManutencao> chamadosConcluidos,
        int chamadosAbertos,
        Map<StatusReserva, Long> reservasPorStatus,
        int totalReservas,
        int totalVisitas
) {

    /** Arrecadação do mês menos os gastos com manutenção. */
    public BigDecimal saldo() {
        return arrecadacao.subtract(gastos);
    }
}