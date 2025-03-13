import java.io.DataOutputStream;

public class ListaClientes  {

    private DataOutputStream salida; 
    private String username;

    // Constructor

    public ListaClientes(DataOutputStream salida, String username) { 
        this.salida = salida;
        this.username = username;
    }

    public DataOutputStream getSalida() { 
        return salida;
    }

    public String getUsername() {
        return username;
    }
}
