package br.com.graphquest.ui;

import javax.swing.*;
import java.awt.*;

/**
 * Diálogo que apresenta as instruções de jogo, controles e mecânicas
 * da masmorra do GraphQuest.
 */
public class DialogoComoJogar extends JDialog {

    public DialogoComoJogar(JFrame proprietario) {
        super(proprietario, "Como Jogar — GraphQuest", true);
        setSize(680, 500);
        setLocationRelativeTo(proprietario);
        setResizable(false);

        JPanel painel = new JPanel(new BorderLayout());
        painel.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Manual do Aventureiro");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);

        JLabel subtitulo = new JLabel("Instruções, mecânicas de jogo e interação com a masmorra");
        subtitulo.setFont(TemaVisual.FONTE_NORMAL);
        subtitulo.setForeground(TemaVisual.COR_TEXTO_CINZA);

        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(subtitulo, BorderLayout.SOUTH);
        painel.add(cabecalho, BorderLayout.NORTH);

        // Conteúdo
        String conteudoTexto = """
                OBJETIVO DA CAMPANHA
                Atravesse três masmorras conectadas pela mesma estrutura de grafo:
                Catacumbas de Pedra, Galerias Rubras e Santuário Astral. Sobreviva aos
                guardiões, colete recursos e derrote o Dragão das Sombras no portal final.

                ----------------------------------------------------------------------
                COMO SE MOVIMENTAR
                • No Mapa Visual: Clique com o botão esquerdo do mouse em qualquer sala
                  vizinha conectada diretamente à sua sala atual.
                • No Painel Lateral: Clique nos botões da seção 'Salas Conectadas'.
                • Salas acessíveis a partir da sua posição pulsam em verde esmeralda!
                • Teclado: use WASD ou as setas para seguir um corredor conectado.
                • Pressione B para calcular a rota BFS até a saída.

                ----------------------------------------------------------------------
                COMBATE, ARMADILHAS E RECURSOS
                • Os marcadores de caveira indicam câmaras guardadas; baús protegidos
                  só podem ser recolhidos após derrotar o inimigo.
                • Durante o combate, escolha Atacar (28 de dano), Usar poção (+40 PV,
                  quando necessário) ou Bomba arcana (50 de dano). O inimigo revida
                  após cada ação. Poções e bombas são limitadas; baús repõem recursos.
                • Se a vida acabar, você recua para a entrada com 55 PV, perde no máximo
                  100 pontos e o inimigo também fica ferido. A aventura não trava.
                • Marcadores vermelhos (!) indicam armadilhas: cada uma causa dano uma
                  vez por fase. Os portais recuperam 30 PV ao avançar.
                • A campanha tem oito fases e paletas próprias; chefes aparecem nas fases 4 e 8.
                • Sala 8 concede a Chave Mística; ela abre o Portão 10 e rende +60 pontos.
                  A chave é opcional, pois existem caminhos alternativos no grafo.
                • A Câmara do Santuário (5) recompensa a exploração com +75 pontos.
                • Cada baú é único; um cristal secreto por fase pode render +50 pontos.

                ----------------------------------------------------------------------
                COMPARANDO AS ROTAS
                • 'BFS • Menos passagens' destaca em dourado a rota com menos corredores.
                • 'Dijkstra • Menor custo' destaca em azul a rota com menor soma de custos.
                • 'Demonstrar BFS passo a passo' mostra os eventos reais do algoritmo no mapa.
                  Use anterior, próximo, executar, pausar ou reiniciar a demonstração.
                • Os números nos corredores indicam o custo; os demais valem 1.
                • Para abrir o Portão de Ferro, visite a Biblioteca e obtenha a chave.
                • A saída também tem rotas alternativas; a chave e o portão são opcionais.
                • A rota via Cofre (9–12) rende tesouro, mas custa 4; o atalho 11–13 custa 2.
                  BFS compara passagens; Dijkstra compara custo e pode preferir o portão.
                • Missões e objetivos são integrados às regras e suas recompensas só são pagas uma vez.
                • Acompanhe fase, vida, recursos, missões, tempo e exploração no painel.
                • A classificação final soma exploração e baús das oito fases; Mestre exige
                  explorar 15 ou mais salas e encontrar ao menos dois terços dos tesouros.
                """;

        JTextArea areaTexto = new JTextArea(conteudoTexto);
        areaTexto.setFont(TemaVisual.FONTE_NORMAL);
        areaTexto.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        areaTexto.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        areaTexto.setEditable(false);
        areaTexto.setMargin(new Insets(16, 20, 16, 20));
        areaTexto.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(areaTexto);
        scroll.setBorder(null);
        painel.add(scroll, BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        JButton btnEntendi = TemaVisual.criarBotao("Entendido, vamos jogar!", TemaVisual.COR_DOURADO, TemaVisual.COR_DOURADO_CLARO);
        btnEntendi.addActionListener(e -> dispose());
        rodape.add(btnEntendi);
        painel.add(rodape, BorderLayout.SOUTH);

        setContentPane(painel);
    }
}
