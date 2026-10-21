package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Resultado independente da busca de menor custo por Dijkstra. */
public class ResultadoDijkstra {
    private final boolean sucesso;
    private final Sala origem;
    private final Sala destino;
    private final List<Sala> caminho;
    private final List<Sala> ordemExploracao;
    private final int custoTotal;
    private final String mensagem;

    public ResultadoDijkstra(boolean sucesso, Sala origem, Sala destino, List<Sala> caminho,
                             List<Sala> ordemExploracao, int custoTotal, String mensagem) {
        this.sucesso = sucesso;
        this.origem = origem;
        this.destino = destino;
        this.caminho = Collections.unmodifiableList(new ArrayList<>(caminho));
        this.ordemExploracao = Collections.unmodifiableList(new ArrayList<>(ordemExploracao));
        this.custoTotal = custoTotal;
        this.mensagem = mensagem;
    }

    public static ResultadoDijkstra falha(Sala origem, Sala destino, String mensagem) {
        return new ResultadoDijkstra(false, origem, destino, Collections.emptyList(),
                Collections.emptyList(), Integer.MAX_VALUE, mensagem);
    }

    public boolean isSucesso() { return sucesso; }
    public Sala getOrigem() { return origem; }
    public Sala getDestino() { return destino; }
    public List<Sala> getCaminho() { return caminho; }
    public List<Sala> getOrdemExploracao() { return ordemExploracao; }
    public int getCustoTotal() { return custoTotal; }
    public int getQuantidadeArestas() { return sucesso ? Math.max(0, caminho.size() - 1) : 0; }
    public String getMensagem() { return mensagem; }

    public String getRotaFormatada() {
        if (!sucesso || caminho.isEmpty()) return "Nenhuma rota disponível.";
        return String.join("  ➜  ", caminho.stream()
                .map(sala -> "[" + sala.getId() + "] " + sala.getNome()).toList());
    }
}
