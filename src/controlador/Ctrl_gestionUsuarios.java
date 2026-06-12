package controlador;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Usuario;
import modelo.UsuarioDAO;
import utils.Sesion;
import vista.Panel_GestionarUsuarios;

public class Ctrl_gestionUsuarios {

    private Panel_GestionarUsuarios vista;
    private Usuario usuarioActual;
    private int idUsuarioSeleccionado = -1;
    private DefaultTableModel modelo;

    public Ctrl_gestionUsuarios(Panel_GestionarUsuarios vista) {

        this.vista = vista;

        this.usuarioActual = Sesion.getInstancia().getUsuario();
        
        llenarComboEstado();
        
        listarUsuarios();

        iniciarEventos();
    }
    
    private void llenarComboEstado() {

    vista.getComboEstado().removeAllItems();

    vista.getComboEstado()
            .addItem("Selecciona un estado");

    vista.getComboEstado()
            .addItem("activo");

    vista.getComboEstado()
            .addItem("inactivo");

    vista.getComboEstado().setSelectedIndex(0);
    
    vista.getComboEstado().setEnabled(false);
}

    private void listarUsuarios() {

        modelo = (DefaultTableModel) vista.getTablaUsuarios().getModel();

        modelo.setRowCount(0);

        List<Usuario> listaUsuarios = UsuarioDAO.listarUsuarios(usuarioActual);

        for (Usuario usuario : listaUsuarios) {

            modelo.addRow(new Object[]{
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getCorreo(),
                usuario.getRol(),
                usuario.getEstado()
            });
        }
        
        vista.getTablaUsuarios()
     .getColumnModel()
     .getColumn(0)
     .setMinWidth(0);

vista.getTablaUsuarios()
     .getColumnModel()
     .getColumn(0)
     .setMaxWidth(0);

vista.getTablaUsuarios()
     .getColumnModel()
     .getColumn(0)
     .setPreferredWidth(0);

    }

    private void iniciarEventos() {

        vista.getTablaUsuarios()
                .addMouseListener(
                        new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        cargarDatosTabla();
                    }
                });

        vista.getBtn_actualizarUsuario()
                .addActionListener(
                        e -> actualizarUsuario()
                );

        vista.getBtn_limpiarUsuario()
                .addActionListener(
                        e -> limpiarCampos()
                );
    }

    private void limpiarCampos() {

        vista.getTextNombre().setText("");
        vista.getTextApellido().setText("");
        vista.getTextCorreo().setText("");
        vista.getTextTelefono().setText("");
        
        idUsuarioSeleccionado = -1;
        
        vista.getTablaUsuarios().clearSelection();
        
        vista.getComboEstado().setSelectedIndex(0);
        vista.getComboEstado().setEnabled(false);
    }


    private void cargarDatosTabla() {

        int fila
                = vista.getTablaUsuarios()
                        .getSelectedRow();

        if (fila == -1) {
            return;
        }

        idUsuarioSeleccionado = Integer.parseInt(vista.getTablaUsuarios().getValueAt(fila, 0).toString()
        );

        Usuario usuario = UsuarioDAO.buscarPorId(idUsuarioSeleccionado);

        if (usuario == null) {
            return;
        }

        vista.getTextNombre()
                .setText(usuario.getNombre());

        vista.getTextApellido()
                .setText(usuario.getApellido());

        vista.getTextCorreo()
                .setText(usuario.getCorreo());
        
        vista.getTextTelefono()
                .setText(usuario.getTelefono());
        
        vista.getComboEstado().setEnabled(true);
        
        vista.getComboEstado()
                .setSelectedItem(usuario.getEstado());
    }
    
private void actualizarUsuario() {

    
    if (vista.getTextNombre().getText().trim().isEmpty()
        || vista.getTextApellido().getText().trim().isEmpty()
        || vista.getTextCorreo().getText().trim().isEmpty()) {

    JOptionPane.showMessageDialog(
            null,
            "Todos los campos son obligatorios");

    return;
}
    
    if (idUsuarioSeleccionado == -1) {

        JOptionPane.showMessageDialog(
                null,
                "Seleccione un usuario");

        return;
    }

    Usuario usuario =
            new Usuario();

    usuario.setIdUsuario(
            idUsuarioSeleccionado);

    usuario.setNombre(
            vista.getTextNombre()
                    .getText()
                    .trim());

    usuario.setApellido(
            vista.getTextApellido()
                    .getText()
                    .trim());

    usuario.setCorreo(
            vista.getTextCorreo()
                    .getText()
                    .trim());

    usuario.setTelefono(
            vista.getTextTelefono()
                    .getText()
                    .trim());

    String estado =
        vista.getComboEstado()
                .getSelectedItem()
                .toString();

if (estado.equals("Selecciona un estado")) {

    JOptionPane.showMessageDialog(
            null,
            "Debe seleccionar un estado");

    return;
}
    
    usuario.setEstado(estado);

    boolean actualizado = UsuarioDAO.actualizarUsuario(usuario);

    if (actualizado) {

        JOptionPane.showMessageDialog(
                null,
                "Usuario actualizado");

        listarUsuarios();

        limpiarCampos();

    } else {

        JOptionPane.showMessageDialog(
                null,
                "Error al actualizar");
    }
}

public void recargarTabla() {

    listarUsuarios();

    limpiarCampos();
}

}
