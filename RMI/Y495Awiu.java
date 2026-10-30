package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

// Interface từ xa - phải nằm trong package RMI để khớp với Server
interface CharacterService extends Remote {
    public String requestCharacter(String studentCode, String qCode) throws RemoteException;
    public void submitCharacter(String studentCode, String qCode, String strSubmit) throws RemoteException;
}

public class Y495Awiu {

    // b. Chuyển đổi số thập phân sang số La Mã
    public static String toRoman(int num) {
        // Bảng tra cứu giá trị và ký hiệu La Mã (bao gồm cả dạng trừ: IV, IX, XL, XC, CD, CM)
        int[]    values  = {1000, 900, 500, 400, 100,  90,  50,  40,  10,   9,   5,   4,   1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            // Trừ dần giá trị và gắn ký hiệu tương ứng
            while (num >= values[i]) {
                sb.append(symbols[i]);
                num -= values[i];
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String serverIp = "36.50.135.242";
        int serverPort = 1099;
        String studentCode = "B23DCCN850";
        String qCode = "Y495Awiu";

        try {
            // a. Kết nối RMI Registry và lấy stub của CharacterService
            Registry registry = LocateRegistry.getRegistry(serverIp, serverPort);
            CharacterService characterService = (CharacterService) registry.lookup("RMICharacterService");

            // a. Triệu gọi requestCharacter để nhận số thập phân từ Server
            String received = characterService.requestCharacter(studentCode, qCode);
            System.out.println("Received: " + received);

            // b. Chuyển đổi chuỗi số thập phân sang số La Mã
            int decimal = Integer.parseInt(received.trim());
            String roman = toRoman(decimal);
            System.out.println("Roman:    " + roman);

            // c. Gửi chuỗi số La Mã lên Server
            characterService.submitCharacter(studentCode, qCode, roman);
            System.out.println("Submitted successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
        // d. Kết thúc chương trình (tự động khi main() trả về)
    }
}
