package br.com.graphquest.jogo;

import java.util.*;

/**
 * Sistema de Conquistas e Estatísticas de Carreira do GraphQuest.
 */
public final class SistemaConquistas {

    public enum Conquista {
        PRIMEIROS_PASSOS("Primeiros Passos", "Conclua a primeira fase da campanha.", "👣", 25),
        CARTOGRAFO("Cartógrafo Mestre", "Explore 100% das salas transitáveis de uma fase.", "🗺", 40),
        SEM_POCOES("Asceta Implacável", "Conclua uma fase sem utilizar poções de cura.", "✨", 30),
        MATADOR_DE_CHEFES("Terror dos Titãs", "Derrote um chefe ou guardião supremo de masmorra.", "👑", 50),
        CACADOR_DE_TESOUROS("Caçador de Relíquias", "Encontre e colete todos os baús de uma região.", "💎", 35),
        MESTRE_BFS("Cálculo Preciso", "Utilize a Busca em Largura (BFS) para traçar o caminho ótimo.", "🔍", 20),
        ESTRATEGISTA_DIJKSTRA("O Menor Custo", "Utilize o algoritmo de Dijkstra para calcular a rota de menor custo.", "⚡", 20),
        IMPECAVEL("Duelo Impecável", "Vença um combate sem sofrer nenhum dano.", "🛡", 30),
        SENHOR_DOS_GRAFOS("Senhor dos Grafos", "Conclua com sucesso todas as 8 fases da campanha.", "🏆", 100),
        BOLSA_CHEIA("Magnata da Masmorra", "Acumule 250 ou mais moedas no perfil do jogador.", "💰", 15);

        private final String titulo;
        private final String descricao;
        private final String icone;
        private final int recompensaMoedas;

        Conquista(String titulo, String descricao, String icone, int recompensaMoedas) {
            this.titulo = titulo;
            this.descricao = descricao;
            this.icone = icone;
            this.recompensaMoedas = recompensaMoedas;
        }

        public String getTitulo() { return titulo; }
        public String getDescricao() { return descricao; }
        public String getIcone() { return icone; }
        public int getRecompensaMoedas() { return recompensaMoedas; }
    }

    private final Set<Conquista> desbloqueadas = new LinkedHashSet<>();
    private final Map<String, Integer> estatisticas = new LinkedHashMap<>();

    public SistemaConquistas() {
        inicializarEstatisticas();
    }

    private void inicializarEstatisticas() {
        estatisticas.put("fases_concluidas", 0);
        estatisticas.put("inimigos_derrotados", 0);
        estatisticas.put("chefes_derrotados", 0);
        estatisticas.put("baus_coletados", 0);
        estatisticas.put("pocoes_usadas", 0);
        estatisticas.put("bombas_usadas", 0);
        estatisticas.put("passos_dados", 0);
        estatisticas.put("buscas_bfs", 0);
        estatisticas.put("buscas_dijkstra", 0);
        estatisticas.put("moedas_acumuladas", 0);
    }

    public synchronized boolean desbloquear(Conquista conquista) {
        if (conquista != null && desbloqueadas.add(conquista)) {
            return true;
        }
        return false;
    }

    public synchronized boolean possuiConquista(Conquista conquista) {
        return desbloqueadas.contains(conquista);
    }

    public synchronized Set<Conquista> getDesbloqueadas() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(desbloqueadas));
    }

    public synchronized void incrementarEstatistica(String chave, int quantidade) {
        estatisticas.put(chave, estatisticas.getOrDefault(chave, 0) + Math.max(0, quantidade));
    }

    public synchronized int getEstatistica(String chave) {
        return estatisticas.getOrDefault(chave, 0);
    }

    public synchronized Map<String, Integer> getTodasEstatisticas() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(estatisticas));
    }

    public synchronized void carregarDados(String conquistasStr, Properties props) {
        if (conquistasStr != null && !conquistasStr.isBlank()) {
            for (String nome : conquistasStr.split(",")) {
                try {
                    desbloqueadas.add(Conquista.valueOf(nome.trim()));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        if (props != null) {
            for (String chave : estatisticas.keySet()) {
                String val = props.getProperty("stat." + chave);
                if (val != null) {
                    try {
                        estatisticas.put(chave, Integer.parseInt(val));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public synchronized void salvarDados(Properties props) {
        List<String> nomes = desbloqueadas.stream().map(Enum::name).toList();
        props.setProperty("conquistas.desbloqueadas", String.join(",", nomes));
        for (Map.Entry<String, Integer> e : estatisticas.entrySet()) {
            props.setProperty("stat." + e.getKey(), String.valueOf(e.getValue()));
        }
    }

    // --- Acessores estáticos globais para a UI (usa instância singleton compartilhada) ---
    private static SistemaConquistas instanciaGlobal;

    public static void setInstanciaGlobal(SistemaConquistas instancia) {
        instanciaGlobal = instancia;
    }

    private static SistemaConquistas global() {
        return instanciaGlobal != null ? instanciaGlobal : new SistemaConquistas();
    }

    public static boolean isDesbloqueada(Conquista c) { return global().desbloqueadas.contains(c); }
    public static int getFasesCompletadas() { return global().getEstatistica("fases_concluidas"); }
    public static int getInimigosDerrotados() { return global().getEstatistica("inimigos_derrotados"); }
    public static int getChefesDerrotados() { return global().getEstatistica("chefes_derrotados"); }
    public static int getTesourosColetados() { return global().getEstatistica("baus_coletados"); }
    public static long getMoedasTotaisColetadas() { return global().getEstatistica("moedas_acumuladas"); }
    public static int getItensConsumidos() { return global().getEstatistica("pocoes_usadas") + global().getEstatistica("bombas_usadas"); }
    public static int getBfsExecutados() { return global().getEstatistica("buscas_bfs"); }
    public static int getDijkstraExecutados() { return global().getEstatistica("buscas_dijkstra"); }
}
