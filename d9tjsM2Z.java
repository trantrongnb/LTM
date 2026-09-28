import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import UDP.Customer;

public class d9tjsM2Z {
    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 2209;
        String studentCode = "B23DCCN850";
        String qCode = "d9tjsM2Z";

        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(5000);
            InetAddress serverAddress = InetAddress.getByName(serverIp);

            // 1. Gửi thông điệp
            String message = ";" + studentCode + ";" + qCode;
            byte[] sendData = message.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddress, serverPort);
            socket.send(sendPacket);
            System.out.println("Sent: " + message);

            // 2. Nhận thông điệp
            byte[] receiveData = new byte[2048];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            socket.receive(receivePacket);

            // Tách 8 byte đầu làm requestId
            byte[] reqIdBytes = new byte[8];
            System.arraycopy(receivePacket.getData(), 0, reqIdBytes, 0, 8);
            String requestId = new String(reqIdBytes);
            System.out.println("Received requestId: " + requestId);

            // Deserialize Customer từ phần còn lại
            ByteArrayInputStream bais = new ByteArrayInputStream(receivePacket.getData(), 8, receivePacket.getLength() - 8);
            ObjectInputStream ois = new ObjectInputStream(bais);
            Customer customer = (Customer) ois.readObject();

            // Lấy ra các trường để xử lý
            String originalName = customer.getName();
            if (originalName != null && !originalName.trim().isEmpty()) {
                String[] words = originalName.trim().toLowerCase().split("\\s+");
                
                // a. Chuẩn hóa tên (nguyen van hai duong -> DUONG, Nguyen Van Hai)
                StringBuilder newName = new StringBuilder();
                newName.append(words[words.length - 1].toUpperCase()).append(",");
                for (int i = 0; i < words.length - 1; i++) {
                    newName.append(" ");
                    String w = words[i];
                    if (w.length() > 0) {
                        newName.append(Character.toUpperCase(w.charAt(0)));
                        if (w.length() > 1) {
                            newName.append(w.substring(1));
                        }
                    }
                }
                customer.setName(newName.toString());

                // c. Tạo userName (nguyen van hai duong -> nvhduong)
                StringBuilder userName = new StringBuilder();
                for (int i = 0; i < words.length - 1; i++) {
                    if (words[i].length() > 0) {
                        userName.append(words[i].charAt(0)); // Các chữ cái đầu
                    }
                }
                userName.append(words[words.length - 1]); // Cộng thêm tên gốc in thường
                customer.setUserName(userName.toString());
            }

            // b. Chuẩn hóa ngày sinh (mm-dd-yyyy -> dd/mm/yyyy)
            String originalDob = customer.getDayOfBirth();
            if (originalDob != null && originalDob.contains("-")) {
                String[] dobParts = originalDob.split("-");
                if (dobParts.length == 3) {
                    String mm = dobParts[0];
                    String dd = dobParts[1];
                    String yyyy = dobParts[2];
                    customer.setDayOfBirth(dd + "/" + mm + "/" + yyyy);
                }
            }

            System.out.println("New Name: " + customer.getName());
            System.out.println("New DOB: " + customer.getDayOfBirth());
            System.out.println("New UserName: " + customer.getUserName());

            // 3. Gửi lại đối tượng đã sửa đổi lên server
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(reqIdBytes); // 8 byte requestId gốc
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(customer);
            oos.flush();

            byte[] responseData = baos.toByteArray();
            DatagramPacket responsePacket = new DatagramPacket(responseData, responseData.length, serverAddress, serverPort);
            socket.send(responsePacket);
            System.out.println("Sent response object.");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Socket closed.");
            }
        }
    }
}
