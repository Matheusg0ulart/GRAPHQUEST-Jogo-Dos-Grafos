package br.com.graphquest.ui;

import br.com.graphquest.grafo.Grafo;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.grafo.EstadoBFS;
import br.com.graphquest.modelo.Sala;
import br.com.graphquest.modelo.TipoSala;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Painel visual de alta fidelidade responsável por desenhar a Masmorra (Grafo).
 * 
 * Utiliza Java2D (Graphics2D) com antialiasing, gradientes, efeitos visuais
 * de iluminação e renderização vetorial.
 * 
 * As conexões desenhadas correspondem RIGOROSAMENTE às arestas reais da
 * lista de adjacência da estrutura de dados Grafo.
 */
public class PainelMapa extends JPanel {

    private final Jogo jogo;
    private Sala salaSobMouse;
    private EstadoBFS passoDemonstracao;
    private float faseAnimacao = 0f;
    private final Timer timerAnimacao;
    private double escalaMapa = 1.0;
    private double deslocamentoX;
    private double deslocamentoY;
    private double jogadorX;
    private double jogadorY;
    private static final double MAPA_MIN_X = 20;
    private static final double MAPA_MIN_Y = 70;
    private double MAPA_LARGURA = 750;
    private double MAPA_ALTURA = 440;

    // Raio das salas (vértices)
    private static final int RAIO_SALA = 26;
    private static final int DIAMETRO_SALA = RAIO_SALA * 2;

    public PainelMapa(Jogo jogo) {
        this.jogo = jogo;
        this.jogadorX = jogo.getSalaAtual().getX();
        this.jogadorY = jogo.getSalaAtual().getY();
        setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        setDoubleBuffered(true);

        // Suporte a interação com o mouse
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Sala anterior = salaSobMouse;
                salaSobMouse = encontrarSalaNasCoordenadas(e.getX(), e.getY());
                if (salaSobMouse != null) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                    setToolTipText(gerarTooltipSala(salaSobMouse));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                    setToolTipText(null);
                }
                if (anterior != salaSobMouse) {
                    repaint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    Sala clicada = encontrarSalaNasCoordenadas(e.getX(), e.getY());
                    if (clicada != null) {
                        jogo.moverPara(clicada);
                    }
                }
            }
        };

        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);

        // Timer de animação para brilho pulsante do jogador e da rota BFS
        timerAnimacao = new Timer(50, e -> {
            faseAnimacao += 0.1f;
            if (faseAnimacao > (float) (Math.PI * 2)) {
                faseAnimacao = 0f;
            }
            Sala alvo = jogo.getSalaAtual();
            jogadorX += (alvo.getX() - jogadorX) * 0.24;
            jogadorY += (alvo.getY() - jogadorY) * 0.24;
            if (Math.abs(alvo.getX() - jogadorX) < 0.7) jogadorX = alvo.getX();
            if (Math.abs(alvo.getY() - jogadorY) < 0.7) jogadorY = alvo.getY();
            repaint();
        });
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & java.awt.event.HierarchyEvent.SHOWING_CHANGED) != 0) {
                if (isShowing()) timerAnimacao.start();
                else timerAnimacao.stop();
            }
        });
    }

    public void exibirPassoDemonstracao(EstadoBFS passo) {
        this.passoDemonstracao = passo;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // Configuração de Renderização em Alta Resolução
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        int largura = getWidth();
        int altura = getHeight();

        // 1. Fundo Gradiente com Efeito de Masmorra Subterrânea
        desenharFundo(g2, largura, altura);

        calcularTransformacaoMapa(largura, altura);
        Graphics2D mapa = (Graphics2D) g2.create();
        mapa.translate(deslocamentoX, deslocamentoY);
        mapa.scale(escalaMapa, escalaMapa);
        desenharArestas(mapa);
        desenharRotaBFS(mapa);
        desenharCaminhoDemonstracao(mapa);
        desenharSalas(mapa);
        desenharJogador(mapa);
        desenharFeedback(g2, largura);
        mapa.dispose();

        // 6. Legenda Visual no rodapé
        desenharLegenda(g2, largura, altura);

        g2.dispose();
    }

    private void desenharCaminhoDemonstracao(Graphics2D g2) {
        if (passoDemonstracao == null || passoDemonstracao.getCaminhoFinal().size() < 2) return;
        List<Sala> caminho = passoDemonstracao.getCaminhoFinal();
        g2.setStroke(new BasicStroke(11f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(34, 197, 94, 95));
        for (int i = 0; i < caminho.size() - 1; i++) {
            Sala a = caminho.get(i), b = caminho.get(i + 1);
            g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
        g2.setStroke(new BasicStroke(4.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(new Color(134, 239, 172));
        for (int i = 0; i < caminho.size() - 1; i++) {
            Sala a = caminho.get(i), b = caminho.get(i + 1);
            g2.drawLine(a.getX(), a.getY(), b.getX(), b.getY());
        }
    }

    private void calcularTransformacaoMapa(int largura, int altura) {
        int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

        for (Sala sala : jogo.getMapaSalas().values()) {
            minX = Math.min(minX, sala.getX());
            maxX = Math.max(maxX, sala.getX());
            minY = Math.min(minY, sala.getY());
            maxY = Math.max(maxY, sala.getY());
        }

        if (minX > maxX) {
            minX = 70; maxX = 750;
            minY = 70; maxY = 440;
        }

        int margemX = 75;
        int margemY = 48;

        double mapaMinX = minX - margemX;
        double mapaMaxX = maxX + margemX;
        double mapaMinY = minY - margemY;
        double mapaMaxY = maxY + margemY;

        MAPA_LARGURA = Math.max(100, mapaMaxX - mapaMinX);
        MAPA_ALTURA = Math.max(100, mapaMaxY - mapaMinY);

        double areaMapaAltura = Math.max(100, altura - 54);
        double escalaX = Math.max(1, largura - 36) / MAPA_LARGURA;
        double escalaY = Math.max(1, areaMapaAltura - 36) / MAPA_ALTURA;

        escalaMapa = Math.max(0.45, Math.min(1.25, Math.min(escalaX, escalaY)));

        double centroGrafoX = (mapaMinX + mapaMaxX) / 2.0;
        double centroGrafoY = (mapaMinY + mapaMaxY) / 2.0;

        deslocamentoX = (largura / 2.0) - (centroGrafoX * escalaMapa);
        deslocamentoY = (areaMapaAltura / 2.0) - (centroGrafoY * escalaMapa);
    }

    private void desenharFeedback(Graphics2D g2, int largura) {
        if (!jogo.isFeedbackRecente() || jogo.getMensagemFeedback() == null) return;
        String texto = jogo.getMensagemFeedback();
        Font fonte = TemaVisual.FONTE_NEGRITO;
        g2.setFont(fonte);
        FontMetrics fm = g2.getFontMetrics();
        int maxTexto = Math.max(80, largura - 72);
        while (fm.stringWidth(texto) > maxTexto && texto.length() > 8) {
            texto = texto.substring(0, texto.length() - 2) + "…";
        }
        int w = Math.min(largura - 32, fm.stringWidth(texto) + 34);
        int x = (largura - w) / 2;
        Color cor = texto.toLowerCase().contains("bloqueada") ? new Color(153, 45, 52, 235)
                : texto.toLowerCase().contains("vitória") ? new Color(20, 120, 82, 235)
                : new Color(120, 82, 24, 235);
        g2.setColor(cor);
        g2.fillRoundRect(x, 14, w, 36, 14, 14);
        g2.setColor(new Color(255, 255, 255, 210));
        g2.drawRoundRect(x, 14, w, 36, 14, 14);
        g2.setColor(Color.WHITE);
        int textX = x + Math.max(12, (w - fm.stringWidth(texto)) / 2);
        g2.drawString(texto, textX, 37);
    }

    /**
     * Fundo escuro com textura vetorial sutil de pedras de masmorra.
     */
    private void desenharFundo(Graphics2D g2, int largura, int altura) {
        Color[] biomas = {new Color(18,24,38), new Color(43,20,27), new Color(23,45,35), new Color(34,25,48),
                new Color(20,39,55), new Color(49,29,22), new Color(26,40,62), new Color(36,20,49)};
        Color tomFase = biomas[(jogo.getFaseAtual() - 1) % biomas.length];
        GradientPaint gradiente = new GradientPaint(
                0, 0, TemaVisual.COR_FUNDO_ESCURECIDO,
                0, altura, tomFase
        );
        g2.setPaint(gradiente);
        g2.fillRect(0, 0, largura, altura);

        // Grade de azulejos sutis de masmorra
        g2.setColor(new Color(255, 255, 255, 4));
        int espacoGrade = 40;
        for (int x = 0; x < largura; x += espacoGrade) {
            g2.drawLine(x, 0, x, altura);
        }
        for (int y = 0; y < altura; y += espacoGrade) {
            g2.drawLine(0, y, largura, y);
        }
    }

    /**
     * Desenha as arestas reais do grafo como corredores de pedra esculpida.
     */
    private void desenharArestas(Graphics2D g2) {
        Grafo grafo = jogo.getGrafo();
        Set<String> arestasDesenhadas = new HashSet<>();

        for (Sala u : grafo.obterTodosVertices()) {
            List<Sala> vizinhos = grafo.obterVizinhos(u);
            for (Sala v : vizinhos) {
                // Evita desenhar a mesma aresta bidirecional duas vezes
                String chaveAresta = Math.min(u.getId(), v.getId()) + "-" + Math.max(u.getId(), v.getId());
                if (arestasDesenhadas.contains(chaveAresta)) {
                    continue;
                }
                arestasDesenhadas.add(chaveAresta);

                // Desenha o corredor base
                g2.setStroke(new BasicStroke(7.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(new Color(20, 26, 36));
                g2.drawLine(u.getX(), u.getY(), v.getX(), v.getY());

                g2.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.setColor(TemaVisual.COR_ARESTA_PADRAO);
                g2.drawLine(u.getX(), u.getY(), v.getX(), v.getY());

                int peso = grafo.obterPeso(u, v);
                if (peso > 1) {
                    int mx = (u.getX() + v.getX()) / 2;
                    int my = (u.getY() + v.getY()) / 2;
                    g2.setColor(new Color(15, 20, 28, 230));
                    g2.fillRoundRect(mx - 12, my - 10, 24, 19, 8, 8);
                    g2.setColor(new Color(255, 220, 130));
                    g2.setFont(TemaVisual.FONTE_PEQUENA);
                    g2.drawString(String.valueOf(peso), mx - 3, my + 4);
                }
            }
        }
    }

    /**
     * Destaca visualmente a rota calculada pelo algoritmo BFS.
     */
    private void desenharRotaBFS(Graphics2D g2) {
        List<Sala> rota = jogo.getRotaDestacada();
        if (rota == null || rota.size() < 2) {
            return;
        }

        float pulso = (float) (Math.sin(faseAnimacao * 2) * 0.3f + 0.7f);

        for (int i = 0; i < rota.size() - 1; i++) {
            Sala u = rota.get(i);
            Sala v = rota.get(i + 1);

            // Brilho exterior translúcido
            g2.setStroke(new BasicStroke(12.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            Color corRota = jogo.isRotaDijkstra() ? new Color(56, 189, 248) : new Color(245, 158, 11);
            g2.setColor(new Color(corRota.getRed(), corRota.getGreen(), corRota.getBlue(), (int) (70 * pulso)));
            g2.drawLine(u.getX(), u.getY(), v.getX(), v.getY());

            // Linha dourada central vibrante
            g2.setStroke(new BasicStroke(5.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(jogo.isRotaDijkstra() ? new Color(125, 211, 252) : TemaVisual.COR_DOURADO_CLARO);
            g2.drawLine(u.getX(), u.getY(), v.getX(), v.getY());

            // Seta ou ponto direcional indicador de fluxo
            desenharMarcadorDirecao(g2, u, v);
        }
    }

    /**
     * Desenha uma pequena seta no meio do corredor para indicar o sentido da rota calculada.
     */
    private void desenharMarcadorDirecao(Graphics2D g2, Sala u, Sala v) {
        double mx = (u.getX() + v.getX()) / 2.0;
        double my = (u.getY() + v.getY()) / 2.0;

        double angulo = Math.atan2(v.getY() - u.getY(), v.getX() - u.getX());
        int tamanhoSeta = 7;

        AffineTransform old = g2.getTransform();
        g2.translate(mx, my);
        g2.rotate(angulo);

        Path2D seta = new Path2D.Double();
        seta.moveTo(tamanhoSeta, 0);
        seta.lineTo(-tamanhoSeta, -tamanhoSeta / 1.5);
        seta.lineTo(-tamanhoSeta / 2.0, 0);
        seta.lineTo(-tamanhoSeta, tamanhoSeta / 1.5);
        seta.closePath();

            g2.setColor(jogo.isRotaDijkstra() ? new Color(14, 165, 233) : TemaVisual.COR_DOURADO);
        g2.fill(seta);
        g2.setTransform(old);
    }

    /**
     * Desenha todas as salas (vértices) com seus respectivos tipos, bordas e ícones.
     */
    private void desenharSalas(Graphics2D g2) {
        Grafo grafo = jogo.getGrafo();
        Sala salaAtual = jogo.getSalaAtual();
        List<Sala> vizinhosAtuais = grafo.obterVizinhos(salaAtual);

        for (Sala sala : grafo.obterTodosVertices()) {
            int cx = sala.getX();
            int cy = sala.getY();
            int x = cx - RAIO_SALA;
            int y = cy - RAIO_SALA;

            boolean ehVizinhoAcessivel = vizinhosAtuais.contains(sala) && jogo.podeAcessarSala(sala);
            boolean ehSalaAtual = sala.equals(salaAtual);
            boolean ehHover = (sala == salaSobMouse);

            if (passoDemonstracao != null) {
                Color corPasso = null;
                if (sala.equals(passoDemonstracao.getSalaAtual())) corPasso = new Color(251, 146, 60);
                else if (passoDemonstracao.getRecemDescobertas().contains(sala)) corPasso = new Color(74, 222, 128);
                else if (passoDemonstracao.getFila().contains(sala)) corPasso = new Color(56, 189, 248);
                else if (passoDemonstracao.getVisitadas().contains(sala)) corPasso = new Color(203, 213, 225);
                if (corPasso != null) {
                    g2.setColor(new Color(corPasso.getRed(), corPasso.getGreen(), corPasso.getBlue(), 100));
                    g2.fillOval(x - 10, y - 10, DIAMETRO_SALA + 20, DIAMETRO_SALA + 20);
                    g2.setColor(corPasso);
                    g2.setStroke(new BasicStroke(2.5f));
                    g2.drawOval(x - 8, y - 8, DIAMETRO_SALA + 16, DIAMETRO_SALA + 16);
                }
            }

            // Indicador de Vizinho Navegável (pulso sutil verde esmeralda)
            if (ehVizinhoAcessivel && !ehSalaAtual) {
                float pulso = (float) (Math.sin(faseAnimacao * 3) * 0.4f + 0.6f);
                g2.setColor(new Color(16, 185, 129, (int) (70 * pulso)));
                g2.fillOval(x - 6, y - 6, DIAMETRO_SALA + 12, DIAMETRO_SALA + 12);

                g2.setColor(new Color(16, 185, 129, (int) (180 * pulso)));
                g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                        0, new float[]{4, 4}, faseAnimacao * 2));
                g2.drawOval(x - 5, y - 5, DIAMETRO_SALA + 10, DIAMETRO_SALA + 10);
            }

            // Pulso temático suave: relíquias douradas, portais azuis e segredos violeta.
            if (sala.temTesouro() || sala.getTipo() == TipoSala.SAIDA || jogo.isEventoSurpresaDisponivel(sala)) {
                float brilho = (float) (Math.sin(faseAnimacao * 2.4) * 0.5 + 0.5);
                Color tom = sala.getTipo() == TipoSala.SAIDA ? new Color(80, 190, 255)
                        : jogo.isEventoSurpresaDisponivel(sala) ? new Color(190, 120, 255) : new Color(255, 205, 85);
                g2.setColor(new Color(tom.getRed(), tom.getGreen(), tom.getBlue(), (int) (35 + brilho * 65)));
                g2.setStroke(new BasicStroke(2.0f + brilho * 2));
                g2.drawOval(x - 4, y - 4, DIAMETRO_SALA + 8, DIAMETRO_SALA + 8);
            }

            // Sombra do Vértice
            g2.setColor(new Color(0, 0, 0, 120));
            g2.fillOval(x + 2, y + 4, DIAMETRO_SALA, DIAMETRO_SALA);

            // Preenchimento gradiente da sala
            Color corBase = obterCorSala(sala);
            Color corClara = corBase.brighter();
            if (ehHover) {
                corClara = corClara.brighter();
            }

            GradientPaint gp = new GradientPaint(cx - 10, cy - 15, corClara, cx + 15, cy + 20, corBase.darker());
            g2.setPaint(gp);
            g2.fillOval(x, y, DIAMETRO_SALA, DIAMETRO_SALA);

            // Borda do Vértice
            g2.setStroke(new BasicStroke(ehHover ? 3.0f : 2.0f));
            g2.setColor(ehHover ? TemaVisual.COR_DOURADO : new Color(255, 255, 255, 160));
            g2.drawOval(x, y, DIAMETRO_SALA, DIAMETRO_SALA);

            // Desenha o Ícone do Tipo de Sala
            desenharIconeSala(g2, sala, cx, cy);

            // Identificador e Nome da Sala
            desenharRotuloSala(g2, sala, cx, cy);

            if (jogo.isEventoSurpresaDisponivel(sala)) {
                g2.setColor(new Color(192, 132, 252));
                g2.fillOval(cx + 13, cy - 29, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(TemaVisual.FONTE_PEQUENA);
                g2.drawString("✦", cx + 16, cy - 16);
            }

            if (jogo.isArmadilhaDisponivel(sala)) {
                g2.setColor(new Color(248, 113, 113));
                g2.fillOval(cx - 22, cy - 25, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(TemaVisual.FONTE_PEQUENA);
                g2.drawString("!", cx - 17, cy - 13);
            }
            if (jogo.isInimigoNaSala(sala)) {
                g2.setColor(jogo.isChefeNaSala(sala) ? new Color(196, 55, 181) : new Color(190, 55, 52));
                g2.fillOval(cx + 9, cy - 29, 19, 19);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 11));
                g2.drawString(jogo.isChefeNaSala(sala) ? "B" : "⚔", cx + 13, cy - 15);
            }
            if (jogo.isEmCombate() && jogo.getSalaAtual().equals(sala)) {
                g2.setColor(new Color(255, 90, 70, 220));
                g2.setStroke(new BasicStroke(3f));
                g2.drawOval(x - 4, y - 4, DIAMETRO_SALA + 8, DIAMETRO_SALA + 8);
            }

            // Badge de Visitada
            if (sala.isVisitada() && !ehSalaAtual) {
                g2.setColor(TemaVisual.COR_ESMERALDA);
                g2.fillOval(cx + RAIO_SALA - 10, cy - RAIO_SALA, 10, 10);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(cx + RAIO_SALA - 10, cy - RAIO_SALA, 10, 10);
            }
        }
    }

    /**
     * Retorna a cor correspondente ao estado e tipo da sala.
     */
    private Color obterCorSala(Sala sala) {
        if (sala.isBloqueada()) {
            return TemaVisual.COR_RUBI;
        }
        if (sala.temTesouro()) {
            return TemaVisual.COR_DOURADO;
        }
        Color base = sala.getTipo().getCorTema();
        Color[] paletas = {new Color(166,55,53), new Color(54,130,79), new Color(111,70,151), new Color(49,113,153),
                new Color(171,84,39), new Color(62,102,171), new Color(133,63,156)};
        if (jogo.getFaseAtual() > 1) return misturar(base, paletas[(jogo.getFaseAtual() - 2) % paletas.length], 0.24f);
        return base;
    }

    private Color misturar(Color a, Color b, float proporcao) {
        return new Color((int)(a.getRed() * (1 - proporcao) + b.getRed() * proporcao),
                (int)(a.getGreen() * (1 - proporcao) + b.getGreen() * proporcao),
                (int)(a.getBlue() * (1 - proporcao) + b.getBlue() * proporcao));
    }

    /**
     * Renderiza ícones vetoriais limpos dentro das salas para facilitar a identificação.
     */
    private void desenharIconeSala(Graphics2D g2, Sala sala, int cx, int cy) {
        g2.setColor(Color.WHITE);

        if (sala.isBloqueada()) {
            // Ícone de Cadeado / X de Bloqueio
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawLine(cx - 7, cy - 7, cx + 7, cy + 7);
            g2.drawLine(cx + 7, cy - 7, cx - 7, cy + 7);
            return;
        }

        switch (sala.getTipo()) {
            case INICIAL:
                // Arco de entrada
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawArc(cx - 7, cy - 8, 14, 16, 0, 180);
                g2.drawLine(cx - 7, cy, cx - 7, cy + 8);
                g2.drawLine(cx + 7, cy, cx + 7, cy + 8);
                break;

            case TESOURO:
                // Baú ou Moeda
                g2.setColor(new Color(255, 235, 120));
                g2.fillRoundRect(cx - 9, cy - 5, 18, 12, 3, 3);
                g2.setColor(new Color(180, 120, 20));
                g2.drawRoundRect(cx - 9, cy - 5, 18, 12, 3, 3);
                g2.drawLine(cx - 9, cy - 1, cx + 9, cy - 1);
                g2.fillOval(cx - 2, cy - 3, 4, 4);
                break;

            case ESPECIAL:
                // Losango mágico / Cristal
                Path2D cristal = new Path2D.Double();
                cristal.moveTo(cx, cy - 10);
                cristal.lineTo(cx + 8, cy);
                cristal.lineTo(cx, cy + 10);
                cristal.lineTo(cx - 8, cy);
                cristal.closePath();
                g2.setColor(new Color(230, 200, 255));
                g2.fill(cristal);
                g2.setColor(Color.WHITE);
                g2.draw(cristal);
                break;

            case SAIDA:
                // Portal Rúnico Estelar
                g2.setStroke(new BasicStroke(2.0f));
                g2.drawOval(cx - 8, cy - 8, 16, 16);
                g2.fillOval(cx - 3, cy - 3, 6, 6);
                break;

            case MERCADOR:
                // Símbolo do Mercador (moeda mística)
                g2.setColor(new Color(251, 191, 36));
                g2.fillOval(cx - 7, cy - 7, 14, 14);
                g2.setColor(new Color(146, 64, 14));
                g2.drawOval(cx - 7, cy - 7, 14, 14);
                g2.setColor(new Color(25, 20, 10));
                g2.setFont(new Font(Font.SERIF, Font.BOLD, 10));
                g2.drawString("✦", cx - 4, cy + 4);
                break;

            case DESCANSO:
                // Cruz esmeralda de recuperação vital
                g2.setColor(new Color(52, 211, 153));
                g2.fillRect(cx - 2, cy - 7, 4, 14);
                g2.fillRect(cx - 7, cy - 2, 14, 4);
                break;

            case CHEFE:
                // Emblema do Guardião Chefe
                g2.setColor(new Color(251, 113, 133));
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                g2.drawString("☠", cx - 5, cy + 4);
                break;

            case COMUM:
            default:
                // Número do Vértice em negrito
                g2.setFont(TemaVisual.FONTE_NEGRITO);
                String num = String.valueOf(sala.getId());
                FontMetrics fm = g2.getFontMetrics();
                int tx = cx - fm.stringWidth(num) / 2;
                int ty = cy + fm.getAscent() / 2 - 2;
                g2.drawString(num, tx, ty);
                break;
        }
    }

    /**
     * Desenha o rótulo do nome da sala abaixo do vértice em uma pílula elegante.
     */
    private void desenharRotuloSala(Graphics2D g2, Sala sala, int cx, int cy) {
        String texto = sala.getId() + ". " + sala.getNome();
        g2.setFont(TemaVisual.FONTE_PEQUENA);
        FontMetrics fm = g2.getFontMetrics();
        int larguraTexto = fm.stringWidth(texto);
        int alturaTexto = fm.getHeight();

        int pillX = cx - larguraTexto / 2 - 6;
        int pillY = cy + RAIO_SALA + 5;
        int pillW = larguraTexto + 12;
        int pillH = alturaTexto + 2;

        // Fundo escuro semitransparente para legibilidade impecável
        g2.setColor(new Color(15, 20, 28, 210));
        g2.fillRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        g2.setColor(TemaVisual.COR_BORDA);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(pillX, pillY, pillW, pillH, 8, 8);

        g2.setColor(sala.isBloqueada() ? new Color(248, 113, 113) : TemaVisual.COR_TEXTO_BRANCO);
        g2.drawString(texto, cx - larguraTexto / 2, pillY + fm.getAscent());
    }

    /**
     * Renderiza o avatar do aventureiro na sala atual.
     */
    private void desenharJogador(Graphics2D g2) {
        Sala atual = jogo.getSalaAtual();
        if (atual == null) return;

        int cx = (int) Math.round(jogadorX);
        int cy = (int) Math.round(jogadorY);

        // Anel de pulso luminoso do personagem
        float pulso = (float) (Math.sin(faseAnimacao * 3.5) * 0.35f + 0.65f);
        int raioPulso = RAIO_SALA + (int) (8 * pulso);

        g2.setColor(new Color(236, 72, 153, (int) (120 * pulso)));
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawOval(cx - raioPulso, cy - raioPulso, raioPulso * 2, raioPulso * 2);

        // Halo interior
        g2.setColor(new Color(255, 255, 255, (int) (160 * pulso)));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawOval(cx - RAIO_SALA - 3, cy - RAIO_SALA - 3, (RAIO_SALA + 3) * 2, (RAIO_SALA + 3) * 2);

        // Emblema da Coroa / Herói acima da sala
        int topoY = cy - RAIO_SALA - 16;
        Path2D coroa = new Path2D.Double();
        coroa.moveTo(cx - 9, topoY + 9);
        coroa.lineTo(cx - 9, topoY);
        coroa.lineTo(cx - 4, topoY + 4);
        coroa.lineTo(cx, topoY - 3);
        coroa.lineTo(cx + 4, topoY + 4);
        coroa.lineTo(cx + 9, topoY);
        coroa.lineTo(cx + 9, topoY + 9);
        coroa.closePath();

        g2.setColor(new Color(251, 191, 36));
        g2.fill(coroa);
        g2.setColor(new Color(180, 83, 9));
        g2.setStroke(new BasicStroke(1.2f));
        g2.draw(coroa);
    }

    /**
     * Desenha a barra de legenda no rodapé do mapa.
     */
    private void desenharLegenda(Graphics2D g2, int largura, int altura) {
        int y = altura - 32;
        int x = 20;

        g2.setColor(new Color(15, 20, 28, 220));
        g2.fillRoundRect(12, y - 6, largura - 24, 30, 8, 8);
        g2.setColor(TemaVisual.COR_BORDA);
        g2.setStroke(new BasicStroke(1.0f));
        g2.drawRoundRect(12, y - 6, largura - 24, 30, 8, 8);

        g2.setFont(TemaVisual.FONTE_PEQUENA);

        // Itens da legenda
        x = desenharItemLegenda(g2, x, y + 10, TemaVisual.COR_JOGADOR, "Herói (Você)");
        x = desenharItemLegenda(g2, x, y + 10, TipoSala.INICIAL.getCorTema(), "Entrada");
        x = desenharItemLegenda(g2, x, y + 10, TipoSala.COMUM.getCorTema(), "Comum");
        x = desenharItemLegenda(g2, x, y + 10, TipoSala.TESOURO.getCorTema(), "Tesouro");
        x = desenharItemLegenda(g2, x, y + 10, new Color(192, 132, 252), "Evento surpresa");
        x = desenharItemLegenda(g2, x, y + 10, TipoSala.ESPECIAL.getCorTema(), "Especial");
        x = desenharItemLegenda(g2, x, y + 10, TemaVisual.COR_RUBI, "Bloqueada");
        x = desenharItemLegenda(g2, x, y + 10, TipoSala.SAIDA.getCorTema(), "Saída");
        Color corRota = jogo.isRotaDijkstra() ? new Color(56, 189, 248) : TemaVisual.COR_DOURADO;
        desenharItemLinhaLegenda(g2, x, y + 10, corRota,
                jogo.isRotaDijkstra() ? "Rota Dijkstra" : "Rota BFS");
    }

    private int desenharItemLegenda(Graphics2D g2, int x, int y, Color cor, String rotulo) {
        g2.setColor(cor);
        g2.fillOval(x, y - 8, 10, 10);
        g2.setColor(Color.WHITE);
        g2.drawOval(x, y - 8, 10, 10);
        g2.drawString(rotulo, x + 14, y);
        return x + g2.getFontMetrics().stringWidth(rotulo) + 24;
    }

    private void desenharItemLinhaLegenda(Graphics2D g2, int x, int y, Color cor, String rotulo) {
        g2.setColor(cor);
        g2.setStroke(new BasicStroke(3.0f));
        g2.drawLine(x, y - 3, x + 16, y - 3);
        g2.setColor(TemaVisual.COR_TEXTO_BRANCO);
        g2.drawString(rotulo, x + 22, y);
    }

    /**
     * Localiza a sala sobre a qual o mouse se encontra no momento.
     */
    private Sala encontrarSalaNasCoordenadas(int mx, int my) {
        double logicoX = (mx - deslocamentoX) / escalaMapa;
        double logicoY = (my - deslocamentoY) / escalaMapa;
        for (Sala sala : jogo.getGrafo().obterTodosVertices()) {
            double distQuadrada = Math.pow(logicoX - sala.getX(), 2) + Math.pow(logicoY - sala.getY(), 2);
            if (distQuadrada <= Math.pow(RAIO_SALA + 4, 2)) {
                return sala;
            }
        }
        return null;
    }

    private String gerarTooltipSala(Sala sala) {
        StringBuilder sb = new StringBuilder("<html><body style='padding:4px; font-family:sans-serif;'>");
        sb.append("<b>[").append(sala.getId()).append("] ").append(sala.getNome()).append("</b><br>");
        sb.append("<span style='color:#9CA3AF;'>Tipo: </span>").append(sala.getTipo().getNomeExibicao()).append("<br>");
        if (sala.isBloqueada()) {
            boolean podeAbrir = sala.getItemNecessario() != null
                    && jogo.getInventario().contains(sala.getItemNecessario());
            sb.append("<span style='color:#EF4444;'><b>")
                    .append(podeAbrir ? "Pode abrir:" : "Bloqueio:").append("</b> ")
                    .append(sala.getMotivoBloqueio()).append("</span><br>");
        }
        if (sala.temTesouro()) {
            sb.append("<span style='color:#F59E0B;'><b>Tesouro:</b> +").append(sala.getValorTesouro()).append(" pts</span><br>");
        }
        if (sala.getEvento() != null && !sala.isEventoProcessado()) {
            sb.append("<span style='color:#34D399;'><b>Evento:</b> ").append(sala.getEvento().getNome()).append("</span><br>");
        }
        if (sala.getItemConcedido() != null) {
            sb.append("<span style='color:#A855F7;'><b>Item:</b> ").append(sala.getItemConcedido()).append("</span><br>");
        }
        if (jogo.isEventoSurpresaDisponivel(sala)) {
            sb.append("<span style='color:#C084FC;'><b>Evento surpresa:</b> cristal secreto (+50 pts)</span><br>");
        }
        sb.append("<i>Clique para se mover até esta sala</i>");
        sb.append("</body></html>");
        return sb.toString();
    }
}
