import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class PrivadoCliente extends JFrame {

    private JTextArea chatCuadro;
    private JTextField enviarCuadro;
    private ClienteCode clienteCode;
    private String nombreReceptor;

    public PrivadoCliente(ClienteCode clienteCode, String nombreEmisor, String nombreReceptor) {

        this.clienteCode = clienteCode;
        this.nombreReceptor = nombreReceptor;

        setTitle("Chat Privado con " + nombreReceptor);
        setBounds(100, 100, 400, 300);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        chatCuadro = new JTextArea();
        chatCuadro.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatCuadro);
        panel.add(scrollPane, BorderLayout.CENTER);

        JTextField enviarCuadro = new JTextField();


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        // Enviar mensaje

        enviarCuadro.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                String mensaje = enviarCuadro.getText();

                if(mensaje != null && !mensaje.isEmpty()){

                    chatCuadro.append("Tu: " + mensaje + "\n");

                    enviarMensaje(nombreEmisor, mensaje, nombreReceptor);
                    enviarCuadro.setText("");
                }
            }
        });
        
        JButton enviarArchivoBoton = new JButton("Enviar Archivo");
        JButton enviarBoton = new JButton("Enviar");

        // Crea un nuevo JPanel con un GridLayout para organizar los botones y el cuadro de texto de entrada

        JPanel entradaPanel = new JPanel(new GridLayout(1, 3)); // 1 fila, 3 columnas
        entradaPanel.add(enviarArchivoBoton);
        entradaPanel.add(enviarCuadro);
        entradaPanel.add(enviarBoton);

        panel.add(entradaPanel, BorderLayout.SOUTH);


        ///////////////////////////////////////////////////////////////////////////////////////////////////////

        // Enviar archivo

        enviarArchivoBoton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                enviarArchivo(nombreEmisor, nombreReceptor);

            }
        });
        

        enviarBoton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mensaje = enviarCuadro.getText();

                if (mensaje != null && !mensaje.isEmpty()) {
                    chatCuadro.append("Tu: " + mensaje + "\n");
                    enviarMensaje(nombreEmisor, mensaje, nombreReceptor);
                    enviarCuadro.setText("");
                }

            }
        });

        add(panel);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Enviar mensaje privado

    public void enviarMensaje(String nombreEmisor, String mensaje, String destinatario) {
        clienteCode.enviarMensajePriv(nombreEmisor, mensaje, destinatario);
    }

    public void imprimirMensaje(String nombreEmisor, String mensaje) {
        chatCuadro.append(nombreEmisor + ": " + mensaje + "\n");
    }

    public void imprimirMensajeArchivo(String mensaje) {
        chatCuadro.append(mensaje);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // Enviar archivo

    private void enviarArchivo(String nombreEmisor, String nombreReceptor) {

        JFileChooser fileChooser = new JFileChooser();
        int seleccion = fileChooser.showOpenDialog(this);
    
        if (seleccion == JFileChooser.APPROVE_OPTION) {

            File archivo = fileChooser.getSelectedFile();

            try {

                String rutaArchivo = archivo.getAbsolutePath(); // Obtener la ruta del archivo como una cadena
                clienteCode.enviarArchivo(nombreEmisor, nombreReceptor, rutaArchivo); // Pasar la ruta del archivo al método enviarArchivo

            } catch (Exception e) {
                
                e.printStackTrace();
            }
        }
    }
    }
