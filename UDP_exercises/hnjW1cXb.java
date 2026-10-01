import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class hnjW1cXb {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2207;
        String studentCode = "B23DCCN850"; // Mã sinh viên của bạn
        String qCode = "hnjW1cXb";

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000); // Tránh bị treo nếu server không phản hồi
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            // a. Gửi thông điệp
            String message = ";" + studentCode + ";" + qCode;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + message);

            // b. Nhận thông điệp
            byte[] receiveData = new byte[2048]; // Tăng buffer lên chút để đảm bảo chứa đủ dãy số
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String receivedStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received: " + receivedStr);

            // c. Xử lý thông điệp và tìm max, min
            // Định dạng: requestId;a1,a2,...,a50
            String[] parts = receivedStr.split(";");
            if (parts.length >= 2) {
                String requestId = parts[0];
                String numbersStr = parts[1];
                
                String[] numberStrings = numbersStr.split(",");
                if (numberStrings.length > 0) {
                    int max = Integer.MIN_VALUE;
                    int min = Integer.MAX_VALUE;
                    
                    for (String numStr : numberStrings) {
                        if (!numStr.trim().isEmpty()) {
                            int num = Integer.parseInt(numStr.trim());
                            if (num > max) {
                                max = num;
                            }
                            if (num < min) {
                                min = num;
                            }
                        }
                    }
                    
                    // Tạo chuỗi phản hồi theo định dạng requestId;max,min
                    String responseMessage = requestId + ";" + max + "," + min;
                    byte[] responseData = responseMessage.getBytes();
                    DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, serverAddress, serverPort);
                    socket.send(responsePacket);
                    System.out.println("Sent response: " + responseMessage);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // d. Đóng socket
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Socket closed.");
            }
        }
    }
}
