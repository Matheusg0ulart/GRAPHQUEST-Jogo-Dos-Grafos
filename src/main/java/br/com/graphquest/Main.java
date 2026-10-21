package br.com.graphquest;

import br.com.graphquest.testes.TestesGrafo;
import br.com.graphquest.ui.TelaInicial;

import javax.swing.*;

/**
 * Ponto de entrada principal da aplicação GraphQuest — A Jornada dos Grafos.
 * 
 * Inicializa a interface gráfica no Event Dispatch Thread (EDT) do Swing
 * e configura o visual e renderização gráfica.
 */
public class Main {

    public static void main(String[] args) {
        // Se chamado com argumento "--testes", executa a suite de testes automatizados no console
        if (args.length > 0 && ("--testes".equalsIgnoreCase(args[0]) || "-t".equalsIgnoreCase(args[0]))) {
            System.out.println("Executando bateria de testes do Grafo e BFS...\n");
            TestesGrafo.main(args);
            return;
        }

        // Configuração de propriedades do sistema para renderização gráfica de alta fidelidade
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        try {
            // Tenta definir o Look and Feel padrão do sistema operacional
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Se falhar, mantém o Look and Feel padrão do Java Swing
        }

        // Inicialização estritamente no Event Dispatch Thread (EDT) conforme boas práticas do Swing
        SwingUtilities.invokeLater(() -> {
            TelaInicial menuInicial = new TelaInicial();
            menuInicial.setVisible(true);
        });
    }
}
