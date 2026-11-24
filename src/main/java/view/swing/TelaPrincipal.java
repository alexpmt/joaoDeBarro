package view.swing;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TelaPrincipal extends JFrame {

    private final Color AZUL_ESCURO = new Color(23, 43, 62);
    private final Color AZUL_CLARO = new Color(49, 91, 118);
    private final Color FUNDO = new Color(241, 245, 249);

    private final JPanel painelConteudo;
    private final CardLayout cardLayout;

    public TelaPrincipal() {

        setTitle("João de Barro - Gestão Condominial");
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        painelConteudo = new JPanel(cardLayout);
        painelConteudo.setBackground(FUNDO);

        add(criarMenuLateral(), BorderLayout.WEST);
        add(painelConteudo, BorderLayout.CENTER);

        painelConteudo.add(criarPainelInicio(), "Início");

        String[] modulos = {
                "Moradores",
                "Apartamentos",
                "Visitantes",
                "Autorizações",
                "Controle de Acesso",
                "Reservas",
                "Manutenção",
                "Cobranças",
                "Relatórios"
        };

        for (String modulo : modulos) {

            switch (modulo) {

                case "Moradores" ->
                        painelConteudo.add(
                                new PainelMoradores(), modulo
                        );

                case "Apartamentos" ->
                        painelConteudo.add(
                                new PainelApartamentos(), modulo
                        );

                case "Visitantes" ->
                        painelConteudo.add(
                                new PainelVisitantes(), modulo
                        );

                default ->
                        painelConteudo.add(
                                criarPainelModulo(modulo), modulo
                        );
            }
        }

        cardLayout.show(painelConteudo, "Início");
    }

    private JPanel criarMenuLateral() {

        JPanel menu = new JPanel();
        menu.setPreferredSize(new Dimension(230, 0));
        menu.setBackground(AZUL_ESCURO);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(new EmptyBorder(25, 15, 20, 15));

        JLabel titulo = new JLabel("João de Barro");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 23));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("GESTÃO CONDOMINIAL");
        subtitulo.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitulo.setForeground(Color.LIGHT_GRAY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        menu.add(titulo);
        menu.add(Box.createVerticalStrut(5));
        menu.add(subtitulo);
        menu.add(Box.createVerticalStrut(30));

        String[] opcoes = {
                "Início",
                "Moradores",
                "Apartamentos",
                "Visitantes",
                "Autorizações",
                "Controle de Acesso",
                "Reservas",
                "Manutenção",
                "Cobranças",
                "Relatórios"
        };

        for (String opcao : opcoes) {
            menu.add(criarBotaoMenu(opcao));
            menu.add(Box.createVerticalStrut(5));
        }

        menu.add(Box.createVerticalGlue());

        JButton sair = criarBotaoMenu("Sair");
        menu.add(sair);

        return menu;
    }

    private JButton criarBotaoMenu(String texto) {

        JButton botao = new JButton(texto);

        botao.setMaximumSize(new Dimension(200, 38));
        botao.setAlignmentX(Component.CENTER_ALIGNMENT);
        botao.setHorizontalAlignment(SwingConstants.LEFT);
        botao.setFont(new Font("SansSerif", Font.PLAIN, 14));
        botao.setForeground(Color.WHITE);
        botao.setBackground(AZUL_ESCURO);
        botao.setFocusPainted(false);
        botao.setBorderPainted(false);
        botao.setOpaque(true);
        botao.setCursor(new Cursor(Cursor.HAND_CURSOR));

        botao.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                botao.setBackground(AZUL_CLARO);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                botao.setBackground(AZUL_ESCURO);
            }
        });

        botao.addActionListener(e -> {

            if (texto.equals("Sair")) {

                int resposta = JOptionPane.showConfirmDialog(
                        this,
                        "Deseja sair do sistema?",
                        "Confirmação",
                        JOptionPane.YES_NO_OPTION
                );

                if (resposta == JOptionPane.YES_OPTION) {
                    dispose();
                }

            } else {
                cardLayout.show(painelConteudo, texto);
            }
        });

        return botao;
    }

    private JPanel criarPainelInicio() {

        JPanel painel = new JPanel(new BorderLayout(20, 20));
        painel.setBackground(FUNDO);
        painel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JPanel cabecalho = new JPanel();
        cabecalho.setOpaque(false);
        cabecalho.setLayout(
                new BoxLayout(cabecalho, BoxLayout.Y_AXIS)
        );

        JLabel titulo = new JLabel("Painel Administrativo");
        titulo.setFont(new Font("SansSerif", Font.BOLD, 30));
        titulo.setForeground(AZUL_ESCURO);

        JLabel descricao = new JLabel(
                "Bem-vindo ao sistema de gestão do condomínio."
        );

        descricao.setFont(new Font("SansSerif", Font.PLAIN, 15));

        cabecalho.add(titulo);
        cabecalho.add(Box.createVerticalStrut(10));
        cabecalho.add(descricao);

        painel.add(cabecalho, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(3, 3, 15, 15));
        cards.setOpaque(false);

        String[] modulos = {
                "Moradores",
                "Apartamentos",
                "Visitantes",
                "Autorizações",
                "Controle de Acesso",
                "Reservas",
                "Manutenção",
                "Cobranças",
                "Relatórios"
        };

        for (String modulo : modulos) {
            cards.add(criarCard(modulo));
        }

        painel.add(cards, BorderLayout.CENTER);

        return painel;
    }

    private JButton criarCard(String modulo) {

        JButton card = new JButton(modulo);

        card.setFont(new Font("SansSerif", Font.BOLD, 17));
        card.setForeground(AZUL_ESCURO);
        card.setBackground(Color.WHITE);
        card.setFocusPainted(false);
        card.setOpaque(true);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        card.setBorder(BorderFactory.createLineBorder(
                new Color(210, 220, 230)
        ));

        card.addActionListener(e ->
                cardLayout.show(painelConteudo, modulo)
        );

        return card;
    }

    private JPanel criarPainelModulo(String modulo) {

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(FUNDO);
        painel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel titulo = new JLabel(modulo);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 28));
        titulo.setForeground(AZUL_ESCURO);

        JLabel mensagem = new JLabel(
                "Módulo em desenvolvimento",
                SwingConstants.CENTER
        );

        mensagem.setFont(new Font("SansSerif", Font.PLAIN, 18));
        mensagem.setForeground(Color.GRAY);

        painel.add(titulo, BorderLayout.NORTH);
        painel.add(mensagem, BorderLayout.CENTER);

        return painel;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            new TelaPrincipal().setVisible(true);
        });
    }
}