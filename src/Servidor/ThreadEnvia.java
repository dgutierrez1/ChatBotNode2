package Servidor;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
        
public class ThreadEnvia implements Runnable {
	Interfaz main; 
    private ObjectOutputStream salida;
    private String mensaje;
    private Socket conexion; 
    private SecretKeySpec serverAesKey;
   
    public ThreadEnvia(Socket conexion, Interfaz main, SecretKeySpec serverAesKey){
        this.conexion = conexion;
        this.main = main;
        this.serverAesKey=serverAesKey;
        
        // Agregar callback al campo texto para enviar mensajes
        main.campoTexto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mensaje = event.getActionCommand();
                enviarDatos(mensaje); 
                main.campoTexto.setText(""); 
            } 
        });
    } 
    
    
   private void enviarDatos(String mensaje){
      try {
    	  // Inicia el cifrado
    	  Cipher cf = Cipher.getInstance("AES/CBC/PKCS5Padding");
          cf.init(Cipher.ENCRYPT_MODE,serverAesKey);
          byte[] encodedParams = cf.getParameters().getEncoded();
          
          // Envia los parametros de cifrado al cliente
          salida.writeObject(encodedParams);
          byte[] theCph = cf.doFinal(mensaje.getBytes());
          
          // Envia el mensaje cifrado al cliente
         salida.writeObject(theCph);
         salida.flush(); 
         
         main.agregarMensaje(conexion.getInetAddress().getHostName()+ " - Tu: ", mensaje, true);
      } 
      catch (Exception e){ 
         main.agregarMensaje("Error escribiendo Mensaje", "", true);
      }   
   } 


   
    public void run() {
         try {
            salida = new ObjectOutputStream(conexion.getOutputStream());
            salida.flush(); 
        } catch (SocketException ex) {
        } catch (IOException ioException) {
          ioException.printStackTrace();
        } catch (NullPointerException ex) {
        }
    }  
} 
