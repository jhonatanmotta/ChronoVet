package controlador;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.JOptionPane;
import modelo.Usuario;
import modelo.UsuarioDAO;
import utils.Sesion;
import vista.Login;
import vista.Menu;

public class Ctrl_login implements ActionListener, KeyListener {

    // atributos del controlador login
    Login log = new Login(); // Objeto de la clase Login
    Menu menu = new Menu(); // Objeto de la clase Menu

    // Constructor con parámetros
    public Ctrl_login(Login log) {
        System.out.println("CTRL LOGIN INSTANCE: " + log);
        this.log = log; // Se le asigna al atributo log la instancia que llega por parametro
        this.log.btn_IniciarSesion.addActionListener(this); // se le añade el ActionListener al boton btn_IniciarSesion
        this.log.txt_correo.addKeyListener(this); // se le añade el KeyListener al textField txt_user
        this.log.txt_password.addKeyListener(this); // se le añade el KeyListener al passwordField txt_password
        styleLogin(); // metodo para inicializar los estilos del JPanel
    }

    // metodo de estilos
    public void styleLogin() {
        // se le asigna el boton de ver contraseñas al passwordField txt_password
        log.txt_password.putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true");
        // se le asigna un placeholder al textField txt_user
        log.txt_correo.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu usuario");
        // se le asigna un placeholder al passwordField txt_password
        log.txt_password.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu contraseña");
    }

    public void iniciarSesion() {
        String correo = log.txt_correo.getText().trim();
        String password = String.valueOf(log.txt_password.getPassword()).trim();

        if (correo.equals("") || password.equals("")) {
            JOptionPane.showMessageDialog(null,
                    "Debes ingresar usuario y contraseña",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario usuarioAutenticado = UsuarioDAO.autenticarUsuario(correo, password);

        if (usuarioAutenticado != null) {
            Sesion sesion = Sesion.getInstancia();

            sesion.setUsuario(
                    usuarioAutenticado
            );

            log.dispose();

            Menu menu = new Menu();
            Ctrl_menu ctrlMenu = new Ctrl_menu(menu);
            menu.setVisible(true);

        } else {

            JOptionPane.showMessageDialog(
                    null,
                    "Usuario o contraseña incorrectos",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == log.btn_IniciarSesion) {
            iniciarSesion();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        // obtiene la accion del textField txt_user
        if (e.getSource() == log.txt_correo) {
            // valida la tecla presionado es el ENTER
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // valida si el texto dentro del textField es vacio
                if (log.txt_correo.getText().equals("")) {
                    // arroja un mensaje informativo
                    JOptionPane.showMessageDialog(null, "Debes agregar un usuario", "Advertencia", JOptionPane.WARNING_MESSAGE);
                } else {
                    // si el textField no es vacio solicita el focus en el campo de password
                    log.txt_password.requestFocus();
                }
            }
            // obtiene la accion del passwordField txt_password    
        } else if (e.getSource() == log.txt_password) {
            // valida la tecla presionado es el ENTER
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                // valida si el texto dentro del textField es vacio
                if (String.valueOf(log.txt_password.getPassword()).equals("")) {
                    // arroja un mensaje informativo
                    JOptionPane.showMessageDialog(null, "Debes agregar la contraseña", "Advertencia", JOptionPane.WARNING_MESSAGE);
                } else {
                    // si el textField no es vacio ejecuta el metodo de iniciar sesion
                    iniciarSesion();
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
