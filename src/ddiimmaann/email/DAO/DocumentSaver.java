package ddiimmaann.email.DAO;

import ddiimmaann.email.InvalidDatumException;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

class DocumentSaver
{
    static void writeDocument (Document doc, String fileName) throws IOException,
            InvalidDatumException
    {
        try
        {
            FileWriter fout = new FileWriter("data" + File.separator + fileName);
            TransformerFactory transFact = TransformerFactory.newInstance();
            Transformer trans = transFact.newTransformer();
            StreamResult result = new StreamResult(fout);
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            trans.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            trans.transform(new DOMSource(doc), result);
            fout.close();
        }
        catch (TransformerException e)
        {
            throw new InvalidDatumException("Failed to save document " +
                    "data" + File.separator + fileName + ". Try again.", e);
        }
    }
    
    static Document loadDocument (String fileName) throws IOException,
            InvalidDatumException
    {
        Document doc;
        try
        {
            File infile = new File("data" + File.separator + fileName);
            if (infile.length() == 0)
                return null;
            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
            doc = docBuild.parse(infile);
        }
        catch (ParserConfigurationException | SAXException e)
        {
            throw new InvalidDatumException("Failed to load document. Check "
                    + "valid of " + "data" + File.separator + fileName + ".", e);
        }
        return doc;
    }
    
    static Document getNewDocument () throws InvalidDatumException
    {
        Document doc;
        try
        {
            DocumentBuilderFactory docBuildFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuild = docBuildFact.newDocumentBuilder();
            doc = docBuild.newDocument();
        }
        catch (ParserConfigurationException e)
        {
            throw new InvalidDatumException("Failed to create new document.", e);
        }
        return doc;
    }
}