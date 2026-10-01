import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class yqqdyR4p {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2207;
        String studentCode = "B23DCCN850"; // Lưu ý: Thay mã sinh viên thực tế của bạn vào đây
        String qCode = "yqqdyR4p";

        DatagramSocket socket = null;
        try {
            // Khởi tạo UDP Socket
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

            // c. Xử lý thông điệp và tìm kiếm các giá trị còn thiếu
            String[] parts = receivedStr.split(";");
            if (parts.length >= 3) {
                String requestId = parts[0];
                int n = Integer.parseInt(parts[1]);
                String[] numbersStr = parts[2].split(",");
                
                // Mảng đánh dấu các số đã xuất hiện
                boolean[] present = new boolean[n + 1];
                for (String numStr : numbersStr) {
                    if (!numStr.trim().isEmpty()) {
                        int num = Integer.parseInt(numStr.trim());
                        if (num >= 1 && num <= n) {
                            present[num] = true;
                        }
                    }
                }

                // Tìm các số còn thiếu và ghép thành chuỗi
                StringBuilder sb = new StringBuilder();
                sb.append(requestId).append(";");
                boolean first = true;
                for (int i = 1; i <= n; i++) {
                    if (!present[i]) {
                        if (!first) {
                            sb.append(",");
                        }
                        sb.append(i);
                        first = false;
                    }
                }

                // Gửi phản hồi lại server
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
