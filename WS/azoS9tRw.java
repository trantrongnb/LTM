import java.io.*;
import java.net.*;
import java.util.*;

public class azoS9tRw {

    static final String ENDPOINT = "http://36.50.135.242:2221/ObjectService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "azoS9tRw";

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

    // Trích nội dung giữa 2 tag XML
    static String extractTag(String xml, String tag) {
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int s = xml.indexOf(open);
        int e = xml.indexOf(close);
        if (s < 0 || e < 0) return "";
        return xml.substring(s + open.length(), e);
    }

    public static void main(String[] args) throws Exception {

        // a. Gọi requestListEmployeeY
        String getBody =
            "<tns:requestListEmployeeY>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:requestListEmployeeY>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse danh sách <return> (mỗi return chứa <name> và <startDate>)
        // Lưu dưới dạng String[] {name, startDate} để dễ sắp xếp và gửi lại
        List<String[]> employees = new ArrayList<>();
        String temp = response;
        while (temp.contains("<return>")) {
            int s = temp.indexOf("<return>") + 8;
            int e = temp.indexOf("</return>", s);
            String block = temp.substring(s, e);
            String name      = extractTag(block, "name");
            String startDate = extractTag(block, "startDate");
            employees.add(new String[]{name, startDate});
            System.out.println("  name=" + name + ", startDate=" + startDate);
            temp = temp.substring(e + 9);
        }

        // b. Sắp xếp theo startDate tăng dần (thâm niên cao nhất đứng đầu)
        // startDate dạng ISO-8601 (xs:dateTime) có thể so sánh lexicographically khi có format đồng nhất
        // Dùng stable sort để giữ nguyên thứ tự ban đầu nếu cùng startDate
        employees.sort(Comparator.comparing(e2 -> e2[1])); // sort theo startDate (String ISO-8601)

        System.out.println("Sorted:");
        for (String[] emp : employees) {
            System.out.println("  name=" + emp[0] + ", startDate=" + emp[1]);
        }

        // c. Gửi lại danh sách đã sắp xếp qua submitListEmployeeY
        StringBuilder dataXml = new StringBuilder();
        for (String[] emp : employees) {
            dataXml.append("<data>")
                   .append("<name>").append(emp[0]).append("</name>")
                   .append("<startDate>").append(emp[1]).append("</startDate>")
                   .append("</data>");
        }

        String submitBody =
            "<tns:submitListEmployeeY>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            dataXml +
            "</tns:submitListEmployeeY>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // d. Kết thúc chương trình
    }
}
