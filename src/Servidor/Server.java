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


public class Server{
    private static ServerSocket socket;
    private static Socket conexion; 
    private static KeyAgreement serverKeyAgree;
   
  

    public static void main(String[] args) {
    	Interfaz interfaz = new Interfaz();

        ExecutorService executor = Executors.newCachedThreadPool(); //Para correr los threads
 
        try {
            socket = new ServerSocket(11111, 100); 
            interfaz.agregarMensaje("Esperando conexiones ...", "", true);

            //Bucle infinito para esperar conexiones de los clientes
            while (true){
                try {
                	//Permite al servidor aceptar conexiones        
                    conexion = socket.accept(); 
                    interfaz.agregarMensaje("Conectado a : ", conexion.getInetAddress().getHostName(), true);
                    
                    // Crea Streams de salida y entrada
                    ObjectOutputStream  out = new ObjectOutputStream(conexion.getOutputStream());
                    ObjectInputStream in = new ObjectInputStream(conexion.getInputStream());
                  
                    try {
                    	
                    	// Genera el par de claves Diffie-Hellman
	                    System.out.println("Servidor: Generando par de claves Diffie-Hellman...");
	                    KeyPairGenerator serverKpairGen = KeyPairGenerator.getInstance("DH");
	                    serverKpairGen.initialize(2048);
	                    KeyPair serverKpair = serverKpairGen.generateKeyPair();
	                    
	                    //Crea inicializa el objeto DH KeyAgreement
	                    serverKeyAgree = KeyAgreement.getInstance("DH");
	                    serverKeyAgree.init(serverKpair.getPrivate());
	                    
	                    //Servido obtiene su clave publica cifrada
	                    byte[] serverPubKeyEnc = serverKpair.getPublic().getEncoded();
	                    
	                    //Servidor envia su clave publica al cliente
	                    out.writeObject(serverPubKeyEnc);
	                    out.flush();
	                    
	                    
	                    // Recibe clave publica cifrada del cliente
	                    byte[] clientPubKeyEnc = (byte[]) in.readObject();
	                    
	                    // Se usa la clave publica del cliente para realizar la fase 1
	                    KeyFactory serverKeyFac = KeyFactory.getInstance("DH");
	                    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(clientPubKeyEnc);
	                    PublicKey clientPubKey = serverKeyFac.generatePublic(x509KeySpec);
	                    System.out.println("Ejecutando fase 1...");
	                    serverKeyAgree.doPhase(clientPubKey, true);
	                    
	                    interfaz.agregarMensaje("Iniciando cifrado", "", true);
	                    System.out.println("Clave publica del cliente: "+clientPubKey.toString());
	                    
                    }catch(Exception e) {
                    	e.printStackTrace();
                    }
                    
                    // Se genera el secreto del servidor
                    byte[] serverSharedSecret = serverKeyAgree.generateSecret();
                    int serverLen = serverSharedSecret.length;
                    
                    // Se envia la longitud del secreto el cliente
                    out.writeInt(serverLen);
                    out.flush();
                    
                    // Se obtiene la clave de cifrado AES del servidor
                    SecretKeySpec serverAesKey = new SecretKeySpec(serverSharedSecret,0,16,"AES");
                    
                    interfaz.agregarMensaje("DH clave de sesión verificada, la información viaja encriptada", "", true);
                    interfaz.habilitarInput(true); 

                    //Ejecucion de los threads
                    executor.execute(new ThreadRecibe(conexion, interfaz,serverAesKey)); //client
                    executor.execute(new ThreadEnvia(conexion, interfaz,serverAesKey));
                } catch (IOException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        } 
        finally {
        }
        executor.shutdown();
    }
}
