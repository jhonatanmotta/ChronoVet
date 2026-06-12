/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package modelo;

import conexion.Conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

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

    public static List<Usuario> listarUsuarios(Usuario usuarioActual) {

    List<Usuario> listaUsuarios = new ArrayList<>();

    Connection con = Conexion.conectar();

    String sql;

    if (usuarioActual.getRol().equals("Administrador")) {

        sql = """
              SELECT u.id_usuario,
                     u.nombre,
                     u.apellido,
                     u.email,
                     u.telefono,
                     r.nombre_rol,
                     u.estado
              FROM usuarios u
              INNER JOIN roles r
                  ON u.id_rol = r.id_rol
              ORDER BY u.id_usuario
              """;

    } else {

        sql = """
              SELECT u.id_usuario,
                     u.nombre,
                     u.apellido,
                     u.email,
                     u.telefono,
                     r.nombre_rol,
                     u.estado
              FROM usuarios u
              INNER JOIN roles r
                  ON u.id_rol = r.id_rol
              WHERE r.nombre_rol IN ('Veterinario', 'Cliente')
              ORDER BY u.id_usuario
              """;
    }

    try {

        PreparedStatement ps =
                con.prepareStatement(sql);

        ResultSet rs =
                ps.executeQuery();

        while (rs.next()) {

            Usuario usuario = new Usuario();

            usuario.setIdUsuario(
                    rs.getInt("id_usuario"));

            usuario.setNombre(
                    rs.getString("nombre"));

            usuario.setApellido(
                    rs.getString("apellido"));

            usuario.setCorreo(
                    rs.getString("email"));

            usuario.setTelefono(
                    rs.getString("telefono"));

            usuario.setRol(
                    rs.getString("nombre_rol"));

            usuario.setEstado(
                    rs.getString("estado"));

            listaUsuarios.add(usuario);
        }

    } catch (Exception e) {

        System.out.println(
                "Error listar usuarios: "
                + e.getMessage());
    }

    return listaUsuarios;
}
    
    
    public static Usuario buscarPorId(int idUsuario) {

    Connection con = Conexion.conectar();

    String sql = """
                 SELECT u.*,
                        r.nombre_rol
                 FROM usuarios u
                 INNER JOIN roles r
                     ON u.id_rol = r.id_rol
                 WHERE u.id_usuario = ?
                 """;

    try {

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setInt(1, idUsuario);

        ResultSet rs =
                ps.executeQuery();

        if(rs.next()) {

            Usuario usuario =
                    new Usuario();

            usuario.setIdUsuario(
                    rs.getInt("id_usuario"));

            usuario.setNombre(
                    rs.getString("nombre"));

            usuario.setApellido(
                    rs.getString("apellido"));

            usuario.setCorreo(
                    rs.getString("email"));
            
            usuario.setTelefono(
                    rs.getString("telefono"));

            usuario.setIdRol(
                    rs.getInt("id_rol"));

            usuario.setRol(
                    rs.getString("nombre_rol"));

            usuario.setEstado(
                    rs.getString("estado"));

            return usuario;
        }

    } catch (Exception e) {

        System.out.println(
                "Error buscar usuario: "
                + e.getMessage());
    }

    return null;
}
    
    public static boolean actualizarUsuario(Usuario usuario) {

    Connection con = Conexion.conectar();

    String sql = """
                 UPDATE usuarios
                 SET nombre = ?,
                     apellido = ?,
                     email = ?,
                     telefono = ?,
                     estado = CAST(? AS tipo_estado_usuario)
                 WHERE id_usuario = ?
                 """;

    try {

        PreparedStatement ps =
                con.prepareStatement(sql);

        ps.setString(1, usuario.getNombre());
        ps.setString(2, usuario.getApellido());
        ps.setString(3, usuario.getCorreo());
        ps.setString(4, usuario.getTelefono());
        ps.setString(5, usuario.getEstado());
        ps.setInt(6, usuario.getIdUsuario());

        return ps.executeUpdate() > 0;

    } catch (Exception e) {

        System.out.println(
                "Error actualizar usuario: "
                + e.getMessage());

        return false;
    }
}
    
    public List<Usuario> listarClientes() {

    List<Usuario> lista = new ArrayList<>();

    String sql = "SELECT * FROM usuarios WHERE id_rol = ? AND estado = ?";

    try (
            Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(sql)
    ) {

        ps.setInt(1, 4); // ID del rol Cliente
        // Usar setObject con el tipo ENUM de PostgreSQL
        ps.setObject(2, "activo", java.sql.Types.OTHER);

        try (ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Usuario usuario = new Usuario();

                usuario.setIdUsuario(
                        rs.getInt("id_usuario")
                );

                usuario.setNombre(
                        rs.getString("nombre")
                );
                
                usuario.setApellido(
                        rs.getString("apellido")
                );
                
                lista.add(usuario);
            }
        }

    } catch (Exception e) {

        System.out.println(
                "Error al listar clientes: "
                + e.getMessage()
        );
    }

    return lista;
}
    
}
