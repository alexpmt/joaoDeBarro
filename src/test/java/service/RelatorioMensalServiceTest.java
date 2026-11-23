package service;

import enums.StatusCobranca;
import enums.TipoCobranca;
import model.ChamadoManutencao;
import model.Cobranca;
import model.RelatorioMensal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import repository.ManutencaoRepository;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

class RelatorioMensalServiceTest extends ServiceTestSupport {

    @TempDir
    Path tmp;

    private final YearMonth mes = YearMonth.now();
    private final LocalDate hoje = LocalDate.now();

    private RelatorioMensalService service(ManutencaoRepository manutencao) {
        return new RelatorioMensalService(cobrancas, manutencao, reservas, registrosVisita);
    }

    private Cobranca paga(LocalDate dataPagamento, String valor) {
        Cobranca c = new Cobranca(null, TipoCobranca.TAXA_CONDOMINIAL, 1L,
                hoje.minusDays(5), new BigDecimal(valor), 1L);
        c.setStatus(StatusCobranca.PAGO);
        c.setDataPagamento(dataPagamento);
        c.setValorPago(new BigDecimal(valor));
        return c;
    }

    @Test
    void arrecadacaoSomaSoPagamentosDoMes() {
        cobrancas.salvar(paga(hoje, "550.00"));
        cobrancas.salvar(paga(hoje, "150.00"));
        cobrancas.salvar(paga(hoje.minusMonths(1), "999.00")); // outro mês: fora

        RelatorioMensal r = service(new ManutencaoRepository(tmp.resolve("m.csv"))).gerarRelatorioMensal(mes);

        assertEquals(0, new BigDecimal("700.00").compareTo(r.arrecadacao()));
        assertEquals(2, r.cobrancasPagas());
    }

    @Test
    void inadimplenciaInclueEncargosAteOFimDoMes() {
        Cobranca atrasada = new Cobranca(null, TipoCobranca.TAXA_CONDOMINIAL, 1L,
                hoje.minusDays(10), new BigDecimal("550.00"), 1L);
        cobrancas.salvar(atrasada);

        RelatorioMensal r = service(new ManutencaoRepository(tmp.resolve("m.csv"))).gerarRelatorioMensal(mes);

        assertEquals(1, r.cobrancasEmAtraso());
        assertTrue(r.inadimplencia().compareTo(new BigDecimal("550.00")) > 0);
        assertEquals(0, atrasada.valorComEncargos(mes.atEndOfMonth()).compareTo(r.inadimplencia()));
    }

    @Test
    void gastosSomamSoChamadosConcluidosNoMes() {
        ManutencaoRepository manutencao = new ManutencaoRepository(tmp.resolve("m.csv"));

        ChamadoManutencao feito = new ChamadoManutencao(null, "Piscina", "Trocar bomba");
        feito.iniciarAtendimento();
        feito.concluir(new BigDecimal("200.00"));
        manutencao.salvar(feito);

        manutencao.salvar(new ChamadoManutencao(null, "Academia", "Esteira parada")); // aberto

        RelatorioMensal r = service(manutencao).gerarRelatorioMensal(mes);

        assertEquals(0, new BigDecimal("200.00").compareTo(r.gastos()));
        assertEquals(1, r.chamadosConcluidos().size());
        assertEquals(2, r.chamadosAbertos());
    }

    @Test
    void mesNuloLancaExcecao() {
        assertThrows(IllegalArgumentException.class,
                () -> service(new ManutencaoRepository(tmp.resolve("m.csv"))).gerarRelatorioMensal(null));
    }
}