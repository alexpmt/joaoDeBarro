package util;

import enums.StatusCobranca;
import enums.StatusReserva;
import enums.TipoCobranca;
import model.Apartamento;
import model.Autorizacao;
import model.ChamadoManutencao;
import model.Cobranca;
import model.Morador;
import model.RegistroAcessoMorador;
import model.RegistroVisita;
import model.Reserva;
import model.Visitante;
import repository.ApartamentoRepository;
import repository.AutorizacaoRepository;
import repository.CobrancaRepository;
import repository.ManutencaoRepository;
import repository.MoradorRepository;
import repository.RegistroAcessoMoradorRepository;
import repository.RegistroVisitaRepository;
import repository.ReservaRepository;
import repository.VisitanteRepository;
import service.RelatorioMensalService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;

/**
 * Popula os CSVs com dados fictícios para testar o sistema e o relatório mensal.
 *
 * ATENÇÃO: apaga os CSVs atuais de ./data (antes, copia todos para
 * data/backup_AAAAMMDD_HHMMSS/). Execute a partir da raiz do projeto.
 *
 * As datas são relativas a hoje, então o mês atual sempre tem movimento.
 * Ao final, gera o PDF do mês atual e do mês anterior em ./relatorios.
 */
public class SeedDados {

    // IDs fixos das áreas comuns (ver AreaComumRepository).
    private static final long CHURRASQUEIRA = 1L, ACADEMIA = 2L, PISCINA = 3L, SALAO = 4L;

    public static void main(String[] args) {

        ApartamentoRepository apartamentos = new ApartamentoRepository();
        MoradorRepository moradores = new MoradorRepository();
        VisitanteRepository visitantes = new VisitanteRepository();
        AutorizacaoRepository autorizacoes = new AutorizacaoRepository();
        ReservaRepository reservas = new ReservaRepository();
        CobrancaRepository cobrancas = new CobrancaRepository();
        RegistroVisitaRepository registrosVisita = new RegistroVisitaRepository();
        RegistroAcessoMoradorRepository acessosMorador = new RegistroAcessoMoradorRepository();
        ManutencaoRepository manutencao = new ManutencaoRepository();

        LocalDate hoje = LocalDate.now();
        YearMonth mes = YearMonth.from(hoje);
        YearMonth anterior = mes.minusMonths(1);

        // ---------- Apartamentos e moradores ----------
        Apartamento ap1 = apartamento(apartamentos, "Torre 1", "101");
        Apartamento ap2 = apartamento(apartamentos, "Torre 1", "102");
        Apartamento ap3 = apartamento(apartamentos, "Torre 2", "201");
        Apartamento ap4 = apartamento(apartamentos, "Torre 2", "202");
        Apartamento ap5 = apartamento(apartamentos, "Torre 2", "301");

        // CPFs fictícios. Taxa = 500 + 50 por vaga de garagem.
        Morador ana = morador(moradores, "Ana Souza", "529.982.247-25", ap1, 1, 2);     // R$ 600
        Morador bruno = morador(moradores, "Bruno Lima", "111.444.777-35", ap2, 0, 1);  // R$ 550
        Morador carla = morador(moradores, "Carla Mendes", "390.533.447-05", ap3, 2, 0); // R$ 500
        Morador diego = morador(moradores, "Diego Rocha", "168.995.350-09", ap4, 0, 2);  // R$ 600
        Morador elisa = morador(moradores, "Elisa Prado", "453.178.287-91", ap5, 1, 1);  // R$ 550

        // ---------- Taxas condominiais: mês anterior (todas pagas) ----------
        for (Morador m : new Morador[]{ana, bruno, carla, diego, elisa}) {
            Cobranca c = taxa(m, anterior.atDay(10));
            pagar(c, anterior.atDay(5), c.getValor().toPlainString());
            cobrancas.salvar(c);
        }

        // ---------- Taxas condominiais: mês atual ----------
        Cobranca taxaAna = taxa(ana, dia(mes, 10));
        pagar(taxaAna, passado(hoje, mes, 5), "600.00");
        cobrancas.salvar(taxaAna);

        // Bruno pagou 3 dias depois do vencimento, com multa e juros (valorPago > valor).
        Cobranca taxaBruno = taxa(bruno, dia(mes, 5));
        pagar(taxaBruno, passado(hoje, mes, 8), "561.54");
        cobrancas.salvar(taxaBruno);

        // Carla: pendente, vencida há 12 dias (atrasada, mas ainda pode reservar).
        cobrancas.salvar(taxa(carla, hoje.minusDays(12)));

        // Diego: pendente, vencida há 45 dias (bloqueada para reservas: > 30 dias).
        cobrancas.salvar(taxa(diego, hoje.minusDays(45)));

        Cobranca taxaElisa = taxa(elisa, dia(mes, 10));
        pagar(taxaElisa, passado(hoje, mes, 9), "550.00");
        cobrancas.salvar(taxaElisa);

        // ---------- Reservas e suas cobranças ----------
        // 1) Salão de festas (6h, R$ 150) — concluída e paga.
        Reserva r1 = reserva(SALAO, ana, passado(hoje, mes, 12), 18, 6, StatusReserva.CONCLUIDO);
        reservas.salvar(r1);
        Cobranca c1 = new Cobranca(r1.getId(), TipoCobranca.TAXA_RESERVA, ana.getId(),
                r1.getData(), new BigDecimal("150.00"), ana.getApartamentoId());
        pagar(c1, r1.getData(), "150.00");
        cobrancas.salvar(c1);

        // 2) Churrasqueira (4h, R$ 80) — concluída e paga.
        Reserva r2 = reserva(CHURRASQUEIRA, bruno, passado(hoje, mes, 15), 12, 4, StatusReserva.CONCLUIDO);
        reservas.salvar(r2);
        Cobranca c2 = new Cobranca(r2.getId(), TipoCobranca.TAXA_RESERVA, bruno.getId(),
                r2.getData(), new BigDecimal("80.00"), bruno.getApartamentoId());
        pagar(c2, r2.getData(), "80.00");
        cobrancas.salvar(c2);

        // 3) Piscina (2h, sem taxa) — concluída.
        reservas.salvar(reserva(PISCINA, elisa, passado(hoje, mes, 18), 10, 2, StatusReserva.CONCLUIDO));

        // 4) Churrasqueira cancelada: taxa original cancelada + multa de 50% (R$ 40) paga.
        Reserva r4 = reserva(CHURRASQUEIRA, bruno, passado(hoje, mes, 20), 12, 4, StatusReserva.CANCELADO);
        reservas.salvar(r4);
        Cobranca c4a = new Cobranca(r4.getId(), TipoCobranca.TAXA_RESERVA, bruno.getId(),
                r4.getData(), new BigDecimal("80.00"), bruno.getApartamentoId());
        c4a.setStatus(StatusCobranca.CANCELADO);
        cobrancas.salvar(c4a);
        Cobranca c4b = new Cobranca(r4.getId(), TipoCobranca.CANCELAMENTO_RESERVA, bruno.getId(),
                r4.getData(), new BigDecimal("40.00"), bruno.getApartamentoId());
        pagar(c4b, r4.getData(), "40.00");
        cobrancas.salvar(c4b);

        // 5) Salão futuro, taxa ainda pendente (vence dia 26, antes do fim do mês).
        Reserva r5 = reserva(SALAO, ana, dia(mes, 28), 19, 4, StatusReserva.RESERVADO);
        reservas.salvar(r5);
        cobrancas.salvar(new Cobranca(r5.getId(), TipoCobranca.TAXA_RESERVA, ana.getId(),
                dia(mes, 26), new BigDecimal("150.00"), ana.getApartamentoId()));

        // 6) Academia futura (1h, sem taxa).
        reservas.salvar(reserva(ACADEMIA, elisa, dia(mes, 22), 7, 1, StatusReserva.RESERVADO));

        // ---------- Visitantes, autorizações e entradas ----------
        Visitante marcos = visitante(visitantes, "Marcos Ferreira", "246.810.121-00");
        Visitante julia = visitante(visitantes, "Julia Alves", "135.791.357-90");
        Visitante pedro = visitante(visitantes, "Pedro Nunes", "987.654.321-00");

        visita(autorizacoes, registrosVisita, ana, marcos, passado(hoje, mes, 3), 14, 16);
        visita(autorizacoes, registrosVisita, ana, julia, passado(hoje, mes, 7), 19, 22);
        visita(autorizacoes, registrosVisita, bruno, pedro, passado(hoje, mes, 12), 10, 11);
        visita(autorizacoes, registrosVisita, carla, marcos, passado(hoje, mes, 19), 15, 18);

        // Visitante que está dentro agora (sem saída).
        autorizacoes.salvar(new Autorizacao(elisa.getId(), julia.getId(), hoje));
        registrosVisita.salvar(new RegistroVisita(julia.getId(), elisa.getId(),
                LocalDateTime.now().minusHours(1)));

        // ---------- Acesso de moradores ----------
        acessosMorador.salvar(new RegistroAcessoMorador(ana.getId(),
                passado(hoje, mes, 2).atTime(7, 30), passado(hoje, mes, 2).atTime(18, 10)));
        acessosMorador.salvar(new RegistroAcessoMorador(bruno.getId(),
                passado(hoje, mes, 4).atTime(8, 0), passado(hoje, mes, 4).atTime(17, 45)));
        acessosMorador.salvar(new RegistroAcessoMorador(carla.getId(),
                LocalDateTime.now().minusHours(3), null)); // ainda no condomínio

        // ---------- Chamados de manutenção ----------
        chamado(manutencao, "Piscina", "Troca da bomba de filtragem", "1200.50");
        chamado(manutencao, "Academia", "Conserto da esteira", "350.00");
        chamado(manutencao, "Salão de festas", "Troca de lâmpadas", "80.00");

        ChamadoManutencao emAtendimento = new ChamadoManutencao(null, "Elevador Torre 1", "Ruído no motor");
        emAtendimento.iniciarAtendimento();
        manutencao.salvar(emAtendimento);

        manutencao.salvar(new ChamadoManutencao(null, "Churrasqueira", "Grelha enferrujada"));

        System.out.println("Dados de exemplo gravados em ./data");

        // ---------- Relatórios ----------
        RelatorioMensalService servico = new RelatorioMensalService(
                cobrancas, manutencao, reservas, registrosVisita);

        for (YearMonth ym : new YearMonth[]{mes, anterior}) {
            servico.transformarRelatorioEmPdf(servico.gerarRelatorioMensal(ym));
        }
    }

    // ---------- auxiliares ----------

    /** Dia do mês, limitado ao último dia (evita dia 30 em fevereiro). */
    private static LocalDate dia(YearMonth mes, int d) {
        return mes.atDay(Math.min(d, mes.lengthOfMonth()));
    }

    /** Dia do mês, mas nunca depois de hoje (para eventos que já aconteceram). */
    private static LocalDate passado(LocalDate hoje, YearMonth mes, int d) {
        LocalDate data = dia(mes, d);
        return data.isAfter(hoje) ? hoje : data;
    }

    private static Apartamento apartamento(ApartamentoRepository repo, String torre, String numero) {
        Apartamento a = new Apartamento(torre, numero);
        repo.salvar(a);
        return a;
    }

    private static Morador morador(MoradorRepository repo, String nome, String cpf,
                                   Apartamento ap, int pets, int vagas) {
        Morador m = new Morador(nome, cpf, ap.getId(), pets, vagas);
        repo.salvar(m);
        return m;
    }

    private static Visitante visitante(VisitanteRepository repo, String nome, String cpf) {
        Visitante v = new Visitante(nome, cpf);
        repo.salvar(v);
        return v;
    }

    private static Cobranca taxa(Morador m, LocalDate vencimento) {
        BigDecimal valor = new BigDecimal("500.00")
                .add(new BigDecimal("50.00").multiply(BigDecimal.valueOf(m.getVagasGaragem())));
        return new Cobranca(null, TipoCobranca.TAXA_CONDOMINIAL, m.getId(),
                vencimento, valor, m.getApartamentoId());
    }

    private static void pagar(Cobranca c, LocalDate dataPagamento, String valorPago) {
        c.setStatus(StatusCobranca.PAGO);
        c.setDataPagamento(dataPagamento);
        c.setValorPago(new BigDecimal(valorPago));
    }

    private static Reserva reserva(long areaId, Morador m, LocalDate data,
                                   int horaInicio, int horas, StatusReserva status) {
        Reserva r = new Reserva(areaId, m.getId(), data, LocalTime.of(horaInicio, 0), horas);
        r.setStatus(status);
        return r;
    }

    private static void visita(AutorizacaoRepository autorizacoes, RegistroVisitaRepository registros,
                               Morador m, Visitante v, LocalDate data, int horaEntrada, int horaSaida) {
        autorizacoes.salvar(new Autorizacao(m.getId(), v.getId(), data));
        RegistroVisita rv = new RegistroVisita(v.getId(), m.getId(), data.atTime(horaEntrada, 0));
        rv.registrarSaida(data.atTime(horaSaida, 0));
        registros.salvar(rv);
    }

    private static void chamado(ManutencaoRepository repo, String area, String descricao, String custo) {
        ChamadoManutencao c = new ChamadoManutencao(null, area, descricao);
        c.iniciarAtendimento();
        c.concluir(new BigDecimal(custo));
        repo.salvar(c);
    }
}