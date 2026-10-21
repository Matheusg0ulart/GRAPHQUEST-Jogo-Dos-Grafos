package br.com.graphquest.ui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Sistema de design visual e paleta de cores do GraphQuest.
 * 
 * Implementa estética de fantasia medieval/masmorra profissional com tons escuros,
 * detalhes dourados, esmeralda e rubi, fontes legíveis e botões com micro-interações.
 */
public final class TemaVisual {

    // Paleta de Cores Primárias (Dark Medieval Theme)
    public static final Color COR_FUNDO_ESCURECIDO = new Color(13, 16, 23);   // Obsidian Profundo
    public static final Color COR_SUPERFICIE        = new Color(22, 27, 34);   // Superfície de painéis
    public static final Color COR_SUPERFICIE_CARD   = new Color(28, 35, 46);   // Cards e caixas internas
    public static final Color COR_BORDA             = new Color(48, 54, 61);   // Bordas sutis
    public static final Color COR_BORDA_FOCO        = new Color(75, 85, 99);

    // Cores de Destaque e Semânticas
    public static final Color COR_DOURADO           = new Color(245, 158, 11); // Ouro / Âmbar
    public static final Color COR_DOURADO_CLARO     = new Color(251, 191, 36);
    public static final Color COR_ESMERALDA         = new Color(16, 185, 129); // Sucesso / Entrada
    public static final Color COR_RUBI              = new Color(239, 68, 68);  // Obstáculo / Alerta
    public static final Color COR_AZUL_PORTAL       = new Color(59, 130, 246); // Saída / Portal
    public static final Color COR_ROXO_ARCANO       = new Color(168, 85, 247); // Especial / Mágico

    // Cores de Arestas e Caminhos no Grafo
    public static final Color COR_ARESTA_PADRAO     = new Color(60, 72, 90);
    public static final Color COR_ARESTA_DESTAQUE   = new Color(251, 191, 36); // Dourado radiante para rota BFS
    public static final Color COR_JOGADOR           = new Color(236, 72, 153); // Rosa/Magenta vibrante para o avatar

    // Tipografia e Texto
    public static final Color COR_TEXTO_BRANCO      = new Color(243, 244, 246);
    public static final Color COR_TEXTO_CINZA       = new Color(156, 163, 175);
    public static final Color COR_TEXTO_MUTED       = new Color(107, 114, 128);

    public static final Font FONTE_TITULO_GRANDE    = new Font(Font.SANS_SERIF, Font.BOLD, 26);
    public static final Font FONTE_SUBTITULO        = new Font(Font.SANS_SERIF, Font.BOLD, 18);
    public static final Font FONTE_SECAO            = new Font(Font.SANS_SERIF, Font.BOLD, 14);
    public static final Font FONTE_NORMAL           = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONTE_NEGRITO          = new Font(Font.SANS_SERIF, Font.BOLD, 13);
    public static final Font FONTE_PEQUENA          = new Font(Font.SANS_SERIF, Font.PLAIN, 11);
    public static final Font FONTE_CODIGO           = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private TemaVisual() {
        // Construtor privado para classe utilitária
    }

    /**
     * Cria um botão estilizado consistente com a estética medieval/fantasia moderna.
     */
    public static JButton criarBotao(String texto, Color corBase, Color corHover) {
        JButton botao = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hovered = getModel().isRollover();
                boolean pressed = getModel().isPressed();

                Color bg = pressed ? corHover.darker() : (hovered ? corHover : corBase);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Borda suave
                g2.setColor(hovered ? COR_DOURADO : COR_BORDA);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        botao.setFont(FONTE_NEGRITO);
        botao.setForeground(COR_TEXTO_BRANCO);
        botao.setContentAreaFilled(false);
        botao.setBorderPainted(false);
        botao.setFocusPainted(false);
        botao.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        botao.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return botao;
    }

    /**
     * Cria uma borda arredondada acolchoada para cards e painéis internos.
     */
    public static Border criarBordaCard() {
        return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COR_BORDA, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        );
    }
}
