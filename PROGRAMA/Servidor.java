import java.io.*;
import java.net.*;

// Listas para poder almacenar los clientes
import java.util.ArrayList;
import java.util.List;

// Librerías para la programación del envío periódico de la lista de usuarios conectados
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Servidor {

    // Asigna un valor al PUERTO
    private static final int PUERTO = 12345;

    // Este es una lista de tipo ListaClientes que es una clase en la que se almacena el flujo de salida de cada cliente
    // junto con su nombre de usuario para identificarlos posteriormente con su username
    private static List<ListaClientes> listaC = new ArrayList<>();

    public static void main(String[] args) {
        iniciarServidor();
    }

    private static void iniciarServidor() {
        try {
            
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            
            // Creamos server socket 

            ServerSocket socketServidor = new ServerSocket(PUERTO);
            System.out.println("Servidor de chat iniciado. Esperando conexiones...");

            // Programar el envío de la lista de usuarios online
            programarEnvioListaUsuariosConectados();

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            while (true) {
                try {

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    
                    // Acepta a el socket de cada cliente

                    Socket socketCliente = socketServidor.accept();
                    System.out.println("Cliente conectado desde " + socketCliente.getInetAddress().getHostAddress()
                            + " en el puerto: " + socketCliente.getPort());

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Recibe el nombre del usuario y si es null, ignora la conexion

                    String username = recibirUsername(socketCliente);

                    if (username == null) {
                        continue;
                    }

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Se Guarda el flujo de salida de cada Cliente

                    DataOutputStream flujoOut = new DataOutputStream(socketCliente.getOutputStream());

                    // Se crea un objeto de tipo ListaClientes

                    ListaClientes listaClientes = new ListaClientes(flujoOut, username);

                    // Se guarda el objeto en la listaC
                    
                    listaC.add(listaClientes);

                    // Se imprime la lista de clientes conectados

                    System.out.println("Clientes conectados: " + listaC.size());

                    enviarListaUsuariosConectados(listaC); //Envia lista de usuarios conectados

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Se crea el hilo con el respectivo cliente

                    Thread hiloSerClien = new Thread(new HiloChat(socketCliente, listaC, username));

                    hiloSerClien.start();

                    System.out.println("Hilo de cliente creado y comenzado.");

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                } catch (IOException ioe) {
                    System.err.println("Hay un error en la creacion de conexiones.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método que envía la lista de usuarios a cada usuario conectado
    // La lista de usuarios en línea se envía como una cadena delimitada por comas

    private static void enviarListaUsuariosConectados(List<ListaClientes> listaC) {

        // Se escribe "Usuarios en linea" antes para que el cliente filtre esta info 

        StringBuilder listaUsuarios = new StringBuilder("Usuarios en línea:");
        
        for (ListaClientes cliente : listaC) {
            listaUsuarios.append(",").append(cliente.getUsername());
        }

        for (ListaClientes cliente : listaC) {

            // Manda la lista al flujo de salida de cada cliente
            DataOutputStream flujoOut = cliente.getSalida();

            try {

                flujoOut.writeUTF(listaUsuarios.toString());
                flujoOut.flush();

            } catch (IOException e) {

                e.printStackTrace();
                
            }
        }
    }

    // Método para recibir el nombre de usuario del cliente

    private static String recibirUsername(Socket socketCliente) throws IOException {

        DataInputStream flujoIn = new DataInputStream(socketCliente.getInputStream());
        String username = flujoIn.readUTF();
        return username;

    }

    // Programa el envío periódico de la lista de usuarios conectados

    private static void programarEnvioListaUsuariosConectados() {

        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
        executorService.scheduleAtFixedRate(() -> {
            enviarListaUsuariosConectados(listaC);
        }, 0, 30, TimeUnit.SECONDS);

    }
}
