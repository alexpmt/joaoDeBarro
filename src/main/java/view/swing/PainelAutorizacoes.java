package view.swing;

import model.Autorizacao;
import model.Morador;
import model.Visitante;

import repository.AutorizacaoRepository;
import repository.MoradorRepository;
import repository.VisitanteRepository;

import service.AutorizacaoService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;

public class PainelAutorizacoes extends JPanel {

    private AutorizacaoService service;
    private MoradorRepository moradorRepository;
    private VisitanteRepository visitanteRepository;

    private final JTable tabela;
    private final DefaultTableModel modelo;

    private final DateTimeFormatter formatoData =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    public PainelAutorizacoes() {

        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(240, 243, 248));
        setBorder(BorderFactory.createEmptyBorder(
                25, 25, 25, 25
        ));

        JLabel titulo = new JLabel("Autorizações de Visitas");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(new Color(22, 40, 58));

        JPanel topo = new JPanel(new BorderLayout(0, 25));
        topo.setOpaque(false);
        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(
                new BoxLayout(cabecalho, BoxLayout.Y_AXIS)
        );

        JLabel descricao = new JLabel(
                "Gerencie as permissões de entrada de visitantes no condomínio."
        );

        descricao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        descricao.setForeground(new Color(90, 100, 110));

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(8));
        cabecalho.add(descricao);

        topo.add(cabecalho, BorderLayout.NORTH);

        JPanel botoes = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        botoes.setOpaque(false);

        JButton cadastrar = new JButton("Autorizar visitante");
        JButton remover = new JButton("Revogar autorização");
        JButton buscar = new JButton("Buscar autorização");
        JButton atualizar = new JButton("Atualizar");

        botoes.add(cadastrar);
        botoes.add(remover);
        botoes.add(buscar);
        botoes.add(atualizar);

        topo.add(botoes, BorderLayout.SOUTH);
        add(topo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{
                        "ID", "Morador responsável", "Visitante",
                        "CPF do visitante", "Data autorizada"
                }, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(28);
        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        cadastrar.addActionListener(e -> cadastrar());
        remover.addActionListener(e -> remover());
        buscar.addActionListener(e -> buscar());
        atualizar.addActionListener(e -> atualizar());

        atualizar();
    }

    private void carregarRepositorios() {

        moradorRepository = new MoradorRepository();
        visitanteRepository = new VisitanteRepository();

        service = new AutorizacaoService(
                moradorRepository,
                visitanteRepository,
                new AutorizacaoRepository()
        );
    }

    private void atualizar() {
        try {
            carregarRepositorios();
            preencherTabela(service.listarTodos());
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void preencherTabela(List<Autorizacao> autorizacoes) {

        modelo.setRowCount(0);

        for (Autorizacao autorizacao : autorizacoes) {

            Morador morador = moradorRepository.buscarPorId(
                    autorizacao.getMoradorId()
            );

            Visitante visitante = visitanteRepository.buscarPorId(
                    autorizacao.getVisitanteId()
            );

            modelo.addRow(new Object[]{
                    autorizacao.getId(),
                    morador != null
                            ? morador.getNome() : "Não encontrado",
                    visitante != null
                            ? visitante.getNome() : "Não encontrado",
                    visitante != null
                            ? visitante.getCpf() : "-",
                    autorizacao.getDia().format(formatoData)
            });
        }
    }

    private void cadastrar() {

        try {
            carregarRepositorios();

            List<Morador> moradores =
                    moradorRepository.listarTudo();

            List<Visitante> visitantes =
                    visitanteRepository.listarTudo();

            if (moradores.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cadastre um morador primeiro."
                );
                return;
            }

            if (visitantes.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Cadastre um visitante primeiro."
                );
                return;
            }

            JComboBox<String> campoMorador = new JComboBox<>();

            for (Morador morador : moradores) {
                campoMorador.addItem(
                        morador.getNome() + " - " + morador.getCpf()
                );
            }

            JComboBox<String> campoVisitante = new JComboBox<>();

            for (Visitante visitante : visitantes) {
                campoVisitante.addItem(
                        visitante.getNome() + " - " + visitante.getCpf()
                );
            }

            JTextField campoData = new JTextField(
                    LocalDate.now().format(formatoData)
            );

            JPanel formulario = new JPanel(
                    new GridLayout(0, 2, 10, 10)
            );

            formulario.add(new JLabel("Morador:"));
            formulario.add(campoMorador);

            formulario.add(new JLabel("Visitante:"));
            formulario.add(campoVisitante);

            formulario.add(new JLabel("Data (dd/MM/aaaa):"));
            formulario.add(campoData);

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    formulario,
                    "Cadastrar autorização",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (resposta != JOptionPane.OK_OPTION) {
                return;
            }

            Morador morador = moradores.get(
                    campoMorador.getSelectedIndex()
            );

            Visitante visitante = visitantes.get(
                    campoVisitante.getSelectedIndex()
            );

            LocalDate data = LocalDate.parse(
                    campoData.getText().trim(), formatoData
            );

            service.cadastrarAutorizacao(
                    morador.getCpf(),
                    data,
                    visitante.getCpf(),
                    visitante.getNome()
            );

            atualizar();

            JOptionPane.showMessageDialog(
                    this,
                    "Autorização cadastrada com sucesso!"
            );

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void remover() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Selecione uma autorização na tabela."
            );
            return;
        }

        try {
            Long id = Long.valueOf(
                    tabela.getValueAt(linha, 0).toString()
            );

            carregarRepositorios();

            Autorizacao autorizacao = service.listarTodos()
                    .stream()
                    .filter(a -> a.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Autorização não encontrada."
                            )
                    );

            Morador morador = moradorRepository.buscarPorId(
                    autorizacao.getMoradorId()
            );

            Visitante visitante = visitanteRepository.buscarPorId(
                    autorizacao.getVisitanteId()
            );

            if (morador == null || visitante == null) {
                throw new IllegalStateException(
                        "Morador ou visitante não encontrado."
                );
            }

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja remover esta autorização?",
                    "Confirmar remoção",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta != JOptionPane.YES_OPTION) {
                return;
            }

            service.removerAutorizacao(
                    morador.getCpf(),
                    autorizacao.getDia(),
                    visitante.getCpf()
            );

            atualizar();

            JOptionPane.showMessageDialog(
                    this,
                    "Autorização removida com sucesso!"
            );

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void buscar() {

        try {
            carregarRepositorios();

            JTextField campoCpf = new JTextField();

            JTextField campoData = new JTextField(
                    LocalDate.now().format(formatoData)
            );

            JPanel formulario = new JPanel(
                    new GridLayout(0, 2, 10, 10)
            );

            formulario.add(new JLabel("CPF do visitante:"));
            formulario.add(campoCpf);

            formulario.add(new JLabel("Data (dd/MM/aaaa):"));
            formulario.add(campoData);

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    formulario,
                    "Buscar autorização",
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE
            );

            if (resposta != JOptionPane.OK_OPTION) {
                return;
            }

            LocalDate data = LocalDate.parse(
                    campoData.getText().trim(), formatoData
            );

            List<Autorizacao> resultado =
                    service.buscarPorVisitanteDia(
                            campoCpf.getText().trim(),
                            data
                    );

            preencherTabela(resultado);

            if (resultado.isEmpty()) {
                JOptionPane.showMessageDialog(
                        this,
                        "Nenhuma autorização encontrada."
                );
            }

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void mostrarErro(Exception e) {

        JOptionPane.showMessageDialog(
                this,
                e.getMessage(),
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}