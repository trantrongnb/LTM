import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RxexCsEL {
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2207;
        String studentCode = "B23DCCN850";
        String questionCode = "RxexCsEL";

        try (Socket socket = new Socket(serverHost, serverPort)) {
            // Set timeout là 5 giây (5000 milliseconds)
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công!");

            // Tạo các luồng DataInputStream và DataOutputStream
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // a. Gửi chuỗi là mã sinh viên và mã câu hỏi
            String msg = studentCode + ";" + questionCode;
            dos.writeUTF(msg); // Tự động đóng gói 2 byte độ dài + UTF-8 giống hệt struct.pack của Python
            dos.flush();
            System.out.println("Đã gửi: " + msg);

            // b. Nhận lần lượt 2 số nguyên a và b từ server
            int a = dis.readInt(); // Tự động đọc đúng 4 byte (Big-Endian)
            int b = dis.readInt();
            System.out.println("Nhận được: a = " + a + ", b = " + b);

            // c. Thực hiện tính toán tổng, tích và gửi lần lượt lên server
            int sum = a + b;
            int product = a * b;
            
            dos.writeInt(sum);
            dos.writeInt(product);
            dos.flush();
            System.out.println("Đã gửi: Tổng = " + sum + ", Tích = " + product);

        } catch (SocketTimeoutException e) {
            System.out.println("Lỗi: Quá thời gian giao tiếp 5s");
        } catch (IOException e) {
            System.out.println("Lỗi kết nối: " + e.getMessage());
        }
    }
}
