package Servidor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;


public class Server extends JFrame{
    public JTextField campoTexto; //Para mostrar mensajes de los usuarios
    public JTextArea areaTexto; //Para ingresar mensaje a enviar
    private static ServerSocket servidor; //
    private static Socket conexion; //Socket para conectarse con el cliente
    private static String ip = "127.0.0.1"; //ip a la cual se conecta
    private static KeyAgreement serverKeyAgree;
    public static Server main; 
    
//    public Server(){
//        super("Servidor"); //Establece titulo al Frame
//        
//        campoTexto = new JTextField(); //crea el campo para texto
//        campoTexto.setEditable(false); //No permite que sea editable el campo de texto
//        add(campoTexto, BorderLayout.SOUTH); //Coloca el campo de texto en la parte superior
//        
//        areaTexto = new JTextArea(); //Crear displayArea
//        areaTexto.setEditable(false);
//        add(new JScrollPane(areaTexto), BorderLayout.CENTER);
//      //  areaTexto.setBackground(Color.orange); //Pone de color cyan al areaTexto
//        //areaTexto.setForeground(Color.BLACK); //pinta azul la letra en el areaTexto
//       // campoTexto.setForeground(Color.BLACK); //pinta toja la letra del mensaje a enviar
//        
//        
//        //Crea menu Archivo y submenu Salir, ademas agrega el submenu al menu
//        // JMenu menuArchivo = new JMenu("Archivo"); 
//        JMenuItem salir = new JMenuItem("Salir");
//        // menuArchivo.add(salir); //Agrega el submenu Salir al menu menuArchivo
//        
//        JMenuBar barra = new JMenuBar(); //Crea la barra de menus
//        setJMenuBar(barra); //Agrega barra de menus a la aplicacion
//        barra.add(salir); //agrega menuArchivo a la barra de menus
//        
//        //Accion que se realiza cuando se presiona el submenu Salir
//        salir.addActionListener(new ActionListener() { //clase interna anonima
//                public void actionPerformed(ActionEvent e) {
//                    System.exit(0); //Sale de la aplicacion
//                }
//        });
//        
//        setSize(300, 320); //Establecer tamano a ventana
//        setVisible(true); //Pone visible la ventana
//    }
    
//    //Para mostrar texto en displayArea
//    public void mostrarMensaje(String mensaje) {
//        areaTexto.append(mensaje + "\n");
//    } 
//    public void habilitarTexto(boolean editable) {
//        campoTexto.setEditable(editable);
//    }
 
    

    public static void main(String[] args) {
    	Interfaz interfaz = new Interfaz(); //Instanciacion de la clase Principalchat

        ExecutorService executor = Executors.newCachedThreadPool(); //Para correr los threads
 
        try {
            //main.mostrarMensaje("No se encuentra Servidor");
            servidor = new ServerSocket(11111, 100); 
            interfaz.agregarMensaje("Esperando conexiones ...");

            //Bucle infinito para esperar conexiones de los clientes
            while (true){
                try {
                    conexion = servidor.accept(); //Permite al servidor aceptar conexiones        

                    //main.mostrarMensaje("Conexion Establecida");
                    interfaz.agregarMensaje("Conectado a : " + conexion.getInetAddress().getHostName());
                    
                    ObjectOutputStream  out = new ObjectOutputStream(conexion.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(conexion.getInputStream());
                  
                    try {
                    System.out.println("Server: Generando keypair Diffie-Hellman...");
                    KeyPairGenerator serverKpairGen = KeyPairGenerator.getInstance("DH");
                    serverKpairGen.initialize(2048);
                    KeyPair serverKpair = serverKpairGen.generateKeyPair();
                    
                    //Creates and initializes her DH KeyAgreement object
                    serverKeyAgree = KeyAgreement.getInstance("DH");
                    serverKeyAgree.init(serverKpair.getPrivate());
                    
                    //Servido obtiene su clave publica cifrada
                    byte[] serverPubKeyEnc = serverKpair.getPublic().getEncoded();
                    
                    //Servidor envia su clave publica al cliente
                    out.writeObject(serverPubKeyEnc);
                    out.flush();
                    
                    
                    // Recibe clave publica cifrada del cliente
                    byte[] clientPubKeyEnc = (byte[]) in.readObject();
                    
                    KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
                    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPubKeyEnc);
                    PublicKey clientPubKey = serverKeyFac.generatePublic(x509KeySpec);
                    System.out.println("Ejecutar PHASE1 con el cliente...");
                    serverKeyAgree.doPhase(clientPubKey, true);
                    
                    
                    interfaz.agregarMensaje("Clave publica del cliente: "+ clientPubKey);
                    }catch(Exception e) {
                    	
                    }
                    
                   
                    byte[] serverSharedSecret = serverKeyAgree.generateSecret();
                    int serverLen = serverSharedSecret.length;
                    
                    out.writeInt(serverLen);
                    out.flush();
                    
                    SecretKeySpec serverAesKey = new SecretKeySpec(serverSharedSecret,0,16,"AES");
                    
                    interfaz.agregarMensaje("DH clave de sesión verificada, la información viaja encriptada");
                    interfaz.habilitarInput(true); //permite escribir texto para enviar

                    //Ejecucion de los threads
                    executor.execute(new ThreadRecibe(conexion, interfaz,serverAesKey)); //client
                    executor.execute(new ThreadEnvia(conexion, interfaz,serverAesKey));
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } //Fin del catch
        finally {
        }
        executor.shutdown();
    }
}
