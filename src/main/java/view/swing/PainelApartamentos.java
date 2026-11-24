package view.swing;

import model.Apartamento;
import repository.ApartamentoRepository;
import repository.MoradorRepository;
import service.ApartamentoService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelApartamentos extends JPanel {

    private final DefaultTableModel modelo;
    private final JTable tabela;

    public PainelApartamentos() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titulo = new JLabel("Apartamentos");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(new Color(23, 43, 62));

        JPanel cabecalho = new JPanel(new BorderLayout(10, 15));
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.setOpaque(false);

        JButton cadastrar = new JButton("Cadastrar");
        JButton remover = new JButton("Remover");
        JButton buscar = new JButton("Buscar");
        JButton atualizar = new JButton("Atualizar");

        botoes.add(cadastrar);
        botoes.add(remover);
        botoes.add(buscar);
        botoes.add(atualizar);

        cabecalho.add(botoes, BorderLayout.SOUTH);
        add(cabecalho, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{"ID", "Torre", "Número"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabela = new JTable(modelo);
        tabela.setRowHeight(30);
        tabela.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        cadastrar.addActionListener(e -> cadastrar());
        remover.addActionListener(e -> remover());
        buscar.addActionListener(e -> buscar());
        atualizar.addActionListener(e -> carregar());

        carregar();
    }

    private ApartamentoService service() {
        return new ApartamentoService(
                new ApartamentoRepository()
        );
    }

    private void carregar() {
        try {
            preencherTabela(service().listarTodos());
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void preencherTabela(List<Apartamento> apartamentos) {

        modelo.setRowCount(0);

        for (Apartamento apartamento : apartamentos) {
            modelo.addRow(new Object[]{
                    apartamento.getId(),
                    apartamento.getTorre(),
                    apartamento.getNumero()
            });
        }
    }

    private void cadastrar() {

        JTextField torre = new JTextField();
        JTextField numero = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(2, 2, 10, 10)
        );

        formulario.add(new JLabel("Torre:"));
        formulario.add(torre);

        formulario.add(new JLabel("Número:"));
        formulario.add(numero);

        int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Cadastrar apartamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String torreValor = torre.getText().trim();
            String numeroValor = numero.getText().trim();

            if (torreValor.isEmpty() || numeroValor.isEmpty()) {
                throw new IllegalArgumentException(
                        "Preencha todos os campos."
                );
            }

            service().cadastrarApartamento(
                    torreValor,
                    numeroValor
            );

            carregar();

            JOptionPane.showMessageDialog(
                    this,
                    "Apartamento cadastrado com sucesso!"
            );

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void remover() {

        try {

            int linha = tabela.getSelectedRow();

            if (linha == -1) {
                throw new IllegalArgumentException(
                        "Selecione um apartamento na tabela."
                );
            }

            Long id = Long.valueOf(
                    modelo.getValueAt(linha, 0).toString()
            );

            String torre = modelo.getValueAt(linha, 1).toString();
            String numero = modelo.getValueAt(linha, 2).toString();

            boolean possuiMoradores = new MoradorRepository()
                    .listarTudo()
                    .stream()
                    .anyMatch(m -> id.equals(m.getApartamentoId()));

            if (possuiMoradores) {
                throw new IllegalStateException(
                        "Este apartamento possui moradores cadastrados."
                );
            }

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente remover este apartamento?",
                    "Confirmação",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta == JOptionPane.YES_OPTION) {

                service().removerApartamento(torre, numero);

                carregar();

                JOptionPane.showMessageDialog(
                        this,
                        "Apartamento removido com sucesso!"
                );
            }

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void buscar() {

        JTextField torre = new JTextField();
        JTextField numero = new JTextField();

        JPanel formulario = new JPanel(
                new GridLayout(2, 2, 10, 10)
        );

        formulario.add(new JLabel("Torre:"));
        formulario.add(torre);

        formulario.add(new JLabel("Número:"));
        formulario.add(numero);

        int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                "Buscar apartamento",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            Apartamento apartamento =
                    service().buscarPorNumeroTorre(
                            torre.getText().trim(),
                            numero.getText().trim()
                    );

            preencherTabela(List.of(apartamento));

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