import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Buscador de archivos
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
// Para guardar los chats privados entre clientes
import java.util.HashMap;
import java.util.Map;

public class Cliente {
    
    private ClienteCode clienteCode;
    private JPanel listaUsuariosPanel;
    private Map<String, PrivadoCliente> ventanasPrivadas;
    private JLabel chatLabel; // Agrega un JLabel para mostrar el nombre de usuario

    public Cliente() {

        clienteCode = new ClienteCode();
        ventanasPrivadas = new HashMap<>(); //Guardar ventanas privadas

        /*-----Seccion encargada de la interfaz con Swing------*/

        JFrame frame = new JFrame("Chat de ");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(600, 300, 800, 510);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JTextField enviarCuadro = new JTextField(20);

        // Se agrega funcionalidad a los cuadros de texto
        enviarCuadro.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String mensaje = enviarCuadro.getText();

                if(mensaje != null && !mensaje.isEmpty()){

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Enviar mensaje al presionar "Enter"

                    clienteCode.enviarMensaje(mensaje);
                    enviarCuadro.setText("");

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                }
            }

        });
        
        panel.add(enviarCuadro, BorderLayout.SOUTH);

        JPanel enviarPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton enviarBoton = new JButton("Enviar");
        enviarPanel.add(enviarBoton);
        panel.add(enviarPanel, BorderLayout.CENTER);

        JTextArea chatGlobalCuadro = new JTextArea(24, 20);
        JScrollPane barraDesplazamiento = new JScrollPane(chatGlobalCuadro);

        // Configura las políticas de las barras de desplazamiento

        barraDesplazamiento.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        barraDesplazamiento.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(barraDesplazamiento, BorderLayout.NORTH);

        chatGlobalCuadro.setEditable(false);

        listaUsuariosPanel = new JPanel();
        listaUsuariosPanel.setLayout(new BoxLayout(listaUsuariosPanel, BoxLayout.Y_AXIS));

        JScrollPane listaUsuariosScrollPane = new JScrollPane(listaUsuariosPanel);
        listaUsuariosScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listaUsuariosScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        listaUsuariosScrollPane.setPreferredSize(new Dimension(200, frame.getHeight()));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listaUsuariosScrollPane, panel);
        splitPane.setDividerLocation(0.8);
        frame.add(splitPane);

        /*------Inicio del CLiente------- */ ///////////////////////////////////////////////////////////////////////////////////////////////////////

        // Solicita el nombre 

        String username = JOptionPane.showInputDialog(frame, "Ingresa tu nombre de usuario:");

        if (username == null || username.isEmpty()) {

            //Maneja errores
            JOptionPane.showMessageDialog(frame, "Debes ingresar un nombre de usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);

        } else {

            clienteCode.enviarMensaje(username);

            // Actualiza el título del JFrame con el nombre de usuario
            
            frame.setTitle("Chat de " + username);
        }

        chatLabel = new JLabel("Chat de " + username); // Inicializa el JLabel con el nombre de usuario
        frame.add(chatLabel, BorderLayout.NORTH); // Agrega el JLabel al BorderLayout.NORTH

        /* ----Trata con los hilos Cliente-Servidor--- *////////////////////////////////////////////////////////////////////////////////////////
        
        Thread hiloClienServ = new Thread(() -> {

            try {

                while (true) {

                    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // RECEPCIÓN DE MENSAJES
                    
                    String respuestaServidor = clienteCode.recibirMensaje();
                    
                    if (respuestaServidor != null) {

                        // Filtra la informacion recibida del servidor si es que son los usuarios conectados

                        if (respuestaServidor.startsWith("Usuarios en línea:")) {   
                            
                            //..Si la info recibida son los usuarios, lo pone como un boton en la tabla de usuarios
                            String usuariosEnLinea = respuestaServidor.substring("Usuarios en línea:".length());
                            String[] usuarios = usuariosEnLinea.split(",");

                            SwingUtilities.invokeLater(() -> {

                                listaUsuariosPanel.removeAll();

                                for (String usuario : usuarios) {

                                    if (!usuario.isEmpty()) { // Verificar si el nombre de usuario no está vacío

                                        JButton botonUsuario = new JButton(usuario);
                                        listaUsuariosPanel.add(botonUsuario);
                                        botonUsuario.setAlignmentX(Component.LEFT_ALIGNMENT);

                                        botonUsuario.addActionListener(new ActionListener() {

                                            @Override
                                            public void actionPerformed(ActionEvent e) {

                                                String nombreReceptor = botonUsuario.getText();
                                                String nombreEmisor = username;
                                                abrirVentanaPrivada(nombreEmisor, nombreReceptor);

                                            }
                                        });
                                    }
                                }

                                listaUsuariosPanel.revalidate();
                                listaUsuariosPanel.repaint();

                            });

                        } else {

                            //Si la respuesta empieza com priv trata el mensaje como uno privado 

                            if (respuestaServidor.contains("priv:")) {

                                String[] partes = respuestaServidor.split("\\:"); //Separa la info

                                if (partes.length == 3) {

                                    //Si tiene la cantidad de info necesaria la separa como corresponde

                                    String emisor = partes[1];
                                    String msj = partes[2];
                                    String nombreYo = username;

                                    abrirVentanaPrivada(nombreYo, emisor); //Manda la info a su respectiva función

                                    // Mandar mensaje a la nueva ventana

                                    ventanasPrivadas.get(nombreYo + emisor).imprimirMensaje(emisor, msj);
                                }
                            } else {
                                //Filtra si la respuesta es un archivo por transferir
                                if (respuestaServidor.contains("TRANSFERENCIA_DE_ARCHIVO")) {

                                    // Cambia a 5 para considerar el emisor del archivo
                                    String nombreEmisor = clienteCode.recibirMensaje();
                                    String nombreArchivo = clienteCode.recibirMensaje();
                                    long tamanoArchivo = clienteCode.recibirMensajeLong();

                                    // Abre un cuadro de diálogo para seleccionar la ubicación de descarga
                                    JFileChooser fileChooser = new JFileChooser();
                                    fileChooser.setSelectedFile(new File(nombreArchivo));
                                    int seleccion = fileChooser.showSaveDialog(null);
                                    
                                    if (seleccion == JFileChooser.APPROVE_OPTION) {
                                        File destinoArchivo = fileChooser.getSelectedFile().getAbsoluteFile();

                                        // Crea un FileOutputStream para escribir el archivo en la ubicación seleccionada
                                        try (FileOutputStream fileOutputStream = new FileOutputStream(destinoArchivo)) {
                                            byte[] buffer = new byte[4096]; // Tamaño del búfer (4 KB)
                                            int bytesRead;
                                            long bytesRecibidos = 0;

                                            while (bytesRecibidos < tamanoArchivo && (bytesRead = clienteCode.flujoIn.read(buffer)) > 0) {

                                                fileOutputStream.write(buffer, 0, bytesRead);
                                                bytesRecibidos += bytesRead;

                                            }

                                            // Notifica al usuario que se ha recibido el archivo

                                            String emisorNombre = username; // Cambia esto al nombre real del emisor
                                            abrirVentanaPrivada(emisorNombre, nombreEmisor); // Abre la ventana privada si aún no está abierta
                                            ventanasPrivadas.get(emisorNombre + nombreEmisor).imprimirMensajeArchivo("\nSISTEMA  Archivo recibido de: " + nombreEmisor + "  Nombre del archivo: " + nombreArchivo + "\n\n");

                                            clienteCode.enviarMensajePriv(emisorNombre, "ARCHIVO RECIBIDO CORRECTAMENTE \n" , nombreEmisor);

                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } else {

                                    chatGlobalCuadro.append(respuestaServidor + "\n");
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        hiloClienServ.start();

        /*----Acciones del cliente ---- */

        //envia el mensaje globaal clickarse el boton
        enviarBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensaje = enviarCuadro.getText();

                //Verifica que haya un mensaje en el cuadro de texto
                if (mensaje != null && !mensaje.isEmpty()) {
                    //Envia el mensaje a la funcion que lo manda al otro cliente
                    clienteCode.enviarMensaje(mensaje); 
                    enviarCuadro.setText("");
                }
            }
        });

        frame.setVisible(true);
    }

    // 
    private void abrirVentanaPrivada(String nombreEmisor, String nombreReceptor) {
        String claveVentana = nombreEmisor + nombreReceptor;
        String claveInversa = nombreReceptor + nombreEmisor;

        // Checa si existen ventantas privadas, si no la crea
        
        if (ventanasPrivadas.containsKey(claveVentana)) {
            ventanasPrivadas.get(claveVentana).setVisible(true);

        } else if (ventanasPrivadas.containsKey(claveInversa)) {
            ventanasPrivadas.get(claveInversa).setVisible(true);

        } else {

            PrivadoCliente nuevaVentana = new PrivadoCliente(clienteCode, nombreEmisor, nombreReceptor);
            ventanasPrivadas.put(claveVentana, nuevaVentana);
            
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Cliente();
        });
    }
}
