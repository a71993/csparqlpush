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
package csparql.streamer;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.impl.PropertyImpl;
import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import polimi.deib.csparql_rest_api.RSP_services_csparql_API;
import polimi.deib.csparql_rest_api.exception.ServerErrorException;
import polimi.deib.csparql_rest_api.exception.StreamErrorException;

public class RabbitStreamer extends BaseStreamer {

	public RabbitStreamer(RSP_services_csparql_API csparqlAPI, String streamName, long sleepTime, String generalIRI) {
		super(csparqlAPI, streamName, sleepTime, generalIRI);
	}

	protected boolean keepRunning = true;

	protected Logger logger = LoggerFactory.getLogger(RabbitStreamer.class.getName());

	public void run() {

		final String QUEUE_NAME = "extract-memberships";

		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.0.93");
		factory.setUsername("admin");
		factory.setPassword("admin");
		Connection connection;
		try {
			connection = factory.newConnection();
			Channel channel = connection.createChannel();

			Consumer consumer = new DefaultConsumer(channel) {
				@Override
				public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
						byte[] body) throws IOException {
					String message = new String(body, "UTF-8");
					Model model = ModelFactory.createDefaultModel();
					if (StringUtils.isNotBlank(message)) {
						model.read(new ByteArrayInputStream(message.getBytes()), null, "N-TRIPLE");
						try {
							System.out.println(" [x]trying to feed to stream '" + message + "'");
							csparqlAPI.feedStream(streamName, model);
						} catch (StreamErrorException | ServerErrorException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					}
				}
			};
			channel.basicConsume(QUEUE_NAME, true, consumer);

		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void stopStream() {
		keepRunning = false;
	}

}
