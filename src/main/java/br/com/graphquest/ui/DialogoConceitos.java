package br.com.graphquest.ui;

import br.com.graphquest.grafo.Grafo;
import br.com.graphquest.jogo.Jogo;
import br.com.graphquest.modelo.Sala;

import javax.swing.*;
import java.awt.*;
import java.util.stream.Collectors;

/**
 * Diálogo acadêmico interativo que explica todos os conceitos teóricos
 * de Estrutura de Dados e Teoria dos Grafos exigidos no enunciado da atividade ADO.
 */
public class DialogoConceitos extends JDialog {

    public DialogoConceitos(JFrame proprietario) {
        this(proprietario, new Jogo().getGrafo());
    }

    public DialogoConceitos(JFrame proprietario, Grafo grafo) {
        super(proprietario, "Conceitos Teóricos de Grafos — GraphQuest", true);
        setSize(780, 560);
        setLocationRelativeTo(proprietario);
        setResizable(true);

        JPanel painelPrincipal = new JPanel(new BorderLayout());
        painelPrincipal.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);

        // Cabeçalho
        JPanel cabecalho = new JPanel(new BorderLayout());
        cabecalho.setBackground(TemaVisual.COR_SUPERFICIE);
        cabecalho.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel titulo = new JLabel("Fundamentos de Grafos & Algoritmos de Busca");
        titulo.setFont(TemaVisual.FONTE_TITULO_GRANDE);
        titulo.setForeground(TemaVisual.COR_DOURADO);

        JLabel subtitulo = new JLabel("Base conceitual da estrutura de dados implementada em Java");
        subtitulo.setFont(TemaVisual.FONTE_NORMAL);
        subtitulo.setForeground(TemaVisual.COR_TEXTO_CINZA);

        cabecalho.add(titulo, BorderLayout.NORTH);
        cabecalho.add(subtitulo, BorderLayout.SOUTH);
        painelPrincipal.add(cabecalho, BorderLayout.NORTH);

        // Abas de Conteúdo
        JTabbedPane abas = new JTabbedPane();
        abas.setBackground(TemaVisual.COR_SUPERFICIE);
        abas.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        abas.setFont(TemaVisual.FONTE_NEGRITO);

        abas.addTab("1. O que é um Grafo?", criarAbaGrafo());
        abas.addTab("2. Lista de Adjacência", criarAbaListaAdjacencia(grafo));
        abas.addTab("3. Busca em Largura (BFS)", criarAbaBFS());
        abas.addTab("4. Complexidade", criarAbaComplexidade());
        abas.addTab("5. Aplicação no Jogo", criarAbaAplicacao());

        painelPrincipal.add(abas, BorderLayout.CENTER);

        // Botão Fechar no rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 12));
        rodape.setBackground(TemaVisual.COR_SUPERFICIE);
        JButton btnFechar = TemaVisual.criarBotao("Fechar e Voltar ao Jogo", TemaVisual.COR_BORDA, TemaVisual.COR_BORDA_FOCO);
        btnFechar.addActionListener(e -> dispose());
        rodape.add(btnFechar);
        painelPrincipal.add(rodape, BorderLayout.SOUTH);

        setContentPane(painelPrincipal);
    }

    private JScrollPane criarAbaGrafo() {
        String texto = """
                ============================================================
                1. DEFINIÇÃO MATEMÁTICA E CONCEITUAL DE GRAFO
                ============================================================

                Um Grafo G é uma estrutura de dados não-linear definida formalmente como
                um par ordenado:
                    G = (V, E)

                Onde:
                • V (Vértices ou Nós): Conjunto não vazio de elementos.
                  No GraphQuest, cada VÉRTICE representa uma CÂMARA / SALA da masmorra.
                
                • E (Arestas ou Arcos): Conjunto de pares de vértices representando
                  as conexões ou passagens entre eles.
                  No GraphQuest, cada ARESTA representa um CORREDOR navegável entre duas salas.

                ------------------------------------------------------------
                GRAFOS NÃO-DIRECIONADOS (BIDIRECIONAIS)
                ------------------------------------------------------------
                No GraphQuest, o grafo é NÃO-DIRECIONADO. Isso significa que se existe um
                corredor entre a Sala A e a Sala B, a aresta é representada pelo par {A, B},
                permitindo ao aventureiro transitar livremente tanto de A para B quanto de B para A.

                ------------------------------------------------------------
                GRAFO PONDERADO E ESCOLHA DO ALGORITMO
                ------------------------------------------------------------
                O mapa tem custos positivos nos corredores. A BFS continua sendo usada para
                encontrar o menor número de passagens: ela trata cada corredor como um passo
                e não soma os pesos. Dijkstra usa os custos das arestas e encontra a rota de
                menor custo total. Assim, a rota com menos passagens pode diferir da rota
                mais econômica.
                """;
        return criarPainelTexto(texto);
    }

    private JScrollPane criarAbaListaAdjacencia(Grafo grafo) {
        String texto = """
                ============================================================
                2. REPRESENTAÇÃO: LISTA DE ADJACÊNCIA EM JAVA
                ============================================================

                Existem duas representações computacionais clássicas para um grafo:
                a) Matriz de Adjacência: Matriz bidimensional V x V.
                b) Lista de Adjacência: Uma lista/coleção de vizinhos para cada vértice.

                NO GRAPHQUEST FOI ADOTADA A LISTA DE ADJACÊNCIA:
                    Map<Sala, List<Sala>> listaAdjacencia;

                POR QUE A LISTA DE ADJACÊNCIA É SUPERIOR NESTE CASO?
                1. Eficiência de Memória (Grafos Esparsos):
                   Uma masmorra tem poucas conexões por sala (grau médio de 2 a 4 arestas).
                   A matriz gastaria O(V²), desperdiçando memória com muitas células vazias.
                   A Lista de Adjacência consome apenas O(V + E) espaço.

                2. Desempenho ao Iterar Vizinhos:
                   Para descobrir para onde o jogador pode se movimentar a partir de uma sala,
                   basta consultar listaAdjacencia.get(salaAtual). Essa operação é O(1) de acesso
                   ao mapa e O(grau) de iteração sobre os vizinhos diretos, sem ter que inspecionar
                   todas as salas da masmorra como ocorreria em uma matriz.

                ADJACÊNCIAS DO MAPA ATUAL (geradas do objeto Grafo):
                """;
        StringBuilder adjacencias = new StringBuilder(texto);
        for (Sala sala : grafo.obterTodosVertices()) {
            String vizinhos = grafo.obterVizinhos(sala).stream()
                    .map(v -> "[" + v.getId() + "] " + v.getNome() + " (custo " + grafo.obterPeso(sala, v) + ")")
                    .collect(Collectors.joining(", "));
            adjacencias.append("\n[ ").append(sala.getId()).append(" ] ").append(sala.getNome())
                    .append(" → ").append(vizinhos.isEmpty() ? "sem conexões" : vizinhos);
        }
        return criarPainelTexto(adjacencias.toString());
    }

    private JScrollPane criarAbaBFS() {
        String texto = """
                ============================================================
                3. O ALGORITMO DE BUSCA EM LARGURA (BFS)
                ============================================================

                A Busca em Largura (Breadth-First Search) é um algoritmo fundamental que visita
                os vértices em ondas concêntricas a partir do nó de partida:
                - Camada 1: Todas as salas a 1 aresta de distância.
                - Camada 2: Todas as salas a 2 arestas de distância.
                - Camada k: Todas as salas a k arestas de distância.

                ESTRUTURAS DE DADOS OBRIGATÓRIAS UTILIZADAS NO CÓDIGO:

                1. FILA (Queue<Sala> fila = new ArrayDeque<>()):
                   Princípio FIFO (First-In, First-Out). Garante que as salas descobertas
                   primeiro sejam exploradas antes das salas de níveis mais profundos.

                2. CONJUNTO DE VISITADOS (Set<Sala> visitados = new LinkedHashSet<>()):
                   Evita que a busca entre em loops infinitos causados por ciclos no grafo
                   e garante que nenhuma sala seja reprocessada desnecessariamente.

                3. MAPA DE PREDECESSORES (Map<Sala, Sala> predecessores = new HashMap<>()):
                   Para cada sala 'v' descoberta através de 'u', registra que u é o predecessor
                   de v (predecessores.put(v, u)).

                RECONSTRUÇÃO DA ROTA ÓTIMA:
                Ao atingir a sala de saída, o algoritmo interrompe a busca e percorre a tabela
                de predecessores do destino voltando até a origem, reconstruindo a menor rota.
                """;
        return criarPainelTexto(texto);
    }

    private JScrollPane criarAbaComplexidade() {
        String texto = """
                ============================================================
                4. ANÁLISE DE COMPLEXIDADE TEMPORAL E ESPACIAL
                ============================================================

                COMPLEXIDADE DE TEMPO: O(V + E)
                • Cada vértice (V) entra e sai da fila exatamente uma vez: O(V).
                • Cada aresta (E) é inspecionada duas vezes (uma para cada extremidade
                  no grafo não-direcionado): O(2E) = O(E).
                • Total de operações: O(V + E).
                
                No grafo do GraphQuest:
                • V = 13 salas
                • E = 19 corredores
                • A ordem de crescimento é O(V + E); o número de operações depende do mapa.

                COMPLEXIDADE DE ESPAÇO AUXILIAR: O(V)
                • A Fila armazena no máximo O(V) vértices.
                • O Conjunto de Visitados armazena até V vértices: O(V).
                • O Mapa de Predecessores armazena até V associações: O(V).
                • Total de espaço adicional: O(V), independente da quantidade de arestas.

                DIJKSTRA COM FILA DE PRIORIDADE:
                • Tempo: O((V + E) log V).
                • Espaço auxiliar: O(V).
                """;
        return criarPainelTexto(texto);
    }

    private JScrollPane criarAbaAplicacao() {
        String texto = """
                ============================================================
                5. APLICAÇÃO PRÁTICA NO GRAPHQUEST
                ============================================================

                INTEGRAÇÃO ENTRE O JOGO E A ESTRUTURA DE DADOS:
                1. Movimentação do Jogador:
                   O jogador só pode ir para outra sala se existir aresta na Lista de Adjacência:
                   grafo.saoVizinhos(salaAtual, destino) == true. Paredes e salas desconectadas
                   são impossíveis de atravessar.

                2. Tratamento de Obstáculos (Salas Bloqueadas):
                   - A sala 6 (Passagem Soterrada) é um obstáculo permanente de desmoronamento.
                   - A sala 10 (Portão de Ferro) é um obstáculo que pode ser destrancado
                     coletando a 'Chave Mística' na Biblioteca Oculta (sala 8).
                   Uma chave nunca libera uma sala marcada como bloqueio permanente.
                   BFS e Dijkstra ignoram nós inacessíveis. A porta pode ser considerada
                   transitável quando o inventário possui a Chave Mística.

                3. Comparação dos caminhos:
                   A BFS minimiza a quantidade de corredores; Dijkstra minimiza a soma dos
                   custos mostrados no mapa. Dijkstra tem complexidade O((V + E) log V).

                4. Coleta de Tesouros e Condição de Vitória:
                   O jogador pode coletar tesouros uma única vez por sala para maximizar
                   sua pontuação antes de alcançar a sala 13 (Portal da Liberdade).
                """;
        return criarPainelTexto(texto);
    }

    private JScrollPane criarPainelTexto(String conteudo) {
        JTextArea area = new JTextArea(conteudo);
        area.setFont(TemaVisual.FONTE_CODIGO);
        area.setForeground(TemaVisual.COR_TEXTO_BRANCO);
        area.setBackground(TemaVisual.COR_FUNDO_ESCURECIDO);
        area.setEditable(false);
        area.setMargin(new Insets(14, 16, 14, 16));
        area.setCaretPosition(0);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
}
