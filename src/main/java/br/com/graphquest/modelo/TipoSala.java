package br.com.graphquest.modelo;

import java.awt.Color;

/**
 * Enumeração que define as categorias de salas presentes na masmorra do GraphQuest.
 * Cada tipo possui uma identidade visual única (cor e rótulo) que auxilia
 * na compreensão didática do grafo.
 */
public enum TipoSala {
    INICIAL("Entrada", new Color(46, 204, 113), "Ponto de partida da aventura"),
    COMUM("Corredor / Câmara", new Color(90, 110, 140), "Câmara regular de trânsito"),
    TESOURO("Tesouro", new Color(241, 196, 15), "Contém riquezas e pontuação"),
    ESPECIAL("Santuário / Enigma", new Color(155, 89, 182), "Local mágico com segredos ou itens"),
    BLOQUEADA("Obstáculo Bloqueado", new Color(231, 76, 60), "Passagem intransitável ou trancada"),
    MERCADOR("Mercador Errante", new Color(230, 126, 34), "Comércio de poções e itens valiosos"),
    DESCANSO("Câmara de Repouso", new Color(26, 188, 156), "Recupera vida e alivia o cansaço"),
    CHEFE("Câmara do Chefe", new Color(192, 57, 43), "Aposento do guardião supremo da fase"),
    SAIDA("Portal de Saída", new Color(52, 152, 219), "Objetivo final: fuga da masmorra");

    private final String nomeExibicao;
    private final Color corTema;
    private final String descricao;

    TipoSala(String nomeExibicao, Color corTema, String descricao) {
        this.nomeExibicao = nomeExibicao;
        this.corTema = corTema;
        this.descricao = descricao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public Color getCorTema() {
        return corTema;
    }

    public String getDescricao() {
        return descricao;
    }
}
