package demo.model;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URI;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by ws on 26.09.15.
 */
public class CBRInfo extends DefaultHandler {

    private static String BASE_URL = "http://www.cbr.ru/scripts/XML_daily.asp";
    private String currency;
    private double value;

    public CBRInfo(String currency) {
        this.currency = currency;
    }

    private static ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

    public static String streamToString(final InputStream inputStream) throws Exception {
        try (
            final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            return br.lines().parallel().collect(Collectors.joining("\n"));
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Info get(Info info) throws Exception {

        String url = BASE_URL + "?date_req=" + info.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String xml = "";
        if (cache.containsKey(url)) xml = cache.get(url);
        else {
            System.out.println(url);
            ClientHttpRequest request = new SimpleClientHttpRequestFactory().createRequest(new URI(url), HttpMethod.GET);
            InputStream stream = request.execute().getBody();
            xml = streamToString(stream);
            cache.put(url, xml);
        }

        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParser saxParser = spf.newSAXParser();
        XMLReader reader = saxParser.getXMLReader();

        CBRInfo cbr = new CBRInfo(info.code);
        reader.setContentHandler(cbr);
        reader.parse(new InputSource(new StringReader(xml)));
        info.rate = cbr.value;

        return info;
    }

    String currentCode;
    double currentValue;

    enum Element { CODE, VALUE, OTHER }

    Element current = Element.OTHER;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "CharCode": current = Element.CODE; break;
            case "Value": current = Element.VALUE; break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        switch (current) {
            case CODE:
                currentCode = new String(Arrays.copyOfRange(ch, start, start + length));
                break;

            case VALUE:
                String text = new String(Arrays.copyOfRange(ch, start, start + length));
                text = text.replaceAll(",", ".");
                text = text.replaceAll("\\s+", "");
                text = text.replaceAll("\u00A0", "");
                currentValue = Double.parseDouble(text);
                break;
        }
        current = Element.OTHER;
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("Valute".equals(qName)) {
            if (currentCode.toUpperCase().equals(currency.toUpperCase())) {
                value = currentValue;
            }
        }
    }
}
