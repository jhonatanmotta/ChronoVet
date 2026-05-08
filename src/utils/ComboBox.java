package utils;

public class ComboBox {
    
    // Atributos de la clase
    private int id; // Identificador (id) de la tabla 
    private String nombre;  // Nombre de que se desea mostrar en las opciones del comboBox

    // Metodo Constructor con parámetros
    public ComboBox(int id, String nombre) {
        this.id = id; //Asigna el id
        this.nombre = nombre; //Asigna el nombre a mostrar en las opciones
    }

    // Método para obtener el ID
    public int getId() {
        return id;
    }  

    // Método para establecer el ID
    public void setId(int id) {
        this.id = id;
    }

    // Método para obtener el nombre
    public String getNombre() {
        return nombre;
    }

    // Método para establecer el nombre
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    // Método para representar el ComboBox como cadena (en este caso, el nombre)
    @Override
    public String toString() {
        return this.getNombre();
    }
}
