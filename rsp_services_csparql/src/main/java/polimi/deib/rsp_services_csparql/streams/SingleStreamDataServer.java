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
package polimi.deib.rsp_services_csparql.streams;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streamreasoning.rsp_services.commons.Rsp_services_Component_Status;

import polimi.deib.rsp_services_csparql.commons.Csparql_Engine;
import polimi.deib.rsp_services_csparql.commons.Csparql_RDF_Stream;
import polimi.deib.rsp_services_csparql.commons.Utilities;
import polimi.deib.rsp_services_csparql.streams.utilities.CsparqlStreamDescriptionForGet;
import polimi.deib.rsp_services_csparql.server.rsp_services_csparql_server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import eu.larkc.csparql.cep.api.RdfQuadruple;
import eu.larkc.csparql.cep.api.RdfStream;

public class SingleStreamDataServer extends ServerResource {

	private static Hashtable<String, Csparql_RDF_Stream> csparqlStreamTable;
	private Csparql_Engine engine;
	private Gson gson = new GsonBuilder().disableHtmlEscaping().create();
	private Logger logger = LoggerFactory.getLogger(SingleStreamDataServer.class.getName());

	@SuppressWarnings("unchecked")
	@Options
	public void optionsRequestHandler(){
		ClientInfo c = getRequest().getClientInfo();
		String origin = c.getAddress();
		Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
		if (responseHeaders == null) {
			responseHeaders = new Series<Header>(Header.class);
			getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
		}
		responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
		responseHeaders.add(new Header("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE"));

	}

	@SuppressWarnings({ "unchecked" })
	@Put
	public void registerStream(){

		try{

			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
			
			csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");
			System.out.println("inputstream: " + inputStreamName);

			if(!csparqlStreamTable.containsKey(inputStreamName)){
				
				RdfStream stream = new RdfStream(inputStreamName);
				
//				rsp_services_csparql_server.channel.exchangeDeclare(stream.getIRI(), "fanout");
				
			
				Csparql_RDF_Stream csparqlStream = new Csparql_RDF_Stream(stream, Rsp_services_Component_Status.RUNNING);
				csparqlStreamTable.put(inputStreamName, csparqlStream);
				engine.registerStream(stream);
				getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
				getContext().getAttributes().put("csparqlengine", engine);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully registered");
				this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully registered"), MediaType.APPLICATION_JSON);
				
//				PUT "http://guest:guest@localhost:15670/endpoint/x/" + stream;
				CloseableHttpClient httpclient = HttpClients.createDefault();
//				HttpPut httpPut = new HttpPut("http://guest:guest@localhost:15670/endpoint/x/amq." + URLEncoder.encode(inputStreamName, "UTF-8")  + "?amqp.exchange_type=topic");			
				HttpPut httpPut = new HttpPut("http://guest:guest@localhost:15670/endpoint/x/amq.direct");			
				CloseableHttpResponse response = httpclient.execute(httpPut);
				
		        try {
		            System.out.println(response.getStatusLine());
//		            HttpEntity entity = response.getEntity();
//			  		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
//			  		String output;
//			  		System.out.println("Trying to set up exchange.. \n Output from Server .... \n");
//			  		while ((output = br.readLine()) != null) {
//			  			System.out.println(output);
//			  		}
//		            EntityUtils.consume(entity);
		        } finally {
		            response.close();
		        }

			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " already exists");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " already exists"), MediaType.APPLICATION_JSON);
			}
		} catch(Exception e){
			logger.error(e.getMessage(), e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
			this.getResponse().setEntity(gson.toJson(e.getMessage()), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

	}

	@SuppressWarnings({ "unchecked" })
	@Delete
	public void unregisterStream(){
		try{

			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
			
			csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				RdfStream stream = csparqlStreamTable.get(inputStreamName).getStream();
				engine.unregisterStream(stream.getIRI());
				csparqlStreamTable.remove(inputStreamName);
				getContext().getAttributes().put("csaprqlinputStreamTable", csparqlStreamTable);
				getContext().getAttributes().put("csparqlengine", engine);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully unregistered");
				this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully unregistered"), MediaType.APPLICATION_JSON);
				
//				DELETE "http://guest:guest@localhost:15670/endpoint/x/" + queryname;
		        CloseableHttpClient httpclient = HttpClients.createDefault();
				HttpDelete httpDelete = new HttpDelete("http://guest:guest@localhost:15670/endpoint/x/"+inputStreamName);			
				try {
					CloseableHttpResponse response = httpclient.execute(httpDelete);
					System.out.println(response.getStatusLine());
					HttpEntity entity = response.getEntity();
					// do something useful with the response body
					// and ensure it is fully consumed
					EntityUtils.consume(entity);
					response.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " does not exist");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exist"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while unregistering stream", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}

	@SuppressWarnings({ "unchecked" })
	@Post
	public void feedStream(Representation rep){

		System.out.println("xxxxxxxxx  Feeding time!  xxxxxxxxx");
		try{

			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
			
			csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");
			engine = (Csparql_Engine) getContext().getAttributes().get("csparqlengine");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				Csparql_RDF_Stream streamRepresentation = csparqlStreamTable.get(inputStreamName);

				String jsonSerialization = rep.getText();

				Model model = ModelFactory.createDefaultModel();
				
//			    ConnectionFactory factory = new ConnectionFactory();
//			    factory.setHost("localhost");
//			    Connection connection = factory.newConnection();
//			    Channel channel = connection.createChannel();
			    
			    

				try{
					model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/JSON");
					long ts = System.currentTimeMillis();

					StmtIterator it = model.listStatements();
					while(it.hasNext()){
						Statement st = it.next();
						streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
						System.out.println(" [x] Sending '" + st + "'");
//					    channel.basicPublish("amq." + URLEncoder.encode(inputStreamName,"UTF-8"),"FOOWO" , null, st.getString().getBytes());
//					    System.out.println(" [x] Sent '" + st + "'");
					}

					this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
					this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
				} catch(Exception e){
					try{
						model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/XML");
						long ts = System.currentTimeMillis();

						StmtIterator it = model.listStatements();
						while(it.hasNext()){
							Statement st = it.next();
							streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
						}

						this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
					} catch(Exception e1){
						try{
							model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"N-TRIPLE");
							long ts = System.currentTimeMillis();

							StmtIterator it = model.listStatements();
							while(it.hasNext()){
								Statement st = it.next();
								streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
							}

							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
						} catch(Exception e2){
							model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"TURTLE");
							long ts = System.currentTimeMillis();

							StmtIterator it = model.listStatements();
							while(it.hasNext()){
								Statement st = it.next();
								streamRepresentation.feed_RDF_stream(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
							}

							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
						}
					}
				}

			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Specified stream does not exists");
				this.getResponse().setEntity(gson.toJson("Specified stream does not exists"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while changing status of a stream", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}

		//		try{
		//			csparqlStreamTable = (Hashtable<String, CsparqlStream>) getContext().getAttributes().get("csaprqlinputStreamTable");
		//			engine = (CsparqlEngine) getContext().getAttributes().get("csparqlengine");
		//
		//			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");
		//			Form f = new Form(rep);
		//			String action = f.getFirstValue("action");
		//
		//			if(csparqlStreamTable.containsKey(inputStreamName)){
		//				CsparqlStream streamRepresentation = csparqlStreamTable.get(inputStreamName);
		//				if(action.equals("feed")){
		//					
		//					String jsonSerialization = f.getFirstValue("jsonSerialization");
		//					Model model = ModelFactory.createDefaultModel();
		//					model.read(new ByteArrayInputStream(jsonSerialization.getBytes("UTF-8")),null,"RDF/JSON");
		//					long ts = System.currentTimeMillis();
		//					
		//					
		//					StmtIterator it = model.listStatements();
		//					while(it.hasNext()){
		//						Statement st = it.next();
		//						streamRepresentation.getStream().put(new RdfQuadruple(st.getSubject().toString(), st.getPredicate().toString(), st.getObject().toString(), ts));
		//					}
		//					
		//					this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " succesfully feeded");
		//					this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully feeded"), MediaType.APPLICATION_JSON);
		//				}
		//				else{
		//					if(action.equals("pause") && streamRepresentation.getStatus().equals(CsparqlComponentStatus.PAUSED)){
		//						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Stream " + inputStreamName + " already paused");
		//						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " already paused"), MediaType.APPLICATION_JSON);
		//					} else if(action.equals("restart") && streamRepresentation.getStatus().equals(CsparqlComponentStatus.RUNNING)){
		//						this.getResponse().setStatus(Status.CLIENT_ERROR_CONFLICT,"Stream " + inputStreamName + " already running");
		//						this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " already running"), MediaType.APPLICATION_JSON);
		//					} else {
		//						if(action.equals("pause")){
		//							streamRepresentation.setStatus(CsparqlComponentStatus.PAUSED);
		//							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " paused");
		//							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully paused"), MediaType.APPLICATION_JSON);
		//						} else if (action.equals("restart")){
		//							streamRepresentation.setStatus(CsparqlComponentStatus.RUNNING);
		//							this.getResponse().setStatus(Status.SUCCESS_OK,"Stream " + inputStreamName + " restarted");
		//							this.getResponse().setEntity(gson.toJson("Stream " + inputStreamName + " succesfully restarted"), MediaType.APPLICATION_JSON);
		//						} else {
		//							this.getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Input parameters error");
		//							this.getResponse().setEntity(gson.toJson("Input parameters error"), MediaType.APPLICATION_JSON);
		//						}
		//					}
		//				}
		//			} else {
		//				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Specified stream does not exists");
		//				this.getResponse().setEntity(gson.toJson("Specified stream does not exists"), MediaType.APPLICATION_JSON);
		//			}
		//
		//		} catch(Exception e){
		//			logger.error("Error while changing status of a stream", e);
		//			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,"Generic Error");
		//			this.getResponse().setEntity(gson.toJson("Generic Error"), MediaType.APPLICATION_JSON);
		//		} finally{
		//			this.getResponse().commit();
		//			this.commit();	
		//			this.release();
		//		}

	}

	@SuppressWarnings({ "unchecked" })
	@Get
	public void getStreamInformations(){

		try{

			String origin = getRequest().getClientInfo().getAddress();
			Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get("org.restlet.http.headers");
			if (responseHeaders == null) {
				responseHeaders = new Series<Header>(Header.class);
				getResponse().getAttributes().put("org.restlet.http.headers", responseHeaders);
			}
			responseHeaders.add(new Header("Access-Control-Allow-Origin", origin));
			
			csparqlStreamTable = (Hashtable<String, Csparql_RDF_Stream>) getContext().getAttributes().get("csaprqlinputStreamTable");

			String inputStreamName = URLDecoder.decode((String) this.getRequest().getAttributes().get("streamname"), "UTF-8");

			if(csparqlStreamTable.containsKey(inputStreamName)){
				Csparql_RDF_Stream streamRepresentation = csparqlStreamTable.get(inputStreamName);
				this.getResponse().setStatus(Status.SUCCESS_OK,"Information about " + inputStreamName + " succesfully extracted");
				this.getResponse().setEntity(gson.toJson(new CsparqlStreamDescriptionForGet(streamRepresentation.getStream().getIRI(), streamRepresentation.getStatus())), MediaType.APPLICATION_JSON);
			} else {
				this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,inputStreamName + " does not exists");
				this.getResponse().setEntity(gson.toJson(inputStreamName + " does not exists"), MediaType.APPLICATION_JSON);
			}

		} catch(Exception e){
			logger.error("Error while getting stream informations", e);
			this.getResponse().setStatus(Status.SERVER_ERROR_INTERNAL,Utilities.getStackTrace(e));
			this.getResponse().setEntity(gson.toJson(Utilities.getStackTrace(e)), MediaType.APPLICATION_JSON);
		} finally{
			this.getResponse().commit();
			this.commit();	
			this.release();
		}
	}
}
