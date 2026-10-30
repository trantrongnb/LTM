import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class zzNkjpTz {
    static final String ENDPOINT = "http://36.50.135.242:2221/CharacterService";
    static final String NS = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT = "B23DCCN850";
    // "svg" trong ban sao de bai la ky hieu giao dien, khong thuoc ma cau hoi.
    static final String QCODE = "zzNkjpTz";

    static String escapeXml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    static Document soapCall(String body) throws Exception {
        String envelope = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\""
                + " xmlns:tns=\"" + NS + "\">"
                + "<soapenv:Body>" + body + "</soapenv:Body></soapenv:Envelope>";

        HttpURLConnection connection = (HttpURLConnection) URI.create(ENDPOINT)
                .toURL().openConnection();
        try {
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
            connection.setRequestProperty("SOAPAction", "\"\"");

            try (OutputStream output = connection.getOutputStream()) {
                output.write(envelope.getBytes(StandardCharsets.UTF_8));
            }

            int status = connection.getResponseCode();
            InputStream stream = status < 400
                    ? connection.getInputStream() : connection.getErrorStream();
            if (stream == null) {
                throw new IOException("Server tra ve HTTP " + status + ", khong co noi dung.");
            }

            Document response;
            try (InputStream input = stream) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                response = factory.newDocumentBuilder().parse(input);
            }

            NodeList faults = response.getElementsByTagNameNS(
                    "http://schemas.xmlsoap.org/soap/envelope/", "Fault");
            if (faults.getLength() > 0) {
                throw new IOException("SOAP Fault: " + faults.item(0).getTextContent());
            }
            if (status >= 400) {
                throw new IOException("Server tra ve HTTP " + status);
            }
            return response;
        } finally {
            // Dong ket noi sau moi loi goi, ke ca khi gap loi.
            connection.disconnect();
        }
    }

    public static void main(String[] args) throws Exception {
        // Co the truyen ma sinh vien va ma cau hoi tu dong lenh.
        String studentCode = args.length > 0 ? args[0] : STUDENT;
        String qCode = args.length > 1 ? args[1] : QCODE;
        String parameters = "<studentCode>" + escapeXml(studentCode) + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        // 1. Nhan chuoi tu requestString.
        Document response = soapCall("<tns:requestString>" + parameters
                + "</tns:requestString>");
        NodeList messages = response.getElementsByTagNameNS(NS, "requestStringResponse");
        if (messages.getLength() != 1) {
            throw new IOException("Phan hoi khong chua requestStringResponse.");
        }
        NodeList values = ((org.w3c.dom.Element) messages.item(0))
                .getElementsByTagNameNS("*", "return");
        if (values.getLength() != 1) {
            throw new IOException("Server khong tra ve chuoi can dao nguoc.");
        }
        String original = values.item(0).getTextContent();
        System.out.println("Chuoi nhan: " + original);

        // 2. Dao nguoc chuoi, giu nguyen khoang trang va ky tu dac biet.
        String reversed = new StringBuilder(original).reverse().toString();
        System.out.println("Chuoi dao nguoc: " + reversed);

        // 3. Schema quy dinh tham so ket qua co ten la data.
        Document submitted = soapCall("<tns:submitString>" + parameters
                + "<data>" + escapeXml(reversed) + "</data>"
                + "</tns:submitString>");
        NodeList receipts = submitted.getElementsByTagNameNS(NS, "submitStringResponse");
        if (receipts.getLength() != 1) {
            throw new IOException("Phan hoi khong chua submitStringResponse.");
        }
        NodeList statuses = ((org.w3c.dom.Element) receipts.item(0))
                .getElementsByTagNameNS("*", "return");
        if (statuses.getLength() > 0) {
            System.out.println("Status: " + statuses.item(0).getTextContent());
        } else {
            // WSDL hien tai khai bao submitString tra ve void.
            System.out.println("Da gui ket qua. Server phan hoi thanh cong, khong tra status AC/WA.");
        }
        // 4. Ket thuc client; stream va ket noi da dong trong soapCall.
    }
}
