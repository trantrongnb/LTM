import java.io.*;
import java.net.*;

public class rDSmFg87 {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2207;
        String studentCode = "B23DCCN850"; // Mã sinh viên của bạn
        String qCode = "rDSmFg87";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công đến server!");

            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // a. Gửi chuỗi định dạng "studentCode;qCode"
            String message = studentCode + ";" + qCode;
            dos.writeUTF(message);
            dos.flush();
            System.out.println("Đã gửi: " + message);

            // b. Nhận chuỗi đã bị mã hóa caesar và giá trị dịch chuyển s
            String encodedString = dis.readUTF();
            int s = dis.readInt();
            System.out.println("Nhận được chuỗi mã hóa: " + encodedString);
            System.out.println("Nhận được độ dịch chuyển s: " + s);

            // c. Thực hiện giải mã và gửi lên Server
            String decodedString = decodeCaesar(encodedString, s);
            System.out.println("Chuỗi sau khi giải mã: " + decodedString);
            
            dos.writeUTF(decodedString);
            dos.flush();
            System.out.println("Đã gửi chuỗi giải mã lên server.");

            // d. Đóng kết nối (đã tự động đóng do dùng try-with-resources)
            dis.close();
            dos.close();

        } catch (SocketTimeoutException e) {
            System.out.println("Lỗi: Thời gian giao tiếp vượt quá 5s (Timeout)");
        } catch (IOException e) {
            System.out.println("Lỗi IO: " + e.getMessage());
        }
    }

    public static String decodeCaesar(String encoded, int s) {
        StringBuilder decoded = new StringBuilder();
        // Dịch chuyển ngược lại để giải mã (với các trường hợp s > 26 hoặc s âm)
        int shift = (s % 26 + 26) % 26; 
        
        for (char c : encoded.toCharArray()) {
            if (Character.isUpperCase(c)) {
                char decodedChar = (char) (c - shift);
                if (decodedChar < 'A') {
                    decodedChar += 26;
                }
                decoded.append(decodedChar);
            } else if (Character.isLowerCase(c)) {
                char decodedChar = (char) (c - shift);
                if (decodedChar < 'a') {
                    decodedChar += 26;
                }
                decoded.append(decodedChar);
            } else {
                // Không phải chữ cái thì giữ nguyên
                decoded.append(c);
            }
        }
        return decoded.toString();
    }
}
