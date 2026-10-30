import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class nLESTUj7 {
    static final String ENDPOINT = "http://36.50.135.242:2221/ObjectService";
    static final String NS = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT = "B23DCCN850";
    // "svg" trong ban sao de bai la ky hieu giao dien, khong thuoc ma cau hoi.
    static final String QCODE = "nLESTUj7";

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

    static org.w3c.dom.Element requiredElement(org.w3c.dom.Element parent, String name)
            throws IOException {
        NodeList elements = parent.getElementsByTagNameNS("*", name);
        if (elements.getLength() != 1) {
            throw new IOException("Phan hoi thieu hoac trung truong: " + name);
        }
        return (org.w3c.dom.Element) elements.item(0);
    }

    static class ProductY {
        float discount;
        float finalPrice;
        float price;
        float taxRate;

        static ProductY fromXml(org.w3c.dom.Element element) throws IOException {
            ProductY product = new ProductY();
            product.discount = Float.parseFloat(requiredElement(element, "discount").getTextContent());
            product.finalPrice = Float.parseFloat(requiredElement(element, "finalPrice").getTextContent());
            product.price = Float.parseFloat(requiredElement(element, "price").getTextContent());
            product.taxRate = Float.parseFloat(requiredElement(element, "taxRate").getTextContent());
            return product;
        }

        void calculateFinalPrice() {
            double calculated = price * (1 + taxRate / 100.0) * (1 - discount / 100.0);
            // Lam tron 2 chu so thap phan theo vi du: 794.948 -> 794.95.
            finalPrice = Math.round(calculated * 100.0) / 100.0f;
        }

        String toXml() {
            // Thu tu cac truong phai khop xs:sequence trong WSDL.
            return "<data><discount>" + discount + "</discount>"
                    + "<finalPrice>" + finalPrice + "</finalPrice>"
                    + "<price>" + price + "</price>"
                    + "<taxRate>" + taxRate + "</taxRate></data>";
        }

        @Override
        public String toString() {
            return "ProductY{price=" + price + ", taxRate=" + taxRate
                    + ", discount=" + discount + ", finalPrice=" + finalPrice + "}";
        }
    }

    public static void main(String[] args) throws Exception {
        String studentCode = args.length > 0 ? args[0] : STUDENT;
        String qCode = args.length > 1 ? args[1] : QCODE;
        String parameters = "<studentCode>" + escapeXml(studentCode) + "</studentCode>"
                + "<qCode>" + escapeXml(qCode) + "</qCode>";

        // 1. Nhan ProductY. WSDL thuc te khong co thuoc tinh name.
        Document response = soapCall("<tns:requestProductY>" + parameters
                + "</tns:requestProductY>");
        NodeList messages = response.getElementsByTagNameNS(NS, "requestProductYResponse");
        if (messages.getLength() != 1) {
            throw new IOException("Phan hoi khong chua requestProductYResponse.");
        }
        org.w3c.dom.Element data = requiredElement(
                (org.w3c.dom.Element) messages.item(0), "return");
        ProductY product = ProductY.fromXml(data);
        System.out.println("Du lieu nhan: " + product);

        // 2. Tinh va gan finalPrice vao doi tuong.
        product.calculateFinalPrice();
        System.out.println("Du lieu nop: " + product);

        // 3. Gui lai ca doi tuong trong tham so data.
        Document submitted = soapCall("<tns:submitProductY>" + parameters
                + product.toXml() + "</tns:submitProductY>");
        NodeList receipts = submitted.getElementsByTagNameNS(NS, "submitProductYResponse");
        if (receipts.getLength() != 1) {
            throw new IOException("Phan hoi khong chua submitProductYResponse.");
        }
        NodeList statuses = ((org.w3c.dom.Element) receipts.item(0))
                .getElementsByTagNameNS("*", "return");
        if (statuses.getLength() > 0) {
            System.out.println("Status: " + statuses.item(0).getTextContent());
        } else {
            System.out.println("Da gui ket qua. Server phan hoi thanh cong, khong tra status AC/WA.");
        }
        // 4. Ket thuc client; stream va ket noi da dong trong soapCall.
    }
}
