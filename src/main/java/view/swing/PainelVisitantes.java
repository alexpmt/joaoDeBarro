package view.swing;

import model.Visitante;
import repository.VisitanteRepository;
import service.VisitanteService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelVisitantes extends JPanel {

    private final VisitanteService service;
    private final JTable tabela;
    private final DefaultTableModel modelo;

    public PainelVisitantes() {
        service = new VisitanteService(
                new VisitanteRepository()
        );

        setLayout(new BorderLayout(0, 20));
        setBackground(new Color(240, 243, 248));
        setBorder(BorderFactory.createEmptyBorder(
                25, 25, 25, 25
        ));

        JLabel titulo = new JLabel("Visitantes");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(22, 41, 60));

        JPanel superior = new JPanel(new BorderLayout(0, 20));
        superior.setOpaque(false);
        superior.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(
                new FlowLayout(FlowLayout.LEFT)
        );
        botoes.setOpaque(false);

        JButton cadastrar = new JButton("Cadastrar");
        JButton remover = new JButton("Remover");
        JButton buscar = new JButton("Buscar CPF");
        JButton atualizar = new JButton("Atualizar");

        botoes.add(cadastrar);
        botoes.add(remover);
        botoes.add(buscar);
        botoes.add(atualizar);

        superior.add(botoes, BorderLayout.SOUTH);
        add(superior, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{"ID", "Nome", "CPF"}, 0
        ) {
            @Override
            public boolean isCellEditable(int linha, int coluna) {
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
        atualizar.addActionListener(e -> atualizarTabela());

        atualizarTabela();
    }

    private void atualizarTabela() {
        preencherTabela(service.listarTodos());
    }

    private void preencherTabela(List<Visitante> visitantes) {
        modelo.setRowCount(0);

        for (Visitante visitante : visitantes) {
            modelo.addRow(new Object[]{
                    visitante.getId(),
                    visitante.getNome(),
                    visitante.getCpf()
            });
        }
    }

    private void cadastrar() {
        JTextField nome = new JTextField();
        JTextField cpf = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(0, 2, 10, 10)
        );

        formulario.add(new JLabel("Nome:"));
        formulario.add(nome);

        formulario.add(new JLabel("CPF:"));
        formulario.add(cpf);

        int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Cadastrar visitante",
                JOptionPane.OK_CANCEL_OPTION
        );

        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            if (nome.getText().isBlank()
                    || cpf.getText().isBlank()) {
                throw new IllegalArgumentException(
                        "Preencha todos os campos."
                );
            }

            service.cadastrarVisitante(
                    nome.getText().trim(),
                    cpf.getText().trim()
            );

            atualizarTabela();

            JOptionPane.showMessageDialog(
                    this,
                    "Visitante cadastrado com sucesso!"
            );

        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void remover() {
        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            mostrarErro("Selecione um visitante na tabela.");
            return;
        }

        String cpf = modelo.getValueAt(
                tabela.convertRowIndexToModel(linha), 2
        ).toString();

        int resposta = JOptionPane.showConfirmDialog(
                this,
                "Deseja remover este visitante?",
                "Confirmar remoção",
                JOptionPane.YES_NO_OPTION
        );

        if (resposta != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.removerVisitante(cpf);

            atualizarTabela();

            JOptionPane.showMessageDialog(
                    this,
                    "Visitante removido com sucesso!"
            );

        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void buscar() {
        String cpf = JOptionPane.showInputDialog(
                this,
                "Informe o CPF do visitante:"
        );

        if (cpf == null || cpf.isBlank()) {
            return;
        }

        try {
            Visitante visitante = service.buscarPorCpf(
                    cpf.trim()
            );

            preencherTabela(List.of(visitante));

        } catch (RuntimeException e) {
            mostrarErro(e.getMessage());
        }
    }

    private void mostrarErro(String mensagem) {
        JOptionPane.showMessageDialog(
                this,
                mensagem,
                "Erro",
                JOptionPane.ERROR_MESSAGE
        );
    }
}