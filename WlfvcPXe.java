import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import UDP.Student;

public class WlfvcPXe {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2209;
        String studentCode = "B23DCCN850"; 
        String qCode = "WlfvcPXe";

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            // a. Gửi thông điệp chứa mã sinh viên và mã câu hỏi
            String message = ";" + studentCode + ";" + qCode;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + message);

            // b. Nhận thông điệp
            byte[] receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);
            
            // 8 byte đầu là requestId
            String requestId = new String(receivePacket.getData(), 0, 8);
            System.out.println("Received requestId: " + requestId);
            
            // Phần còn lại là đối tượng Student
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 8, receivePacket.getLength() - 8);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Student student = (Student) ois.readObject();
            System.out.println("Received Student name: " + student.getName());

            // c. Chuẩn hóa tên và tạo email
            student.setCode(studentCode); // Gán mã sinh viên vào thuộc tính code vì Server có thể yêu cầu kiểm tra
            String originalName = student.getName();
            if (originalName != null && !originalName.trim().isEmpty()) {
                String[] words = originalName.trim().toLowerCase().split("\\s+");
                
                // Chuẩn hóa tên (Viết hoa chữ cái đầu)
                StringBuilder normalizedName = new StringBuilder();
                for (int i = 0; i < words.length; i++) {
                    String w = words[i];
                    if (w.length() > 0) {
                        normalizedName.append(Character.toUpperCase(w.charAt(0)));
                        if (w.length() > 1) {
                            normalizedName.append(w.substring(1));
                        }
                        if (i < words.length - 1) {
                            normalizedName.append(" ");
                        }
                    }
                }
                student.setName(normalizedName.toString());
                
                // Tạo email
                StringBuilder email = new StringBuilder();
                if (words.length > 0) {
                    email.append(words[words.length - 1]); // Tên
                    for (int i = 0; i < words.length - 1; i++) {
                        if (words[i].length() > 0) {
                            email.append(words[i].charAt(0)); // Chữ cái đầu của họ, tên đệm
                        }
                    }
                    email.append("@ptit.edu.vn");
                }
                student.setEmail(email.toString());
                
                System.out.println("Normalized Name: " + student.getName());
                System.out.println("Generated Email: " + student.getEmail());
            }

            // Gửi đối tượng lên server
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(receivePacket.getData(), 0, 8); // 8 byte đầu là requestId (sử dụng mảng byte gốc để tránh sai lệch encoding)
            
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(student);
            oos.flush();
            
            byte[] responseData = baos.toByteArray();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, serverAddress, serverPort);
            socket.send(responsePacket);
            System.out.println("Sent response object.");

        } catch (Exception e) {
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
