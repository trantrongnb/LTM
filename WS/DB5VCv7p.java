import java.io.*;
import java.net.*;
import java.util.*;

public class DB5VCv7p {

    static final String ENDPOINT = "http://36.50.135.242:2221/CharacterService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "DB5VCv7p";

    // Gửi SOAP request và nhận response dạng String
    static String soapCall(String body) throws Exception {
        String envelope =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<soapenv:Envelope " +
            "  xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" " +
            "  xmlns:tns=\"" + NS + "\">" +
            "<soapenv:Body>" + body + "</soapenv:Body>" +
            "</soapenv:Envelope>";

        HttpURLConnection conn = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        conn.setRequestProperty("SOAPAction", "\"\"");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(envelope.getBytes("UTF-8"));
        }

        InputStream is = conn.getResponseCode() < 400
                ? conn.getInputStream() : conn.getErrorStream();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        // a. Gọi requestCharacter để nhận danh sách số nguyên (mã ASCII của ký tự)
        String getBody =
            "<tns:requestCharacter>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:requestCharacter>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse các phần tử <return> trong response
        List<Integer> chars = new ArrayList<>();
        String temp = response;
        while (temp.contains("<return>")) {
            int s = temp.indexOf("<return>") + 8;
            int e = temp.indexOf("</return>", s);
            chars.add(Integer.parseInt(temp.substring(s, e).trim()));
            temp = temp.substring(e + 9);
        }
        System.out.println("Chars (ASCII): " + chars);
        System.out.print("Chars (text):  ");
        for (int c : chars) System.out.print((char) c);
        System.out.println();

        // b. Xoay vòng phải (rotate right)
        // Số lần xoay = giá trị ASCII của ký tự đầu tiên
        int n = chars.size();
        int rotateCount = chars.get(0) % n; // mod n để tránh xoay thừa vòng
        System.out.println("First char ASCII: " + chars.get(0) + ", rotate right: " + chars.get(0) + " % " + n + " = " + rotateCount);

        // Xoay phải k lần: phần tử cuối cùng k phần tử lên đầu
        List<Integer> rotated = new ArrayList<>();
        // Vị trí bắt đầu sau khi xoay phải k = n - k
        int start = n - rotateCount;
        for (int i = 0; i < n; i++) {
            rotated.add(chars.get((start + i) % n));
        }
        System.out.print("Rotated (text): ");
        for (int c : rotated) System.out.print((char) c);
        System.out.println();
        System.out.println("Rotated (ASCII): " + rotated);

        // c. Gọi submitCharacterCharArray để gửi mảng đã xoay
        StringBuilder dataXml = new StringBuilder();
        for (int c : rotated) {
            dataXml.append("<data>").append(c).append("</data>");
        }

        String submitBody =
            "<tns:submitCharacterCharArray>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            dataXml +
            "</tns:submitCharacterCharArray>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // d. Kết thúc chương trình
    }
}
