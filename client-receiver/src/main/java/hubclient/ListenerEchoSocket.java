package hubclient;

import hubclient.resources.ListOfRDFTriples;
import hubclient.resources.RDFTriple;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphUtil;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.graph.GraphFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
 
/**
 * Example EchoSocket using Listener.
 */
public class ListenerEchoSocket implements WebSocketListener {
 
    private Session outbound;
 
    private final static String QUEUE_NAME = "FLOOWO";
 
    @Override
    public void onWebSocketBinary(byte[] payload, int offset, int len) {
    }
 
    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        this.outbound = null;
    }
 
    @Override
    public void onWebSocketConnect(Session session) {
        this.outbound = session;
        try{
        	this.rabbitReceive();
        }catch(Exception e){
        	System.out.println(e);
        }
        
    }
 
    @Override
    public void onWebSocketError(Throwable cause) {
        cause.printStackTrace(System.err);
    }
 
    @Override
    public void onWebSocketText(String message) {
        if ((outbound != null) && (outbound.isOpen())) {
            System.out.printf("Echoing back message [%s]%n", message);
            outbound.getRemote().sendString(message, null);
        }
    }
    
    private void sendClient(String str) {
        try {
            this.outbound.getRemote().sendString(str);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void sendClient(ByteBuffer buff) {
        try {
            this.outbound.getRemote().sendBytes(buff);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void rabbitReceive()
        throws java.io.IOException,
               java.lang.InterruptedException {

      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost("localhost");
      Connection connection = factory.newConnection();
      Channel channel = connection.createChannel();
      
      try{
    	  
      
      String exchange = "amq.http%3A%2F%2Fex.org%2Frabbit";
//	  channel.exchangeDeclare(exchange, "topic");
      String queueName = channel.queueDeclare().getQueue();
      channel.queueBind(queueName, exchange, "#");

//      channel.queueDeclare(QUEUE_NAME, false, false, false, null);

      QueueingConsumer consumer = new QueueingConsumer(channel);
      channel.basicConsume(queueName, true, consumer);

      while (true) {
    	System.out.println("Alustame");
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
        String routingKey = delivery.getEnvelope().getRoutingKey();

        System.out.println(" [x] Received '" + routingKey + "':'" + message + "'");
        
        Model model = ModelFactory.createDefaultModel();
    	try{
    		model.read(new ByteArrayInputStream(message.getBytes()),"", "RDF/JSON");
    	}catch(JenaException e){
    		System.out.println("Exception " + e);
    	}
    	Gson gson = new Gson();
    	
    	ArrayList<RDFTriple> triples = new ArrayList<RDFTriple>();
    	Graph graph = model.getGraph();
    	System.out.println(graph.toString());
    	ExtendedIterator<Triple> iterator = GraphUtil.findAll(graph);
    	StmtIterator iterator2 = model.listStatements();
    	try{
	    	while(iterator2.hasNext()){
	    		RDFTriple triple = new RDFTriple();
	    		com.hp.hpl.jena.rdf.model.Statement statement = iterator2.nextStatement();
	    		triple.setSubject(statement.getSubject().toString());
	    		triple.setObject(statement.getObject().toString());
	    		triple.setPredicate(statement.getPredicate().toString());
	    		triples.add(triple);
	    		this.sendClient(gson.toJson(triple));
	    		System.out.println(gson.toJson(triple));
	    	}
    	}catch(NoSuchElementException e){
    		System.out.println("Exception: " + e);
    	}
//    	try{
//	    	while(iterator.hasNext()){
//	    		RDFTriple triple = new RDFTriple();
//	    		Triple jenaTriple = iterator.next();
//	    		triple.setSubject(jenaTriple.getSubject().toString());
//	    		triple.setObject(jenaTriple.getObject().toString());
//	    		triple.setPredicate(jenaTriple.getPredicate().toString());
//	    		triples.add(triple);
//	    		this.sendClient(gson.toJson(triple));
//	    		System.out.println(gson.toJson(triple));
//	    	}
//	  	}catch(NoSuchElementException e){
//			System.out.println("Exception: " + e);
//		}
    	
//     	ListOfRDFTriples listOfRDFTriples = new ListOfRDFTriples(triples);
//        this.sendClient(gson.toJson(listOfRDFTriples));
//        System.out.println(" [x] Received '" + message + "'");
      }
      }catch(Exception e){
    	  e.printStackTrace();
      }
    }
}