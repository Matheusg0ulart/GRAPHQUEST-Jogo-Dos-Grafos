package br.com.graphquest.ui;

import br.com.graphquest.jogo.PerfilJogador;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

/**
 * Tela Inicial (Menu Principal) do GraphQuest.
 * 
 * Atende ao Requisito 4.1 do enunciado com:
 * - Nome do jogo "GraphQuest" e subtítulo acadêmico sobre grafos.
 * - Botão "Iniciar aventura".
 * - Botão "Como jogar".
 * - Botão "Conceitos de grafos".
 * - Botão "Sair".
 * - Arte vetorial temática desenvolvida inteiramente com Java2D.
 */
public class TelaInicial extends JFrame {

    private TelaPrincipal telaPrincipal;
    private final PerfilJogador perfil = new PerfilJogador();
    private JLabel lblBolsa;

    public TelaInicial() {
        super("GraphQuest — Menu Principal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(820, 640);
        setMinimumSize(new Dimension(750, 580));
        setLocationRelativeTo(null);

        JPanel painelFundo = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Gradiente escuro medieval profundo
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(11, 14, 20),
                        0, h, new Color(20, 26, 38)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, w, h);

                // Desenho decorativo de nós de grafo conectados em segundo plano
                desenharGrafoFundoDecorativo(g2, w, h);

                g2.dispose();
            }
        };

        painelFundo.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 20, 8, 20);

        // 1. Painel de Título e Brasão
        JPanel painelTitulo = new JPanel();
        painelTitulo.setLayout(new BoxLayout(painelTitulo, BoxLayout.Y_AXIS));
        painelTitulo.setOpaque(false);

        JLabel lblBrasao = new JLabel("⚔  🏰  ⚔");
        lblBrasao.setFont(new Font(Font.SERIF, Font.BOLD, 32));
        lblBrasao.setForeground(TemaVisual.COR_DOURADO);
        lblBrasao.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTitulo = new JLabel("GraphQuest");
        lblTitulo.setFont(new Font(Font.SERIF, Font.BOLD, 46));
        lblTitulo.setForeground(TemaVisual.COR_DOURADO);
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSubtitulo = new JLabel("A Jornada dos Grafos");
        lblSubtitulo.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        lblSubtitulo.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblDescricao = new JLabel("Exploração de Masmorra e Algoritmo BFS em Java");
        lblDescricao.setFont(TemaVisual.FONTE_NORMAL);
        lblDescricao.setForeground(TemaVisual.COR_TEXTO_CINZA);
        lblDescricao.setAlignmentX(Component.CENTER_ALIGNMENT);

        painelTitulo.add(lblBrasao);
        painelTitulo.add(Box.createVerticalStrut(10));
        painelTitulo.add(lblTitulo);
        painelTitulo.add(Box.createVerticalStrut(4));
        painelTitulo.add(lblSubtitulo);
        painelTitulo.add(Box.createVerticalStrut(8));
        painelTitulo.add(lblDescricao);
        lblBolsa = new JLabel("✦  " + perfil.moedas() + " moedas  •  " + perfil.getCampanhasVencidas() + " vitórias");
        lblBolsa.setFont(TemaVisual.FONTE_PEQUENA);
        lblBolsa.setForeground(TemaVisual.COR_DOURADO_CLARO);
        lblBolsa.setAlignmentX(Component.CENTER_ALIGNMENT);
        painelTitulo.add(Box.createVerticalStrut(8));
        painelTitulo.add(lblBolsa);

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 20, 30, 20);
        painelFundo.add(painelTitulo, gbc);

        // 2. Card Central com os 4 Botões Obrigatórios
        JPanel cardBotoes = new JPanel();
        cardBotoes.setLayout(new BoxLayout(cardBotoes, BoxLayout.Y_AXIS));
        cardBotoes.setBackground(new Color(24, 30, 42, 220));
        cardBotoes.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(24, 40, 24, 40)
        ));

        // Botão 1: Iniciar Aventura
        JButton btnIniciar = TemaVisual.criarBotao(
                "⚔  Iniciar / Continuar Aventura",
                TemaVisual.COR_DOURADO,
                TemaVisual.COR_DOURADO_CLARO
        );
        btnIniciar.setFont(TemaVisual.FONTE_SUBTITULO);
        btnIniciar.setPreferredSize(new Dimension(300, 44));
        btnIniciar.setMaximumSize(new Dimension(300, 44));
        btnIniciar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIniciar.addActionListener(e -> iniciarAventura());

        // Botão 2: Como Jogar
        JButton btnComoJogar = TemaVisual.criarBotao(
                "📜  Como Jogar",
                new Color(40, 52, 70),
                new Color(55, 72, 95)
        );
        btnComoJogar.setFont(TemaVisual.FONTE_SECAO);
        btnComoJogar.setPreferredSize(new Dimension(280, 40));
        btnComoJogar.setMaximumSize(new Dimension(280, 40));
        btnComoJogar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnComoJogar.addActionListener(e -> new DialogoComoJogar(this).setVisible(true));

        // Botão 3: Conceitos de Grafos
        JButton btnConceitos = TemaVisual.criarBotao(
                "📚  Conceitos de Grafos",
                new Color(30, 64, 110),
                new Color(40, 85, 145)
        );
        btnConceitos.setFont(TemaVisual.FONTE_SECAO);
        btnConceitos.setPreferredSize(new Dimension(280, 40));
        btnConceitos.setMaximumSize(new Dimension(280, 40));
        btnConceitos.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnConceitos.addActionListener(e -> new DialogoConceitos(this).setVisible(true));

        // Botão 4: Sair
        JButton btnSair = TemaVisual.criarBotao(
                "🚪  Sair",
                new Color(60, 25, 25),
                new Color(90, 35, 35)
        );
        btnSair.setFont(TemaVisual.FONTE_SECAO);
        btnSair.setPreferredSize(new Dimension(280, 40));
        btnSair.setMaximumSize(new Dimension(280, 40));
        btnSair.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSair.addActionListener(e -> System.exit(0));

        cardBotoes.add(btnIniciar);
        cardBotoes.add(Box.createVerticalStrut(12));
        cardBotoes.add(btnComoJogar);
        cardBotoes.add(Box.createVerticalStrut(12));
        cardBotoes.add(btnConceitos);
        cardBotoes.add(Box.createVerticalStrut(12));
        JButton btnLoja = TemaVisual.criarBotao("✦  Loja e melhorias", new Color(94, 61, 27), new Color(130, 88, 35));
        JButton btnPerfil = TemaVisual.criarBotao("📜  Inventário e perfil", new Color(55, 48, 78), new Color(78, 65, 108));
        for (JButton botao : new JButton[]{btnLoja, btnPerfil}) {
            botao.setFont(TemaVisual.FONTE_SECAO); botao.setPreferredSize(new Dimension(280, 40));
            botao.setMaximumSize(new Dimension(280, 40)); botao.setAlignmentX(Component.CENTER_ALIGNMENT);
            cardBotoes.add(botao); cardBotoes.add(Box.createVerticalStrut(12));
        }
        btnLoja.addActionListener(e -> abrirLoja());
        btnPerfil.addActionListener(e -> abrirPerfil());
        cardBotoes.add(btnSair);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 20, 20, 20);
        painelFundo.add(cardBotoes, gbc);

        // 3. Rodapé Acadêmico
        JLabel lblRodape = new JLabel("Atividade Prática Acadêmica ADO • Estrutura de Dados em Java");
        lblRodape.setFont(TemaVisual.FONTE_PEQUENA);
        lblRodape.setForeground(TemaVisual.COR_TEXTO_MUTED);
        lblRodape.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 2;
        gbc.insets = new Insets(10, 20, 10, 20);
        painelFundo.add(lblRodape, gbc);

        setContentPane(painelFundo);
    }

    private void iniciarAventura() {
        if (telaPrincipal == null) {
            telaPrincipal = new TelaPrincipal(this::voltarAoMenu, perfil);
        } else if (telaPrincipal.getJogo().isPartidaConcluida()) {
            telaPrincipal.dispose();
            telaPrincipal = new TelaPrincipal(this::voltarAoMenu, perfil);
        }
        this.setVisible(false);
        telaPrincipal.setVisible(true);
    }

    private void abrirPerfil() {
        StringBuilder texto = new StringBuilder("MOEDAS: " + perfil.moedas() + "\nCampanhas vencidas: " + perfil.getCampanhasVencidas()
                + "\nCampanhas perdidas: " + perfil.getCampanhasPerdidas() + "\n\nMELHORIAS\n");
        for (PerfilJogador.Melhoria m : PerfilJogador.Melhoria.values()) texto.append(m.getNome()).append(": nível ")
                .append(perfil.nivel(m)).append("/").append(m.getNivelMaximo()).append("\n");
        texto.append("\nEquipamentos: ").append(perfil.getEquipamentos().isEmpty() ? "nenhum" : perfil.getEquipamentos());
        texto.append("\nConsumíveis guardados: ").append(perfil.getPocoesGuardadas()).append(" poções, ")
                .append(perfil.getBombasGuardadas()).append(" bombas");
        JOptionPane.showMessageDialog(this, texto.toString(), "Perfil do aventureiro", JOptionPane.INFORMATION_MESSAGE);
    }

    private void abrirLoja() {
        JDialog dialogo = new JDialog(this, "Mercado das Relíquias", true);
        JPanel lista = new JPanel(); lista.setLayout(new BoxLayout(lista, BoxLayout.Y_AXIS));
        lista.setBackground(TemaVisual.COR_SUPERFICIE); lista.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JLabel saldo = new JLabel("Bolsa: " + perfil.moedas() + " moedas");
        saldo.setForeground(TemaVisual.COR_DOURADO); saldo.setFont(TemaVisual.FONTE_SUBTITULO); lista.add(saldo);
        for (PerfilJogador.Melhoria m : PerfilJogador.Melhoria.values()) {
            int nivel = perfil.nivel(m); int preco = perfil.custoMelhoria(m);
            adicionarOferta(lista, m.getNome() + " — nível " + nivel + "/" + m.getNivelMaximo() + " — " + m.getDescricao(),
                    nivel >= m.getNivelMaximo() ? "Máximo" : preco + " moedas", () -> perfil.comprarMelhoria(m), saldo, dialogo);
        }
        for (PerfilJogador.Equipamento item : PerfilJogador.Equipamento.values())
            adicionarOferta(lista, item.getNome() + " — " + item.getDescricao(), perfil.possui(item) ? "Adquirido" : item.getPreco() + " moedas",
                    () -> perfil.comprarEquipamento(item), saldo, dialogo, perfil.possui(item));
        adicionarOferta(lista, "Poção de cura — guardar para a próxima campanha", "25 moedas", perfil::comprarPocao, saldo, dialogo);
        adicionarOferta(lista, "Bomba arcana — guardar para a próxima campanha", "35 moedas", perfil::comprarBomba, saldo, dialogo);
        JButton fechar = TemaVisual.criarBotao("Fechar", new Color(55, 60, 70), new Color(75, 80, 95));
        fechar.addActionListener(e -> dialogo.dispose());
        lista.add(Box.createVerticalStrut(10)); lista.add(fechar);
        JScrollPane rolagem = new JScrollPane(lista); rolagem.setPreferredSize(new Dimension(590, 540));
        dialogo.setContentPane(rolagem); dialogo.pack(); dialogo.setLocationRelativeTo(this); dialogo.setVisible(true);
    }

    private void adicionarOferta(JPanel lista, String descricao, String preco, java.util.function.BooleanSupplier compra,
                                 JLabel saldo, JDialog dialogo) { adicionarOferta(lista, descricao, preco, compra, saldo, dialogo, false); }
    private void adicionarOferta(JPanel lista, String descricao, String preco, java.util.function.BooleanSupplier compra,
                                 JLabel saldo, JDialog dialogo, boolean indisponivel) {
        JPanel linha = new JPanel(new BorderLayout(10, 4)); linha.setOpaque(false);
        JLabel nome = new JLabel("<html><b>" + descricao + "</b><br>" + preco + "</html>");
        nome.setForeground(TemaVisual.COR_TEXTO_BRANCO); linha.add(nome, BorderLayout.CENTER);
        JButton comprar = TemaVisual.criarBotao("Comprar", new Color(93, 65, 28), new Color(130, 90, 35));
        comprar.setEnabled(!indisponivel && !preco.equals("Máximo") && !preco.equals("Adquirido"));
        comprar.addActionListener(e -> {
            boolean sucesso = compra.getAsBoolean();
            JOptionPane.showMessageDialog(dialogo, sucesso ? "Compra realizada e salva no perfil." : "Moedas insuficientes.");
            saldo.setText("Bolsa: " + perfil.moedas() + " moedas");
            if (sucesso) comprar.setEnabled(false);
        });
        linha.add(comprar, BorderLayout.EAST); linha.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0)); lista.add(linha);
    }

    private void voltarAoMenu() {
        atualizarResumoPerfil();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    private void atualizarResumoPerfil() {
        if (lblBolsa != null) lblBolsa.setText("✦  " + perfil.moedas() + " moedas  •  " + perfil.getCampanhasVencidas() + " vitórias");
    }

    /**
     * Desenha um grafo translúcido decorativo no fundo para enriquecer a estética visual.
     */
    private void desenharGrafoFundoDecorativo(Graphics2D g2, int w, int h) {
        g2.setColor(new Color(245, 158, 11, 15));
        g2.setStroke(new BasicStroke(1.5f));

        int[][] pontos = {
                {w / 6, h / 4}, {w / 3, h / 6}, {w / 2, h / 3},
                {w * 4 / 6, h / 5}, {w * 5 / 6, h / 3}, {w * 5 / 6, h * 3 / 4},
                {w * 3 / 5, h * 4 / 5}, {w / 3, h * 3 / 4}, {w / 7, h * 2 / 3}
        };

        // Arestas de fundo
        for (int i = 0; i < pontos.length; i++) {
            int proximo = (i + 1) % pontos.length;
            g2.drawLine(pontos[i][0], pontos[i][1], pontos[proximo][0], pontos[proximo][1]);
            int oposto = (i + 3) % pontos.length;
            g2.drawLine(pontos[i][0], pontos[i][1], pontos[oposto][0], pontos[oposto][1]);
        }

        // Nós de fundo
        for (int[] p : pontos) {
            g2.fillOval(p[0] - 6, p[1] - 6, 12, 12);
        }
    }
}
