package util;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import model.ChamadoManutencao;
import model.RelatorioMensal;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class RelatorioPdfGenerator {

    private static final Locale PT_BR = Locale.of("pt", "BR");
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final Path pasta;

    public RelatorioPdfGenerator() {
        this(Path.of("relatorios"));
    }

    public RelatorioPdfGenerator(Path pasta) {
        this.pasta = pasta;
    }

    public Path gerar(RelatorioMensal r) {
        Path destino = pasta.resolve(
                "relatorio_" + r.mes() + ".pdf");

        try {
            Files.createDirectories(pasta);

            try (PdfWriter writer = new PdfWriter(destino.toString());
                 PdfDocument pdf = new PdfDocument(writer);
                 Document doc = new Document(pdf)) {

                String nomeMes = r.mes().getMonth().getDisplayName(TextStyle.FULL, PT_BR);

                doc.add(new Paragraph("Condomínio Vista Alegre")
                        .simulateBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER));
                doc.add(new Paragraph("Relatório mensal - " + nomeMes + " de " + r.mes().getYear())
                        .setFontSize(13).setTextAlignment(TextAlignment.CENTER)
                        .setMarginBottom(16));

                secaoResumo(doc, r);
                secaoArrecadacao(doc, r);
                secaoManutencao(doc, r);
                secaoOperacao(doc, r);
            }

        } catch (IOException e) {
            throw new IllegalStateException("Erro ao gerarRelatorioMensal o PDF: " + destino, e);
        }

        return destino;
    }

    private void secaoResumo(Document doc, RelatorioMensal r) {
        doc.add(titulo("Resumo financeiro"));
        Table t = criarTabela();
        addLinha(t, "Arrecadação (" + r.cobrancasPagas() + " cobranças pagas)", stringMoeda(r.arrecadacao()));
        addLinha(t, "Inadimplência (" + r.cobrancasEmAtraso() + " cobranças em atraso)", stringMoeda(r.inadimplencia()));
        addLinha(t, "Gastos com manutenção", stringMoeda(r.gastos()));
        addLinha(t, "Saldo do mês", stringMoeda(r.saldo()));
        doc.add(t);
    }

    private void secaoArrecadacao(Document doc, RelatorioMensal r) {
        doc.add(titulo("Arrecadação por tipo de cobrança"));
        if (r.arrecadacaoPorTipo().isEmpty()) {
            doc.add(new Paragraph("Nenhum pagamento recebido neste mês."));
            return;
        }
        Table t = criarTabela();
        addCabecalhoTabela(t, "Tipo", "Valor recebido");
        r.arrecadacaoPorTipo().forEach((key, value) -> {
            addLinha(t, formataEnum(key.toString()), stringMoeda(value));
        });
        doc.add(t);
    }

    private void secaoManutencao(Document doc, RelatorioMensal r) {
        doc.add(titulo("Manutenção"));
        doc.add(new Paragraph("Chamados abertos no mês: " + r.chamadosAbertos()));

        if (r.chamadosConcluidos().isEmpty()) {
            doc.add(new Paragraph("Nenhum chamado concluído neste mês."));
            return;
        }
        Table t = new Table(UnitValue.createPercentArray(new float[]{20, 40, 20, 20}))
                .useAllAvailableWidth();
        addCabecalhoTabela(t, "Área", "Descrição", "Concluído em", "Custo");
        for (ChamadoManutencao c : r.chamadosConcluidos()) {
            t.addCell(new Cell().add(new Paragraph(c.getArea())));
            t.addCell(new Cell().add(new Paragraph(c.getDescricao())));
            t.addCell(new Cell().add(new Paragraph(c.getDataConclusao().format(DATA))));
            t.addCell(new Cell().add(new Paragraph(stringMoeda(c.getCusto()))
                    .setTextAlignment(TextAlignment.RIGHT)));
        }
        doc.add(t);
    }

    private void secaoOperacao(Document doc, RelatorioMensal r) {
        doc.add(titulo("Reservas e visitantes"));
        Table t = criarTabela();
        addLinha(t, "Reservas no mês", String.valueOf(r.totalReservas()));
        r.reservasPorStatus().forEach((key, value) -> {
            addLinha(t, "   " + formataEnum(key.toString()), String.valueOf(value));
        });
        addLinha(t, "Entradas de visitantes", String.valueOf(r.totalVisitas()));
        doc.add(t);
    }

    private Paragraph titulo(String texto) {
        return new Paragraph(texto).simulateBold().setFontSize(14)
                .setMarginTop(14).setMarginBottom(6);
    }

    private Table criarTabela() {
        return new Table(UnitValue.createPercentArray(new float[]{65, 35}))
                .useAllAvailableWidth();
    }

    private void addCabecalhoTabela(Table t, String... textos) {
        for (String s : textos) {
            t.addHeaderCell(new Cell()
                    .add(new Paragraph(s).simulateBold()));
        }
    }

    private void addLinha(Table t, String rotulo, String valor) {
        t.addCell(new Cell().add(new Paragraph(rotulo)));
        t.addCell(new Cell().add(new Paragraph(valor).setTextAlignment(TextAlignment.RIGHT)));
    }

    private String stringMoeda(BigDecimal v) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(v == null ? BigDecimal.ZERO : v);
    }

    // TAXA_RESERVA -> "Taxa reserva"
    private String formataEnum(String enumName) {
        String s = enumName.toLowerCase(PT_BR).replace('_', ' ');
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }
}