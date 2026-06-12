package controlador;

import modelo.Consulta;
import modelo.ConsultaDAO;
import utils.Sesion;
import javax.swing.JOptionPane;
import modelo.VeterinarioDAO;

public class Ctrl_Consulta {
    
    private ConsultaDAO consultaDAO;
    private VeterinarioDAO veterinarioDAO; 
    
    public Ctrl_Consulta() {
        this.consultaDAO = new ConsultaDAO();
        this.veterinarioDAO = new VeterinarioDAO();
    }
    
    public boolean registrarConsulta(int idCita, String diagnostico, String tratamiento, String observaciones) {
        
        
        if (diagnostico == null || diagnostico.trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, 
                "El diagnóstico es obligatorio.", 
                "Error de validación", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Obtener ID del usuario desde la sesión
        Integer idUsuarioLogueado = null;
        try {
            idUsuarioLogueado = Sesion.getInstancia().getUsuario().getIdUsuario();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, 
                "Error al obtener la sesión: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (idUsuarioLogueado == null) {
            JOptionPane.showMessageDialog(null, 
                "No se pudo identificar al usuario. Inicie sesión nuevamente.", 
                "Error de sesión", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        int idVeterinario = veterinarioDAO.obtenerIdVeterinario(idUsuarioLogueado);
        
        if (idVeterinario == -1) {
            JOptionPane.showMessageDialog(null, 
                "El usuario logueado no está registrado como veterinario.\n" +
                "ID Usuario: " + idUsuarioLogueado, 
                "Error de permisos", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        // Crear objeto Consulta
        Consulta consulta = new Consulta();
        consulta.setIdCita(idCita);
        consulta.setIdVeterinario(idVeterinario);
        consulta.setDiagnostico(diagnostico);
        consulta.setTratamiento(tratamiento);
        consulta.setObservaciones(observaciones);
        
        // Registrar usando el DAO
        Object[] resultado = consultaDAO.registrarConsulta(consulta);
        Integer idConsultaGenerado = (Integer) resultado[0];
        String mensaje = (String) resultado[1];
        
        // Mostrar mensaje al usuario
        if (idConsultaGenerado != null && idConsultaGenerado > 0) {
            JOptionPane.showMessageDialog(null, 
                mensaje, 
                "Éxito", 
                JOptionPane.INFORMATION_MESSAGE);
            return true;
        } else {
            JOptionPane.showMessageDialog(null, 
                "Error: " + mensaje, 
                "Error al registrar consulta", 
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
