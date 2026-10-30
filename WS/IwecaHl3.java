import java.io.*;
import java.net.*;
import java.util.*;

public class IwecaHl3 {

    static final String ENDPOINT = "http://36.50.135.242:2221/CharacterService";
    static final String NS       = "http://soap.server.network.judge.dblab.ptit/";
    static final String STUDENT  = "B23DCCN850";
    static final String QCODE    = "IwecaHl3";

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

    // Đếm số nguyên âm trong một từ
    static int countVowels(String word) {
        int count = 0;
        for (char c : word.toLowerCase().toCharArray()) {
            if ("aeiou".indexOf(c) >= 0) count++;
        }
        return count;
    }

    public static void main(String[] args) throws Exception {

        // a. Gọi requestStringArray để nhận danh sách chuỗi
        String getBody =
            "<tns:requestStringArray>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            "</tns:requestStringArray>";

        String response = soapCall(getBody);
        System.out.println("Response: " + response);

        // Parse các phần tử <return>
        List<String> words = new ArrayList<>();
        String temp = response;
        while (temp.contains("<return>")) {
            int s = temp.indexOf("<return>") + 8;
            int e = temp.indexOf("</return>", s);
            words.add(temp.substring(s, e).trim());
            temp = temp.substring(e + 9);
        }
        System.out.println("Words: " + words);

        // b. Phân loại từ theo số lượng nguyên âm
        // Dùng TreeMap để tự sắp xếp các nhóm theo số nguyên âm tăng dần
        Map<Integer, List<String>> groups = new TreeMap<>();
        for (String word : words) {
            int vowelCount = countVowels(word);
            groups.computeIfAbsent(vowelCount, k -> new ArrayList<>()).add(word);
        }

        // Sắp xếp từ theo thứ tự từ điển trong mỗi nhóm
        // Kết quả là danh sách phẳng (flat) từng từ riêng lẻ, thứ tự: nhóm tăng dần → từ điển trong nhóm
        List<String> result = new ArrayList<>();
        for (Map.Entry<Integer, List<String>> entry : groups.entrySet()) {
            List<String> group = entry.getValue();
            Collections.sort(group);
            System.out.println("Vowels=" + entry.getKey() + ": " + group);
            result.addAll(group); // Thêm từng từ riêng lẻ, không ghép thành 1 chuỗi
        }

        System.out.println("Result (flat words): " + result);

        // c. Gọi submitCharacterStringArray để gửi kết quả
        StringBuilder dataXml = new StringBuilder();
        for (String s : result) {
            dataXml.append("<data>").append(s).append("</data>");
        }

        String submitBody =
            "<tns:submitCharacterStringArray>" +
            "<studentCode>" + STUDENT + "</studentCode>" +
            "<qCode>" + QCODE + "</qCode>" +
            dataXml +
            "</tns:submitCharacterStringArray>";

        String submitResponse = soapCall(submitBody);
        System.out.println("Submit response: " + submitResponse);

        // d. Kết thúc chương trình
    }
}
