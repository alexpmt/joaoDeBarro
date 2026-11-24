package view.swing;

import model.Morador;
import repository.ApartamentoRepository;
import repository.MoradorRepository;
import service.MoradorService;
import model.Apartamento;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class PainelMoradores extends JPanel {

    private final DefaultTableModel modelo;
    private final JTable tabela;

    public PainelMoradores() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(241, 245, 249));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titulo = new JLabel("Moradores");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(new Color(23, 43, 62));

        JPanel cabecalho = new JPanel(new BorderLayout(10, 15));
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.NORTH);

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        botoes.setOpaque(false);

        JButton cadastrar = new JButton("Cadastrar");
        JButton editar = new JButton("Editar");
        JButton remover = new JButton("Remover");
        JButton buscar = new JButton("Buscar CPF");
        JButton atualizar = new JButton("Atualizar");

        botoes.add(cadastrar);
        botoes.add(editar);
        botoes.add(remover);
        botoes.add(buscar);
        botoes.add(atualizar);

        cabecalho.add(botoes, BorderLayout.SOUTH);
        add(cabecalho, BorderLayout.NORTH);

        modelo = new DefaultTableModel(
                new String[]{
                        "ID", "Nome", "CPF",
                        "Apartamento ID", "Pets", "Vagas"
                }, 0
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

        cadastrar.addActionListener(e -> formulario(null));
        editar.addActionListener(e -> editar());
        remover.addActionListener(e -> remover());
        buscar.addActionListener(e -> buscar());
        atualizar.addActionListener(e -> carregar());

        carregar();
    }

    private MoradorService service() {
        return new MoradorService(
                new ApartamentoRepository(),
                new MoradorRepository()
        );
    }

    private void carregar() {
        try {
            preencherTabela(service().listarTodos());
        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void preencherTabela(List<Morador> moradores) {

        modelo.setRowCount(0);

        for (Morador morador : moradores) {
            modelo.addRow(new Object[]{
                    morador.getId(),
                    morador.getNome(),
                    morador.getCpf(),
                    morador.getApartamentoId(),
                    morador.getQtdPets(),
                    morador.getVagasGaragem()
            });
        }
    }

    private String cpfSelecionado() {

        int linha = tabela.getSelectedRow();

        if (linha == -1) {
            throw new IllegalArgumentException(
                    "Selecione um morador na tabela."
            );
        }

        return modelo.getValueAt(linha, 2).toString();
    }

    private void formulario(Morador morador) {

        JTextField nome = new JTextField();
        JTextField cpf = new JTextField();

        List<Apartamento> apartamentos =
                new ApartamentoRepository().listarTudo();

        if (apartamentos.isEmpty()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Cadastre um apartamento primeiro.",
                    "Aviso",
                    JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        JComboBox<String> apartamento = new JComboBox<>();

        for (Apartamento ap : apartamentos) {

            apartamento.addItem(
                    "ID " + ap.getId()
                            + " - Torre " + ap.getTorre()
                            + " - Nº " + ap.getNumero()
            );
        }

        JTextField pets = new JTextField("0");
        JTextField vagas = new JTextField("0");

        if (morador != null) {

            nome.setText(morador.getNome());
            cpf.setText(morador.getCpf());
            apartamento.setSelectedIndex(-1);

            for (int i = 0; i < apartamentos.size(); i++) {

                if (apartamentos.get(i).getId()
                        .equals(morador.getApartamentoId())) {

                    apartamento.setSelectedIndex(i);
                    break;
                }
            }
            pets.setText(
                    String.valueOf(morador.getQtdPets())
            );
            vagas.setText(
                    String.valueOf(morador.getVagasGaragem())
            );

            cpf.setEditable(false);
        }

        JPanel formulario = new JPanel(
                new GridLayout(5, 2, 10, 10)
        );

        formulario.add(new JLabel("Nome:"));
        formulario.add(nome);

        formulario.add(new JLabel("CPF:"));
        formulario.add(cpf);

        formulario.add(new JLabel("Apartamento:"));
        formulario.add(apartamento);

        formulario.add(new JLabel("Quantidade de pets:"));
        formulario.add(pets);

        formulario.add(new JLabel("Vagas de garagem:"));
        formulario.add(vagas);

        int resposta = JOptionPane.showConfirmDialog(
                this,
                formulario,
                morador == null
                        ? "Cadastrar morador"
                        : "Editar morador",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (resposta != JOptionPane.OK_OPTION) {
            return;
        }

        try {

            String nomeValor = nome.getText().trim();
            String cpfValor = cpf.getText().trim();

            int indice = apartamento.getSelectedIndex();

            if (indice == -1) {
                throw new IllegalArgumentException(
                        "Selecione um apartamento."
                );
            }

            Long apartamentoId = apartamentos.get(indice).getId();

            int quantidadePets = Integer.parseInt(
                    pets.getText().trim()
            );

            int quantidadeVagas = Integer.parseInt(
                    vagas.getText().trim()
            );

            if (nomeValor.isEmpty() || cpfValor.isEmpty()) {
                throw new IllegalArgumentException(
                        "Nome e CPF são obrigatórios."
                );
            }

            if (quantidadePets < 0 || quantidadeVagas < 0) {
                throw new IllegalArgumentException(
                        "Pets e vagas não podem ser negativos."
                );
            }

            if (morador == null) {

                service().cadastrarMorador(
                        nomeValor,
                        cpfValor,
                        apartamentoId,
                        quantidadePets,
                        quantidadeVagas
                );

            } else {

                service().editarMorador(
                        nomeValor,
                        cpfValor,
                        apartamentoId,
                        quantidadePets,
                        quantidadeVagas
                );
            }

            carregar();

            JOptionPane.showMessageDialog(
                    this,
                    "Operação realizada com sucesso!"
            );

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void editar() {

        try {
            Morador morador = service().buscarPorCpf(
                    cpfSelecionado()
            );

            formulario(morador);

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void remover() {

        try {

            String cpf = cpfSelecionado();

            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente remover este morador?",
                    "Confirmação",
                    JOptionPane.YES_NO_OPTION
            );

            if (resposta == JOptionPane.YES_OPTION) {

                service().removerMorador(cpf);
                carregar();

                JOptionPane.showMessageDialog(
                        this,
                        "Morador removido com sucesso!"
                );
            }

        } catch (Exception e) {
            mostrarErro(e);
        }
    }

    private void buscar() {

        String cpf = JOptionPane.showInputDialog(
                this,
                "Digite o CPF do morador:"
        );

        if (cpf == null || cpf.isBlank()) {
            return;
        }

        try {

            Morador morador = service().buscarPorCpf(
                    cpf.trim()
            );

            preencherTabela(List.of(morador));

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