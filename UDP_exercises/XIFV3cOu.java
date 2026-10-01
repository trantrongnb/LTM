import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class XIFV3cOu {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2208;
        String studentCode = "B23DCCN850"; // Mã sinh viên của bạn
        String qCode = "XIFV3cOu";

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000); // Đặt timeout 5s để tránh bị treo
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            // a. Gửi thông điệp
            String message = ";" + studentCode + ";" + qCode;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + message);

            // b. Nhận thông điệp
            byte[] receiveData = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            String receivedStr = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Received: " + receivedStr);

            // c. Chuẩn hóa chuỗi
            // Cắt chuỗi với limit = 2 để đảm bảo data không bị cắt sai nếu data chứa ký tự ';'
            String[] parts = receivedStr.split(";", 2);
            if (parts.length >= 2) {
                String requestId = parts[0];
                String data = parts[1];

                // Thực hiện chuẩn hóa
                String[] words = data.trim().split("\\s+");
                StringBuilder normalized = new StringBuilder();
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    if (word.length() > 0) {
                        // Ký tự đầu tiên viết hoa
                        normalized.append(Character.toUpperCase(word.charAt(0)));
                        // Các ký tự còn lại viết thường
                        if (word.length() > 1) {
                            normalized.append(word.substring(1).toLowerCase());
                        }
                        
                        // Thêm dấu cách giữa các từ
                        if (i < words.length - 1) {
                            normalized.append(" ");
                        }
                    }
                }

                // Gửi phản hồi lên server theo định dạng requestId;data_đã_chuẩn_hóa
                String responseMessage = requestId + ";" + normalized.toString();
                byte[] responseData = responseMessage.getBytes();
                DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, serverAddress, serverPort);
                socket.send(responsePacket);
                System.out.println("Sent response: " + responseMessage);
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
