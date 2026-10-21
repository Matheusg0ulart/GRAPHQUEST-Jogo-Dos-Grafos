package br.com.graphquest.util;

import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.modelo.Sala;
import br.com.graphquest.ui.DialogoConceitos;
import br.com.graphquest.ui.DialogoDemonstracaoBFS;
import br.com.graphquest.ui.TelaInicial;
import br.com.graphquest.ui.TelaPrincipal;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utilitário para captura programática de telas reais da aplicação
 * para inserção na apresentação acadêmica e na documentação.
 */
public class CapturaTelas {

    public static void main(String[] args) {
        System.out.println("Iniciando captura de telas da aplicação GraphQuest...");

        File pastaDestino = new File("docs/screenshots");
        if (!pastaDestino.exists()) {
            pastaDestino.mkdirs();
        }

        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");

            SwingUtilities.invokeAndWait(() -> {
                try {
                    // 1. Captura da Tela Inicial
                    capturarTelaInicial(new File(pastaDestino, "screenshot_01_menu_inicial.png"));

                    // 2. Captura da Tela Principal no Estado Inicial
                    capturarTelaPrincipal(new File(pastaDestino, "screenshot_02_mapa_inicial.png"), false);

                    // 3. Captura da Tela Principal com Rota BFS Ativa
                    capturarTelaPrincipal(new File(pastaDestino, "screenshot_03_mapa_bfs_rota.png"), true);

                    // 4. Captura do Diálogo de Conceitos Acadêmicos
                    capturarDialogoConceitos(new File(pastaDestino, "screenshot_04_dialogo_conceitos.png"));

                    // 5. Captura do Diálogo de Detalhamento Didático da BFS
                    capturarExplicacaoBFS(new File(pastaDestino, "screenshot_05_bfs_detalhes.png"));

                    // 6. Captura comparativa: conserva os dois resultados e destaca Dijkstra
                    capturarComparacaoAlgoritmos(new File(pastaDestino, "screenshot_06_comparacao_rotas.png"));

                    // 7–8. Demonstração real da BFS: estado do mapa e controles passo a passo
                    capturarDemonstracaoBFS(new File(pastaDestino, "screenshot_07_bfs_execucao.png"),
                            new File(pastaDestino, "screenshot_08_bfs_controles.png"));

                    // 9. Painel de missões após descobrir o Santuário e a Chave
                    capturarProgressoMissoes(new File(pastaDestino, "screenshot_09_missoes_e_progresso.png"));

                    // 10. Card e mapa durante um encontro com guardião
                    capturarCombate(new File(pastaDestino, "screenshot_10_combate.png"));

                    System.out.println("Todas as capturas de tela foram geradas com sucesso em: docs/screenshots/");
                } catch (Exception e) {
                    System.err.println("Erro na captura: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            System.err.println("Erro ao agendar capturas: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    private static void capturarTelaInicial(File arquivoDestino) throws Exception {
        TelaInicial frame = new TelaInicial();
        renderizarFrame(frame, 820, 640, arquivoDestino);
        frame.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarTelaPrincipal(File arquivoDestino, boolean executarBFS) throws Exception {
        TelaPrincipal frame = new TelaPrincipal(null);
        Jogo jogo = frame.getJogo();

        if (executarBFS) {
            Sala sala2 = jogo.getMapaSalas().get(2);
            Sala sala7 = jogo.getMapaSalas().get(7);
            if (sala2 != null) jogo.moverPara(sala2);
            if (sala7 != null) jogo.moverPara(sala7);

            jogo.executarBFSAteSaida();
        }

        renderizarFrame(frame, 1200, 780, arquivoDestino);
        frame.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarDialogoConceitos(File arquivoDestino) throws Exception {
        JFrame fakeOwner = new JFrame();
        fakeOwner.addNotify();
        DialogoConceitos dialogo = new DialogoConceitos(fakeOwner);
        renderizarComponente(dialogo.getContentPane(), 780, 560, arquivoDestino);
        dialogo.dispose();
        fakeOwner.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarExplicacaoBFS(File arquivoDestino) throws Exception {
        Jogo jogo = new Jogo();
        Sala s1 = jogo.getMapaSalas().get(1);
        Sala s13 = jogo.getMapaSalas().get(13);
        br.com.graphquest.grafo.ResultadoBFS res = jogo.getGrafo().buscarMenorCaminho(s1, s13);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(br.com.graphquest.ui.TemaVisual.COR_FUNDO_ESCURECIDO);

        JLabel titulo = new JLabel(" Detalhamento Didático: Execução da Busca em Largura (BFS)");
        titulo.setFont(br.com.graphquest.ui.TemaVisual.FONTE_SECAO);
        titulo.setForeground(br.com.graphquest.ui.TemaVisual.COR_DOURADO);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(titulo, BorderLayout.NORTH);

        JTextArea areaTexto = new JTextArea(res.getExplicacaoDidatica());
        areaTexto.setFont(br.com.graphquest.ui.TemaVisual.FONTE_CODIGO);
        areaTexto.setForeground(br.com.graphquest.ui.TemaVisual.COR_TEXTO_BRANCO);
        areaTexto.setBackground(br.com.graphquest.ui.TemaVisual.COR_SUPERFICIE_CARD);
        areaTexto.setMargin(new Insets(12, 14, 12, 14));
        panel.add(areaTexto, BorderLayout.CENTER);

        renderizarComponente(panel, 740, 520, arquivoDestino);
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarComparacaoAlgoritmos(File arquivoDestino) throws Exception {
        TelaPrincipal frame = new TelaPrincipal(null);
        Jogo jogo = frame.getJogo();
        jogo.moverPara(jogo.getMapaSalas().get(2));
        jogo.moverPara(jogo.getMapaSalas().get(7));
        jogo.executarBFSAteSaida();
        jogo.executarDijkstraAteSaida();
        renderizarFrame(frame, 1200, 1040, arquivoDestino);
        frame.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarDemonstracaoBFS(File mapaDestino, File dialogoDestino) throws Exception {
        TelaPrincipal frame = new TelaPrincipal(null);
        Jogo jogo = frame.getJogo();
        var resultado = jogo.prepararDemonstracaoBFSAteSaida();
        DialogoDemonstracaoBFS dialogo = new DialogoDemonstracaoBFS(
                frame, jogo, frame.getPainelMapa(), resultado);
        int etapaDescoberta = 0;
        for (int i = 0; i < resultado.getPassos().size(); i++) {
            if (resultado.getPassos().get(i).getTipo()
                    == br.com.graphquest.grafo.PassoBFS.Tipo.VIZINHO_DESCOBERTO) {
                etapaDescoberta = i;
                break;
            }
        }
        dialogo.mostrarPasso(etapaDescoberta);
        renderizarFrame(frame, 1200, 780, mapaDestino);
        renderizarComponente(dialogo.getContentPane(), 660, 480, dialogoDestino);
        dialogo.dispose();
        frame.dispose();
        System.out.println("✓ Gerado: " + mapaDestino.getName());
        System.out.println("✓ Gerado: " + dialogoDestino.getName());
    }

    private static void capturarProgressoMissoes(File arquivoDestino) throws Exception {
        TelaPrincipal frame = new TelaPrincipal(null);
        Jogo jogo = frame.getJogo();
        int[] rota = {2, 3, 5, 8};
        for (int id : rota) {
            jogo.moverPara(jogo.getMapaSalas().get(id));
            while (jogo.isEmCombate()) jogo.atacar();
        }
        renderizarFrame(frame, 1200, 780, arquivoDestino);
        frame.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void capturarCombate(File arquivoDestino) throws Exception {
        TelaPrincipal frame = new TelaPrincipal(null);
        Jogo jogo = frame.getJogo();
        jogo.moverPara(jogo.getMapaSalas().get(2));
        jogo.moverPara(jogo.getMapaSalas().get(3));
        renderizarFrame(frame, 1200, 780, arquivoDestino);
        frame.dispose();
        System.out.println("✓ Gerado: " + arquivoDestino.getName());
    }

    private static void renderizarFrame(JFrame frame, int w, int h, File arquivo) throws Exception {
        Container content = frame.getContentPane();
        renderizarComponente(content, w, h, arquivo);
    }

    private static void renderizarComponente(Container comp, int w, int h, File arquivo) throws Exception {
        comp.setSize(w, h);
        comp.setPreferredSize(new Dimension(w, h));
        layoutRecursive(comp);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        comp.paint(g2);
        g2.dispose();

        ImageIO.write(img, "png", arquivo);
    }

    private static void layoutRecursive(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) {
            if (child instanceof Container) {
                layoutRecursive((Container) child);
            }
        }
    }
}
