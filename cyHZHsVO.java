
import java.io.Serializable;

public class Laptop implements Serializable {
    // Trường dữ liệu theo đúng yêu cầu đề bài
    private static final long serialVersionUID = 20150711L;
    
    // Các thuộc tính
    private int id;
    private String code;
    private String name;
    private int quantity;

    // Hàm khởi tạo đầy đủ các thuộc tính
    public Laptop(int id, String code, String name, int quantity) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.quantity = quantity;
    }

    // Các hàm Getter và Setter để thao tác thay đổi giá trị
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "Laptop{id=" + id + ", code='" + code + "', name='" + name + "', quantity=" + quantity + "}";
    }

    // --- HÀM MAIN: Code toàn bộ xử lý mạng vào chung 1 file ---
    public static void main(String[] args) {
        String serverHost = "36.50.135.242";
        int serverPort = 2209; 
        String studentCode = "B23DCCN850"; // Dùng mã SV của bạn
        String questionCode = "cyHZHsVO";

        try (java.net.Socket socket = new java.net.Socket(serverHost, serverPort)) {
            socket.setSoTimeout(5000);
            System.out.println("Kết nối thành công!");

            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(socket.getOutputStream());
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(socket.getInputStream());

            // 1. Gửi chuỗi là mã sinh viên và mã câu hỏi
            String msg = studentCode + ";" + questionCode;
            oos.writeObject(msg);
            oos.flush();

            // 2. Nhận đối tượng Laptop từ server
            Laptop laptop = (Laptop) ois.readObject();
            System.out.println("Nhận được Laptop: " + laptop);

            // 3. Sửa thông tin Laptop
            // a) Đổi ngược từ đầu tiên và từ cuối cùng trong tên
            String[] nameParts = laptop.getName().trim().split("\\s+");
            if (nameParts.length > 1) {
                String temp = nameParts[0];
                nameParts[0] = nameParts[nameParts.length - 1];
                nameParts[nameParts.length - 1] = temp;
                laptop.setName(String.join(" ", nameParts));
            }
            
            // b) Đảo ngược số lượng
            String quantityStr = String.valueOf(laptop.getQuantity());
            String reversedQuantityStr = new StringBuilder(quantityStr).reverse().toString();
            laptop.setQuantity(Integer.parseInt(reversedQuantityStr));
            
            System.out.println("Laptop sau khi sửa: " + laptop);

            // 4. Gửi lại đối tượng đã sửa
            oos.writeObject(laptop);
            oos.flush();
            System.out.println("Đã gửi kết quả thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
