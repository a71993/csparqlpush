package csparql.streamer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import javax.swing.event.ListSelectionEvent;

import org.apache.jena.atlas.lib.ListUtils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

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
//				List<File> files = new ArrayList<>();
//				for (File f : listOfFiles){
//					files.add(f);
//				}
//				Collections.sort(files);
				for (File file : listOfFiles) {
					if (keepRunning) {
						Model modelFromDir = ModelFactory.createDefaultModel();
						String path = file.getPath();
						String absolutePath = file.getAbsolutePath();
						System.out.println("path: " + path);
						System.out.println("absolute path: " + absolutePath);
						try {
							String canonicalPath = file.getCanonicalPath();
							System.out.println("canonical path: " + canonicalPath);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						modelFromDir.read(path);
						csparqlAPI.feedStream(streamName, modelFromDir);
						Thread.sleep(sleepTime);
					}
				}
			} else {
				Model modelFromSingleFile = ModelFactory.createDefaultModel();
				modelFromSingleFile.read(filePath);
				csparqlAPI.feedStream(streamName, modelFromSingleFile);
			}
		} catch (InterruptedException e) {
			logger.error("Error while launching the sleep operation", e);
		} catch (StreamErrorException e) {
			logger.error("StreamErrorException Occurred", e);
		} catch (ServerErrorException e) {
			logger.error("ServerErrorException Occurred", e);
		} catch (NoSuchElementException e) {
			System.out.println("Exception: " + e);
		} catch (JenaException e) {
			System.out.println("JenaException: " + e);
		}

	}

}
