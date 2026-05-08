package utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JOptionPane;

public class Validaciones {
    // Método para validar que todos los strings en el arreglo sean diferentes de vacío
    public static boolean validarNoVacios(String mensajeError, String... strings) {
        for (String str : strings) {// for que itera todos los strings del array
            if (str.isEmpty()) { // validacion si el elemento es vacio
                // aroja un mensaje de dialogo si el string es vacio
                JOptionPane.showMessageDialog(null, mensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
                return false; // Al menos uno está vacío, devuelve false
            }
        }
        return true; // Todos son diferentes de vacío, devuelve true
    }

    // Método para validar la cantidad de caracteres en un String
    public static boolean validarCantidadCaracteres(String str, int cantidadMaxima, String MensajeError) {
        if (str.length() <= cantidadMaxima) { //si la cantidad de caracteres del String es <= a la cantidad maxima
            return true; //devuelve true
        } else { // si es mayor devuelve false y un mensaje de dialogo
            JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Método para validar si un String se puede parsear a un entero
    public static boolean validarParseoAEntero(String str, String MensajeError) {
        try {
            Integer.parseInt(str); //parse del String a entero
            return true; //si es existoso el parseo devuelve true
        } catch (NumberFormatException e) { // si el catch atrapa alguna excepcion devuelve un mensaje de dialogo y false
            JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Método para validar si un String se puede parsear a un long
    public static boolean validarParseoLong(String MensajeError, String... numeros) {
        boolean validacion = false;
        for (String num : numeros) {// for que itera todos los strings del array
            try {
                Long.parseLong(num);
                validacion = true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
                validacion = false; // Si falla, la cadena no es un número long válido
            }
        }
        return validacion;
    }

    // Método para validar si un String se puede parsear a un double
    public static boolean validarParseoADouble(String str, String MensajeError) {
        try {
            Double.parseDouble(str);//parse del String a double
            return true; //si es existoso el parseo devuelve true
        } catch (NumberFormatException e) {// si el catch atrapa alguna excepcion devuelve un mensaje de dialogo y false
            JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Método para validar si un String es un número entero mayor, menor o igual a cero
    public static boolean validarNumeroEnteroNoNegativo(String str, String MensajeError) {
        try {
            int numero = Integer.parseInt(str);//parse del String a entero
            if (numero > 0) { // validacion si el numero es mayor 
                return true; // devuelve true
            } else { // si es menor o igual a cero devuelve un cuadro de dialogo y false
                JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {// si el catch atrapa alguna excepcion devuelve un mensaje de dialogo y false
            JOptionPane.showMessageDialog(null, "Hay un error revisa la cantidad que estas ingresando", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }
    }

    // Método para verificar si un monto es menor que otro
    public static boolean valorMenor(double totalPago, double montoPago, String MensajeError) {
        if (montoPago < totalPago) { // Comprueba si el monto de pago es menor que el total a pagar
            // Muestra un mensaje de advertencia si el monto es menor
            JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false; // Devuelve falso si el monto de pago es menor
        } else {
            return true; // Devuelve true si el monto de pago es igual o mayor al total a pagar
        }
    }

    // Método para validar el formato de un correo electrónico
    public static boolean validarCorreo(String correo) {
        // Expresión regular para verificar el formato de un correo electrónico
        String expresion = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

        // Compilar la expresión regular
        Pattern patron = Pattern.compile(expresion);

        // Comparar el texto ingresado con la expresión regular
        Matcher matcher = patron.matcher(correo);

        // Devolver true si coincide con el formato de correo electrónico
        return matcher.matches();
    }

    // Método para validar la longitud de un texto dentro de un rango específico
    public static boolean validarRangoCaracteres(String texto, int minimo, int maximo, String MensajeError) {
        int longitudTexto = texto.length(); // Obtiene la longitud del texto proporcionado

        // Comprueba si la longitud del texto está dentro del rango especificado
        if (longitudTexto >= minimo && longitudTexto <= maximo) {
            return true; // Retorna true si el texto está dentro del rango
        } else {
            // Muestra un mensaje de advertencia si la longitud del texto está fuera del rango
            JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false; // Retorna false si el texto está fuera del rango
        }
    }

    // Método para validar el formato de una contraseña
    public static boolean validarContrasena(String contraseña) {
        // Expresión regular para verificar el formato de la contraseña
        String expresion = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!¡?¿*(){}\\[\\]_|<>,:;/-]).{10,}$";

        // Compilar la expresión regular
        Pattern patron = Pattern.compile(expresion);

        // Comparar la contraseña con la expresión regular
        Matcher matcher = patron.matcher(contraseña);

        // Devolver true si coincide con el formato de contraseña
        return matcher.matches();
    }

    public static boolean validarSinNumeros(String MensajeError, String... cadena) {
        // Compilar la expresión regular para encontrar dígitos
        Pattern patron = Pattern.compile("\\d"); // "\\d" encuentra cualquier dígito

        for (String str : cadena) {// for que itera todos los strings del array
            Matcher matcher = patron.matcher(str); // Crear un matcher para la cadena dada
            patron.matcher(str);
            if (matcher.find()) { // validacion si el elemento contiene numeros
                // aroja un mensaje de dialogo si el string es vacio
                JOptionPane.showMessageDialog(null, MensajeError, "Advertencia", JOptionPane.WARNING_MESSAGE);
                return false; // Al menos uno está vacío, devuelve false
            }
        }
        return true;
    }
    
    public static void mostrarError(String mensaje) {
    JOptionPane.showMessageDialog(null, mensaje, "Advertencia", JOptionPane.WARNING_MESSAGE);
}
}
