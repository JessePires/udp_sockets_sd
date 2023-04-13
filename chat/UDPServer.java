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
        int offset = 2;

        buffer.put(0, messageType);
        buffer.put(1, normalizedNickSize);

        for (int i = 0; i < normalizedNickSize; i++) {
            buffer.put(offset, nicknameInBytes[i]);
            offset++;
        }

        buffer.put(66, normalizedMessageSize);

        offset = 67;
        for (int i = 0; i < normalizedMessageSize; i++) {
            buffer.put(offset, messageInBytes[i]);
            offset++;
        }

        return buffer;
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
