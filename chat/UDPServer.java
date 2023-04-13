package chat;

/**
 * UDPServer: Servidor UDP
 * Descricao: Recebe um datagrama de um cliente, imprime o conteudo e retorna o mesmo
 * datagrama ao cliente
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.swing.JOptionPane;

import java.io.*;

public class UDPServer {
    public static void main(String args[]) {

        // String nickname = "Jhonatan";
        // String message = "oi!";
        // byte messageType = 1;
        // byte nicknameLength = (byte) nickname.length();
        // byte messageLength = (byte) message.length();

        // ByteBuffer header = createHeader(messageType, nicknameLength, nickname,
        // messageLength, message);

        // Obtem as informações do header que recebo do outro usuário
        // String remoteUserNickName = getNickName(header);
        // String remoteUserMessage = getMessage(header);
        // int resp = 0;

        DatagramSocket dgramSocket = null;

        try {

            int srcPort = Integer.parseInt(JOptionPane.showInputDialog("Digite a porta de origem:"));

            dgramSocket = new DatagramSocket(srcPort); // cria um socket datagrama em uma

            int dstPort = Integer.parseInt(JOptionPane.showInputDialog("Digite a porta de destino:"));
            String nickname = new String(JOptionPane.showInputDialog("Digite seu nome de usuário:"));

            /* armazena o IP do destino */
            InetAddress serverAddr = InetAddress.getByName("127.0.0.1");
            int serverPort = dstPort; // porta do servidor

            SendDatagramThread send = new SendDatagramThread(dgramSocket, serverAddr, serverPort, nickname);
            ReceiveDatagramThread receive = new ReceiveDatagramThread(dgramSocket);

            send.start();
            receive.start();

            send.join();

        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException: " + e.getMessage());
        } finally {
            dgramSocket.close();
        }
    }
}

class SendDatagramThread extends Thread {

    public static final byte POS_MESSAGE_TYPE = 0;
    public static final byte POS_NICKNAME_SIZE = 1;
    public static final byte POS_BEGIN_NICKNAME = 2;
    public static final byte POS_MESSAGE_SIZE = 65;
    public static final byte POS_BEGIN_MESSAGE = 66;

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    DatagramSocket datagramSocket;
    InetAddress address;
    int port;
    String nickname;
    byte nicknameSize;

    public SendDatagramThread(DatagramSocket datagramSocket, InetAddress address, int port, String nickname) {
        this.datagramSocket = datagramSocket;
        this.address = address;
        this.port = port;
        this.nickname = nickname;
        this.nicknameSize = (byte) nickname.length();
    }

    public ByteBuffer createHeader(
            byte messageType,
            byte nickSize,
            String nickname,
            byte messageSize,
            String message) {
        ByteBuffer buffer = ByteBuffer.allocate(322);
        byte[] nicknameInBytes = nickname.getBytes();
        byte[] messageInBytes = message.getBytes();

        byte normalizedNickSize = nickSize > 64 ? (byte) 64 : nickSize;
        byte normalizedMessageSize = (byte) messageSize > 255 ? (byte) 255 : messageSize;

        buffer.put(POS_MESSAGE_TYPE, messageType);
        buffer.put(POS_NICKNAME_SIZE, normalizedNickSize);

        int offset = POS_BEGIN_NICKNAME;
        for (int i = 0; i < normalizedNickSize; i++) {
            buffer.put(offset, nicknameInBytes[i]);
            offset++;
        }

        buffer.put(POS_MESSAGE_SIZE, normalizedMessageSize);

        offset = POS_BEGIN_MESSAGE;
        for (int i = 0; i < normalizedMessageSize; i++) {
            buffer.put(offset, messageInBytes[i]);
            offset++;
        }

        return buffer;
    }

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        int resp = 0;

        Scanner reader = new Scanner(System.in);
        String message = "";
        try {
            do {
                // String message = JOptionPane.showInputDialog("Mensagem:");
                System.out.print(ANSI_GREEN + "Mesagem: " + ANSI_RESET);
                message = reader.nextLine();
                byte messageSize = (byte) message.length();

                byte messageType = 1;
                ByteBuffer header = this.createHeader(messageType, this.nicknameSize, this.nickname, messageSize,
                        message);
                byte[] headerByteArray = header.array();

                // /* cria um pacote datagrama */
                DatagramPacket request = new DatagramPacket(headerByteArray, headerByteArray.length,
                        this.address,
                        this.port);

                // /* envia o pacote */
                this.datagramSocket.send(request);

                System.out.println(ANSI_RED + this.nickname + ": " + ANSI_RESET + message);

                // resp = JOptionPane.showConfirmDialog(null, "Nova mensagem?", "Continuar",
                // JOptionPane.YES_NO_OPTION);
                // } while (message != JOptionPane.NO_OPTION);
            } while (message != "exit");
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());

        }
    }
}

class ReceiveDatagramThread extends Thread {

    public static final byte POS_MESSAGE_TYPE = 0;
    public static final byte POS_NICKNAME_SIZE = 1;
    public static final byte POS_BEGIN_NICKNAME = 2;
    public static final byte POS_MESSAGE_SIZE = 65;
    public static final byte POS_BEGIN_MESSAGE = 66;

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";

    DatagramSocket datagramSocket;

    public ReceiveDatagramThread(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public String getNickName(ByteBuffer header) {
        byte nicknameSize = header.get(POS_NICKNAME_SIZE);
        byte[] nicknameBytes = new byte[nicknameSize];

        byte position = POS_BEGIN_NICKNAME;
        for (int i = 0; i < nicknameSize; i++) {
            nicknameBytes[i] = header.get(position);
            position++;
        }

        String nickname = new String(nicknameBytes);

        return nickname;
    }

    public String getMessage(ByteBuffer header) {
        byte messageSize = header.get(POS_MESSAGE_SIZE);
        byte[] messageBytes = new byte[messageSize];

        byte position = POS_BEGIN_MESSAGE;
        for (int i = 0; i < messageSize; i++) {
            messageBytes[i] = header.get(position);
            position++;
        }

        String message = new String(messageBytes);

        return message;
    }

    /* metodo executado ao iniciar a thread - start() */
    @Override
    public void run() {
        try {
            while (true) {
                /* cria um buffer vazio para receber datagramas */
                byte[] buffer = new byte[1000];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                /* aguarda datagramas */
                this.datagramSocket.receive(reply);

                ByteBuffer header = ByteBuffer.wrap(buffer);

                String nickname = getNickName(header);
                String message = getMessage(header);

                System.out.println("\n" + ANSI_RED + nickname + ": " + ANSI_RESET + message);
                System.out.print(ANSI_GREEN + "Mesagem: " + ANSI_RESET);

            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        }
    }
}
