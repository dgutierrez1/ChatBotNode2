 package Servidor;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class ThreadRecibe implements Runnable {
    private Interfaz main;
    private byte[] mensaje; 
    private ObjectInputStream entrada;
    private Socket cliente;
    private SecretKeySpec serverAesKey;
    private String reconstitutedString;
   
    
   //Inicializar chatServer y configurar GUI
   public ThreadRecibe(Socket cliente, Interfaz main, SecretKeySpec serverAesKey){
       this.cliente = cliente;
       this.main = main;
       this.serverAesKey=serverAesKey;
   }  

    public void mostrarMensaje(String mensaje) {
        main.agregarMensaje(mensaje);
    } 
   
    public void run() {
        try {
            entrada = new ObjectInputStream(cliente.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ThreadRecibe.class.getName()).log(Level.SEVERE, null, ex);
        }
        do { //procesa los mensajes enviados dsd el servidor
            try {//leer el mensaje y mostrarlo 
            	
            	byte[] encodedParams=(byte[]) entrada.readObject();
            	mensaje = (byte[]) entrada.readObject(); //leer nuevo mensaje
            	AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                aesParams.init(encodedParams);
                Cipher serverCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                serverCipher.init(Cipher.DECRYPT_MODE, serverAesKey, aesParams);
                byte[] recovered = serverCipher.doFinal(mensaje);             
				reconstitutedString = new String(recovered);
                
                
                
                main.agregarMensaje("Cliente>>> " + reconstitutedString);
            } //fin try
            catch (SocketException ex) {
            }
            catch (EOFException eofException) {
                main.agregarMensaje("Fin de la conexion");
                System.out.println("Fin de la conexion");
                break;
            } //fin catch
            catch (IOException ex) {
                Logger.getLogger(ThreadRecibe.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException classNotFoundException) {
                main.agregarMensaje("Objeto desconocido");
            } //fin catch               
 catch (IllegalBlockSizeException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (BadPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidKeyException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (InvalidAlgorithmParameterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (NoSuchPaddingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

        } while (!mensaje.equals("Servidor>>> TERMINATE")); //Ejecuta hasta que el server escriba TERMINATE

        try {
            entrada.close(); //cierra input Stream
            cliente.close(); //cieraa Socket
        } //Fin try
        catch (IOException ioException) {
            ioException.printStackTrace();
        } //fin catch

        main.agregarMensaje("Fin de la conexion");
        System.exit(0);
    } 
} 
