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
import java.security.MessageDigest;
import java.util.Arrays;
import java.io.*;

public class Server {

    public static void main(String args[]) {
        try {
            int serverPort = 6666; // porta do servidor

            /* cria um socket e mapeia a porta para aguardar conexao */
            // DatagramSocket dgramSocket = new DatagramSocket(serverPort);
            DatagramSocket dgramSocket = new DatagramSocket(serverPort);

            /* cria um thread para atender a conexao */
            ServerThread c = new ServerThread(dgramSocket);

            /* inicializa a thread */
            c.start();
            c.join();

        } catch (Exception e) {
            System.out.println("Listen socket:" + e.getMessage());
        } // catch
    } // main
} // class

class ServerThread extends Thread {

    public static final int POS_FILENAME_SIZE = 0;
    public static final int POS_FILENAME = 1;
    public static final int POS_FILE_SIZE = 33;
    public static final int POS_CHUNK_SIZE = 35;
    public static final int POS_CHUNK = 37;
    public static final int POS_CHECKSUM = 1061;
    public static final int POS_ORDER = 1081;
    public static final int CHECKSUM_SIZE = 20;

    DatagramSocket dgramSocket;
    String currentPath;

    public ServerThread(DatagramSocket dgramSocket) {
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

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            byte[] buffer;
            DatagramPacket reply;
            while (true) {

                buffer = new byte[1310];
                reply = new DatagramPacket(buffer, buffer.length);

                /* aguarda datagramas */
                this.dgramSocket.receive(reply);

                ByteBuffer header = ByteBuffer.wrap(buffer);
                header.order(ByteOrder.BIG_ENDIAN);

                byte lengthFilename = header.get(POS_FILENAME_SIZE);
                byte[] fileNameInBytes = new byte[lengthFilename];

                int offset = POS_FILENAME;
                for (int i = 0; i < lengthFilename; i++) {
                    fileNameInBytes[i] = header.get(offset + i);
                }

                String filename = new String(fileNameInBytes);
                int fileSize = header.getInt(POS_FILE_SIZE);

                int amountOfChunks = fileSize / 1024;
                int chunkAmmount = amountOfChunks > 1 ? amountOfChunks : 1;

                byte[] file = new byte[fileSize];

                for (int i = 0; i < chunkAmmount; i++) {
                    this.dgramSocket.receive(reply);
                    header = ByteBuffer.wrap(buffer);
                    header.order(ByteOrder.BIG_ENDIAN);
                    header.position(POS_ORDER);
                    short order = header.getShort();
                    header.position(POS_CHUNK_SIZE);
                    short chunkSize = header.getShort();
                    int start = order * i;
                    int end = start + chunkSize;

                    for (int j = start; j < end; j++) {
                        file[j] = header.get(POS_CHUNK + j);
                    }
                }

                this.dgramSocket.receive(reply);
                header = ByteBuffer.wrap(buffer);
                header.order(ByteOrder.BIG_ENDIAN);
                header.get(POS_CHECKSUM);

                byte[] checksum = new byte[CHECKSUM_SIZE];

                for (int i = 0; i < CHECKSUM_SIZE; i++) {
                    checksum[i] = header.get(POS_CHECKSUM + i);
                }

                MessageDigest md;
                try {
                    md = MessageDigest.getInstance("SHA-1");
                    // Convert input string to byte array
                    byte[] hashBytes = md.digest(file);

                    if (!Arrays.equals(checksum, hashBytes)) {
                        System.out.println("Algo de errado ocorreu no upload.");
                        return;
                    }

                    try {
                        FileOutputStream fos = new FileOutputStream(this.currentPath + "/" + filename);
                        fos.write(file);
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } catch (Exception e) {
                    System.out.println(e.getStackTrace().toString());
                }

                System.out.println("aaa");
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
