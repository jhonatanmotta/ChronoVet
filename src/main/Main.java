package main;

import com.formdev.flatlaf.themes.FlatMacLightLaf;
import controlador.Ctrl_login;
import vista.Login;

public class Main {

    public static void main(String[] args) {
        // 1. Inicializar el Look and Feel (tema visual)
        FlatMacLightLaf.setup();
        // 2. Ejecutar la interfaz en el hilo correcto de Swing
        java.awt.EventQueue.invokeLater(() -> {
            // 3. Abrir la primera pantalla (Login)
            Login login = new Login();
            Ctrl_login controladorLogin = new Ctrl_login(login);
            login.setVisible(true);
        });
    }
}
