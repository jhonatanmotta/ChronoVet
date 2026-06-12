package controlador;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Cita;
import modelo.CitaDAO;
import modelo.Usuario;
import modelo.VeterinarioDAO;
import utils.Sesion;
import vista.Panel_GestionarCitas;
import vista.Panel_RegistrarConsulta;

public class Ctrl_GestionCitas {

    private Panel_GestionarCitas vista;

    private Usuario usuarioActual;

    private int idCitaSeleccionada = -1;

    public Ctrl_GestionCitas(Panel_GestionarCitas vista) {

        this.vista = vista;

        this.usuarioActual
                = Sesion.getInstancia().getUsuario();

        configurarCamposSoloLectura();
        
        configurarVistaSegunRol();
        
        vista.getBtnAccionCita()
            .setEnabled(false);

        cargarTabla();

        iniciarEventos();
    }

    private void configurarCamposSoloLectura() {

        vista.getTextIdCita()
                .setEditable(false);

        vista.getTextMascota()
                .setEditable(false);

        vista.getTextCliente()
                .setEditable(false);

        vista.getTextVeterinario()
                .setEditable(false);

        vista.getTextEspecialidad()
                .setEditable(false);

        vista.getTextFechaYHora()
                .setEditable(false);

        vista.getTextEstado()
                .setEditable(false);

        vista.getTextMotivo()
                .setEditable(false);
    }

    private void iniciarEventos() {

        vista.getTablaCitas()
                .addMouseListener(
                        new MouseAdapter() {

                    @Override
                    public void mouseClicked(
                            MouseEvent e) {

                        cargarDatosSeleccionados();
                    }
                });

        vista.getBtnAccionCita()
                .addActionListener(
                        e -> ejecutarAccionPrincipal()
                );

        vista.getBtnLimpiar()
                .addActionListener(
                        e -> limpiarCampos()
                );
    }

    private void ejecutarAccionPrincipal() {

        String rol = usuarioActual.getRol();

        if (rol.equals("Veterinario")) {

            realizarConsulta();

        } else {

            cancelarCita();
        }
    }

    private void configurarVistaSegunRol() {

        vista.getTextEstado().setVisible(false);
        
        String rol = usuarioActual.getRol();

        if (rol.equals("Veterinario")) {

            vista.getBtnAccionCita()
                    .setText("Realizar consulta");

            vista.getBtnAccionCita()
                    .setBackground(
                            new java.awt.Color(40, 167, 69)
                    );

        } else {

            vista.getBtnAccionCita()
                    .setText("Cancelar cita");

            vista.getBtnAccionCita()
                    .setBackground(
                            new java.awt.Color(220, 53, 69)
                    );
        }
    }

    private void cargarTabla() {

        DefaultTableModel modelo
                = (DefaultTableModel) vista.getTablaCitas().getModel();

        modelo.setRowCount(0);

        CitaDAO citaDAO = new CitaDAO();

        List<Cita> lista;

        String rol = usuarioActual.getRol();

        if (rol.equals("Cliente")) {

            lista = citaDAO.listarPorCliente(
                    usuarioActual.getIdUsuario()
            );

        } else if (rol.equals("Veterinario")) {

            VeterinarioDAO veterinarioDAO
                    = new VeterinarioDAO();

            int idVeterinario
                    = veterinarioDAO.obtenerIdVeterinario(
                            usuarioActual.getIdUsuario()
                    );

            lista = citaDAO.listarPorVeterinario(
                    idVeterinario
            );

        } else {

            lista = citaDAO.listarTodas();
        }

        for (Cita cita : lista) {

            modelo.addRow(new Object[]{
                cita.getIdCita(),
                cita.getNombreMascota(),
                cita.getNombreCliente(),
                cita.getNombreVeterinario(),
                cita.getNombreEspecialidad(),
                cita.getFechaHora(),
                cita.getEstado()
            });
        }
    }

    private void cargarDatosSeleccionados() {

        int fila
                = vista.getTablaCitas()
                        .getSelectedRow();

        if (fila == -1) {
            return;
        }

        idCitaSeleccionada
                = Integer.parseInt(
                        vista.getTablaCitas()
                                .getValueAt(fila, 0)
                                .toString()
                );

        CitaDAO dao = new CitaDAO();

        Cita cita = dao.buscarPorId(idCitaSeleccionada);

        if (cita == null) {
            return;
        }

        vista.getTextMotivo()
                .setText(
                        cita.getMotivoConsulta()
                );

        vista.getTextIdCita()
                .setText(
                        "" + cita.getIdCita()
                );

        vista.getTextMascota()
                .setText(
                        cita.getNombreMascota()
                );

        vista.getTextVeterinario()
                .setText(
                        cita.getNombreVeterinario()
                );

        vista.getTextCliente()
                .setText(
                        cita.getNombreCliente()
                );

        vista.getTextEspecialidad()
                .setText(
                        cita.getNombreEspecialidad()
                );

        SimpleDateFormat formatoFechaHora = new SimpleDateFormat("dd/MM/yyyy HH:mm");

        if (cita.getFechaHora() != null) {
            String fechaHoraFormateada = formatoFechaHora.format(cita.getFechaHora());
            vista.getTextFechaYHora().setText(fechaHoraFormateada);
        } else {
            vista.getTextFechaYHora().setText("");
        }

        vista.getTextEstado()
                .setText(
                        cita.getEstado()
                );
        
String estado = cita.getEstado();

vista.getBtnAccionCita()
        .setEnabled(
                estado.equalsIgnoreCase("pendiente")
        );
    }

    private void cancelarCita() {

        if (idCitaSeleccionada == -1) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione una cita"
            );

            return;
        }

        int opcion
                = JOptionPane.showConfirmDialog(
                        null,
                        "¿Desea cancelar esta cita?",
                        "Confirmar",
                        JOptionPane.YES_NO_OPTION
                );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        
String estado = vista.getTextEstado().getText();

if (!estado.equalsIgnoreCase("pendiente")) {

    JOptionPane.showMessageDialog(
            null,
            "Solo se pueden cancelar citas pendientes"
    );

    return;
}

        
        CitaDAO dao = new CitaDAO();

        boolean resultado
                = dao.cancelarCita(idCitaSeleccionada);

        if (resultado) {

            JOptionPane.showMessageDialog(
                    null,
                    "Cita cancelada correctamente"
            );

            cargarTabla();

            limpiarCampos();

        } else {

            JOptionPane.showMessageDialog(
                    null,
                    "No fue posible cancelar la cita"
            );
        }
    }

    private void realizarConsulta() {

 if (idCitaSeleccionada == -1) {
        JOptionPane.showMessageDialog(
                vista,
                "Seleccione una cita",
                "Atención",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Obtener el estado de la cita desde el campo de texto
    String estado = vista.getTextEstado().getText();
    
    if (!estado.equalsIgnoreCase("pendiente")) {
        JOptionPane.showMessageDialog(
                vista,
                "Solo se pueden atender citas en estado pendiente.\n" +
                "La cita seleccionada está: " + estado,
                "Acción no permitida",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Verificar que el veterinario logueado sea el asignado
    String veterinarioAsignado = vista.getTextVeterinario().getText();
    String veterinarioLogueado = usuarioActual.getNombre() + " " + usuarioActual.getApellido();
    
    if (!veterinarioAsignado.equals(veterinarioLogueado)) {
        JOptionPane.showMessageDialog(
                vista,
                "Solo el veterinario asignado puede atender esta cita.\n" +
                "Veterinario asignado: " + veterinarioAsignado,
                "Acceso denegado",
                JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    // Usar JDialog para mostrar el panel de registro de consulta
    java.awt.Frame parent = (java.awt.Frame) javax.swing.SwingUtilities.getWindowAncestor(vista);
    javax.swing.JDialog dialog = new javax.swing.JDialog(parent, "Registrar Consulta Médica", true);
    
    // Crear el panel con los parametros necesarios
    Panel_RegistrarConsulta panelConsulta = new Panel_RegistrarConsulta(
        idCitaSeleccionada,
        () -> {
            // Callback: cuando se registre o cancele exitosamente
            dialog.dispose();        // Cerrar el diálogo
            recargarTabla();         // Recargar la tabla de citas
            limpiarCampos();         // Limpiar campos del formulario
            JOptionPane.showMessageDialog(vista,
                "La lista de citas ha sido actualizada.",
                "Actualización",
                JOptionPane.INFORMATION_MESSAGE);
        }
    );
    
    dialog.add(panelConsulta);
    dialog.setSize(1050, 750);
    dialog.setLocationRelativeTo(vista);
    dialog.setVisible(true);
    }

    private void limpiarCampos() {

        idCitaSeleccionada = -1;

        vista.getTablaCitas()
                .clearSelection();

        vista.getTextMotivo()
                .setText("");

        vista.getTextIdCita()
                .setText("");

        vista.getTextMascota()
                .setText("");

        vista.getTextCliente()
                .setText("");

        vista.getTextVeterinario()
                .setText("");

        vista.getTextEspecialidad()
                .setText("");

        vista.getTextFechaYHora()
                .setText("");

        vista.getTextEstado()
                .setText("");
        
        vista.getBtnAccionCita()
            .setEnabled(false);
    }
    
    public void recargarTabla() {
    cargarTabla();
    
}
}
