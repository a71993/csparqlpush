package csparql;


import csparql.json_dataset_deserialization.RDFTriple;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import org.apache.commons.lang.StringUtils;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

//import com.rabbitmq.client.Channel;
//import com.rabbitmq.client.Connection;
//import com.rabbitmq.client.ConnectionFactory;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;
import csparql.configuration.Config;
import csparql.json_dataset_deserialization.List_of_Sparql_json_results_oracle;
import csparql.results_manipulator.ResultsManipulator_Oracle;
import csparql.streamer.BaseStreamer;
import csparql.streamer.FromFileStreamer;

public class Client_Server_Provider extends Application {
	
	private static Component component;
	private static Hashtable<String, String> queryProxyIdTable = new Hashtable<String, String>();
	private static Logger logger = LoggerFactory.getLogger(Client_Server_Provider.class.getName());

	private static int ID = 0;
	
	private static List_of_Sparql_json_results_oracle json_results_list = new List_of_Sparql_json_results_oracle();
	
	private static String csparqlServerAddress = "http://localhost:8175";
	
//	private final static String QUEUE_NAME = "csparqlstream";
	
	@SuppressWarnings("unused")	
	public static void main(String[] args) throws Exception {
		
		boolean startExample = true;
		
		final int SINGLE_CONSTRUCT_QUERY_SINGLE_OBSERVER = 0;
		
		int key = SINGLE_CONSTRUCT_QUERY_SINGLE_OBSERVER;
		
		String actual_client_address;
		int actual_client_port;

		component = new Component();
		component.getServers().add(Protocol.HTTP, Config.getInstance().getServerPort());
		component.getClients().add(Protocol.FILE);  

		Client_Server_Provider csparqlServer = new Client_Server_Provider();
		component.getDefaultHost().attach("", csparqlServer);

		component.start();
	
		
		System.out.println(args[0]);
	
		
		if (startExample) {
			
			actual_client_address = component.getServers().get(0).getAddress();
			if(actual_client_address == null)
				actual_client_address = "http://localhost";
			actual_client_port = component.getServers().get(0).getActualPort();

			String query;
			String inputstreamName = null;
			String streamName;
			String queryURI;

			String generalIRI = "http://ex.org/";
			
			RSP_services_csparql_API csparqlAPI = new RSP_services_csparql_API(csparqlServerAddress);
			
			switch(key){
			case SINGLE_CONSTRUCT_QUERY_SINGLE_OBSERVER:
				try{
					
					inputstreamName = generalIRI + "rabbit";
					
					String streamsInfo = csparqlAPI.getStreamsInfo();
					System.out.println(streamsInfo);

					if(!streamsInfo.contains(inputstreamName)) {
						csparqlAPI.registerStream(inputstreamName);
					}
					
					
					Thread.sleep(30000);
					
					
//					query = "REGISTER STREAM RabbitWorld AS " +
//							"CONSTRUCT { ?s ?p ?o } " +
//							"FROM STREAM <" + inputstreamName + "> [RANGE 2s STEP 2s] " +
//							"WHERE { ?s ?p ?o }";
//					
//					queryURI = csparqlAPI.registerQuery("RabbitWorld", query);
					
					json_results_list.setStartTS(System.currentTimeMillis());
					
//					Client_Server_Provider.queryProxyIdTable.put(query, queryURI);
					
//					String obsURI = csparqlAPI.addObserver(queryURI, actual_client_address + ":" + actual_client_port + "/results");
					

					BaseStreamer demostream;
					
					if(args.length > 0) {
						demostream = new FromFileStreamer(csparqlAPI, inputstreamName, 3000, generalIRI, args[0]);
					}else {
						
						demostream = new BaseStreamer(csparqlAPI, inputstreamName, 3000, generalIRI);
					}
					new Thread(demostream).start();
					

					
					System.out.println(csparqlAPI.getStreamInfo(inputstreamName));

					
					Thread.sleep(600000);
//					
//					System.out.println(csparqlAPI.deleteObserver(obsURI));
//					System.out.println(csparqlAPI.unregisterQuery(queryURI));
					demostream.stopStream();
				} catch (ServerErrorException e) {
					logger.error("rsp_server4csparql_server error", e);
				} catch (InterruptedException e) {
					logger.error("Error while launching the sleep operation", e);
				}
				break;
			default:
				System.exit(0);
				break;
			}

//			csparqlAPI.unregisterStream(inputstreamName);
		
		}
		
	}
	
	public Restlet createInboundRoot(){

		getContext().getAttributes().put("queryProxyIdTable", Client_Server_Provider.queryProxyIdTable);
		getContext().getAttributes().put("json_results_list", Client_Server_Provider.json_results_list);

		Router router = new Router(getContext());
		router.setDefaultMatchingMode(Template.MODE_EQUALS);

		router.attach("/results", ResultsManipulator_Oracle.class);

		return router;
	}
	
	public static int nextID() {
		return ID++;
	}

}
