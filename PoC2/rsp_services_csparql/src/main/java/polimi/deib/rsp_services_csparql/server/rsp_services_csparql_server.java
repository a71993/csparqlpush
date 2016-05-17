/*******************************************************************************
 * Copyright 2013 Marco Balduini, Emanuele Della Valle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Acknowledgements:
 *   
 * This work was partially supported by the European project LarKC (FP7-215535) 
 * and by the European project MODAClouds (FP7-318484)
 ******************************************************************************/
package polimi.deib.rsp_services_csparql.server;

import java.io.File;
import java.net.URL;
import java.util.Hashtable;

import org.apache.log4j.PropertyConfigurator;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.HttpClientHelper;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import polimi.deib.rsp_services_csparql.commons.Csparql_Engine;
import polimi.deib.rsp_services_csparql.commons.Csparql_Query;
import polimi.deib.rsp_services_csparql.commons.Csparql_RDF_Stream;
import polimi.deib.rsp_services_csparql.configuration.Config;
import polimi.deib.rsp_services_csparql.knowledge_base.Update;
import polimi.deib.rsp_services_csparql.observers.MultipleObserversDataServer;
import polimi.deib.rsp_services_csparql.observers.SingleObserverDataServer;
import polimi.deib.rsp_services_csparql.queries.MultipleQueriesDataServer;
import polimi.deib.rsp_services_csparql.queries.SingleQueryDataServer;
import polimi.deib.rsp_services_csparql.streams.MultipleStreamsDataServer;
import polimi.deib.rsp_services_csparql.streams.SingleStreamDataServer;

public class rsp_services_csparql_server extends Application{
	
    private static ConnectionFactory factory;
    private static Connection connection; 
    public static Channel channel;

	private static Component component;
	private static Csparql_Engine engine = null;
	private static Hashtable<String, Csparql_RDF_Stream> csparqlStreamTable = new Hashtable<String, Csparql_RDF_Stream>();
	private static Hashtable<String, Csparql_Query> csparqlQueryTable = new Hashtable<String, Csparql_Query>();

	private static Application resources;
	private static String resourcesPath;

	private static String propertiesFilePath = new String();

	private static Logger logger = LoggerFactory.getLogger(rsp_services_csparql_server.class.getName());

	public static void main(String[] args) throws Exception{

		try{
			PropertyConfigurator.configure(new URL("http://streamreasoning.org/configuration_files/rspCsparql/log4j.properties"));
		} catch(Exception e){
			PropertyConfigurator.configure("propertiesFiles/log4j.properties");
		}

		if(args.length > 0){
			propertiesFilePath = args[0];
		} else {
			propertiesFilePath = "http://streamreasoning.org/configuration_files/rspCsparql/setup.properties";
		}

		Config.initialize(propertiesFilePath);

		engine = new Csparql_Engine();
		engine.initialize();

		resourcesPath = new File(Config.getInstance().getResourcesPath()).getAbsolutePath(); 
		logger.debug("Static resources path: {}", resourcesPath);

		if(System.getProperty("os.name").contains("Windows")){
			resources = new Application() {  
				@Override  
				public Restlet createInboundRoot() {  
					return new Directory(getContext(), "file:///" + resourcesPath + "/");  
				}  
			};  
		} else {
			resources = new Application() {  
				@Override  
				public Restlet createInboundRoot() {  
					return new Directory(getContext(), "file://" + resourcesPath + "/");  
				}  
			};  
		}
		
		

		component = new Component();
		component.getServers().add(Protocol.HTTP, Config.getInstance().getServerPort());
		component.getClients().add(Protocol.FILE);  

		rsp_services_csparql_server csparqlServer = new rsp_services_csparql_server();
		component.getDefaultHost().attach("", csparqlServer);

		component.getDefaultHost().attach(resources); 

		component.start();

	}

	public Restlet createInboundRoot(){

		String server_address = component.getServers().get(0).getAddress();
		if(server_address == null){
			server_address = "http://localhost";
			server_address = server_address + ":" + String.valueOf(component.getServers().get(0).getActualPort());
		}

		getContext().getAttributes().put("complete_server_address", server_address);
		getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
		getContext().getAttributes().put("csaprqlQueryTable", csparqlQueryTable);
		getContext().getAttributes().put("csparqlengine", engine);

		Router router = new Router(getContext());
		router.setDefaultMatchingMode(Template.MODE_EQUALS);

		router.attach("/demo", resources.createInboundRoot());
		router.attach("/streams", MultipleStreamsDataServer.class);
		router.attach("/streams/{streamname}", SingleStreamDataServer.class);
		router.attach("/queries", MultipleQueriesDataServer.class);
		router.attach("/queries/{queryname}", SingleQueryDataServer.class);
		router.attach("/queries/{queryname}/observers", MultipleObserversDataServer.class);
		router.attach("/queries/{queryname}/observers/{id}", SingleObserverDataServer.class);

		router.attach("/updatekb", Update.class);

		return router;
	}

}
