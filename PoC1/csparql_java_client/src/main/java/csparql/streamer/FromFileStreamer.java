package csparql.streamer;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.SequenceInputStream;
import java.io.StringBufferInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.ListSelectionEvent;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.io.IOUtils;
import org.apache.jena.atlas.lib.ListUtils;
import org.apache.xerces.jaxp.SAXParserFactoryImpl;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.TDBLoader;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;
import polimi.deib.csparql_rest_api.exception.StreamErrorException;

public class FromFileStreamer extends BaseStreamer {

	protected String filePath;

	public FromFileStreamer(RSP_services_csparql_API csparqlAPI,
			String streamName, long sleepTime, String generalIRI,
			String filePath) {
		super(csparqlAPI, streamName, sleepTime, generalIRI);
		this.filePath = filePath;
	}

	@Override
	public void run() {
		try {
			File fileEntry = new File(filePath);
			if (fileEntry.isDirectory()) {
				File[] listOfFiles = fileEntry.listFiles();
				for (File file : listOfFiles) {
					if (keepRunning) {
						Model modelFromDir = ModelFactory.createDefaultModel();
						String path = file.getPath();
						String absolutePath = file.getAbsolutePath();
						System.out.println("absolute path: " + absolutePath);
						modelFromDir.read(path);
						csparqlAPI.feedStream(streamName, modelFromDir);
						Thread.sleep(sleepTime);
					}
				}
			} else {
//				final SAXParserFactory saxParserFactory = SAXParserFactory
//						.newInstance();
//				SAXParser saxParser = saxParserFactory.newSAXParser();
//
//				XMLReader xmlReader = saxParser.getXMLReader();
//				// Trying to add root element
//				Enumeration<InputStream> streams = Collections
//						.enumeration(Arrays.asList(new InputStream[] {
//								new ByteArrayInputStream(
//										"<?xml version=\"1.0\" encoding=\"utf-8\"?><RDFs>"
//												.getBytes()),
//								new FileInputStream(filePath),
//								new ByteArrayInputStream("</RDFs>".getBytes()), }));
//				SequenceInputStream sequenceInputStream = new SequenceInputStream(
//						streams);
//				saxParser.parse(sequenceInputStream, new DefaultHandler());

				// Make a TDB-backed dataset
//				String directory = "TDB";
//				Dataset dataset = TDBFactory.createDataset(directory);
//				dataset.begin(ReadWrite.WRITE);
//				Model model = dataset.getDefaultModel();
//				TDBLoader.loadModel(model, filePath);
//				dataset.end();
//
//				dataset.begin(ReadWrite.READ);
//				model = dataset.getDefaultModel();
//				csparqlAPI.feedStream(streamName, model);
//				dataset.end();

				// XMLInputFactory xif = XMLInputFactory.newInstance();
				// XMLStreamReader xsr = xif.createXMLStreamReader(new
				// FileReader(
				// filePath));
				// xsr.nextTag(); // Advance to statements element
				//
				// TransformerFactory tf = TransformerFactory.newInstance();
				// Transformer t = tf.newTransformer();
				// while (xsr.nextTag() == XMLStreamConstants.START_ELEMENT
				// && keepRunning) {
				// DOMResult result = new DOMResult();
				// t.transform(new StAXSource(xsr), result);
				// Node node = result.getNode();
				// // Model modelFromSingleFile =
				// ModelFactory.createDefaultModel();
				// // modelFromSingleFile.read("");
				// System.out.println(node.toString());
				// System.out.println(node.getTextContent());
				// if (node.getTextContent() == null) {
				// continue;
				// }
				// }
				
				FileInputStream fis = new FileInputStream(new File(filePath));
				 
				//Construct BufferedReader from InputStreamReader
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			 
				String line = "";
				String rdf = "";
				int nr = 0;
				while ((line = br.readLine()) != null && keepRunning) {
					rdf += line + "\n";
					if (line.contains("</rdf:RDF>")) {
						nr += 1;
						System.out.println(rdf);
//						 Model modelFromSingleFile = ModelFactory.createDefaultModel();
						 ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(rdf.getBytes(StandardCharsets.UTF_8));
//						modelFromSingleFile.read(byteArrayInputStream, null);
//						 csparqlAPI.feedStream(streamName, modelFromSingleFile);
						 Files.copy(byteArrayInputStream, new File("rdf3/rdf" + nr + ".rdf").toPath());
//						 modelFromSingleFile.write(new FileOutputStream("rdf3/rdf" + nr + ".rdf"));
						 rdf = "";
//						 Thread.sleep(10000);
					}
				}
			 
				br.close();
				
				// Model modelFromSingleFile =
				// ModelFactory.createDefaultModel();
				// modelFromSingleFile.read(sequenceInputStream, "");
				// csparqlAPI.feedStream(streamName, modelFromSingleFile);
			}
		} catch (InterruptedException e) {
			logger.error("Error while launching the sleep operation", e);
			e.printStackTrace();
		} catch (StreamErrorException e) {
			logger.error("StreamErrorException Occurred", e);
			e.printStackTrace();
		} catch (ServerErrorException e) {
			logger.error("ServerErrorException Occurred", e);
			e.printStackTrace();
		} catch (NoSuchElementException e) {
			System.out.println("Exception: " + e);
			e.printStackTrace();
		} catch (JenaException e) {
			System.out.println("JenaException: " + e);
			e.printStackTrace();
			// } catch (TransformerConfigurationException e) {
			// System.out.println("TransformerConfigurationException: " + e);
			// e.printStackTrace();
			// } catch (TransformerException e) {
			// System.out.println("TransformerException: " + e);
			// e.printStackTrace();
			// } catch (XMLStreamException e) {
			// System.out.println("XMLStreamException: " + e);
			// e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException: " + e);
			e.printStackTrace();
//		} catch (ParserConfigurationException | SAXException e2) {
//			e2.printStackTrace();
		}
	}

}
