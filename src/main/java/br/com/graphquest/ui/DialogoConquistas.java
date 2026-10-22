package br.com.graphquest.ui;

import br.com.graphquest.jogo.SistemaConquistas;
import br.com.graphquest.jogo.SistemaConquistas.Conquista;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo visual de Conquistas e Estatísticas de Carreira do GraphQuest.
 * 
 * Exibe todas as 10 conquistas com status de desbloqueio, recompensa em moedas,
 * ícone temático e painel consolidado com a telemetria e recordes do aventureiro.
 */
public class DialogoConquistas extends JDialog {

    public DialogoConquistas(Window proprietario) {
        super(proprietario, "Conquistas e Estatísticas — GraphQuest", ModalityType.APPLICATION_MODAL);
        setSize(780, 580);
        setLocationRelativeTo(proprietario);
        setResizable(false);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, TemaVisual.COR_BORDA),
                BorderFactory.createEmptyBorder(14, 20, 14, 20)
        ));

        JLabel titulo = new JLabel("🏆 Conquistas & Registro de Carreira");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);

        long desbloqueadas = 0;
        for (Conquista c : Conquista.values()) {
            if (SistemaConquistas.isDesbloqueada(c)) desbloqueadas++;
        }

        JLabel progresso = new JLabel(desbloqueadas + " / " + Conquista.values().length + " Desbloqueadas");
        progresso.setFont(TemaVisual.FONTE_SUBTITULO);
        progresso.setForeground(TemaVisual.COR_ESMERALDA);

        cabecalho.add(titulo, BorderLayout.WEST);
        cabecalho.add(progresso, BorderLayout.EAST);
        raiz.add(cabecalho, BorderLayout.NORTH);

        // Painel Central com Tabs (Conquistas | Estatísticas)
        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(TemaVisual.COR_SUPERFICIE);
        abas.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        abas.setFont(TemaVisual.FONTE_NORMAL);

        // Aba 1: Lista de Conquistas
        JPanel painelConquistas = new JPanel();
        painelConquistas.setLayout(new BoxLayout(painelConquistas, BoxLayout.Y_AXIS));
        painelConquistas.setBackground(TemaVisual.COR_SUPERFICIE);
        painelConquistas.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        for (Conquista c : Conquista.values()) {
            painelConquistas.add(criarCardConquista(c));
            painelConquistas.add(Box.createVerticalStrut(8));
        }

        JScrollPane scrollConquistas = new JScrollPane(painelConquistas);
        scrollConquistas.setBorder(null);
        scrollConquistas.getVerticalScrollBar().setUnitIncrement(12);
        abas.addTab("Conquistas Heroicas", scrollConquistas);

        // Aba 2: Estatísticas de Carreira
        JPanel painelStats = criarPainelEstatisticas();
        abas.addTab("Registro de Batalha & Grafos", painelStats);

        raiz.add(abas, BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 10));
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, TemaVisual.COR_BORDA));

        JButton btnFechar = TemaVisual.criarBotao("Fechar", new Color(45, 55, 72), new Color(60, 75, 95));
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);
        raiz.add(rodape, BorderLayout.SOUTH);

        setContentPane(raiz);
    }

    private JPanel criarCardConquista(Conquista c) {
        boolean desbloqueada = SistemaConquistas.isDesbloqueada(c);

        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(desbloqueada ? TemaVisual.COR_SUPERFICIE_CARD : new Color(20, 24, 32));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(desbloqueada ? TemaVisual.COR_DOURADO : TemaVisual.COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));

        // Ícone
        JLabel lblIcone = new JLabel(desbloqueada ? c.getIcone() : "🔒");
        lblIcone.setFont(new Font(Font.SERIF, Font.PLAIN, 28));
        lblIcone.setPreferredSize(new Dimension(42, 42));
        lblIcone.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(lblIcone, BorderLayout.WEST);

        // Textos
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblTitulo = new JLabel(c.getTitulo());
        lblTitulo.setFont(TemaVisual.FONTE_NEGRITO);
        lblTitulo.setForeground(desbloqueada ? TemaVisual.COR_DOURADO_CLARO : TemaVisual.COR_TEXTO_MUTED);

        JLabel lblDesc = new JLabel(c.getDescricao());
        lblDesc.setFont(TemaVisual.FONTE_PEQUENA);
        lblDesc.setForeground(desbloqueada ? TemaVisual.COR_TEXTO_CINZA : new Color(90, 96, 110));

        info.add(lblTitulo);
        info.add(Box.createVerticalStrut(3));
        info.add(lblDesc);
        card.add(info, BorderLayout.CENTER);

        // Status
        JPanel painelStatus = new JPanel();
        painelStatus.setLayout(new BoxLayout(painelStatus, BoxLayout.Y_AXIS));
        painelStatus.setOpaque(false);

        JLabel lblEstado = new JLabel(desbloqueada ? "CONQUISTADO" : "BLOQUEADO");
        lblEstado.setFont(TemaVisual.FONTE_PEQUENA);
        lblEstado.setForeground(desbloqueada ? TemaVisual.COR_ESMERALDA : TemaVisual.COR_TEXTO_MUTED);
        lblEstado.setAlignmentX(Component.RIGHT_ALIGNMENT);

        JLabel lblRecompensa = new JLabel("+" + c.getRecompensaMoedas() + " ✦");
        lblRecompensa.setFont(TemaVisual.FONTE_PEQUENA);
        lblRecompensa.setForeground(desbloqueada ? TemaVisual.COR_DOURADO : TemaVisual.COR_TEXTO_MUTED);
        lblRecompensa.setAlignmentX(Component.RIGHT_ALIGNMENT);

        painelStatus.add(lblEstado);
        painelStatus.add(lblRecompensa);
        card.add(painelStatus, BorderLayout.EAST);

        return card;
    }

    private JPanel criarPainelEstatisticas() {
        JPanel painel = new JPanel(new GridLayout(4, 2, 14, 14));
        painel.setBackground(TemaVisual.COR_SUPERFICIE);
        painel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        adicionarItemEstatistica(painel, "Fases Concluídas", String.valueOf(SistemaConquistas.getFasesCompletadas()), "🗺");
        adicionarItemEstatistica(painel, "Inimigos Derrotados", String.valueOf(SistemaConquistas.getInimigosDerrotados()), "⚔");
        adicionarItemEstatistica(painel, "Chefes de Masmorra Derrotados", String.valueOf(SistemaConquistas.getChefesDerrotados()), "👑");
        adicionarItemEstatistica(painel, "Baús e Tesouros Abertos", String.valueOf(SistemaConquistas.getTesourosColetados()), "💎");
        adicionarItemEstatistica(painel, "Total de Moedas Acumuladas", SistemaConquistas.getMoedasTotaisColetadas() + " ✦", "🪙");
        adicionarItemEstatistica(painel, "Poções e Itens Consumidos", String.valueOf(SistemaConquistas.getItensConsumidos()), "🧪");
        adicionarItemEstatistica(painel, "Rotas BFS Calculadas", String.valueOf(SistemaConquistas.getBfsExecutados()), "✦");
        adicionarItemEstatistica(painel, "Rotas Dijkstra Calculadas", String.valueOf(SistemaConquistas.getDijkstraExecutados()), "⚖");

        return painel;
    }

    private void adicionarItemEstatistica(JPanel container, String rotulo, String valor, String icone) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setBackground(TemaVisual.COR_SUPERFICIE_CARD);
        card.setBorder(TemaVisual.criarBordaCard());

        JLabel lblIcone = new JLabel(icone);
        lblIcone.setFont(new Font(Font.SERIF, Font.PLAIN, 26));
        lblIcone.setForeground(TemaVisual.COR_DOURADO);
        card.add(lblIcone, BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblRot = new JLabel(rotulo);
        lblRot.setFont(TemaVisual.FONTE_PEQUENA);
        lblRot.setForeground(TemaVisual.COR_TEXTO_MUTED);

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(TemaVisual.FONTE_SUBTITULO);
        lblVal.setForeground(TemaVisual.COR_TEXTO_BRANCO);

        info.add(lblRot);
        info.add(Box.createVerticalStrut(2));
        info.add(lblVal);
        card.add(info, BorderLayout.CENTER);

        container.add(card);
    }
}
