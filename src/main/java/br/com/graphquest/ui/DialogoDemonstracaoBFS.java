package br.com.graphquest.ui;

import br.com.graphquest.grafo.PassoBFS;
import br.com.graphquest.grafo.EstadoBFS;
import br.com.graphquest.grafo.ResultadoBFS;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.jogo.JogoListener;
import br.com.graphquest.modelo.Sala;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.stream.Collectors;

/** Controles para percorrer os eventos registrados pela execução real da BFS. */
public class DialogoDemonstracaoBFS extends JDialog {
    private final Jogo jogo;
    private final PainelMapa painelMapa;
    private final ResultadoBFS resultado;
    private final JLabel lblProgresso = new JLabel();
    private final JTextArea txtEtapa = new JTextArea();
    private final JLabel lblFila = new JLabel();
    private final JLabel lblVisitadas = new JLabel();
    private final JLabel lblAnalisadas = new JLabel();
    private final JLabel lblDescobertas = new JLabel();
    private final JLabel lblLegenda = new JLabel();
    private final JButton btnAnterior;
    private final JButton btnProximo;
    private final JButton btnAuto;
    private final Timer timer;
    private final int versaoPartida;
    private final int movimentosIniciais;
    private int indice;
    private boolean reproduzindo;
    private final JogoListener listener;

    public DialogoDemonstracaoBFS(JFrame proprietario, Jogo jogo, PainelMapa painelMapa,
                                  ResultadoBFS resultado) {
        super(proprietario, "BFS passo a passo", true);
        this.jogo = jogo;
        this.painelMapa = painelMapa;
        this.resultado = resultado;
        this.versaoPartida = jogo.getVersaoPartida();
        this.movimentosIniciais = jogo.getQuantidadeMovimentos();
        this.listener = () -> {
            if (jogo.getVersaoPartida() != versaoPartida
                    || jogo.getQuantidadeMovimentos() != movimentosIniciais) {
                SwingUtilities.invokeLater(this::dispose);
            }
        };
        jogo.registrarOuvinte(listener);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(560, 420));
        setSize(660, 480);
        setLocationRelativeTo(proprietario);

        JPanel raiz = new JPanel(new BorderLayout(12, 12));
        raiz.setBackground(TemaVisual.COR_SUPERFICIE);
        raiz.setBorder(BorderFactory.createEmptyBorder(18, 20, 16, 20));

        JLabel titulo = new JLabel("Como a busca percorre o mapa");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);
        lblProgresso.setFont(TemaVisual.FONTE_PEQUENA);
        lblProgresso.setForeground(TemaVisual.COR_TEXTO_MUTED);
        JPanel cabecalho = new JPanel(new BorderLayout(4, 4));
        cabecalho.setOpaque(false);
        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(lblProgresso, BorderLayout.SOUTH);

        txtEtapa.setFont(TemaVisual.FONTE_SUBTITULO);
        txtEtapa.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        txtEtapa.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        txtEtapa.setLineWrap(true);
        txtEtapa.setWrapStyleWord(true);
        txtEtapa.setEditable(false);
        txtEtapa.setFocusable(false);
        txtEtapa.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        JPanel detalhes = new JPanel(new GridLayout(5, 1, 4, 4));
        detalhes.setOpaque(false);
        configurarLinha(lblFila);
        configurarLinha(lblVisitadas);
        configurarLinha(lblAnalisadas);
        configurarLinha(lblDescobertas);
        configurarLinha(lblLegenda);
        detalhes.add(lblFila);
        detalhes.add(lblVisitadas);
        detalhes.add(lblAnalisadas);
        detalhes.add(lblDescobertas);
        lblLegenda.setText("Mapa: laranja processando • azul na fila • verde descoberta • cinza visitada");
        lblLegenda.setForeground(new Color(190, 198, 210));
        detalhes.add(lblLegenda);
        JPanel centro = new JPanel(new BorderLayout(0, 10));
        centro.setOpaque(false);
        centro.add(txtEtapa, BorderLayout.CENTER);
        centro.add(detalhes, BorderLayout.SOUTH);

        btnAnterior = TemaVisual.criarBotao("← Anterior", new Color(45, 55, 72), new Color(60, 75, 95));
        btnProximo = TemaVisual.criarBotao("Próximo →", new Color(45, 75, 105), new Color(55, 100, 140));
        btnAuto = TemaVisual.criarBotao("▶ Executar", new Color(24, 120, 82), new Color(32, 150, 100));
        JButton btnReiniciar = TemaVisual.criarBotao("Reiniciar", new Color(90, 65, 30), new Color(120, 85, 38));
        JButton btnFechar = TemaVisual.criarBotao("Fechar", new Color(45, 55, 72), new Color(60, 75, 95));
        JPanel acoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 7, 0));
        acoes.setOpaque(false);
        acoes.add(btnAnterior);
        acoes.add(btnProximo);
        acoes.add(btnAuto);
        acoes.add(btnReiniciar);
        acoes.add(btnFechar);

        raiz.add(cabecalho, BorderLayout.NORTH);
        raiz.add(centro, BorderLayout.CENTER);
        raiz.add(acoes, BorderLayout.SOUTH);
        setContentPane(raiz);

        btnAnterior.addActionListener(e -> mostrarPasso(indice - 1));
        btnProximo.addActionListener(e -> mostrarPasso(indice + 1));
        btnAuto.addActionListener(e -> alternarReproducao());
        btnReiniciar.addActionListener(e -> { pausar(); mostrarPasso(0); });
        btnFechar.addActionListener(e -> dispose());
        timer = new Timer(900, e -> {
            if (indice >= resultado.getPassos().size() - 1) pausar();
            else mostrarPasso(indice + 1);
        });
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent e) {
                pausar();
                jogo.removerOuvinte(listener);
                painelMapa.exibirPassoDemonstracao(null);
                if (jogo.isMesmaPartida(versaoPartida, movimentosIniciais)) {
                    jogo.executarBFSAteSaida();
                }
            }
        });

        mostrarPasso(0);
    }

    private void configurarLinha(JLabel label) {
        label.setFont(TemaVisual.FONTE_NORMAL);
        label.setForeground(TemaVisual.COR_TEXTO_CINZA);
    }

    public void mostrarPasso(int novoIndice) {
        List<PassoBFS> passos = resultado.getPassos();
        if (passos.isEmpty()) return;
        indice = Math.max(0, Math.min(novoIndice, passos.size() - 1));
        PassoBFS passo = passos.get(indice);
        EstadoBFS estado = resultado.criarEstadoPasso(indice);
        lblProgresso.setText("Etapa " + (indice + 1) + " de " + passos.size()
                + (passo.isConcluido() ? " • demonstração concluída" : ""));
        txtEtapa.setText(passo.getDescricao());
        painelMapa.exibirPassoDemonstracao(estado);
        lblFila.setText("Fila atual: " + formatar(estado.getFila()));
        lblVisitadas.setText("Visitadas: " + formatar(estado.getVisitadas()));
        lblAnalisadas.setText("Vizinhos analisados: " + formatar(estado.getVizinhosAnalisados()));
        lblDescobertas.setText("Novas salas na fila: " + formatar(estado.getRecemDescobertas()));
        btnAnterior.setEnabled(indice > 0);
        btnProximo.setEnabled(indice < passos.size() - 1);
        if (passo.isConcluido()) pausar();
    }

    private String formatar(List<Sala> salas) {
        if (salas.isEmpty()) return "—";
        return salas.stream().map(s -> "[" + s.getId() + "] " + s.getNome())
                .collect(Collectors.joining("  •  "));
    }

    private void alternarReproducao() {
        if (reproduzindo) pausar();
        else if (indice < resultado.getPassos().size() - 1) {
            reproduzindo = true;
            btnAuto.setText("Ⅱ Pausar");
            timer.start();
        }
    }

    private void pausar() {
        reproduzindo = false;
        if (timer != null) timer.stop();
        if (btnAuto != null) btnAuto.setText("▶ Executar");
    }
}
