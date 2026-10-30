import java.io.*;
import java.net.*;
import java.util.*;

public class lFlvR2wW {

    static final String ENDPOINT = "http://36.50.135.242:2221/ObjectService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "1FlvR2wW";

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

    static String extractTag(String xml, String tag) {
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int s = xml.indexOf(open);
        int e = xml.indexOf(close);
        if (s < 0 || e < 0) return "";
        return xml.substring(s + open.length(), e);
    }

    public static void main(String[] args) throws Exception {

        // a. Gọi requestListCustomer để nhận danh sách Customer
        String getBody =
            "<tns:requestListCustomer>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:requestListCustomer>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse danh sách <return> (mỗi return chứa customerId, location, purchaseCount, totalSpent)
        // Lưu dưới dạng String[] {customerId, location, purchaseCount, totalSpent}
        List<String[]> allCustomers  = new ArrayList<>();
        List<String[]> filteredCustomers = new ArrayList<>();

        String temp = response;
        while (temp.contains("<return>")) {
            int s = temp.indexOf("<return>") + 8;
            int e = temp.indexOf("</return>", s);
            String block = temp.substring(s, e);

            String customerId    = extractTag(block, "customerId");
            String location      = extractTag(block, "location");
            String purchaseCount = extractTag(block, "purchaseCount");
            String totalSpent    = extractTag(block, "totalSpent");

            allCustomers.add(new String[]{customerId, location, purchaseCount, totalSpent});
            System.out.println("  id=" + customerId + ", location=" + location
                    + ", purchaseCount=" + purchaseCount + ", totalSpent=" + totalSpent);
            temp = temp.substring(e + 9);
        }

        // b. Lọc: totalSpent > 5000 VÀ purchaseCount >= 5
        for (String[] c : allCustomers) {
            float totalSpent  = Float.parseFloat(c[3]);
            int   purchaseCount = Integer.parseInt(c[2]);
            if (totalSpent > 5000 && purchaseCount >= 5) {
                filteredCustomers.add(c);
            }
        }

        System.out.println("Filtered (" + filteredCustomers.size() + " customers):");
        for (String[] c : filteredCustomers) {
            System.out.println("  id=" + c[0] + ", purchaseCount=" + c[2] + ", totalSpent=" + c[3]);
        }

        // c. Gửi lại danh sách đã lọc qua submitListCustomer
        StringBuilder dataXml = new StringBuilder();
        for (String[] c : filteredCustomers) {
            dataXml.append("<data>")
                   .append("<customerId>").append(c[0]).append("</customerId>")
                   .append("<location>").append(c[1]).append("</location>")
                   .append("<purchaseCount>").append(c[2]).append("</purchaseCount>")
                   .append("<totalSpent>").append(c[3]).append("</totalSpent>")
                   .append("</data>");
        }

        String submitBody =
            "<tns:submitListCustomer>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            dataXml +
            "</tns:submitListCustomer>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // d. Kết thúc chương trình
    }
}
