import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class uNMFWDk0 {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2208;
        String studentCode = "B23DCCN850"; // Tôi đã cập nhật mã sinh viên dựa theo lần chạy trước của bạn
        String qCode = "uNMFWDk0";

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
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

            // c. Xử lý thông điệp
            // Định dạng: requestId;data
            String[] parts = receivedStr.split(";", 2);
            if (parts.length >= 2) {
                String requestId = parts[0];
                String data = parts[1];

                // Đếm số lần xuất hiện của từng ký tự
                int[] counts = new int[256];
                int maxCount = 0;
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    counts[c]++;
                    if (counts[c] > maxCount) {
                        maxCount = counts[c];
                    }
                }

                // Tìm ký tự xuất hiện nhiều nhất (ưu tiên ký tự xuất hiện đầu tiên nếu có nhiều ký tự cùng số lượng)
                char mostFrequentChar = 0;
                for (int i = 0; i < data.length(); i++) {
                    char c = data.charAt(i);
                    if (counts[c] == maxCount) {
                        mostFrequentChar = c;
                        break;
                    }
                }

                // Tìm vị trí xuất hiện (index bắt đầu từ 1)
                StringBuilder sb = new StringBuilder();
                sb.append(requestId).append(";").append(mostFrequentChar).append(":");
                for (int i = 0; i < data.length(); i++) {
                    if (data.charAt(i) == mostFrequentChar) {
                        sb.append(i + 1).append(","); // Thêm dấu phẩy ở cuối giống với ví dụ trong đề bài
                    }
                }

                String responseMessage = sb.toString();
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
