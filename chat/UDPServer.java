package aula_udp;

/**
 * UDPServer: Servidor UDP
 * Descricao: Recebe um datagrama de um cliente, imprime o conteudo e retorna o mesmo
 * datagrama ao cliente
 */

import java.net.*;
import java.nio.ByteBuffer;
import java.io.*;

public class UDPServer {

    public static final byte POS_MESSAGE_TYPE = 0;
    public static final byte POS_NICKNAME_SIZE = 1;
    public static final byte POS_BEGIN_NICKNAME = 2;
    public static final byte POS_MESSAGE_SIZE = 65;
    public static final byte POS_BEGIN_MESSAGE = 66;

    public static ByteBuffer createHeader(
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

    public static String getNickName(ByteBuffer header) {
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

    public static String getMessage(ByteBuffer header) {
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

    public static void main(String args[]) {

        String nickname = "Jhonatan";
        String message = "oi!";
        byte messageType = 1;
        byte nicknameLength = (byte) nickname.length();
        byte messageLength = (byte) message.length();

        ByteBuffer header = createHeader(messageType, nicknameLength, nickname, messageLength, message);
        // DatagramSocket senderDgramSocket = null;
        // DatagramSocket receiverDgramSocket = null;

        // Obtem as informações do header que recebo do outro usuário
        String remoteUserNickName = getNickName(header);
        String remoteUserMessage = getMessage(header);
        int resp = 0;

        // try {
        // dgramSocket = new DatagramSocket(6666); // cria um socket datagrama em uma
        // porta especifica
        // receiverDgramSocket = new DatagramSocket();

        // String dstIP = JOptionPane.showInputDialog("IP Destino?");
        // int dstPort = Integer.parseInt(JOptionPane.showInputDialog("Porta
        // Destino?"));

        // /* armazena o IP do destino */
        // InetAddress serverAddr = InetAddress.getByName(dstIP);
        // int serverPort = dstPort; // porta do servidor

        // while (true) {
        // byte[] buffer = new byte[1000]; // cria um buffer para receber requisições

        // /* cria um pacote vazio */
        // DatagramPacket dgramPacket = new DatagramPacket(buffer, buffer.length);
        // dgramSocket.receive(dgramPacket); // aguarda a chegada de datagramas

        // /* imprime e envia o datagrama de volta ao cliente */
        // System.out.println("Cliente: " + new String(dgramPacket.getData(), 0,
        // dgramPacket.getLength()));
        // DatagramPacket reply = new DatagramPacket(dgramPacket.getData(),
        // dgramPacket.getLength(), dgramPacket.getAddress(), dgramPacket.getPort()); //
        // cria um pacote com
        // // os dados
        // dgramSocket.send(reply); // envia o pacote
        // } // while
        // } catch (SocketException e) {
        // System.out.println("Socket: " + e.getMessage());
        // } catch (IOException e) {
        // System.out.println("IO: " + e.getMessage());
        // } finally {
        // dgramSocket.close();
        // } // finally
    } // main
}
// class
