package br.com.graphquest.jogo;

/**
 * Representa uma habilidade especial de combate ou exploração do aventureiro.
 */
public enum Habilidade {
    GOLPE_DEVASTADOR(
            "Golpe Devastador",
            "Desfere um ataque concentrado causando 1.7x do dano com alta chance de crítico.",
            3,
            80,
            "⚔"
    ),
    ESCUDO_RUNICO(
            "Escudo Rúnico",
            "Ergue uma barreira mágica que reduz 60% do próximo dano recebido e reflete 15 de dano.",
            3,
            75,
            "🛡"
    ),
    CURA_ARCANA(
            "Cura Arcana",
            "Canaliza energia sagrada para restaurar 35 pontos de vida imediatamente.",
            4,
            70,
            "✨"
    ),
    PASSO_ESPECTRAL(
            "Passo Espectral",
            "Muda de fase no espaço dimensional, concedendo 80% de chance de esquivar do próximo ataque.",
            3,
            85,
            "💨"
    ),
    OLHAR_CARTOGRAFO(
            "Olhar do Cartógrafo",
            "Sintetiza a percepção do grafo, revelando a localização de baús, segredos e rotas ótimas.",
            5,
            65,
            "👁"
    );

    private final String nome;
    private final String descricao;
    private final int cooldownMaximo;
    private final int custoDesbloqueio;
    private final String icone;

    Habilidade(String nome, String descricao, int cooldownMaximo, int custoDesbloqueio, String icone) {
        this.nome = nome;
        this.descricao = descricao;
        this.cooldownMaximo = cooldownMaximo;
        this.custoDesbloqueio = custoDesbloqueio;
        this.icone = icone;
    }

    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public int getCooldownMaximo() { return cooldownMaximo; }
    public int getTempoRecargaTurnos() { return cooldownMaximo; }
    public int getCustoDesbloqueio() { return custoDesbloqueio; }
    public int getCustoMoedas() { return custoDesbloqueio; }
    public String getIcone() { return icone; }
}
