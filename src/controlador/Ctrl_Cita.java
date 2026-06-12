package controlador;

import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Cita;
import modelo.CitaDAO;
import modelo.Especialidad;
import modelo.EspecialidadDAO;
import modelo.Mascota;
import modelo.MascotaDAO;
import modelo.Usuario;
import modelo.VeterinarioDAO;
import utils.ComboBox;
import utils.RespuestaCita;
import utils.Sesion;
import vista.Panel_RegistrarCitas;

public class Ctrl_Cita {

    private Panel_RegistrarCitas vista;

    private Usuario usuarioActual;

    public Ctrl_Cita(Panel_RegistrarCitas vista) {

        this.vista = vista;

        this.usuarioActual = Sesion.getInstancia().getUsuario();
        
        configurarTabla();
        
        cargarTablaCitas();
        
        vista.getDateFecha().setMinSelectableDate(new java.util.Date());

        llenarComboMascotas();
        llenarComboEspecialidades();
        llenarComboHoras();

        iniciarEventos();
    }

    private void configurarTabla() {

    vista.getTablaCitas()
            .setRowSelectionAllowed(false);

    vista.getTablaCitas()
            .setCellSelectionEnabled(false);

    vista.getTablaCitas()
            .setFocusable(false);
}
    
    private void iniciarEventos() {

        vista.getBtn_resgistrarCita()
                .addActionListener(
                        e -> registrarCita()
                );

        vista.getBtn_limpiar()
                .addActionListener(
                        e -> limpiarCampos()
                );
    }

    private void llenarComboMascotas() {

        vista.getComboMascota().removeAllItems();

        vista.getComboMascota().addItem(
                new ComboBox(
                        0,
                        "Seleccione una mascota"
                )
        );

        List<Mascota> listaMascotas;

        String rol = usuarioActual.getRol();

        MascotaDAO mascotaDAO = new MascotaDAO();
        if (rol.equals("Cliente")) {

            listaMascotas
                    = mascotaDAO.listarPorDueno(
                            usuarioActual.getIdUsuario()
                    );

        } else {

            listaMascotas
                    = mascotaDAO.listarTodas();
        }

        for (Mascota mascota : listaMascotas) {

            String textoMostrar;

            if (usuarioActual.getRol().equals("Cliente")) {

                textoMostrar = mascota.getNombre();

            } else {

                textoMostrar
                        = mascota.getNombre()
                        + " - "
                        + mascota.getNombreDueno();
            }

            vista.getComboMascota().addItem(
                    new ComboBox(
                            mascota.getIdMascota(),
                            textoMostrar
                    )
            );
        }
    }

    private void llenarComboEspecialidades() {

        vista.getComboEspecialidad()
                .removeAllItems();

        vista.getComboEspecialidad()
                .addItem(
                        new ComboBox(
                                0,
                                "Seleccione una especialidad"
                        )
                );

        List<Especialidad> lista
                = EspecialidadDAO.listarEspecialidades();

        for (Especialidad especialidad : lista) {

            vista.getComboEspecialidad()
                    .addItem(
                            new ComboBox(
                                    especialidad.getIdEspecialidad(),
                                    especialidad.getNombre()
                            )
                    );
        }
    }

    private void llenarComboHoras() {

        vista.getComboHora().removeAllItems();

        vista.getComboHora()
                .addItem(
                        "Seleccione una hora"
                );

        for (int hora = 8; hora <= 16; hora++) {

            vista.getComboHora()
                    .addItem(
                            String.format(
                                    "%02d:00",
                                    hora
                            )
                    );
        }
    }

    private void limpiarCampos() {

        vista.getComboMascota()
                .setSelectedIndex(0);

        vista.getComboEspecialidad()
                .setSelectedIndex(0);

        vista.getComboHora()
                .setSelectedIndex(0);

        vista.getDateFecha()
                .setDate(null);

        vista.getTextMotivo()
                .setText("");
    }

    private void registrarCita() {

        try {

            // Obtener valores de los combos
            ComboBox mascotaSel = (ComboBox) vista.getComboMascota().getSelectedItem();
            ComboBox especialidadSel = (ComboBox) vista.getComboEspecialidad().getSelectedItem();
            String horaSel = (String) vista.getComboHora().getSelectedItem();

            // Validaciones basicas
            if (mascotaSel == null || mascotaSel.getId() == 0
                    || especialidadSel == null || especialidadSel.getId() == 0
                    || horaSel.equals("Seleccione una hora")
                    || vista.getDateFecha().getDate() == null) {

                JOptionPane.showMessageDialog(null, "Debe completar todos los campos");
                return;
            }

            // IDs
            int idMascota = mascotaSel.getId();
            int idEspecialidad = especialidadSel.getId();
            int idUsuario;
            String rol = usuarioActual.getRol();

            if (rol.equals("Cliente")) {

                idUsuario = usuarioActual.getIdUsuario();

            } else {

                MascotaDAO mascotaDAO = new MascotaDAO();
                Mascota mascota = mascotaDAO.buscarPorId(idMascota);

                if (mascota == null) {
                    JOptionPane.showMessageDialog(null, "No se encontró la mascota");
                    return;
                }

                idUsuario = mascota.getIdDueno();
            }

            // Fecha + hora: JDateChooser + combo hora
            java.util.Date fechaBase = vista.getDateFecha().getDate();

            int hora = Integer.parseInt(horaSel.split(":")[0]);

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(fechaBase);
            cal.set(java.util.Calendar.HOUR_OF_DAY, hora);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);

            java.sql.Timestamp fechaHora = new java.sql.Timestamp(cal.getTimeInMillis());

            java.sql.Timestamp ahora = new java.sql.Timestamp(System.currentTimeMillis());

            if (fechaHora.before(ahora)) {
                JOptionPane.showMessageDialog(
                        null,
                        "No se pueden agendar citas en fechas u horas pasadas."
                );
                return;
            }

            // Motivo
            String motivo = vista.getTextMotivo().getText();

            if (motivo.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null, "Debe ingresar el motivo de la cita");
                return;
            }

            // Crear objeto Cita
            Cita cita = new Cita();
            cita.setIdUsuario(idUsuario);
            cita.setIdMascota(idMascota);
            cita.setIdEspecialidad(idEspecialidad);
            cita.setFechaHora(fechaHora);
            cita.setMotivoConsulta(motivo);

            // Llamar al DAO
            CitaDAO dao = new CitaDAO();
            RespuestaCita resp = dao.registrar(cita);

            // mostrar mensaje del SP
            JOptionPane.showMessageDialog(null, resp.getMensaje());

            if (resp.getIdCita() > 0) {
                JOptionPane.showMessageDialog(null, "Cita creada con ID: " + resp.getIdCita());
                limpiarCampos();
                cargarTablaCitas();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al registrar cita: " + e.getMessage());
        }
    }
    
    private void cargarTablaCitas() {

    DefaultTableModel modelo =
            (DefaultTableModel) vista.getTablaCitas().getModel();

    modelo.setRowCount(0);

    CitaDAO citaDAO = new CitaDAO();

    List<Cita> lista;

    String rol = usuarioActual.getRol();

    if (rol.equals("Cliente")) {

        lista = citaDAO.listarPorCliente(
                usuarioActual.getIdUsuario()
        );

    } else if (rol.equals("Veterinario")) {

        VeterinarioDAO veterinarioDAO =
                new VeterinarioDAO();

        int idVeterinario =
        veterinarioDAO.obtenerIdVeterinario(
                usuarioActual.getIdUsuario()
        );

        lista = citaDAO.listarPorVeterinario(
                idVeterinario
        );

    } else {

        // Administrador y Administrativo
        lista = citaDAO.listarTodas();
    }

    for (Cita cita : lista) {

        modelo.addRow(new Object[]{
            cita.getIdCita(),
            cita.getNombreMascota(),
            cita.getNombreVeterinario(),
            cita.getNombreEspecialidad(),
            cita.getFechaHora(),
            cita.getEstado()
        });
    }
}
    
    public void cargarDatos() {

    llenarComboMascotas();

    limpiarCampos();
}
    
}
