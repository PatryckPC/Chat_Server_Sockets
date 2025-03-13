import java.io.*;
import java.net.*;

public class ClienteCode {
    private Socket socket;
    public DataInputStream flujoIn;
    private DataOutputStream flujoOut;

    /*------Se crea el socket 
    y se abre el flujo de entrada y de salida------ */

    public ClienteCode() {
        try {

            socket = new Socket("localhost", 12345);
            flujoIn = new DataInputStream(socket.getInputStream());
            flujoOut = new DataOutputStream(socket.getOutputStream());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void enviarMensaje(String mensaje) {
        try {
            flujoOut.writeUTF(mensaje);
            flujoOut.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Se encarga de mandar el mensaje

    public void enviarMensajePriv(String emisor, String mensaje, String destinatario) {

        //Compacta la info necesaria en un solo string

        String mensajeCompleto = destinatario + "|" + mensaje + "|" + emisor;

        try {

            flujoOut.writeUTF(mensajeCompleto);
            flujoOut.flush();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public String recibirMensaje() throws IOException {
        return flujoIn.readUTF();
    }

    public long recibirMensajeLong() throws IOException {
        return flujoIn.readLong();
    }


    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //Se encarga de enviar el archivo

    public void enviarArchivo(String nombreEmisor, String nombreReceptor, String rutaArchivo) {
        try {

            // Lee el archivo des de la ruta especificada

            File archivo = new File(rutaArchivo);

            // Creamos un FileInputStream para leer el archivo Byte por Byte
            FileInputStream fileInputStream = new FileInputStream(archivo);

            byte[] buffer = new byte[4096]; // Tamaño del búfer (4 KB)
    
            // Envía la señal de inicio de transferencia de archivo

            flujoOut.writeUTF("TRANSFERENCIA_DE_ARCHIVO"); 
            flujoOut.writeUTF(nombreReceptor); // Envía el nombre del receptor
            flujoOut.writeUTF(nombreEmisor); 
            flujoOut.writeUTF(archivo.getName()); // Envía el nombre del archivo
            flujoOut.writeLong(archivo.length()); // Envía el tamaño del archivo
    
            // Envía el contenido del archivo

            // Enviamos el archivo en bloques de 4 kb
            int count;
            while ((count = fileInputStream.read(buffer)) > 0) {

                flujoOut.write(buffer, 0, count);

            }

            flujoOut.flush();
            fileInputStream.close();

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
