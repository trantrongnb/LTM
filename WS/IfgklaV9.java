import java.io.*;
import java.net.*;
import java.util.*;

public class IfgklaV9 {

    static final String ENDPOINT = "http://36.50.135.242:2221/DataService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "IfgklaV9";

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

        // b. Gọi getData để nhận danh sách số nguyên
        String getBody =
            "<tns:getData>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:getData>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse các phần tử <return>
        List<Integer> numbers = new ArrayList<>();
        String temp = response;
        while (temp.contains("<return>")) {
            int s = temp.indexOf("<return>") + 8;
            int e = temp.indexOf("</return>", s);
            numbers.add(Integer.parseInt(temp.substring(s, e).trim()));
            temp = temp.substring(e + 9);
        }
        System.out.println("Numbers: " + numbers);

        // c. Đếm số chẵn và số lẻ
        int even = 0, odd = 0;
        for (int n : numbers) {
            if (n % 2 == 0) even++;
            else            odd++;
        }
        System.out.println("EVEN=" + even + ", ODD=" + odd);

        // d. Gửi kết quả qua submitDataStringArray
        String submitBody =
            "<tns:submitDataStringArray>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "<data>EVEN=" + even + "</data>" +
            "<data>ODD=" + odd + "</data>" +
            "</tns:submitDataStringArray>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // e. Kết thúc chương trình
    }
}
