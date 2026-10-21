package br.com.graphquest.ui;

import br.com.graphquest.grafo.ResultadoBFS;
import br.com.graphquest.grafo.ResultadoDijkstra;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.jogo.JogoListener;
import br.com.graphquest.jogo.Missao;
import br.com.graphquest.jogo.ResumoAventura;
import br.com.graphquest.modelo.Sala;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Painel lateral direito de informações, telemetria e controles do aventureiro.
 * 
 * Apresenta a sala atual, botões de ação para salas vizinhas conectadas no grafo,
 * execução e explicação do algoritmo BFS, inventário, placar e histórico de eventos.
 */
public class PainelInformacoes extends JPanel implements JogoListener {

    private final Jogo jogo;
    private final Runnable onAbrirConceitos;
    private final Runnable onAbrirComoJogar;
    private final Runnable onDemonstrarBFS;

    // Componentes de interface que se atualizam a cada ação
    private JLabel lblPontuacao;
    private JLabel lblMovimentos;
    private JLabel lblInventario;
    private JLabel lblProgresso;
    private JLabel lblObjetivo;
    private JLabel lblTempo;
    private JLabel lblFase;
    private JProgressBar barraVida;
    private JLabel lblRecursos;
    private JLabel lblCombate;
    private JButton btnAtacar;
    private JButton btnPocao;
    private JButton btnBomba;
    private JPanel painelMissoes;

    private JLabel lblNomeSala;
    private JLabel lblTipoSala;
    private JTextArea txtDescricaoSala;

    private JPanel painelBotoesVizinhos;
    private JTextArea txtResultadoBFS;
    private JTextArea txtResultadoDijkstra;
    private JButton btnExplicarBFS;
    private JTextArea txtHistoricoMensagens;

    public PainelInformacoes(Jogo jogo, Runnable onAbrirConceitos, Runnable onAbrirComoJogar,
                             Runnable onDemonstrarBFS) {
        this.jogo = jogo;
        this.onAbrirConceitos = onAbrirConceitos;
        this.onAbrirComoJogar = onAbrirComoJogar;
        this.onDemonstrarBFS = onDemonstrarBFS;

        jogo.registrarOuvinte(this);

        setLayout(new BorderLayout());
        setBackground(TemaVisual.COR_SUPERFICIE);
        setPreferredSize(new Dimension(380, 0));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, TemaVisual.COR_BORDA));

        // Painel interno rolável
        JPanel conteudo = new PainelRolavel();
        conteudo.setLayout(new BoxLayout(conteudo, BoxLayout.Y_AXIS));
        conteudo.setBackground(TemaVisual.COR_SUPERFICIE);
        conteudo.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // 1. Cabeçalho de Status / Placar
        conteudo.add(criarCardStatus());
        conteudo.add(Box.createVerticalStrut(12));
        conteudo.add(criarCardCombate());
        conteudo.add(Box.createVerticalStrut(12));

        conteudo.add(criarCardMissoes());
        conteudo.add(Box.createVerticalStrut(12));

        // 2. Card da Câmara Atual
        conteudo.add(criarCardSalaAtual());
        conteudo.add(Box.createVerticalStrut(12));

        // 3. Card de Movimentação (Vizinhos da lista de adjacência)
        conteudo.add(criarCardMovimento());
        conteudo.add(Box.createVerticalStrut(12));

        // 4. Card da Busca de Caminho BFS
        conteudo.add(criarCardBFS());
        conteudo.add(Box.createVerticalStrut(12));

        conteudo.add(criarCardDijkstra());
        conteudo.add(Box.createVerticalStrut(12));

        // 5. Card de Ações Gerais (Reiniciar, Conceitos, Como Jogar)
        conteudo.add(criarCardAcoesGerais());
        conteudo.add(Box.createVerticalStrut(12));

        // 6. Card do Diário de Eventos (Log)
        conteudo.add(criarCardLog());

        JScrollPane scroll = new JScrollPane(conteudo);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        atualizarInterface();
    }

    private JPanel criarCardStatus() {
        JPanel card = new JPanel(new GridLayout(8, 1, 4, 4));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        lblPontuacao = new JLabel("Tesouro Acumulado: 0 pts");
        lblPontuacao.setFont(TemaVisual.FONTE_NEGRITO);
        lblPontuacao.setForeground(TemaVisual.COR_DOURADO);

        lblMovimentos = new JLabel("Passos Realizados: 0");
        lblMovimentos.setFont(TemaVisual.FONTE_NORMAL);
        lblMovimentos.setForeground(TemaVisual.COR_TEXTO_BRANCO);

        lblInventario = new JLabel("Inventário: Vazio");
        lblInventario.setFont(TemaVisual.FONTE_PEQUENA);
        lblInventario.setForeground(TemaVisual.COR_TEXTO_CINZA);

        lblProgresso = new JLabel("Exploração: 1/13 salas • 3 tesouros restantes");
        lblProgresso.setFont(TemaVisual.FONTE_PEQUENA);
        lblProgresso.setForeground(new Color(147, 197, 253));

        lblObjetivo = new JLabel("Objetivo: encontre a Chave Mística na Sala 8");
        lblObjetivo.setFont(TemaVisual.FONTE_PEQUENA);
        lblObjetivo.setForeground(new Color(216, 180, 254));

        lblTempo = new JLabel("Tempo de aventura: 00 s");
        lblTempo.setFont(TemaVisual.FONTE_PEQUENA);
        lblTempo.setForeground(new Color(203, 213, 225));

        lblFase = new JLabel("FASE 1/8 • Catacumbas de Pedra");
        lblFase.setFont(TemaVisual.FONTE_NEGRITO);
        lblFase.setForeground(TemaVisual.COR_DOURADO_CLARO);
        barraVida = new JProgressBar(0, jogo.getVidaMaxima());
        barraVida.setStringPainted(true);
        barraVida.setForeground(new Color(48, 170, 112));
        barraVida.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        lblRecursos = new JLabel("♥ 100 PV   ⚗ 2 poções   ✹ 1 bomba");
        lblRecursos.setFont(TemaVisual.FONTE_PEQUENA);
        lblRecursos.setForeground(TemaVisual.COR_TEXTO_CINZA);

        card.add(lblPontuacao);
        card.add(lblMovimentos);
        card.add(lblInventario);
        card.add(lblProgresso);
        card.add(lblObjetivo);
        card.add(lblTempo);
        card.add(lblFase);
        card.add(barraVida);
        return card;
    }

    private JPanel criarCardCombate() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(48, 31, 38));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(133, 65, 62)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel titulo = new JLabel("⚔ COMBATE");
        titulo.setFont(TemaVisual.FONTE_NEGRITO);
        titulo.setForeground(new Color(255, 190, 145));
        lblCombate = new JLabel("Sem inimigos por perto.");
        lblCombate.setFont(TemaVisual.FONTE_PEQUENA);
        lblCombate.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblRecursos.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnAtacar = TemaVisual.criarBotao("Atacar 28", new Color(135, 48, 44), new Color(175, 62, 53));
        btnPocao = TemaVisual.criarBotao("Poção +40", new Color(32, 112, 75), new Color(45, 145, 94));
        btnBomba = TemaVisual.criarBotao("Bomba 50", new Color(101, 70, 143), new Color(126, 88, 173));
        JPanel acoes = new JPanel(new GridLayout(1, 3, 5, 0));
        acoes.setOpaque(false);
        acoes.add(btnAtacar); acoes.add(btnPocao); acoes.add(btnBomba);
        btnAtacar.addActionListener(e -> jogo.atacar());
        btnPocao.addActionListener(e -> jogo.usarPocao());
        btnBomba.addActionListener(e -> jogo.usarBomba());
        card.add(titulo); card.add(Box.createVerticalStrut(7)); card.add(lblCombate);
        card.add(Box.createVerticalStrut(6)); card.add(lblRecursos); card.add(Box.createVerticalStrut(8)); card.add(acoes);
        return card;
    }

    private JPanel criarCardMissoes() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());
        JLabel titulo = new JLabel("MISSÕES DA AVENTURA");
        titulo.setFont(TemaVisual.FONTE_PEQUENA);
        titulo.setForeground(TemaVisual.COR_TEXTO_MUTED);
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        painelMissoes = new JPanel();
        painelMissoes.setLayout(new BoxLayout(painelMissoes, BoxLayout.Y_AXIS));
        painelMissoes.setOpaque(false);
        painelMissoes.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titulo);
        card.add(Box.createVerticalStrut(8));
        card.add(painelMissoes);
        return card;
    }

    private JPanel criarCardSalaAtual() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JLabel tituloSecao = new JLabel("LOCALIZAÇÃO ATUAL");
        tituloSecao.setFont(TemaVisual.FONTE_PEQUENA);
        tituloSecao.setForeground(TemaVisual.COR_TEXTO_MUTED);
        tituloSecao.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblNomeSala = new JLabel("1. Entrada da Cripta");
        lblNomeSala.setFont(TemaVisual.FONTE_SUBTITULO);
        lblNomeSala.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        lblNomeSala.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTipoSala = new JLabel("Tipo: Ponto de Partida");
        lblTipoSala.setFont(TemaVisual.FONTE_PEQUENA);
        lblTipoSala.setForeground(TemaVisual.COR_ESMERALDA);
        lblTipoSala.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtDescricaoSala = new JTextArea("Descrição da sala...");
        txtDescricaoSala.setFont(TemaVisual.FONTE_NORMAL);
        txtDescricaoSala.setForeground(TemaVisual.COR_TEXTO_CINZA);
        txtDescricaoSala.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        txtDescricaoSala.setLineWrap(true);
        txtDescricaoSala.setWrapStyleWord(true);
        txtDescricaoSala.setEditable(false);
        txtDescricaoSala.setFocusable(false);
        txtDescricaoSala.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtDescricaoSala.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        card.add(tituloSecao);
        card.add(Box.createVerticalStrut(4));
        card.add(lblNomeSala);
        card.add(lblTipoSala);
        card.add(txtDescricaoSala);
        return card;
    }

    private JPanel criarCardMovimento() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JLabel tituloSecao = new JLabel("SALAS CONECTADAS");
        tituloSecao.setFont(TemaVisual.FONTE_PEQUENA);
        tituloSecao.setForeground(TemaVisual.COR_TEXTO_MUTED);
        tituloSecao.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subTexto = new JLabel("Mouse, WASD ou setas • B: rota BFS");
        subTexto.setFont(TemaVisual.FONTE_PEQUENA);
        subTexto.setForeground(TemaVisual.COR_TEXTO_CINZA);
        subTexto.setAlignmentX(Component.LEFT_ALIGNMENT);

        painelBotoesVizinhos = new JPanel();
        painelBotoesVizinhos.setLayout(new BoxLayout(painelBotoesVizinhos, BoxLayout.Y_AXIS));
        painelBotoesVizinhos.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        painelBotoesVizinhos.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(tituloSecao);
        card.add(Box.createVerticalStrut(2));
        card.add(subTexto);
        card.add(Box.createVerticalStrut(8));
        card.add(painelBotoesVizinhos);
        return card;
    }

    private JPanel criarCardBFS() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JLabel tituloSecao = new JLabel("BFS • MENOS PASSAGENS");
        tituloSecao.setFont(TemaVisual.FONTE_PEQUENA);
        tituloSecao.setForeground(TemaVisual.COR_TEXTO_MUTED);
        tituloSecao.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnExecutarBFS = TemaVisual.criarBotao(
                "✦ BFS • Menos passagens",
                new Color(30, 64, 110),
                new Color(40, 85, 145)
        );
        btnExecutarBFS.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnExecutarBFS.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnExecutarBFS.addActionListener(e -> jogo.executarBFSAteSaida());

        txtResultadoBFS = new JTextArea("Clique no botão acima para acionar o algoritmo BFS.", 4, 25);
        txtResultadoBFS.setFont(TemaVisual.FONTE_NORMAL);
        txtResultadoBFS.setForeground(TemaVisual.COR_DOURADO_CLARO);
        txtResultadoBFS.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        txtResultadoBFS.setLineWrap(true);
        txtResultadoBFS.setWrapStyleWord(true);
        txtResultadoBFS.setEditable(false);
        txtResultadoBFS.setFocusable(false);
        txtResultadoBFS.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        txtResultadoBFS.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel painelBotoesAux = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 6));
        painelBotoesAux.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        painelBotoesAux.setAlignmentX(Component.LEFT_ALIGNMENT);

        btnExplicarBFS = TemaVisual.criarBotao("Explicar Teoria BFS", new Color(45, 55, 72), new Color(60, 75, 95));
        btnExplicarBFS.setFont(TemaVisual.FONTE_PEQUENA);
        btnExplicarBFS.setEnabled(false);
        btnExplicarBFS.addActionListener(e -> exibirExplicacaoDidaticaBFS());

        JButton btnLimparRota = TemaVisual.criarBotao("Limpar Rota", new Color(45, 55, 72), new Color(60, 75, 95));
        btnLimparRota.setFont(TemaVisual.FONTE_PEQUENA);
        btnLimparRota.addActionListener(e -> jogo.limparRotaDestacada());

        JButton btnDemonstrar = TemaVisual.criarBotao("▶ Demonstrar BFS passo a passo",
                new Color(24, 120, 82), new Color(32, 150, 100));
        btnDemonstrar.setFont(TemaVisual.FONTE_PEQUENA);
        btnDemonstrar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnDemonstrar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnDemonstrar.addActionListener(e -> onDemonstrarBFS.run());

        painelBotoesAux.add(btnExplicarBFS);
        painelBotoesAux.add(Box.createHorizontalStrut(8));
        painelBotoesAux.add(btnLimparRota);

        card.add(tituloSecao);
        card.add(Box.createVerticalStrut(8));
        card.add(btnExecutarBFS);
        card.add(Box.createVerticalStrut(8));
        card.add(btnDemonstrar);
        card.add(Box.createVerticalStrut(8));
        card.add(txtResultadoBFS);
        card.add(Box.createVerticalStrut(4));
        card.add(painelBotoesAux);
        return card;
    }

    private JPanel criarCardAcoesGerais() {
        JPanel card = new JPanel(new GridLayout(1, 3, 6, 6));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JButton btnReiniciar = TemaVisual.criarBotao("Reiniciar", new Color(120, 40, 40), new Color(160, 50, 50));
        btnReiniciar.setFont(TemaVisual.FONTE_PEQUENA);
        btnReiniciar.addActionListener(e -> {
            int resposta = JOptionPane.showConfirmDialog(
                    this,
                    "Deseja realmente reiniciar a aventura e restaurar a masmorra?",
                    "Confirmar Reinício",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (resposta == JOptionPane.YES_OPTION) {
                jogo.reiniciarPartida();
            }
        });

        JButton btnConceitos = TemaVisual.criarBotao("Conceitos", new Color(45, 55, 72), new Color(60, 75, 95));
        btnConceitos.setFont(TemaVisual.FONTE_PEQUENA);
        btnConceitos.addActionListener(e -> onAbrirConceitos.run());

        JButton btnAjuda = TemaVisual.criarBotao("Como Jogar", new Color(45, 55, 72), new Color(60, 75, 95));
        btnAjuda.setFont(TemaVisual.FONTE_PEQUENA);
        btnAjuda.addActionListener(e -> onAbrirComoJogar.run());

        card.add(btnReiniciar);
        card.add(btnConceitos);
        card.add(btnAjuda);
        return card;
    }

    private JPanel criarCardDijkstra() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());
        JLabel titulo = new JLabel("MENOR CUSTO (DIJKSTRA)");
        titulo.setFont(TemaVisual.FONTE_PEQUENA);
        titulo.setForeground(TemaVisual.COR_TEXTO_MUTED);
        JButton executar = TemaVisual.criarBotao("⚖ Dijkstra • Menor custo", new Color(74, 55, 110), new Color(98, 72, 145));
        executar.setAlignmentX(Component.LEFT_ALIGNMENT);
        executar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        executar.addActionListener(e -> jogo.executarDijkstraAteSaida());
        txtResultadoDijkstra = new JTextArea("Os números nos corredores indicam o custo; passagens sem número custam 1.", 4, 25);
        txtResultadoDijkstra.setFont(TemaVisual.FONTE_NORMAL);
        txtResultadoDijkstra.setForeground(new Color(180, 220, 255));
        txtResultadoDijkstra.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        txtResultadoDijkstra.setLineWrap(true);
        txtResultadoDijkstra.setWrapStyleWord(true);
        txtResultadoDijkstra.setEditable(false);
        txtResultadoDijkstra.setFocusable(false);
        txtResultadoDijkstra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TemaVisual.COR_BORDA), BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        txtResultadoDijkstra.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtResultadoDijkstra.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        card.add(titulo);
        card.add(Box.createVerticalStrut(8));
        card.add(executar);
        card.add(Box.createVerticalStrut(8));
        card.add(txtResultadoDijkstra);
        return card;
    }

    private JPanel criarCardLog() {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JLabel titulo = new JLabel("DIÁRIO DA AVENTURA");
        titulo.setFont(TemaVisual.FONTE_PEQUENA);
        titulo.setForeground(TemaVisual.COR_TEXTO_MUTED);

        txtHistoricoMensagens = new JTextArea(6, 20);
        txtHistoricoMensagens.setFont(TemaVisual.FONTE_PEQUENA);
        txtHistoricoMensagens.setForeground(TemaVisual.COR_TEXTO_CINZA);
        txtHistoricoMensagens.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        txtHistoricoMensagens.setEditable(false);
        txtHistoricoMensagens.setFocusable(false);
        txtHistoricoMensagens.setLineWrap(true);
        txtHistoricoMensagens.setWrapStyleWord(true);

        JScrollPane scrollLog = new JScrollPane(txtHistoricoMensagens);
        scrollLog.setBorder(BorderFactory.createLineBorder(TemaVisual.COR_BORDA));

        card.add(titulo, BorderLayout.NORTH);
        card.add(scrollLog, BorderLayout.CENTER);
        return card;
    }

    private void atualizarInterface() {
        // 1. Placar
        lblPontuacao.setText("Tesouro Acumulado: " + jogo.getPontuacao() + " pts");
        lblMovimentos.setText("Passos Realizados: " + jogo.getQuantidadeMovimentos());
        if (jogo.getInventario().isEmpty()) {
            lblInventario.setText("Inventário: Vazio");
        } else {
            lblInventario.setText("Inventário: " + String.join(", ", jogo.getInventario()));
        }
        long visitadas = jogo.getMapaSalas().values().stream().filter(Sala::isVisitada).count();
        lblProgresso.setText("Exploração: " + visitadas + "/" + jogo.getMapaSalas().size()
                + " salas • " + jogo.getTesourosRestantes() + " tesouros restantes");
        boolean temChave = jogo.getInventario().contains("Chave Mística");
        lblObjetivo.setText(jogo.isPartidaConcluida() ? "Objetivo concluído: você escapou!"
                : "Missão principal: alcance o Portal da Liberdade");
        lblObjetivo.setToolTipText(temChave
                ? "Você já tem a chave; o portão agora é uma rota opcional."
                : "A Chave Mística abre o portão, mas há outras rotas para a saída.");
        lblTempo.setText("Tempo de aventura: " + ResumoAventura.formatarTempo(jogo.getTempoDecorridoSegundos()));
        lblFase.setText("FASE " + jogo.getFaseAtual() + "/" + jogo.getTotalFases() + " • " + jogo.getNomeFase());
        barraVida.setValue(jogo.getVida());
        barraVida.setString("Vida " + jogo.getVida() + "/" + jogo.getVidaMaxima());
        barraVida.setForeground(jogo.getVida() <= 30 ? new Color(202, 65, 58) : new Color(48, 170, 112));
        lblRecursos.setText("⚗ " + jogo.getPocoes() + " poções   ✹ " + jogo.getBombas() + " bombas   ↶ " + jogo.getRecuosRestantes() + " recuos");
        var inimigo = jogo.getInimigoAtual();
        lblCombate.setText(inimigo == null ? "Sem inimigos por perto. Explore a próxima câmara." :
                "<html><b>" + (inimigo.isChefe() ? "BOSS" : "INIMIGO") + "</b> • " + inimigo.getNome()
                        + "<br>Vida: " + inimigo.getVida() + "/" + inimigo.getVidaMaxima() + " PV</html>");
        btnAtacar.setEnabled(inimigo != null);
        btnPocao.setEnabled(jogo.getPocoes() > 0 && jogo.getVida() < jogo.getVidaMaxima());
        btnBomba.setEnabled(inimigo != null && jogo.getBombas() > 0);
        atualizarMissoes();

        // 2. Sala Atual
        Sala atual = jogo.getSalaAtual();
        lblNomeSala.setText(atual.getId() + ". " + atual.getNome());
        lblTipoSala.setText("Categoria: " + atual.getTipo().getNomeExibicao());
        lblTipoSala.setForeground(atual.getTipo().getCorTema());

        StringBuilder desc = new StringBuilder(atual.getDescricao());
        if (atual.temTesouro()) {
            desc.append("\n\n[!] Há um baú de tesouro intacto aqui (+").append(atual.getValorTesouro()).append(" pts)!");
        } else if (atual.isTesouroColetado()) {
            desc.append("\n\n[✓] O baú desta sala já foi aberto.");
        }
        if (atual.getItemConcedido() != null) {
            desc.append("\n\n[★] Local sagrado: concede '").append(atual.getItemConcedido()).append("'.");
        }
        if (jogo.isEventoSurpresaDisponivel(atual)) {
            desc.append("\n\n[✦] Evento surpresa! Um cristal secreto vale +50 pontos.");
        }
        txtDescricaoSala.setText(desc.toString());

        // 3. Botões de Vizinhos Conectados no Grafo
        painelBotoesVizinhos.removeAll();
        List<Sala> vizinhos = jogo.getGrafo().obterVizinhos(atual);

        for (Sala vizinho : vizinhos) {
            int custoVizinho = jogo.getGrafo().obterPeso(atual, vizinho);
            String rotulo = String.format("[%d] (%d) %s", vizinho.getId(), custoVizinho, vizinho.getNome());
            boolean bloqueioSemChave = !jogo.podeAcessarSala(vizinho);
            if (bloqueioSemChave) {
                rotulo += " (Bloqueada)";
            } else if (vizinho.temTesouro()) {
                rotulo += " ★ Tesouro";
            }

            Color corBase = bloqueioSemChave ? new Color(70, 30, 30) : new Color(35, 45, 60);
            Color corHover = bloqueioSemChave ? new Color(90, 40, 40) : new Color(50, 65, 85);

            JButton btnVizinho = TemaVisual.criarBotao(rotulo, corBase, corHover);
            btnVizinho.setEnabled(!jogo.isEmCombate());
            btnVizinho.setToolTipText("Ir para " + vizinho.getNome() + " • custo "
                    + jogo.getGrafo().obterPeso(atual, vizinho));
            btnVizinho.setFont(TemaVisual.FONTE_NORMAL);
            btnVizinho.setHorizontalAlignment(SwingConstants.LEFT);
            btnVizinho.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
            btnVizinho.addActionListener(e -> jogo.moverPara(vizinho));

            painelBotoesVizinhos.add(btnVizinho);
            painelBotoesVizinhos.add(Box.createVerticalStrut(4));
        }

        painelBotoesVizinhos.revalidate();
        painelBotoesVizinhos.repaint();

        // 4. Status da BFS
        ResultadoBFS res = jogo.getUltimoResultadoBFS();
        if (res != null) {
            if (res.isSucesso()) {
                txtResultadoBFS.setText("✓ Menos passagens: " + res.getQuantidadeArestas()
                        + "\nRota pelas salas: " + formatarIdsRota(res.getCaminho()));
                txtResultadoBFS.setToolTipText(res.getRotaFormatada());
                btnExplicarBFS.setEnabled(true);
            } else {
                txtResultadoBFS.setText("✗ " + res.getMensagem());
                btnExplicarBFS.setEnabled(true);
            }
        } else {
            txtResultadoBFS.setText("Clique em 'BFS • Menos passagens' para calcular uma rota.");
            btnExplicarBFS.setEnabled(false);
        }

        ResultadoDijkstra dijkstra = jogo.getUltimoResultadoDijkstra();
        if (dijkstra == null) {
            txtResultadoDijkstra.setText("Clique para achar o menor custo. Dijkstra considera os pesos das passagens.");
        } else if (dijkstra.isSucesso()) {
            txtResultadoDijkstra.setText("✓ Custo: " + dijkstra.getCustoTotal() + " • "
                    + dijkstra.getQuantidadeArestas() + " passagem(ns)\nSalas: " + formatarIdsRota(dijkstra.getCaminho()));
            txtResultadoDijkstra.setToolTipText(dijkstra.getRotaFormatada());
        } else {
            txtResultadoDijkstra.setText("✗ " + dijkstra.getMensagem());
        }

        // 5. Histórico de Mensagens
        StringBuilder sbLog = new StringBuilder();
        List<String> msgs = jogo.getHistoricoMensagens();
        for (int i = msgs.size() - 1; i >= 0; i--) {
            sbLog.append("• ").append(msgs.get(i)).append("\n");
        }
        txtHistoricoMensagens.setText(sbLog.toString());
        txtHistoricoMensagens.setCaretPosition(0);
    }

    private void atualizarMissoes() {
        painelMissoes.removeAll();
        for (Missao missao : jogo.getMissoes()) {
            String marcador = missao.isConcluida() ? "✓" : missao.getEstado() == Missao.Estado.EM_ANDAMENTO ? "◉" : "○";
            Color cor = missao.isPrincipal() ? TemaVisual.COR_DOURADO_CLARO
                    : missao.isConcluida() ? TemaVisual.COR_ESMERALDA : TemaVisual.COR_TEXTO_BRANCO;
            JPanel item = new JPanel();
            item.setLayout(new BoxLayout(item, BoxLayout.Y_AXIS));
            item.setOpaque(false);
            JLabel titulo = new JLabel(marcador + "  " + missao.getTitulo() + (missao.isPrincipal() ? "  ★" : ""));
            titulo.setFont(missao.isPrincipal() ? TemaVisual.FONTE_NEGRITO : TemaVisual.FONTE_PEQUENA);
            titulo.setForeground(cor);
            item.setAlignmentX(Component.LEFT_ALIGNMENT);
            String status = missao.getEstado().getRotulo()
                    + (missao.isPrincipal() ? "" : " • " + missao.getProgressoFormatado())
                    + (missao.getRecompensa() > 0 ? " • +" + missao.getRecompensa() + " pts" : "");
            JLabel detalheStatus = new JLabel(status);
            detalheStatus.setFont(TemaVisual.FONTE_PEQUENA);
            detalheStatus.setForeground(cor);
            detalheStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
            int linhasDescricao = Math.max(1, (int) Math.ceil(missao.getDescricao().length() / 42.0));
            JTextArea descricao = new JTextArea(missao.getDescricao(), linhasDescricao, 42);
            descricao.setFont(TemaVisual.FONTE_PEQUENA);
            descricao.setForeground(TemaVisual.COR_TEXTO_MUTED);
            descricao.setOpaque(false);
            descricao.setEditable(false);
            descricao.setFocusable(false);
            descricao.setLineWrap(true);
            descricao.setWrapStyleWord(true);
            descricao.setBorder(BorderFactory.createEmptyBorder());
            descricao.setAlignmentX(Component.LEFT_ALIGNMENT);
            item.setToolTipText(missao.getEstado().getRotulo() + " — " + missao.getDescricao());
            item.add(titulo);
            item.add(detalheStatus);
            item.add(descricao);
            painelMissoes.add(item);
            painelMissoes.add(Box.createVerticalStrut(5));
        }
        painelMissoes.revalidate();
        painelMissoes.repaint();
    }

    public void atualizarTempo() {
        lblTempo.setText("Tempo de aventura: " + ResumoAventura.formatarTempo(jogo.getTempoDecorridoSegundos()));
    }

    private void exibirExplicacaoDidaticaBFS() {
        ResultadoBFS res = jogo.getUltimoResultadoBFS();
        if (res == null) return;

        JTextArea areaTexto = new JTextArea(res.getExplicacaoDidatica());
        areaTexto.setFont(TemaVisual.FONTE_CODIGO);
        areaTexto.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        areaTexto.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        areaTexto.setEditable(false);
        areaTexto.setMargin(new Insets(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setPreferredSize(new Dimension(650, 420));
        scroll.setBorder(BorderFactory.createLineBorder(TemaVisual.COR_BORDA));

        JOptionPane.showMessageDialog(
                this,
                scroll,
                "Detalhamento Didático: Execução da Busca em Largura (BFS)",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private String formatarIdsRota(List<Sala> caminho) {
        return String.join(" → ", caminho.stream().map(sala -> String.valueOf(sala.getId())).toList());
    }

    @Override
    public void onEstadoAtualizado() {
        if (SwingUtilities.isEventDispatchThread()) {
            atualizarInterface();
        } else {
            SwingUtilities.invokeLater(this::atualizarInterface);
        }
    }

    private static class PainelRolavel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) { return 16; }
        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return Math.max(16, visibleRect.height - 16);
        }
        @Override
        public boolean getScrollableTracksViewportWidth() { return true; }
        @Override
        public boolean getScrollableTracksViewportHeight() { return false; }
    }
}
