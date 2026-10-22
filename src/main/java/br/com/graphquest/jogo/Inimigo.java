package br.com.graphquest.jogo;

import java.util.Random;

/**
 * Entidade de combate do GraphQuest.
 * 
 * Modela os monstros e chefes da masmorra, com atributos de vida, dano, defesa,
 * habilidades especiais e comportamentos distintos de acordo com a fase e a progressão.
 */
public final class Inimigo {
    private final String nome;
    private final int vidaMaxima;
    private final int dano;
    private final boolean chefe;
    private final int defesa;
    private final String habilidadeEspecial;
    private final String descricao;
    private int vida;
    private boolean enfurecido;
    private final Random random = new Random();

    public record AcaoInimigo(int danoEfetivo, String mensagem, boolean golpeEspecial) {}

    public Inimigo(String nome, int vida, int dano, boolean chefe) {
        this(nome, vida, dano, chefe, chefe ? 4 : 1, chefe ? "Fúria Ancestral" : "Golpe Rápido", "Um habitante hostil das profundezas.");
    }

    public Inimigo(String nome, int vida, int dano, boolean chefe, int defesa, String habilidadeEspecial, String descricao) {
        this.nome = nome;
        this.vidaMaxima = vida;
        this.vida = vida;
        this.dano = dano;
        this.chefe = chefe;
        this.defesa = defesa;
        this.habilidadeEspecial = habilidadeEspecial;
        this.descricao = descricao;
        this.enfurecido = false;
    }

    public String getNome() { return nome; }
    public int getVida() { return vida; }
    public int getVidaMaxima() { return vidaMaxima; }
    public int getDano() { return dano; }
    public boolean isChefe() { return chefe; }
    public int getDefesa() { return defesa; }
    public String getHabilidadeEspecial() { return habilidadeEspecial; }
    public String getDescricao() { return descricao; }
    public boolean isDerrotado() { return vida <= 0; }
    public boolean isEnfurecido() { return enfurecido; }

    public void receberDano(int quantidade) {
        vida = Math.max(0, vida - Math.max(0, quantidade));
        if (chefe && !enfurecido && vida <= vidaMaxima / 2) {
            enfurecido = true;
        }
    }

    /**
     * Executa o turno do inimigo, aplicando variação de dano e habilidades especiais.
     */
    public AcaoInimigo executarTurno(int turno) {
        if (isDerrotado()) {
            return new AcaoInimigo(0, nome + " está derrotado.", false);
        }

        // Se for chefe abaixo de 50% vida, tem maior chance de golpe especial
        boolean usarEspecial = (chefe && enfurecido && random.nextInt(100) < 45) || (random.nextInt(100) < 25);

        if (usarEspecial) {
            int danoEspecial = (int) Math.round(dano * (enfurecido ? 1.5 : 1.3));
            String msg = (enfurecido ? "⚡ [FÚRIA] " : "💥 ") + nome + " canaliza " + habilidadeEspecial + " causando " + danoEspecial + " de dano!";
            return new AcaoInimigo(danoEspecial, msg, true);
        }

        int variacao = random.nextInt(5) - 2; // -2 a +2
        int danoTurno = Math.max(1, dano + variacao);
        String msg = nome + " ataca com ferocidade e causa " + danoTurno + " de dano.";
        return new AcaoInimigo(danoTurno, msg, false);
    }

    /**
     * Fábrica de inimigos balanceados por bioma e fase.
     */
    public static Inimigo criarParaFase(int fase, int idSala, boolean chefe) {
        if (chefe) {
            return switch (fase) {
                case 4 -> new Inimigo("Arauto do Abismo", 140, 20, true, 4,
                        "Rajada Sombria", "Um general espectral que comanda as sombras do abismo.");
                case 8 -> new Inimigo("Dragão das Sombras", 220, 26, true, 6,
                        "Sopro do Caos", "O guardião milenar que desafia apenas os verdadeiros mestres dos grafos.");
                default -> new Inimigo("Guardião Supremo da Fase " + fase, 100 + fase * 14, 14 + fase * 2, true, 3,
                        "Impacto Sísmico", "Guardião monumental da masmorra.");
            };
        }

        return switch (fase) {
            case 1 -> new Inimigo("Guardião de Pedra", 48, 10, false, 1,
                    "Golpe Esmagador", "Uma estátua animada por magia antiga.");
            case 2 -> new Inimigo("Gárgula de Sangue", 55, 12, false, 2,
                    "Rasgar Asas", "Monstro alado com garras afiadas.");
            case 3 -> new Inimigo("Espectro das Almas", 62, 14, false, 1,
                    "Toque Frígido", "Espírito inquieto que assombra os corredores.");
            case 4 -> new Inimigo("Cavaleiro Espectral", 74, 16, false, 3,
                    "Investida Espiritual", "Guerreiro reanimado vestindo armadura pesada.");
            case 5 -> new Inimigo("Golem de Gelo", 86, 17, false, 4,
                    "Geada Esmagadora", "Criatura colossal de gelo impenetrável.");
            case 6 -> new Inimigo("Demônio das Cinzas", 98, 19, false, 2,
                    "Chamas Negras", "Entidade infernal forjada no magma profundo.");
            case 7 -> new Inimigo("Autômato Arcano", 110, 21, false, 4,
                    "Descarga Elétrica", "Máquina rúnica desenhada para proteger o templo.");
            default -> new Inimigo("Sentinela do Caos", 125, 23, false, 3,
                    "Vórtice Dimensional", "Guerreiro das sombras habitando o santuário astral.");
        };
    }
}
