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
 ******************************************************************************/
package csparql.results_manipulator;

import java.io.StringReader;

import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import csparql.json_dataset_deserialization.List_of_Sparql_json_results_oracle;
import csparql.json_dataset_deserialization.Sparql_json_results_oracle;
import csparql.utilities.Utilities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class ResultsManipulator_Oracle  extends ServerResource {

	private Logger logger = LoggerFactory.getLogger(ResultsManipulator_Oracle.class.getName());
	private List_of_Sparql_json_results_oracle json_results_list;
	
	private final static String QUEUE_NAME = "csparqlstream";
	
	@Post
	public void getData(Representation entity)throws java.io.IOException{

		long actualTS = System.currentTimeMillis();
		json_results_list = (List_of_Sparql_json_results_oracle) getContext().getAttributes().get("json_results_list");

	    ConnectionFactory factory = new ConnectionFactory();
	    factory.setHost("localhost");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();
	    
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		//		Gson gson = new Gson();
		String results = new String();

		try {
			results = entity.getText();
			System.out.println();
			//In this example the results manipulator only prints received data on the console
			try{
				Model m = ModelFactory.createDefaultModel();
				m.read(new StringReader(results), null);
				m.write(System.out);
			} catch(Exception e){

				Sparql_json_results_oracle deserialized_results = gson.fromJson(results, Sparql_json_results_oracle.class);
//				System.out.println(results);
				
			    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
			    channel.basicPublish("", QUEUE_NAME, null, results.getBytes());
			    System.out.println(" [x] Sent '" + results + "'");
			    
				deserialized_results.setTimestamp(actualTS - json_results_list.getStartTS());
				json_results_list.add(deserialized_results);
				Utilities.writeJsonToFile("results.json", gson, json_results_list);
				getContext().getAttributes().put("json_results_list", json_results_list);
				
				System.out.println(gson.toJson(json_results_list));
//				json_results_list.printSerializationOnConsole();
//				deserialized_results.setTimestamp(System.currentTimeMillis());
//				deserialized_results.printSerializationOnConsole();
			}

			this.getResponse().setStatus(Status.SUCCESS_OK, "Result succesfully received");
			this.getResponse().setEntity(gson.toJson("Result succesfully received"), MediaType.APPLICATION_JSON);

		} catch(org.apache.jena.riot.RiotException e){
			System.out.println(results);
			System.out.println();		
			this.getResponse().setStatus(Status.SUCCESS_OK, "Result succesfully received");
			this.getResponse().setEntity(gson.toJson("Result succesfully received"), MediaType.APPLICATION_JSON);
		} catch(com.hp.hpl.jena.shared.InvalidPropertyURIException e){
			System.out.println(results);
			System.out.println();		
			this.getResponse().setStatus(Status.SUCCESS_OK, "Result succesfully received");
			this.getResponse().setEntity(gson.toJson("Result succesfully received"), MediaType.APPLICATION_JSON);
		} catch (Exception e) {
			logger.error("Error while decoding the results", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Error while receiving data");
			this.getResponse().setEntity(gson.toJson("Error while receiving data"), MediaType.APPLICATION_JSON);
		} finally {
		    channel.close();
		    connection.close();
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
