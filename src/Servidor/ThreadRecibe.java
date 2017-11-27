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
   
    public void run() {
        try {
            entrada = new ObjectInputStream(cliente.getInputStream());
        } catch (IOException ex) {
            Logger.getLogger(ThreadRecibe.class.getName()).log(Level.SEVERE, null, ex);
        }
        do { 
            try { 
            	// Recibe los parametros cifrados del cliente
            	byte[] encodedParams=(byte[]) entrada.readObject();
            	
            	// Recibe el mensaje cifrado del cliente
            	mensaje = (byte[]) entrada.readObject();
            	
            	// Inicia el decifrado
            	AlgorithmParameters aesParams = AlgorithmParameters.getInstance("AES");
                aesParams.init(encodedParams);
                Cipher serverCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                serverCipher.init(Cipher.DECRYPT_MODE, serverAesKey, aesParams);
                
                // Descifra el mensaje enviado por el cliente
                byte[] recovered = serverCipher.doFinal(mensaje);             
				reconstitutedString = new String(recovered);
                
                // Imprime al mensaje
				String clienteIP = cliente.getInetAddress().getHostAddress();
	            String nombreCliente = cliente.getInetAddress().getHostName();
	            main.agregarMensaje(clienteIP+" - "+ nombreCliente +" Cliente:" , reconstitutedString, false);

            } 
            catch (SocketException ex) {
            }
            catch (EOFException eofException) {
                main.agregarMensaje("Fin de la conexion", "", true);
                System.out.println("Fin de la conexion");
                break;
            } 
            catch (IOException ex) {
                Logger.getLogger(ThreadRecibe.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException classNotFoundException) {
                main.agregarMensaje("Objeto desconocido", "", true);
            } catch (IllegalBlockSizeException e1) {
				e1.printStackTrace();
			} catch (BadPaddingException e1) {
				e1.printStackTrace();
			} catch (InvalidKeyException e1) {
				e1.printStackTrace();
			} catch (InvalidAlgorithmParameterException e1) {
				e1.printStackTrace();
			} catch (NoSuchAlgorithmException e1) {
				e1.printStackTrace();
			} catch (NoSuchPaddingException e1) {
				e1.printStackTrace();
			}

        } while (!mensaje.equals("TERMINAR")); 

        try {
        	// Cerrar Streams
            entrada.close(); 
            cliente.close(); 
        } 
        catch (IOException ioException) {
            ioException.printStackTrace();
        } 

        main.agregarMensaje("Fin de la conexion", "", true);
        System.exit(0);
    } 
} 
