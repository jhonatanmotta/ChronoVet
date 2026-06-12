package controlador;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import modelo.Mascota;
import modelo.MascotaDAO;
import modelo.Usuario;
import modelo.UsuarioDAO;
import utils.ComboBox;
import utils.Validaciones;
import vista.Panel_GestionarMascotas;

public class Ctrl_Mascota implements ActionListener {

    private Panel_GestionarMascotas vista;
    private MascotaDAO daoMascota = new MascotaDAO();
    private UsuarioDAO daoUsuario = new UsuarioDAO();

    private Usuario usuarioSesion;

    private DefaultTableModel modelo;
    private int idMascotaSeleccionada = 0;

    public Ctrl_Mascota(Panel_GestionarMascotas vista, Usuario usuarioSesion) {
        this.vista = vista;
        this.usuarioSesion = usuarioSesion;

        this.vista.getBtn_resgistrarMascota().addActionListener(this);
        this.vista.getBtn_actualizarMascota().addActionListener(this);
        this.vista.getBtn_limpiarMascota().addActionListener(this);

        this.vista.getTablaMascotas().addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                cargarSeleccionTabla();
            }
        });

        cargarTabla();
        llenarComboEspecie();
        configurarVistaPorRol();
    }

    private void llenarComboEspecie() {

        vista.getComboEspecie().removeAllItems();

        vista.getComboEspecie()
                .addItem("Selecciona una especie");

        vista.getComboEspecie()
                .addItem("perro");

        vista.getComboEspecie()
                .addItem("gato");

        vista.getComboEspecie().setSelectedIndex(0);

    }

    private void limpiarCampos() {

        // Limpiar campos de texto
        vista.getTextNombre().setText("");
        vista.getTextRaza().setText("");
        vista.getTextFecha().setText("");

        // Reiniciar ComboBox de especie
        if (vista.getComboEspecie().getItemCount() > 0) {
            vista.getComboEspecie().setSelectedIndex(0);
        }

        // Reiniciar ComboBox de propietario
        if (vista.getComboPropietario().isVisible()
                && vista.getComboPropietario().getItemCount() > 0) {

            vista.getComboPropietario().setSelectedIndex(0);
        }

        // Quitar selección de la tabla
        vista.getTablaMascotas().clearSelection();

        // Devolver el foco al campo nombre
        vista.getTextNombre().requestFocus();
    }

    private void cargarComboPropietarios() {

        vista.getComboPropietario().removeAllItems();
        vista.getComboPropietario().addItem(new ComboBox(0, "Selecciona un propietario"));
        vista.getComboPropietario().setSelectedIndex(0);
        List<Usuario> clientes = daoUsuario.listarClientes();

        for (Usuario cliente : clientes) {

            vista.getComboPropietario().addItem(
                    new ComboBox(
                            cliente.getIdUsuario(),
                            cliente.getNombre() + " " + cliente.getApellido()
                    )
            );
        }
    }

    private void configurarVistaPorRol() {

        String rol = usuarioSesion.getRol();

        if ("Cliente".equals(rol)) {

            vista.getComboPropietario().setVisible(false);

            vista.getLabelPropietario().setVisible(false);

        } else {

            cargarComboPropietarios();
        }
    }

    private void cargarTabla() {

        modelo = (DefaultTableModel) vista.getTablaMascotas().getModel();

        modelo.setRowCount(0);

        List<Mascota> lista;

        if (usuarioSesion.getRol().equals("Cliente")) {
            lista = daoMascota.listarPorDueno(usuarioSesion.getIdUsuario());
        } else {
            lista = daoMascota.listarTodas();
        }

        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");

        for (Mascota m : lista) {

            String fechaFormateada = "";
            if (m.getFechaNacimiento() != null) {
                fechaFormateada = formatoFecha.format(m.getFechaNacimiento());
            }

            modelo.addRow(new Object[]{
                m.getIdMascota(),
                m.getNombre(),
                m.getEspecie(),
                m.getRaza(),
                fechaFormateada,
                m.getNombreDueno()
            });
        }

        vista.getTablaMascotas().setModel(modelo);
    }

    private void registrarMascota() {

        String nombre = vista.getTextNombre().getText().trim();
        String especie = vista.getComboEspecie().getSelectedItem().toString();
        String raza = vista.getTextRaza().getText().trim();
        String fechaTexto = vista.getTextFecha().getText().trim();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debe ingresar el nombre de la mascota");
            return;
        }

        if (raza.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debe ingresar la raza");
            return;
        }

        if (fechaTexto.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                    "Debe ingresar una fecha de nacimiento");
            return;
        }

        if (!"Cliente".equals(usuarioSesion.getRol())) {

            ComboBox propietario
                    = (ComboBox) vista.getComboPropietario()
                            .getSelectedItem();

            if (propietario == null || propietario.getId() == 0) {

                JOptionPane.showMessageDialog(
                        null,
                        "Debe seleccionar un propietario"
                );

                vista.getComboPropietario().requestFocus();
                return;
            }
        }

        java.util.Date fechaNacimiento;

        try {

            SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");

            formato.setLenient(false);

            fechaNacimiento = formato.parse(fechaTexto);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "La fecha debe tener el formato dd/MM/yyyy"
            );

            return;
        }

        if (especie.equals("Selecciona una especie")) {

            JOptionPane.showMessageDialog(
                    null,
                    "Debe seleccionar una especie"
            );

            vista.getComboEspecie().requestFocus();

            return;
        }

        Mascota mascota = new Mascota();

        int idDueno;

        if ("Cliente".equals(usuarioSesion.getRol())) {

            idDueno = usuarioSesion.getIdUsuario();

        } else {

            ComboBox propietarioSeleccionado
                    = (ComboBox) vista.getComboPropietario()
                            .getSelectedItem();

            idDueno = propietarioSeleccionado.getId();
        }

        mascota.setIdDueno(idDueno);
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setFechaNacimiento(fechaNacimiento);
        mascota.setIdDueno(idDueno);

        boolean registrado = daoMascota.registrar(mascota);

        if (registrado) {

            JOptionPane.showMessageDialog(null,
                    "Mascota registrada correctamente");

            cargarTabla();
            limpiarCampos();

        } else {

            JOptionPane.showMessageDialog(null,
                    "No fue posible registrar la mascota");
        }
    }

    private void cargarSeleccionTabla() {

        int fila = vista.getTablaMascotas().getSelectedRow();

        if (fila == -1) {
            return;
        }

        idMascotaSeleccionada = Integer.parseInt(
                vista.getTablaMascotas()
                        .getValueAt(fila, 0)
                        .toString()
        );

        Mascota mascota
                = daoMascota.buscarPorId(idMascotaSeleccionada);

        if (mascota == null) {
            return;
        }

        vista.getTextNombre()
                .setText(mascota.getNombre());

        vista.getComboEspecie()
                .setSelectedItem(mascota.getEspecie());

        vista.getTextRaza()
                .setText(mascota.getRaza());

        SimpleDateFormat formatoFecha
                = new SimpleDateFormat("dd/MM/yyyy");

        if (mascota.getFechaNacimiento() != null) {

            vista.getTextFecha().setText(
                    formatoFecha.format(
                            mascota.getFechaNacimiento()
                    )
            );
        }

        if (!"Cliente".equals(usuarioSesion.getRol())) {

            for (int i = 0;
                    i < vista.getComboPropietario().getItemCount();
                    i++) {

                ComboBox item
                        = (ComboBox) vista.getComboPropietario()
                                .getItemAt(i);

                if (item.getId() == mascota.getIdDueno()) {

                    vista.getComboPropietario()
                            .setSelectedIndex(i);

                    break;
                }
            }
        }
    }

    private void editarMascota() {

        if (idMascotaSeleccionada == 0) {

            JOptionPane.showMessageDialog(
                    null,
                    "Seleccione una mascota de la tabla"
            );

            return;
        }

        String nombre = vista.getTextNombre().getText().trim();
        String raza = vista.getTextRaza().getText().trim();
        String fechaTexto = vista.getTextFecha().getText().trim();
        String especie = vista.getComboEspecie().getSelectedItem().toString();

        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar el nombre");
            return;
        }

        if (raza.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar la raza");
            return;
        }

        if (fechaTexto.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Debe ingresar la fecha");
            return;
        }

        if (especie.equals("Selecciona una especie")) {

            JOptionPane.showMessageDialog(
                    null,
                    "Debe seleccionar una especie"
            );

            return;
        }

        java.util.Date fechaNacimiento;

        try {

            SimpleDateFormat formato
                    = new SimpleDateFormat("dd/MM/yyyy");

            formato.setLenient(false);

            fechaNacimiento = formato.parse(fechaTexto);

        } catch (Exception e) {

            JOptionPane.showMessageDialog(
                    null,
                    "La fecha debe tener formato dd/MM/yyyy"
            );

            return;
        }

        int idDueno;

        if ("Cliente".equals(usuarioSesion.getRol())) {

            idDueno = usuarioSesion.getIdUsuario();

        } else {

            ComboBox propietario
                    = (ComboBox) vista.getComboPropietario()
                            .getSelectedItem();

            if (propietario == null || propietario.getId() == 0) {

                JOptionPane.showMessageDialog(
                        null,
                        "Debe seleccionar un propietario"
                );

                return;
            }

            idDueno = propietario.getId();
        }

        Mascota mascota = new Mascota();

        mascota.setIdMascota(idMascotaSeleccionada);
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setFechaNacimiento(fechaNacimiento);
        mascota.setIdDueno(idDueno);

        boolean actualizado = daoMascota.actualizar(mascota);

        if (actualizado) {

            JOptionPane.showMessageDialog(
                    null,
                    "Mascota actualizada correctamente"
            );

            cargarTabla();
            limpiarCampos();

        } else {

            JOptionPane.showMessageDialog(
                    null,
                    "No fue posible actualizar la mascota"
            );
        }
    }

    public void recargarDatos() {

    cargarTabla();

    cargarComboPropietarios();

    limpiarCampos();
}
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == vista.getBtn_resgistrarMascota()) {
            registrarMascota();
        }

        if (e.getSource() == vista.getBtn_actualizarMascota()) {
            editarMascota();
        }

        if (e.getSource() == vista.getBtn_limpiarMascota()) {
            limpiarCampos();
        }
    }
}
