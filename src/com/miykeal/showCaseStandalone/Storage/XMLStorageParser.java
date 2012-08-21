package com.miykeal.showCaseStandalone.Storage;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.miykeal.showCaseStandalone.ShowCaseStandalone;
import com.miykeal.showCaseStandalone.ShopInternals.Storage;
import com.miykeal.showCaseStandalone.Utilities.Properties;

public class XMLStorageParser {
	private static final String nodeAttrType		= "type";
	private static final String nodeAttrTypeString	= "string";
	private static final String nodeAttrTypeDouble	= "double";
	private static final String nodeAttrTypeInteger	= "integer";
	private static final String nodeAttrTypeBoolean	= "boolean";
	private static final String nodeAttrTypeStorage	= "storage";
	
	private static final String nodeAttrStrgVersion	= "version";

	
	/**
	 * Ignoring wrong or broken nodes !!!
	 * @param node
	 * @return
	 */
	public static Storage nodeToStorage (Node node) {
		if (!node.hasChildNodes())
			return null;
		
		Storage		storage	= null;
		
		for (int i = 0; i < node.getAttributes().getLength(); i++) {
			Node attr	= node.getAttributes().item(i);
			
			if (nodeAttrStrgVersion.equalsIgnoreCase(attr.getNodeName())) {
				try {
					int version = Integer.parseInt(node.getNodeValue());
					storage		= new Storage(version);
					break;
				} catch (Exception e) {}
			}
			
		}
		
		// old storage
		if (storage == null)
			storage = new Storage(0);
		
		// debug
		if (Properties.saveDebug)
			ShowCaseStandalone.slog(Level.INFO, "<--- Utilities.nodeToShopStorage: Loading storage --->");
		
		for (int i = 0; i < node.getChildNodes().getLength(); i++) {
			try {
				
				Node 	child	= node.getChildNodes().item(i);
				Node 	attr	= child.getAttributes().getNamedItem(nodeAttrType);
				String	type	= attr.getNodeValue();
				
				String	name	= child.getNodeName();
				String	value	= child.getTextContent();
				
				// debug
				if (Properties.saveDebug)
					ShowCaseStandalone.slog(Level.INFO, String.format("%-20s=%-45s, type=%s", name, value, type));
				
				if (nodeAttrTypeDouble.equalsIgnoreCase(type))
					storage.setDouble(name, Double.parseDouble(value));
				
				if (nodeAttrTypeInteger.equalsIgnoreCase(type))
					storage.setInteger(name, Integer.parseInt(value));

				if (nodeAttrTypeBoolean.equalsIgnoreCase(type))
					storage.setBoolean(name, Boolean.parseBoolean(value));
				
				if (nodeAttrTypeString.equalsIgnoreCase(type))
					storage.setString(name, value);
				
				if (nodeAttrTypeStorage.equalsIgnoreCase(type))
					storage.setStorage(name, nodeToStorage(child));
				
				
			} catch (Exception e) {
				// don't care
			}
		}
		
		storage.resetHasChanged();
		
		// debug
		if (Properties.saveDebug)
			ShowCaseStandalone.slog(Level.INFO, "<--- Utilities.nodeToShopStorage: Loaded --->");
		
		return storage;
	}

	/**
	 * @param node
	 * @param p
	 */
	public static Node storageToNode (Document document, Storage storage, String storageName) {
		Element		node	= document.createElement(storageName);
					node.setAttribute(nodeAttrStrgVersion, ""+storage.getVersion());
		
		// Strings
		for (String key : storage.getStringKeys()) {
			Element child	= document.createElement(key);
					child.setAttribute(nodeAttrType, nodeAttrTypeString);
					child.setTextContent(storage.getString(key));
			node.appendChild(child);
		}
		
		// Doubles
		for (String key : storage.getDoubleKeys()) {
			Element	child	= document.createElement(key);
					child.setAttribute(nodeAttrType, nodeAttrTypeDouble);
					child.setTextContent("" + storage.getDouble(key));
			node.appendChild(child);
		}
		
		// Integers
		for (String key : storage.getIntegerKeys()) {
			Element	child	= document.createElement(key);
					child.setAttribute(nodeAttrType, nodeAttrTypeInteger);
					child.setTextContent("" + storage.getInteger(key));
			node.appendChild(child);
		}
		
		// Booleans
		for (String key : storage.getBooleanKeys()) {
			Element	child	= document.createElement(key);
					child.setAttribute(nodeAttrType, nodeAttrTypeBoolean);
					child.setTextContent("" + storage.getBoolean(key));
			node.appendChild(child);
		}
		
		// Storages
		for (String key : storage.getStorageKeys()) {
			Storage	stor	= storage.getStorage(key);
			
			if (stor == null)
				continue;
			
			Element	child	= (Element)storageToNode(document, stor, key);
					child.setAttribute(nodeAttrType, 		nodeAttrTypeStorage);
					child.setAttribute(nodeAttrStrgVersion, ""+stor.getVersion());
					
					
			node.appendChild(child);
		}
		
		return node;
	}
	
	/**
	 * @param file
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 */
	public static Document parseDocument (File file) throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory 	dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder 		db 	= dbf.newDocumentBuilder();
		Document 				doc = db.parse(file);
								doc.getDocumentElement().normalize();
		return doc;
	}
	
	/**
	 * @return
	 * @throws ParserConfigurationException
	 */
	public static Document createDocument () throws ParserConfigurationException {
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}
	
	/**
	 * @param document
	 * @return
	 * @throws TransformerException
	 */
	public static String transform (Document document) throws TransformerException {
		TransformerFactory 	transformerFactory	= TransformerFactory.newInstance();
							transformerFactory.setAttribute			("indent-number", new Integer(4));
		
		Transformer			transformer			= transformerFactory.newTransformer();
							transformer.setOutputProperty			(OutputKeys.INDENT, 				"yes");
							
		StreamResult 		result 				= new StreamResult	(new StringWriter()	);
		DOMSource 			source 				= new DOMSource		(document			);
							transformer.transform(source, result);

		return result.getWriter().toString();
	}
}
