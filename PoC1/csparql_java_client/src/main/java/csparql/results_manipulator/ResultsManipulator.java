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

import com.google.gson.Gson;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ResultsManipulator  extends ServerResource {

	private Logger logger = LoggerFactory.getLogger(ResultsManipulator.class.getName());

	@Post
	public void getData(Representation entity){

		Gson gson = new Gson();
		String results = new String();
		try {
			results = entity.getText();

			//In this example the results manipulator only prints received data on the console
			try{
				Model m = ModelFactory.createDefaultModel();
				m.read(new StringReader(results), null);
				m.write(System.out);
			} catch(Exception e){
				System.out.println(results);
//				Sparql_json_results deserialized_results = gson.fromJson(results, Sparql_json_results.class);
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
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
