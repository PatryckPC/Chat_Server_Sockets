import java.io.*;
import java.net.Socket;
import java.util.List;

// Clase interna para manejar la comunicación con un cliente individual.

public class HiloChat implements Runnable {

    // Creamos las variables

    private Socket socketCliente;
    private DataInputStream flujoIn;
    private String username;

    private List<ListaClientes> listaC;

    public HiloChat(Socket socket, List<ListaClientes> listaC, String username) {

        this.socketCliente = socket;
        this.listaC = listaC;
        this.username = username;
    
        try {

            flujoIn = new DataInputStream(socketCliente.getInputStream()); 

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
    

    @Override
    public void run() {

        try {

            String mensaje;
            
            // Se pone en espera de mensajes}

            while ((mensaje = flujoIn.readUTF()) != null) {

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Para redirigir archivos

                if (mensaje.contains("TRANSFERENCIA_DE_ARCHIVO")) {

                    // Recibe detalles del archivo
                    String nombreReceptor = flujoIn.readUTF();
                    String nombreEmisor = flujoIn.readUTF();
                    String nombreArchivo = flujoIn.readUTF();
                    long tamanoArchivo = flujoIn.readLong();
    
                    // Invoca el método para manejar la transferencia de archivo privado
                    archivoPriv(nombreReceptor,nombreEmisor, nombreArchivo, tamanoArchivo);
    
                } else{

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Para redirigir un mensaje privado

                    if (mensaje.contains("|")) {
                        String[] partes = mensaje.split("\\|");

                        if (partes.length == 3) {

                            String destinatario = partes[0];
                            String msj = partes[1];
                            String emisor = partes[2];

                            msgPriv(emisor, msj, destinatario);

                        }

                    } else {

                ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                // Para redirigir un mensaje grupal

                        
                        System.out.println("Mensaje recibido de " + username + ": " + mensaje);
                        broadcastMessage(mensaje, username);

                    }
                }
            }
        } catch (IOException e) {

            // Maneja el cierre de conexión del cliente

            System.err.println("Cliente " + username + " ha cerrado la conexion.");

            // Elimina al cliente de la lista de clientes

            listaC.removeIf(cliente -> cliente.getUsername().equals(username));

            System.out.println("Clientes conectados: " + listaC.size());

        } finally {
            try {

                flujoIn.close();
                socketCliente.close();

            } catch (IOException e) {
                
                e.printStackTrace();
            }
        }
    }

    // Método para reenviar un mensaje a todos los clientes conectados.

    private void broadcastMessage(String mensaje, String emisorNombre) {

        System.out.println("Enviando mensaje a todos los clientes: " + mensaje);

        for (ListaClientes cliente : listaC) {

            DataOutputStream enviaraCliente = cliente.getSalida(); 

            try {

                enviaraCliente.writeUTF(emisorNombre + ": " + mensaje); // Utiliza writeUTF
                enviaraCliente.flush(); // Asegura que los datos se envíen

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    private void msgPriv(String emisor, String msg, String destinatario) {

        // Busca el cliente destinatario en la lista de clientes conectados

        ListaClientes clienteDestinatario = null;

        for (ListaClientes cliente : listaC) {

            if (cliente.getUsername().equals(destinatario)) {

                clienteDestinatario = cliente;
                break;
            }
        }

        if (clienteDestinatario != null) {

            DataOutputStream enviaraCliente = clienteDestinatario.getSalida(); // Cambio en el tipo de variable

            try {

                enviaraCliente.writeUTF("priv:" + emisor + ":" + msg); // Utiliza writeUTF
                enviaraCliente.flush(); // Asegura que los datos se envíen

            } catch (IOException e) {

                e.printStackTrace();

            }
        } else {

            // El destinatario no se encontró en la lista de clientes conectados
            // Puedes manejar este caso de acuerdo a tus requerimientos, por ejemplo, informar al emisor que el destinatario no existe.
            System.out.println("El destinatario " + destinatario + " no se encontro en la lista de clientes conectados.");

        }
    }

    private void archivoPriv(String nombreReceptor, String nombreEmisor, String nombreArchivo, long tamanoArchivo) {
        // Busca el cliente destinatario en la lista de clientes conectados
        ListaClientes clienteDestinatario = null;

    
        for (ListaClientes cliente : listaC) {
            if (cliente.getUsername().equals(nombreReceptor)) {
                clienteDestinatario = cliente;
                break;
            }
        }
    
        if (clienteDestinatario != null) {
            try {
                // Abre un DataOutputStream para enviar la señal de inicio de transferencia de archivo
                DataOutputStream enviaraCliente = clienteDestinatario.getSalida();
                enviaraCliente.writeUTF("TRANSFERENCIA_DE_ARCHIVO");
                enviaraCliente.flush();
                enviaraCliente.writeUTF(nombreEmisor);
                enviaraCliente.writeUTF(nombreArchivo);
                enviaraCliente.writeLong(tamanoArchivo);
                enviaraCliente.flush();
    
                // Abre un DataInputStream para recibir el archivo del emisor
                DataInputStream archivoIn = new DataInputStream(socketCliente.getInputStream());
    
                byte[] buffer = new byte[4096];
                int bytesRead;
                long bytesRecibidos = 0;
    
                // Lee y envía el archivo en bloques de 4kb
                while (bytesRecibidos < tamanoArchivo && (bytesRead = archivoIn.read(buffer)) > 0) {

                    enviaraCliente.write(buffer,0,bytesRead);
                    bytesRecibidos += bytesRead;
                }
    

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // El destinatario no se encontró en la lista de clientes conectados
            // Puedes manejar este caso de acuerdo a tus requerimientos, por ejemplo, informar al emisor que el destinatario no existe.
            System.out.println("El destinatario " + username + " no se encontro en la lista de clientes conectados.");
        }
    }
}
