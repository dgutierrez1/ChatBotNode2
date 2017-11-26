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
        
        //Evento que ocurre al escribir en el areaTexto
        main.campoTexto.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                mensaje = event.getActionCommand();
                enviarDatos(mensaje); //se envia el mensaje
                main.campoTexto.setText(""); //borra el texto del enterfield
            } //Fin metodo actionPerformed
        } 
        );//Fin llamada a addActionListener
    } 
    
    
    
   //enviar objeto a cliente 
   private void enviarDatos(String mensaje){
      try {
    	  
    	  Cipher cf = Cipher.getInstance("AES/CBC/PKCS5Padding");
          cf.init(Cipher.ENCRYPT_MODE,serverAesKey);
          byte[] encodedParams = cf.getParameters().getEncoded();

          salida.writeObject(encodedParams);
          byte[] theCph = cf.doFinal(mensaje.getBytes());
          
          
         salida.writeObject(theCph);
         
         salida.flush(); //flush salida a cliente
         main.agregarMensaje("Servidor>>> " + mensaje);
      } //Fin try
      catch (Exception e){ 
         main.agregarMensaje("Error escribiendo Mensaje");
      } //Fin catch  
      
   } //Fin methodo enviarDatos


   
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
    
//    public static byte[] hexToBytes(String str) {
//        if (str==null) {
//           return null;
//        } else if (str.length() < 2) {
//           return null;
//        } else {
//           int len = str.length() / 2;
//           byte[] buffer = new byte[len];
//           for (int i=0; i<len; i++) {
//               buffer[i] = (byte) Integer.parseInt(
//                  str.substring(i*2,i*2+2),16);
//           }
//           return buffer;
//        }
//
//     }
//     public static String bytesToHex(byte[] data) {
//        if (data==null) {
//           return null;
//        } else {
//           int len = data.length;
//           String str = "";
//           for (int i=0; i<len; i++) {
//              if ((data[i]&0xFF)<16) str = str + "0" 
//                 + java.lang.Integer.toHexString(data[i]&0xFF);
//              else str = str
//                 + java.lang.Integer.toHexString(data[i]&0xFF);
//           }
//           return str.toUpperCase();
//        }
//     }            
//   
} 
