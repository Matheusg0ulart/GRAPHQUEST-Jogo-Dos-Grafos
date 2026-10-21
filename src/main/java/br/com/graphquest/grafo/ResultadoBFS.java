package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;
import java.util.Collections;
import java.util.List;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Encapsula o resultado da execução do algoritmo de Busca em Largura (BFS) no Grafo.
 * 
 * Contém informações essenciais para a interface gráfica e fins didáticos:
 * - O caminho reconstruído passo a passo (sequência de salas).
 * - A quantidade de arestas percorridas (custo do menor caminho).
 * - A ordem em que os vértices foram explorados na fila.
 * - Uma explicação acadêmica detalhada do cálculo realizado.
 */
public class ResultadoBFS {
    private final boolean sucesso;
    private final Sala origem;
    private final Sala destino;
    private final List<Sala> caminho;
    private final List<Sala> ordemExploracao;
    private final int quantidadeArestas;
    private final String mensagem;
    private final String explicacaoDidatica;
    private final List<PassoBFS> passos;

    public ResultadoBFS(boolean sucesso, Sala origem, Sala destino, List<Sala> caminho,
                        List<Sala> ordemExploracao, String mensagem, String explicacaoDidatica) {
        this(sucesso, origem, destino, caminho, ordemExploracao, mensagem, explicacaoDidatica, Collections.emptyList());
    }

    public ResultadoBFS(boolean sucesso, Sala origem, Sala destino, List<Sala> caminho,
                        List<Sala> ordemExploracao, String mensagem, String explicacaoDidatica,
                        List<PassoBFS> passos) {
        this.sucesso = sucesso;
        this.origem = origem;
        this.destino = destino;
        this.caminho = copia(caminho);
        this.ordemExploracao = copia(ordemExploracao);
        this.passos = passos == null ? Collections.emptyList()
                : Collections.unmodifiableList(new java.util.ArrayList<>(passos));
        this.quantidadeArestas = (sucesso && this.caminho.size() > 1) ? (this.caminho.size() - 1) : 0;
        this.mensagem = mensagem;
        this.explicacaoDidatica = explicacaoDidatica;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public Sala getOrigem() {
        return origem;
    }

    public Sala getDestino() {
        return destino;
    }

    public List<Sala> getCaminho() {
        return caminho;
    }

    public List<Sala> getOrdemExploracao() {
        return ordemExploracao;
    }

    public int getQuantidadeArestas() {
        return quantidadeArestas;
    }

    public String getMensagem() {
        return mensagem;
    }

    public String getExplicacaoDidatica() {
        return explicacaoDidatica;
    }

    public List<PassoBFS> getPassos() { return passos; }

    /** Reproduz os deltas do registro até o passo solicitado para obter a fila e os conjuntos atuais. */
    public EstadoBFS criarEstadoPasso(int indice) {
        if (passos.isEmpty()) return null;
        int limite = Math.max(0, Math.min(indice, passos.size() - 1));
        Queue<Sala> filaAtual = new ArrayDeque<>();
        Set<Sala> visitadasAtuais = new LinkedHashSet<>();
        List<Sala> analisados = new java.util.ArrayList<>();
        List<Sala> descobertas = new java.util.ArrayList<>();
        List<Sala> caminhoAtual = Collections.emptyList();
        Sala processando = null;
        PassoBFS evento = passos.get(0);
        for (int i = 0; i <= limite; i++) {
            evento = passos.get(i);
            switch (evento.getTipo()) {
                case INICIO -> {
                    filaAtual.add(evento.getSalaAfetada());
                    visitadasAtuais.add(evento.getSalaAfetada());
                }
                case PROCESSAMENTO -> {
                    filaAtual.remove(evento.getSalaAtual());
                    processando = evento.getSalaAtual();
                    analisados.clear();
                    descobertas.clear();
                }
                case VIZINHO_BLOQUEADO, VIZINHO_VISITADO -> analisados.add(evento.getSalaAfetada());
                case VIZINHO_DESCOBERTO -> {
                    analisados.add(evento.getSalaAfetada());
                    descobertas.add(evento.getSalaAfetada());
                    visitadasAtuais.add(evento.getSalaAfetada());
                    filaAtual.add(evento.getSalaAfetada());
                }
                case DESTINO_ENCONTRADO -> { }
                case CAMINHO_RECONSTRUIDO -> caminhoAtual = evento.getCaminhoFinal();
                case SEM_CAMINHO, ERRO -> { }
            }
        }
        return new EstadoBFS(evento.getDescricao(), processando, filaAtual, visitadasAtuais,
                analisados, descobertas, caminhoAtual, evento.isConcluido());
    }

    private static List<Sala> copia(List<Sala> salas) {
        return salas == null ? Collections.emptyList()
                : Collections.unmodifiableList(new java.util.ArrayList<>(salas));
    }

    /**
     * Retorna a rota formatada em texto (ex: "[1] Entrada -> [2] Corredor -> [7] Salão").
     */
    public String getRotaFormatada() {
        if (!sucesso || caminho.isEmpty()) {
            return "Nenhuma rota disponível.";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < caminho.size(); i++) {
            sb.append(caminho.get(i).getNome());
            if (i < caminho.size() - 1) {
                sb.append("  ➜  ");
            }
        }
        return sb.toString();
    }
}
