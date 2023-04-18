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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.io.*;

public class Client {

  public static void main(String args[]) {
    try {
      int serverPort = 6666; // porta do servidor

      /* cria um socket e mapeia a porta para aguardar conexao */
      // DatagramSocket dgramSocket = new DatagramSocket(serverPort);
      DatagramSocket dgramSocket = null;

      /* cria um thread para atender a conexao */
      ClientThread c = new ClientThread(dgramSocket);

      /* inicializa a thread */
      c.start();
      c.join();

    } catch (Exception e) {
      System.out.println("Listen socket:" + e.getMessage());
    } // catch
  } // main
} // class

class ClientThread extends Thread {

  public static final byte POS_FILENAME_SIZE = 0;
  public static final byte POS_FILENAME = 1;
  public static final byte POS_

  DatagramSocket dgramSocket;
  String currentPath;

  public ClientThread(DatagramSocket dgramSocket) {
    try {
      this.dgramSocket = dgramSocket;

      File theDir = new File("./file_upload/uploads");
      if (!theDir.exists()) {
        theDir.mkdirs();
      }

      // aqui devemos concatenar o nome da pasta do usuario
      this.currentPath = System.getProperty("user.dir") + "/file_upload/uploads";
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

  void sendFile(String filename) {
    String path = this.currentPath + "/" + filename;

    File file = new File(path);

    if (!file.exists())
      return;

    ByteBuffer header = ByteBuffer.allocate(1306); // Criando header
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