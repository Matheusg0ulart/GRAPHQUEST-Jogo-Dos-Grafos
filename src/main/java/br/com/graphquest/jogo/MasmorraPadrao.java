package br.com.graphquest.jogo;

import br.com.graphquest.grafo.Grafo;
import br.com.graphquest.modelo.Sala;
import br.com.graphquest.modelo.TipoSala;

import java.util.*;

/**
 * Fábrica e configuradora do mapa padrão da masmorra do GraphQuest.
 * 
 * Cria os vértices, as conexões e os custos dos corredores da masmorra padrão.
 * Mantém também os dados iniciais usados ao reiniciar a partida.
 */
public class MasmorraPadrao {

    public static class DadosMasmorra {
        private final Grafo grafo;
        private final Sala salaInicial;
        private final Sala salaSaida;
        private final Map<Integer, Sala> mapaSalas;

        public DadosMasmorra(Grafo grafo, Sala salaInicial, Sala salaSaida, Map<Integer, Sala> mapaSalas) {
            this.grafo = grafo;
            this.salaInicial = salaInicial;
            this.salaSaida = salaSaida;
            this.mapaSalas = mapaSalas;
        }

        public Grafo getGrafo() {
            return grafo;
        }

        public Sala getSalaInicial() {
            return salaInicial;
        }

        public Sala getSalaSaida() {
            return salaSaida;
        }

        public Map<Integer, Sala> getMapaSalas() {
            return Collections.unmodifiableMap(mapaSalas);
        }
    }

    /**
     * Constrói e conecta todos os vértices e arestas da masmorra clássica.
     */
    public static DadosMasmorra criarMasmorra() {
        Grafo grafo = new Grafo();
        Map<Integer, Sala> salas = new LinkedHashMap<>();

        // 1. Instanciação dos Vértices (Salas) com coordenadas visuais (x, y) e dados de jogo
        // Sala Inicial
        Sala s1 = new Sala(1, "Entrada da Cripta",
                "Arcos góticos de pedra úmida marcam o início seguro da masmorra.",
                TipoSala.INICIAL, 90, 270);

        // Corredores e Salas Comuns
        Sala s2 = new Sala(2, "Corredor dos Ecos",
                "Um longo corredor onde sussurros do passado ecoam pelas paredes.",
                TipoSala.COMUM, 220, 270);

        // Salas de Tesouros
        Sala s3 = new Sala(3, "Câmara dos Tesouros",
                "Um antigo cofre de pedra guardando moedas antigas e cálices de prata.",
                TipoSala.TESOURO, 220, 120, 100);

        Sala s4 = new Sala(4, "Armaria Abandonada",
                "Espadas enferrujadas e escudos quebrados de guardiões antigos.",
                TipoSala.COMUM, 220, 420);

        // Sala Especial: Santuário
        Sala s5 = new Sala(5, "Santuário de Cristal",
                "Um monolito azul pulsa com energia arcana, transmitindo paz e coragem.",
                TipoSala.ESPECIAL, 370, 120, 0, false, false, null, null, "Bênção do Cristal");

        // Obstáculo Permanente: Desmoronamento
        Sala s6 = new Sala(6, "Passagem Soterrada",
                "Enormes blocos de rocha desabaram, bloqueando totalmente a travessia.",
                TipoSala.BLOQUEADA, 370, 440, true, true, "Desmoronamento intransitável de rochas", null);

        // Salão Central (Hub do Grafo)
        Sala s7 = new Sala(7, "Salão dos Pilares",
                "Um salão majestoso com colunas de mármore e múltiplos arcos de passagem.",
                TipoSala.COMUM, 370, 270);

        // Sala Especial: Biblioteca Oculta com a Chave Mística
        Sala s8 = new Sala(8, "Biblioteca Oculta",
                "Estantes arcanas repletas de tomos sobre teoria dos grafos e segredos arcanos.",
                TipoSala.ESPECIAL, 510, 120, 0, false, false, null, null, "Chave Mística");

        // Tesouro Nobre
        Sala s9 = new Sala(9, "Galeria Dourada",
                "Um pedestal central abriga um baú banhado a ouro entalhado com runas.",
                TipoSala.TESOURO, 510, 270, 300);

        // Obstáculo Destrancável: Portão de Ferro
        Sala s10 = new Sala(10, "Portão de Ferro",
                "Grades maciças de ferro reforçado trancadas com um cadeado rúnico.",
                TipoSala.BLOQUEADA, 440, 360, true, false, "Portão trancado - requer Chave Mística", "Chave Mística");

        // Sala Comum de Transição
        Sala s11 = new Sala(11, "Abismo Esquecido",
                "Uma ponte estreita de pedra suspensa sobre uma fenda profunda e escura.",
                TipoSala.COMUM, 570, 420);

        // Tesouro Místico
        Sala s12 = new Sala(12, "Cofre das Joias",
                "Arrecadação secreta repleta de gemas brilhantes e safiras raras.",
                TipoSala.TESOURO, 650, 270, 200);

        // Saída da Masmorra
        Sala s13 = new Sala(13, "Portal da Liberdade",
                "Um arco rúnico luminoso que leva à superfície e à vitória na aventura!",
                TipoSala.SAIDA, 690, 420);

        // Registro no mapa auxiliar
        salas.put(1, s1);
        salas.put(2, s2);
        salas.put(3, s3);
        salas.put(4, s4);
        salas.put(5, s5);
        salas.put(6, s6);
        salas.put(7, s7);
        salas.put(8, s8);
        salas.put(9, s9);
        salas.put(10, s10);
        salas.put(11, s11);
        salas.put(12, s12);
        salas.put(13, s13);

        // Adiciona todos os vértices
        for (Sala s : salas.values()) {
            grafo.adicionarVertice(s);
        }

        // 2. Conexões Reais da Lista de Adjacência (Arestas Não-Direcionadas)
        grafo.adicionarAresta(s1, s2); // [Entrada] <-> [Corredor dos Ecos]

        grafo.adicionarAresta(s2, s3); // [Corredor] <-> [Câmara Tesouro]
        grafo.adicionarAresta(s2, s4); // [Corredor] <-> [Armaria]
        grafo.adicionarAresta(s2, s7); // [Corredor] <-> [Salão Pilares]

        grafo.adicionarAresta(s3, s5); // [Câmara Tesouro] <-> [Santuário Cristal]

        grafo.adicionarAresta(s4, s6); // [Armaria] <-> [Passagem Soterrada (Bloqueada)]
        grafo.adicionarAresta(s4, s7); // [Armaria] <-> [Salão Pilares]

        grafo.adicionarAresta(s5, s8); // [Santuário] <-> [Biblioteca Oculta]

        grafo.adicionarAresta(s6, s11); // [Passagem Soterrada] <-> [Abismo] (corta atalho)

        grafo.adicionarAresta(s7, s8);  // [Salão Pilares] <-> [Biblioteca Oculta]
        grafo.adicionarAresta(s7, s9, 6);  // Trecho perigoso: curto, mas caro
        grafo.adicionarAresta(s7, s10); // [Salão Pilares] <-> [Portão de Ferro (Bloqueado)]

        grafo.adicionarAresta(s8, s9);  // [Biblioteca] <-> [Galeria Dourada]

        grafo.adicionarAresta(s9, s11, 6); // Atalho instável com custo elevado
        grafo.adicionarAresta(s9, s12, 4); // Rota até o cofre: mais recompensadora e com custo maior para Dijkstra

        grafo.adicionarAresta(s10, s11); // [Portão Ferro] <-> [Abismo] (atalho quando destrancado!)

        grafo.adicionarAresta(s11, s12); // [Abismo] <-> [Cofre das Joias]
        grafo.adicionarAresta(s11, s13, 2); // Atalho direto pelo Abismo, liberado ao abrir o portão

        grafo.adicionarAresta(s12, s13); // [Cofre das Joias] <-> [Portal da Liberdade (Saída)]

        return new DadosMasmorra(grafo, s1, s13, salas);
    }

    /** Gera fases maiores mantendo uma espinha conectada e corredores extras para exploração. */
    public static DadosMasmorra criarMasmorraProcedural(int quantidade, long semente) {
        return criarMasmorraProcedural(2, quantidade, semente);
    }

    /** Gera fases com dificuldade progressiva, biomas temáticos e garantia matemática de conectividade. */
    public static DadosMasmorra criarMasmorraProcedural(int fase, int quantidade, long semente) {
        return GeradorMasmorraProcedural.gerar(fase, quantidade, semente);
    }
}
