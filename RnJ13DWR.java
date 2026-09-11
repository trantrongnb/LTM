package TCP;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Customer implements Serializable {
    private static final long serialVersionUID = 20170711L;
    
    private int id;
    private String code;
    private String name;
    private String dayOfBirth;
    private String userName;

    public Customer(int id, String code, String name, String dayOfBirth, String userName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.dayOfBirth = dayOfBirth;
        this.userName = userName;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDayOfBirth() { return dayOfBirth; }
    public void setDayOfBirth(String dayOfBirth) { this.dayOfBirth = dayOfBirth; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", dayOfBirth='" + dayOfBirth + '\'' +
                ", userName='" + userName + '\'' +
                '}';
    }

    // --- HÀM MAIN VÀ XỬ LÝ CHÍNH ---
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2209;
        String studentCode = "B23DCCN850"; // Mã sinh viên của bạn
        String questionCode = "RnJ13DWR"; // Mã câu hỏi bạn vừa cung cấp
        
        try (Socket socket = new Socket(serverHost, serverPort)) {
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công tới server!");

            // 1. Luôn khởi tạo ObjectOutputStream trước ObjectInputStream
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // 2. Gửi mã sinh viên và câu hỏi
            String msg = studentCode + ";" + questionCode;
            oos.writeObject(msg);
            oos.flush();
            System.out.println("Đã gửi: " + msg);

            // 3. Nhận Customer từ server
            Customer customer = (Customer) ois.readObject();
            System.out.println("Nhận được khách hàng: " + customer);

            // 4. Chuẩn hoá thông tin
            normalizeCustomer(customer);
            System.out.println("Sau khi chuẩn hóa: " + customer);

            // 5. Gửi lại kết quả đã sửa đổi
            oos.writeObject(customer);
            oos.flush();
            System.out.println("Đã gửi kết quả thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hàm chuẩn hóa thông tin
    public static void normalizeCustomer(Customer customer) {
        String name = customer.getName();
        if (name != null) {
            name = name.trim().replaceAll("\\s+", " ");
            String[] words = name.split(" ");
            
            if (words.length > 0) {
                String lastWord = words[words.length - 1].toUpperCase();
                
                StringBuilder nameBuilder = new StringBuilder();
                StringBuilder userBuilder = new StringBuilder();
                
                nameBuilder.append(lastWord).append(",");
                
                for (int i = 0; i < words.length - 1; i++) {
                    String word = words[i].toLowerCase();
                    userBuilder.append(word.charAt(0));
                    
                    String capitalized = word.substring(0, 1).toUpperCase() + word.substring(1);
                    nameBuilder.append(" ").append(capitalized);
                }
                
                userBuilder.append(words[words.length - 1].toLowerCase());
                
                customer.setName(nameBuilder.toString());
                customer.setUserName(userBuilder.toString());
            }
        }
        
        String dob = customer.getDayOfBirth();
        if (dob != null && dob.contains("-")) {
            String[] parts = dob.split("-");
            if (parts.length == 3) {
                String mm = parts[0];
                String dd = parts[1];
                String yyyy = parts[2];
                customer.setDayOfBirth(dd + "/" + mm + "/" + yyyy);
            }
        }
    }
}
