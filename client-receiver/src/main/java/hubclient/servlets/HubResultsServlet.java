package hubclient.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.JenaException;

import hubclient.resources.RDFTriple;


@SuppressWarnings("serial")
public class HubResultsServlet extends HttpServlet {
	
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		String token = request.getParameter("hub.challenge");
		
		if (token != null) {
			System.out.println("HubResultsServlet - doGet token: " + token);
			
			response.setHeader("Content-Type", "text/plain");

			PrintWriter writer = response.getWriter();
			writer.write(token);
			writer.flush();
			writer.close();
		} else {		
		
			Model model = ModelFactory.createDefaultModel();
	    	try{
	    		model.read("results.xml");
	    	}catch(JenaException e){
	    		System.out.println("JenaException " + e);
	    	}
			
	    	Gson gson = new Gson();
	    	
	    	ArrayList<RDFTriple> triples = new ArrayList<RDFTriple>();
	    	Graph graph = model.getGraph();
//	    	System.out.println(graph.toString());
	    	StmtIterator iterator = model.listStatements();
	    	try{
		    	while(iterator.hasNext()){
		    		RDFTriple triple = new RDFTriple();
		    		com.hp.hpl.jena.rdf.model.Statement statement = iterator.nextStatement();
		    		triple.setSubject(statement.getSubject().toString());
		    		triple.setObject(statement.getObject().toString());
		    		triple.setPredicate(statement.getPredicate().toString());
		    		triples.add(triple);
		    	}
	    	}catch(NoSuchElementException e){
	    		System.out.println("Exception: " + e);
	    	}
			
	    	HttpSession session=request.getSession();
	    	session.setAttribute("triples", triples);
	    	session.setAttribute("time", new Date());
	    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/results.jsp");
	    	rd.forward(request, response);
		}
		
	}
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    	
    	System.out.println("Proovime lugeda RDF-i!");
		InputStream inputStream = request.getInputStream();
		Model model = ModelFactory.createDefaultModel();
		Map<String, String[]> parameterMap = request.getParameterMap();
		for (Entry<String, String[]> param : parameterMap.entrySet()){
			System.out.print(param.getKey()+": ");
			System.out.println(param.getValue()[0]);
		}
		System.out.println("requestURI: "+request.getRequestURI());
    	try{
    		model.read(inputStream,"", "RDF/JSON");
    		if(new File("results.xml").exists()){
    			model.read(new FileInputStream("results.xml"),"");
    		}
    		System.out.println("Õnnestus!");
    	}catch(JenaException e){
    		System.out.println("Exception " + e);
    	}

    	System.out.println("Kirjutame tulemused results.xml-i");
    	model.write(new FileOutputStream("results.xml"));
    }
}
