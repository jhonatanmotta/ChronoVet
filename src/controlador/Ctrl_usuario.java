package controlador;

import com.formdev.flatlaf.FlatClientProperties;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.JOptionPane;
import modelo.Rol;
import modelo.RolDAO;
import modelo.Usuario;
import modelo.UsuarioDAO;
import utils.ComboBox;
import utils.Sesion;
import utils.Validaciones;
import vista.Panel_RegistrarUsuario;

public class Ctrl_usuario implements ActionListener {

    private Panel_RegistrarUsuario vista;
    Usuario usuarioActual = Sesion.getInstancia().getUsuario();

    public Ctrl_usuario(Panel_RegistrarUsuario vista) {
        this.vista = vista;
        this.vista.getBtn_registrarUsuario().addActionListener(this);
        this.vista.getBtn_limpiarUsuario().addActionListener(this);
        llenarComboRoles();
        styleRegistroUsuarios();
    }

    public void styleRegistroUsuarios() {
        vista.getTextPassword().putClientProperty(FlatClientProperties.STYLE, "" + "showRevealButton:true");
        vista.getTextNombre().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu nombre");
        vista.getTextApellido().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu apellido");
        vista.getTextTelefono().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu telefono");
        vista.getTextCorreo().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu correo");
        vista.getTextPassword().putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Ingresa tu contraseña");

        if (usuarioActual.getIdRol() == 2 || usuarioActual.getIdRol() == 4) {
            vista.getComboRol().setVisible(false);
            vista.getComboRol().setEnabled(false);
        }
    }

    private void llenarComboRoles() {

        vista.getComboRol().removeAllItems();
        vista.getComboRol().addItem(new ComboBox(0, "Selecciona un rol"));
        vista.getComboRol().setSelectedIndex(0);

        String rolActual = usuarioActual.getRol();

        List<Rol> listaRoles = RolDAO.listarRoles();

        for (Rol rol : listaRoles) {
            String nombreRol = rol.getNombreRol();

            if (puedeMostrarRol(rolActual, nombreRol)) {
                vista.getComboRol().addItem(new ComboBox(rol.getIdRol(), nombreRol));
            }
        }
    }

    private boolean puedeMostrarRol(String rolActual, String rolMostrar) {

        if (rolActual.equals("Administrador")) {
            return true;
        }

        if (rolActual.equals("Administrativo")) {
            return rolMostrar.equals("Veterinario")
                || rolMostrar.equals("Cliente");
        }
        return false;

    }

    public void registrarUsuario() {

        String nombre = vista.getTextNombre().getText().trim();
        String apellido = vista.getTextApellido().getText().trim();
        String correo = vista.getTextCorreo().getText().trim();
        String telefono = vista.getTextTelefono().getText().trim();
        String password = String.valueOf(vista.getTextPassword().getPassword());

        if (!Validaciones.validarNoVacios(
                "Todos los campos son obligatorios",
                nombre, apellido, correo, telefono, password)) {
            return;
        }

        if (!Validaciones.validarCorreo(correo)) {
            Validaciones.mostrarError("El correo no tiene formato válido");
            return;
        }

//    if (!Validaciones.validarRangoCaracteres(password, 10, 30,
//            "La contraseña debe tener mínimo 10 caracteres")) {
//        return;
//    }
        if (!Validaciones.validarRangoCaracteres(telefono, 10, 15,
                "El teléfono debe tener entre 10 y 15 caracteres")) {
            return;
        }

//    if (!Validaciones.validarContrasena(password)) {
//        Validaciones.mostrarError(
//                "La contraseña debe tener mayúsculas, minúsculas, números y símbolo");
//        return;
//    }
        Object selected = vista.getComboRol().getSelectedItem();

        // se le asigna el valor a la variable rol
        if (!(selected instanceof ComboBox rol)) {
            Validaciones.mostrarError("Debes seleccionar un rol válido");
            return;
        }

        if (rol.getId() == 0) {
            Validaciones.mostrarError("Debes seleccionar un rol válido");
            return;
        }

        String rolActual = usuarioActual.getRol();
        
        if (rolActual.equals("Administrativo")
            &&
            rol.getNombre().equals("Administrador")) {

        Validaciones.mostrarError(
                "No tienes permisos para crear administradores");

        return;
    }

        Usuario user = new Usuario();
        user.setNombre(nombre);
        user.setApellido(apellido);
        user.setCorreo(correo);
        user.setTelefono(telefono);
        user.setPassword(password);
        user.setIdRol(rol.getId());

        boolean resultado = UsuarioDAO.registroUsuario(user);

        if (resultado) {
            limpiarContenidoInputs();
            JOptionPane.showMessageDialog(null, "Usuario registrado con éxito");
        } else {
            JOptionPane.showMessageDialog(null, "Error al registrar el usuario");
        }
    }

    public void limpiarContenidoInputs() {
        vista.getTextNombre().setText("");
        vista.getTextApellido().setText("");
        vista.getTextCorreo().setText("");
        vista.getTextTelefono().setText("");
        vista.getTextPassword().setText("");
        vista.getComboRol().setSelectedIndex(0);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtn_registrarUsuario()) {
            registrarUsuario();
        } else if (e.getSource() == vista.getBtn_limpiarUsuario()) {
            limpiarContenidoInputs();
        }
    }
}
