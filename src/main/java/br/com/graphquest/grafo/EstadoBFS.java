package br.com.graphquest.grafo;

import br.com.graphquest.modelo.Sala;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

/** Estado derivado de eventos até um índice; os eventos armazenados permanecem compactos. */
public final class EstadoBFS {
    private final String descricao;
    private final Sala salaAtual;
    private final List<Sala> fila;
    private final List<Sala> visitadas;
    private final List<Sala> vizinhosAnalisados;
    private final List<Sala> recemDescobertas;
    private final List<Sala> caminhoFinal;
    private final boolean concluido;

    EstadoBFS(String descricao, Sala salaAtual, Queue<Sala> fila, Set<Sala> visitadas,
              List<Sala> vizinhosAnalisados, List<Sala> recemDescobertas,
              List<Sala> caminhoFinal, boolean concluido) {
        this.descricao = descricao;
        this.salaAtual = salaAtual;
        this.fila = imutavel(new ArrayList<>(fila));
        this.visitadas = imutavel(new ArrayList<>(visitadas));
        this.vizinhosAnalisados = imutavel(vizinhosAnalisados);
        this.recemDescobertas = imutavel(recemDescobertas);
        this.caminhoFinal = imutavel(caminhoFinal);
        this.concluido = concluido;
    }

    private static List<Sala> imutavel(List<Sala> dados) {
        return Collections.unmodifiableList(new ArrayList<>(dados));
    }

    public String getDescricao() { return descricao; }
    public Sala getSalaAtual() { return salaAtual; }
    public List<Sala> getFila() { return fila; }
    public List<Sala> getVisitadas() { return visitadas; }
    public List<Sala> getVizinhosAnalisados() { return vizinhosAnalisados; }
    public List<Sala> getRecemDescobertas() { return recemDescobertas; }
    public List<Sala> getCaminhoFinal() { return caminhoFinal; }
    public boolean isConcluido() { return concluido; }
}
