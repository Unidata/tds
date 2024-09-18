package thredds.util.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.springframework.mock.web.MockHttpServletResponse;

public class XmlUtil {

  private XmlUtil() {}

  public static Document getStringResponseAsDoc(MockHttpServletResponse response) throws JDOMException, IOException {
    return getStringResponseAsDoc(response.getContentAsByteArray());
  }

  public static Document getStringResponseAsDoc(byte[] response) throws JDOMException, IOException {
    SAXBuilder sb = new SAXBuilder();
    sb.setExpandEntities(false);
    return sb.build(new ByteArrayInputStream(response));
  }

  public static List<Element> evaluateXPath(Document doc, String strXpath) {
    try {
      XPathExpression<Element> xpath = XPathFactory.instance().compile(strXpath, Filters.element());
      return xpath.evaluate(doc);
    } catch (IllegalStateException e) {
      e.printStackTrace();
    }
    return null;
  }

}
