package br.com.graphquest.ui;

import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.jogo.JogoListener;
import br.com.graphquest.jogo.ResumoAventura;
import br.com.graphquest.jogo.PerfilJogador;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal de exploração e jogabilidade do GraphQuest.
 * 
 * Integra o PainelMapa (Java2D com renderização vetorial do grafo),
 * o PainelInformacoes (lateral com controles e BFS) e a barra de telemetria acadêmica.
 */
public class TelaPrincipal extends JFrame implements JogoListener {

    private final Jogo jogo;
    private final PainelMapa painelMapa;
    private final PainelInformacoes painelInformacoes;
    private final JLabel lblBarraStatus;
    private final Timer timerInterface;
    private final Runnable onVoltarMenu;
    private boolean vitoriaExibida = false;

    public TelaPrincipal(Runnable onVoltarMenu) {
        this(onVoltarMenu, new PerfilJogador());
    }

    public TelaPrincipal(Runnable onVoltarMenu, PerfilJogador perfil) {
        super("GraphQuest — A Jornada dos Grafos");
        this.onVoltarMenu = onVoltarMenu;
        this.jogo = new Jogo(perfil);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 780);
        setMinimumSize(new Dimension(1000, 650));
        setLocationRelativeTo(null);

        // Inicialização dos Painéis
        this.painelMapa = new PainelMapa(jogo);
        this.painelInformacoes = new PainelInformacoes(
                jogo,
                this::abrirDialogoConceitos,
                this::abrirDialogoComoJogar,
                this::abrirDemonstracaoBFS
        );
        this.timerInterface = new Timer(1000, e -> painelInformacoes.atualizarTempo());
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) { timerInterface.start(); }
            @Override public void componentHidden(java.awt.event.ComponentEvent e) { timerInterface.stop(); }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosing(java.awt.event.WindowEvent e) { timerInterface.stop(); }
            @Override public void windowClosed(java.awt.event.WindowEvent e) { timerInterface.stop(); }
        });

        jogo.registrarOuvinte(this);
        jogo.registrarOuvinte(() -> painelMapa.repaint());

        JPanel painelGeral = new JPanel(new BorderLayout());
        painelGeral.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // 1. Cabeçalho Superior Elegante
        painelGeral.add(criarCabecalho(), BorderLayout.NORTH);

        // 2. Área Central (Mapa Java2D)
        painelGeral.add(painelMapa, BorderLayout.CENTER);

        // 3. Painel Lateral Direito (Telemetria e Ações)
        painelGeral.add(painelInformacoes, BorderLayout.EAST);

        // 4. Barra de Status Inferior (Métricas do Grafo)
        lblBarraStatus = new JLabel();
        lblBarraStatus.setFont(TemaVisual.FONTE_PEQUENA);
        lblBarraStatus.setForeground(TemaVisual.COR_TEXTO_CINZA);
        lblBarraStatus.setBorder(BorderFactory.createEmptyBorder(6, 16, 6, 16));
        lblBarraStatus.setToolTipText("Movimente-se com WASD ou as setas. Pressione B para calcular a rota BFS.");

        JPanel rodape = new JPanel(new BorderLayout());
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaVisual.COR_BORDA));
        rodape.add(lblBarraStatus, BorderLayout.WEST);

        JLabel lblCreditos = new JLabel("Projeto Acadêmico ADO — Estrutura de Dados em Java  ");
        lblCreditos.setFont(TemaVisual.FONTE_PEQUENA);
        lblCreditos.setForeground(TemaVisual.COR_TEXTO_MUTED);
        rodape.add(lblCreditos, BorderLayout.EAST);

        painelGeral.add(rodape, BorderLayout.SOUTH);

        setContentPane(painelGeral);
        configurarControlesTeclado();
        atualizarMetricasRodape();
    }

    private void configurarControlesTeclado() {
        registrarAtalho("UP", "mover-cima", 0, -1);
        registrarAtalho("W", "mover-cima-w", 0, -1);
        registrarAtalho("DOWN", "mover-baixo", 0, 1);
        registrarAtalho("S", "mover-baixo-s", 0, 1);
        registrarAtalho("LEFT", "mover-esquerda", -1, 0);
        registrarAtalho("A", "mover-esquerda-a", -1, 0);
        registrarAtalho("RIGHT", "mover-direita", 1, 0);
        registrarAtalho("D", "mover-direita-d", 1, 0);
        InputMap entradas = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getRootPane().getActionMap();
        entradas.put(KeyStroke.getKeyStroke("B"), "calcular-bfs-teclado");
        acoes.put("calcular-bfs-teclado", new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!jogo.isPartidaConcluida()) jogo.executarBFSAteSaida();
            }
        });
    }

    private void registrarAtalho(String tecla, String nome, int dx, int dy) {
        InputMap entradas = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap acoes = getRootPane().getActionMap();
        entradas.put(KeyStroke.getKeyStroke(tecla), nome);
        acoes.put(nome, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent e) {
                if (!jogo.isPartidaConcluida()) jogo.moverParaDirecao(dx, dy);
            }
        });
    }

    private JPanel criarCabecalho() {
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaVisual.COR_BORDA),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));

        // Título e Ícone
        JPanel painelTitulos = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        painelTitulos.setOpaque(false);

        JLabel lblLogo = new JLabel("⚔");
        lblLogo.setFont(new Font(Font.SERIF, Font.BOLD, 24));
        lblLogo.setForeground(TemaVisual.COR_DOURADO);

        JLabel lblTitulo = new JLabel("GraphQuest");
        lblTitulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        lblTitulo.setForeground(TemaVisual.COR_DOURADO);

        JLabel lblSub = new JLabel("— A Jornada dos Grafos");
        lblSub.setFont(TemaVisual.FONTE_SUBTITULO);
        lblSub.setForeground(TemaVisual.COR_TEXTO_CINZA);

        painelTitulos.add(lblLogo);
        painelTitulos.add(lblTitulo);
        painelTitulos.add(lblSub);

        // Botões de Acesso Rápido no Cabeçalho
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        painelBotoes.setOpaque(false);

        JButton btnMenu = TemaVisual.criarBotao("Menu Principal", new Color(45, 55, 72), new Color(60, 75, 95));
        btnMenu.addActionListener(e -> {
            if (onVoltarMenu != null) {
                this.setVisible(false);
                onVoltarMenu.run();
            }
        });

        JButton btnConceitos = TemaVisual.criarBotao("Conceitos de Grafos", new Color(30, 64, 110), new Color(40, 85, 145));
        btnConceitos.addActionListener(e -> abrirDialogoConceitos());

        painelBotoes.add(btnConceitos);
        painelBotoes.add(btnMenu);

        cabecalho.add(painelTitulos, BorderLayout.WEST);
        cabecalho.add(painelBotoes, BorderLayout.EAST);
        return cabecalho;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public PainelMapa getPainelMapa() { return painelMapa; }

    private void atualizarMetricasRodape() {
        int totalVertices = jogo.getGrafo().obterQuantidadeVertices();
        int totalArestas = jogo.getGrafo().obterQuantidadeArestas();
        lblBarraStatus.setText(String.format(
                "Grafo ponderado (Lista de Adjacência)  |  Vértices: %d  |  Corredores: %d  |  BFS: O(V + E)  |  Dijkstra: O((V + E) log V)",
                totalVertices, totalArestas
        ));
    }

    public void abrirDialogoConceitos() {
        DialogoConceitos dialogo = new DialogoConceitos(this, jogo.getGrafo());
        dialogo.setVisible(true);
    }

    public void abrirDialogoComoJogar() {
        DialogoComoJogar dialogo = new DialogoComoJogar(this);
        dialogo.setVisible(true);
    }

    private void abrirDemonstracaoBFS() {
        var resultado = jogo.prepararDemonstracaoBFSAteSaida();
        DialogoDemonstracaoBFS dialogo = new DialogoDemonstracaoBFS(this, jogo, painelMapa, resultado);
        dialogo.setVisible(true);
    }

    @Override
    public void onEstadoAtualizado() {
        atualizarMetricasRodape();

        // Verifica vitória e exibe diálogo parabenizando o jogador
        if (jogo.isPartidaConcluida() && !vitoriaExibida) {
            vitoriaExibida = true;
            SwingUtilities.invokeLater(this::exibirTelaVitoria);
        } else if (!jogo.isPartidaConcluida()) {
            vitoriaExibida = false;
        }
    }

    private void exibirTelaVitoria() {
        JDialog dialogo = new JDialog(this, "Aventura concluída", true);
        dialogo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialogo.setResizable(false);

        JPanel conteudo = new JPanel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(TemaVisual.COR_SUPERFICIE);
        conteudo.setBorder(BorderFactory.createEmptyBorder(24, 30, 24, 30));

        JLabel icone = new JLabel("✦");
        icone.setFont(new Font(Font.SERIF, Font.BOLD, 42));
        icone.setForeground(TemaVisual.COR_DOURADO);
        icone.setAlignmentX(Component.CENTER_ALIGNMENT);
        boolean venceu = jogo.isCampanhaVencida();
        JLabel titulo = new JLabel(venceu ? "Você venceu a campanha!" : "A expedição terminou");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        PerfilJogador.ResultadoMoedas moedas = jogo.getRecompensaMoedas();
        JLabel subtitulo = new JLabel("Recompensa: +" + (moedas == null ? 0 : moedas.getTotalRecebido()) + " moedas  •  Saldo: "
                + (moedas == null ? 0 : moedas.getSaldoFinal()));
        subtitulo.setFont(TemaVisual.FONTE_NORMAL);
        subtitulo.setForeground(TemaVisual.COR_TEXTO_CINZA);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel detalheMoedas = new JLabel("Base " + (moedas == null ? 0 : moedas.getBase())
                + "  •  Chefes " + (moedas == null ? 0 : moedas.getChefes())
                + "  •  Objetivos " + (moedas == null ? 0 : moedas.getObjetivos())
                + "  •  Segredos " + (moedas == null ? 0 : moedas.getSegredos())
                + "  •  Eficiência " + (moedas == null ? 0 : moedas.getEficiencia()));
        detalheMoedas.setFont(TemaVisual.FONTE_PEQUENA);
        detalheMoedas.setForeground(TemaVisual.COR_DOURADO_CLARO);
        detalheMoedas.setAlignmentX(Component.CENTER_ALIGNMENT);

        ResumoAventura resumo = jogo.getResumoAventura();
        if (resumo == null) return;
        JPanel placar = new JPanel(new GridLayout(3, 2, 12, 10));
        placar.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        placar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA), BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        adicionarEstatistica(placar, "PONTUAÇÃO", resumo.getPontuacao() + " pts");
        adicionarEstatistica(placar, "TEMPO", ResumoAventura.formatarTempo(resumo.getTempoSegundos()));
        adicionarEstatistica(placar, "MOVIMENTOS", String.valueOf(resumo.getMovimentos()));
        adicionarEstatistica(placar, "EXPLORAÇÃO", resumo.getSalasVisitadas() + "/" + resumo.getTotalSalas()
                + " (" + resumo.getPercentualExploracao() + "%)");
        adicionarEstatistica(placar, "TESOUROS", resumo.getTesourosEncontrados() + "/" + resumo.getTotalTesouros());
        adicionarEstatistica(placar, "MISSÕES", resumo.getMissoesConcluidas() + "/" + resumo.getTotalMissoes());

        JLabel avaliacao = new JLabel("Classificação: " + resumo.getClassificacao() + " — " + resumo.getCriterioClassificacao());
        avaliacao.setFont(TemaVisual.FONTE_PEQUENA);
        avaliacao.setForeground(TemaVisual.COR_DOURADO_CLARO);
        avaliacao.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton jogarNovamente = TemaVisual.criarBotao("Jogar novamente", new Color(24, 120, 82), new Color(32, 150, 100));
        jogarNovamente.addActionListener(e -> {
            vitoriaExibida = false;
            dialogo.dispose();
            jogo.reiniciarPartida();
        });
        JButton voltarMenu = TemaVisual.criarBotao("Voltar ao menu", new Color(45, 55, 72), new Color(60, 75, 95));
        voltarMenu.addActionListener(e -> {
            dialogo.dispose();
            if (onVoltarMenu != null) {
                setVisible(false);
                onVoltarMenu.run();
            }
        });
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        acoes.setOpaque(false);
        acoes.add(jogarNovamente);
        acoes.add(voltarMenu);

        conteudo.add(icone);
        conteudo.add(titulo);
        conteudo.add(Box.createVerticalStrut(6));
        conteudo.add(subtitulo);
        conteudo.add(Box.createVerticalStrut(5));
        conteudo.add(detalheMoedas);
        conteudo.add(Box.createVerticalStrut(22));
        conteudo.add(placar);
        conteudo.add(Box.createVerticalStrut(10));
        conteudo.add(avaliacao);
        conteudo.add(Box.createVerticalStrut(20));
        conteudo.add(acoes);
        dialogo.setContentPane(conteudo);
        dialogo.pack();
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private void adicionarEstatistica(JPanel painel, String rotulo, String valor) {
        JPanel item = new JPanel();
        item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
        item.setOpaque(false);
        JLabel nome = new JLabel(rotulo);
        nome.setFont(TemaVisual.FONTE_PEQUENA);
        nome.setForeground(TemaVisual.COR_TEXTO_MUTED);
        JLabel dado = new JLabel(valor);
        dado.setFont(TemaVisual.FONTE_SUBTITULO);
        dado.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        item.add(nome);
        item.add(Box.createVerticalStrut(3));
        item.add(dado);
        painel.add(item);
    }
}
