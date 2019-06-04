package com.github.lucacampanella;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    public static List<String> parseXMLFile(String path) throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        List<String> res = new ArrayList<>();

        FileInputStream fileIS = new FileInputStream(new File(path));
        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = builderFactory.newDocumentBuilder();
        Document xmlDocument = builder.parse(fileIS);
        XPath xPath = XPathFactory.newInstance().newXPath();
        String expression = "//text";
        NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(xmlDocument, XPathConstants.NODESET);
        System.out.println(nodeList);
        System.out.println(nodeList.item(0));

        for(int i = 0; i < nodeList.getLength(); ++i) {
            res.add(nodeList.item(i).getFirstChild().getTextContent());
        }

        return res;
    }

    public static String fromClassSrcToPath(Class klass) {
        String path = klass.getCanonicalName();
        path = path.replace(".", System.getProperty("file.separator"));
        path = path + ".java";
        Path pathObj = Paths.get(System.getProperty("user.dir"), "src", "test", "java", path);
        return pathObj.toString();
    }
}
