package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Evento compacto registrado durante a execução da BFS. */
public final class PassoBFS {
    public enum Tipo { INICIO, PROCESSAMENTO, VIZINHO_BLOQUEADO, VIZINHO_VISITADO,
        VIZINHO_DESCOBERTO, DESTINO_ENCONTRADO, CAMINHO_RECONSTRUIDO, SEM_CAMINHO, ERRO }

    private final Tipo tipo;
    private final String descricao;
    private final Sala salaAtual;
    private final Sala salaAfetada;
    private final List<Sala> caminhoFinal;
    private final boolean concluido;

    public PassoBFS(Tipo tipo, String descricao, Sala salaAtual, Sala salaAfetada,
                    List<Sala> caminhoFinal, boolean concluido) {
        this.tipo = tipo;
        this.descricao = descricao;
        this.salaAtual = salaAtual;
        this.salaAfetada = salaAfetada;
        this.caminhoFinal = caminhoFinal == null ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(caminhoFinal));
        this.concluido = concluido;
    }

    public Tipo getTipo() { return tipo; }
    public String getDescricao() { return descricao; }
    public Sala getSalaAtual() { return salaAtual; }
    public Sala getSalaAfetada() { return salaAfetada; }
    public List<Sala> getCaminhoFinal() { return caminhoFinal; }
    public boolean isConcluido() { return concluido; }
}
