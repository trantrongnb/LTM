import java.io.*;
import java.net.*;
import java.util.*;

public class CuuHSc1s {

    static final String ENDPOINT = "http://36.50.135.242:2221/DataService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "CuuHSc1s";

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

    // Trích xuất nội dung giữa 2 thẻ XML
    static String extractTag(String xml, String tag) {
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int s = xml.indexOf(open);
        int e = xml.indexOf(close, s);
        if (s < 0 || e < 0) return "";
        return xml.substring(s + open.length(), e);
    }

    // c. Tìm số lớn nhất bằng cách sắp xếp theo comparator ab vs ba
    static String largestNumber(List<String> nums) {
        nums.sort((a, b) -> (b + a).compareTo(a + b));
        // Nếu tất cả đều là "0"
        if (nums.get(0).equals("0")) return "0";
        StringBuilder sb = new StringBuilder();
        for (String s : nums) sb.append(s);
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        // a. Gọi getData để nhận danh sách số nguyên
        String getBody =
            "<tns:getData>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:getData>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse các phần tử <return> trong response
        List<String> numbers = new ArrayList<>();
        String temp = response;
        while (temp.contains("<return>")) {
            String val = extractTag(temp, "return");
            numbers.add(val.trim());
            temp = temp.substring(temp.indexOf("</return>") + 9);
        }
        System.out.println("Numbers: " + numbers);

        // b. Tìm số lớn nhất có thể tạo được
        String largest = largestNumber(numbers);
        System.out.println("Largest: " + largest);

        // c. Gọi submitDataString để gửi kết quả
        String submitBody =
            "<tns:submitDataString>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "<data>" + largest + "</data>" +
            "</tns:submitDataString>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // d. Kết thúc chương trình
    }
}
