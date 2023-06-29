package file_upload;

/**
 * Descrição: Servidor UDP para upload de arquivos.
 *  * 
 * Autores: Jhonatan Guilherme de Oliveira Cunha, Jessé Pires Barbato Rocha
 * 
 * Data de criação: 17/03/2023
 * Data última atualização: 11/04/2023 
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class Client {

  public static void main(String args[]) {
    try {
      int serverPort = 6666; // porta do servidor
      int clientPort = 6665; // porta do servidor

      /* cria um socket e mapeia a porta para aguardar conexao */
      // DatagramSocket dgramSocket = new DatagramSocket(serverPort);
      InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
      DatagramSocket dgramSocket = new DatagramSocket(clientPort);

      /* cria um thread para atender a conexao */
      ClientThread c = new ClientThread(dgramSocket, serverAddr, serverPort);

      /* inicializa a thread */
      c.start();
      c.join();

    } catch (Exception e) {
      System.out.println("Listen socket:" + e.getMessage());
    } // catch
  } // main
} // class

class ClientThread extends Thread {

  public static final int POS_FILENAME_SIZE = 0;
  public static final int POS_FILENAME = 1;
  public static final int POS_FILE_SIZE = 33;
  public static final int POS_CHUNK_SIZE = 35;
  public static final int POS_CHUNK = 37;
  public static final int POS_CHECKSUM = 1061;
  public static final int POS_ORDER = 1081;

  DatagramSocket dgramSocket;
  String currentPath;
  int port;
  InetAddress address;

  public ClientThread(DatagramSocket dgramSocket, InetAddress address, int port) {
    try {
      this.dgramSocket = dgramSocket;
      this.address = address;
      this.port = port;

      this.currentPath = System.getProperty("user.dir");
      System.out.println(currentPath);
    } catch (Exception ioe) {
      System.out.println("IOE:" + ioe.getMessage());
    }
  }

  private void lsCommand() {
    try {
      File directory = new File(this.currentPath);
      File[] arrayFiles = directory.listFiles();

      List<String> fileList = new ArrayList<String>();

      for (File f : arrayFiles) {
        if (f.isFile()) {
          fileList.add(f.getName());
        }
      }

      for (String fileItem : fileList) {
        System.out.println(fileItem);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void getDirs() {
    try {
      File directory = new File(this.currentPath);
      File[] arrayFiles = directory.listFiles();

      // create a new ArrayList
      List<String> fileList = new ArrayList<String>();

      for (File f : arrayFiles) {
        if (f.isDirectory()) {
          fileList.add(f.getName());
        }
      }

      for (String fileItem : fileList) {
        System.out.println(fileItem);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private void cdCommand(String args) {
    try {
      String[] argsArray = args.split("/");

      if (argsArray.length == 0) {
        return;
      }

      String[] currentDir = this.currentPath.split("/");
      List<String> listCurrentDir = new ArrayList<String>(Arrays.asList(currentDir));

      for (String p : argsArray) {
        if (!p.equals(".")) {

          if (p.equals("..")) {
            listCurrentDir.remove(listCurrentDir.size() - 1);
          } else {
            listCurrentDir.add(p);
          }

        }
      }

      String newPwd = String.join("/", listCurrentDir);
      Path newPath = Paths.get(newPwd);

      if (Files.isDirectory(newPath)) {
        this.currentPath = newPwd;
      }

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  public static byte[] readFileToByteArray(String fileName) {
    File file = new File(fileName);
    
    long size = file.length();
    byte[] fileContent = new byte[(int)size];

    try (FileInputStream fis = new FileInputStream(file)) {
      fis.read(fileContent);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return fileContent;
  }

  void sendFile(String filename) {
    String path = this.currentPath + "/" + filename;
    System.out.println(path);

    File file = new File(path);

    if (!file.exists())
      return;

    ByteBuffer header = ByteBuffer.allocate(1311); // Criando header
    header.order(ByteOrder.BIG_ENDIAN);

    byte lengthFilename = (byte) filename.length();
    byte[] fileNameInBytes = filename.getBytes();
    byte normalizedFilenameSize = (byte) lengthFilename > 255 ? (byte) 255 : lengthFilename; //

    header.put(POS_FILENAME_SIZE, lengthFilename); // adicionando tamanho do filename no header

    int offset = POS_FILENAME;

    // Adicionando nome do arquivo no header
    for (int i = 0; i < normalizedFilenameSize; i++) {
      header.put(offset, fileNameInBytes[i]); //
      offset++;
    }

    // Adicionando tamanho do arquivo no header
    byte[] fileContent = readFileToByteArray(path);
    int fileSize = fileContent.length;
    System.out.printf("fileSize: %d\n", fileSize);
    header.putInt(POS_FILE_SIZE, fileSize);

    // transformando header in byteArray
    byte[] headerByteArray = header.array();

    // /* cria um pacote datagrama */
    DatagramPacket request = new DatagramPacket(
        headerByteArray,
        headerByteArray.length,
        this.address,
        this.port);

    // /* envia o primeiro pacote */
    try {
      this.dgramSocket.send(request);
    } catch (IOException e) {
      e.printStackTrace();
    }

    // Calculando quantidade de chunks totais
    int amountOfChunks = (int) Math.ceil((double) fileSize / 1024);
    int chunkAmmount = amountOfChunks > 1 ? amountOfChunks : 1;

    int start = 0;
    int end = fileContent.length > 1024 ? 1024 : fileContent.length;

    // enviando chunks
    for (int i = 0; i < chunkAmmount; i++) {
      System.out.printf("enviou chunk %d\n", i+1);

      if (end > fileContent.length){
        end = fileContent.length;
      }
      
      byte[] chunk = Arrays.copyOfRange(fileContent, start, end);
      // int oldStart =start;

      
      // if (end > fileContent.length){
      //   // int diffEnd = fileContent.length - oldStart;
      //   // end = diffEnd > 1024 ? fileContent.length - (diffEnd - 1024) : diffEnd;
      //   // start = oldStart;
      //   end = fileContent.length;
      //   chunk = Arrays.copyOfRange(fileContent, start, end);
      // }
      


      short ammounOfBytesInChunk = (short) (chunk.length);
      header.putShort(POS_CHUNK_SIZE, ammounOfBytesInChunk);

      header.position(POS_CHUNK);
      header.put(chunk);
      header.putInt(POS_ORDER, i);
      headerByteArray = header.array();

      request = new DatagramPacket(
          headerByteArray,
          headerByteArray.length,
          this.address,
          this.port);

      try {
        System.out.println("vai enviar");
        this.dgramSocket.send(request);
        
        // Espera confirmação de recebimento para continuar a enviar
        byte[] buffer = new byte[1];
        DatagramPacket reply = new DatagramPacket(buffer, buffer.length);	
        this.dgramSocket.receive(reply);
        System.out.println("voltou a enviar");

        start = end;
        end += 1024;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Create SHA-1 message digest instance
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("SHA-1");
      // Convert input string to byte array
      byte[] hashBytes = md.digest(fileContent);

      header.position(POS_CHECKSUM);
      header.put(hashBytes);

      request = new DatagramPacket(
          headerByteArray,
          headerByteArray.length,
          this.address,
          this.port);

      this.dgramSocket.send(request);
      
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /* metodo executado ao iniciar a thread - start() */
  @Override
  public void run() {
    Scanner reader = new Scanner(System.in);
    try {

      System.out.println("args: " + this.currentPath);
      String buffer = "";
      loop: while (true) {
        buffer = reader.nextLine(); /* Aguarda usuario digitar comando */

        String[] args = buffer.split(" ", 2);

        switch (args[0]) {
          case "GETFILES":
            this.lsCommand();
            break;
          case "CHDIR":
            this.cdCommand(args[1]);
            break;
          case "GETDIRS":
            this.getDirs();
            break;
          case "UPLOAD":
            this.sendFile(args[1]);
            break;
          case "EXIT":
            break loop;
          default:
            System.out.println("Comando não encontrado");
            break;
        }

      }
    } catch (Exception e) {
      System.out.println("e: " + e.getMessage());
    } finally {
      try {
        dgramSocket.close();
      } catch (Exception e) {
        System.err.println("e: " + e.getMessage());
      }
    }
  }
}
