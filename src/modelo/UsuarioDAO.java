/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 *
 * @author jhona
 */
public class UsuarioDAO {

//    private static Connection conexion = Conexion.conectar();
//    private static PreparedStatement ps = null;
//    private static ResultSet retorno;
    public static Usuario autenticarUsuario(String correo, String password) {

        Usuario usuario = null;

        Connection con = Conexion.conectar();

        String sql
                = "SELECT u.id_usuario, "
                + "u.nombre, "
                + "u.apellido, "
                + "u.email, "
                + "r.id_rol, "
                + "r.nombre_rol "
                + "FROM usuarios u "
                + "JOIN roles r "
                + "ON u.id_rol = r.id_rol "
                + "WHERE u.email = ? "
                + "AND u.contrasena = crypt(?, u.contrasena)";

        try {

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, correo);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                usuario = new Usuario();

                usuario.setIdUsuario(rs.getInt("id_usuario"));

                usuario.setNombre(rs.getString("nombre"));

                usuario.setApellido(rs.getString("apellido"));

                usuario.setCorreo(rs.getString("email"));

                usuario.setIdRol(rs.getInt("id_rol"));
                
                usuario.setRol(rs.getString("nombre_rol"));
                
            }

        } catch (Exception e) {
            System.out.println(
                    "Error login: " + e.getMessage());
        }

        return usuario;
    }
    
    public static boolean registroUsuario(Usuario user) {
        
        Connection con = Conexion.conectar();
        
        String sql = "INSERT INTO usuarios (nombre, apellido, email, telefono, contrasena, id_rol) VALUES (?, ?, ?, ?, crypt(?, gen_salt('bf')), ?)";
        
        try {
            PreparedStatement ps = con.prepareStatement(sql);
            
//            ps = con.prepareStatement(sql);
            ps.setString(1, user.getNombre());
            ps.setString(2, user.getApellido());
            ps.setString(3, user.getCorreo());
            ps.setString(4, user.getTelefono());
            ps.setString(5, user.getPassword());
            ps.setInt(6, user.getIdRol());
            ps.execute();
            return true;
        } catch (Exception e) {
            System.out.println(
                "Error registro usuario: " + e.getMessage());
            return false;
        }
    }
}
