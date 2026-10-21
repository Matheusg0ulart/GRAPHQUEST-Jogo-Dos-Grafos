package br.com.graphquest.jogo;

/**
 * Interface observadora (Observer Pattern) para notificar as telas e painéis Swing
 * sobre alterações no estado da partida (movimentos, pontuação, BFS e reinício).
 */
public interface JogoListener {
    void onEstadoAtualizado();
}
