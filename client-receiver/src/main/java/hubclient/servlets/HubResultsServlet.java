package hubclient.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

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
			try {
				model.read("results.xml");
			} catch (JenaException e) {
				System.out.println("JenaException " + e);
			}

			File fileEntry = new File("results");
			File[] listOfFiles = fileEntry.listFiles();
			Map<String, ArrayList<RDFTriple>> fromFilesTriples = new HashMap<>();
			for (File file : listOfFiles) {
				Model modelFromDir = ModelFactory.createDefaultModel();
				String filePath = file.getPath();
				System.out.println("file name: " + filePath);
				modelFromDir.read(filePath);
				ArrayList<RDFTriple> fromFileTriples = getTriplesFromModel(modelFromDir);
				fromFilesTriples.put(filePath, fromFileTriples);
			}
			ArrayList<RDFTriple> triples = getTriplesFromModel(model);

			HttpSession session = request.getSession();
			session.setAttribute("triples", triples);
			session.setAttribute("ftriples", fromFilesTriples);
			RequestDispatcher rd = getServletContext().getRequestDispatcher(
					"/results.jsp");
			rd.forward(request, response);
		}

	}

	private ArrayList<RDFTriple> getTriplesFromModel(Model model) {
		ArrayList<RDFTriple> triples = new ArrayList<RDFTriple>();
		StmtIterator iterator = model.listStatements();
		try {
			while (iterator.hasNext()) {
				RDFTriple triple = new RDFTriple();
				com.hp.hpl.jena.rdf.model.Statement statement = iterator
						.nextStatement();
				triple.setSubject(statement.getSubject().toString());
				triple.setObject(statement.getObject().toString());
				triple.setPredicate(statement.getPredicate().toString());
				triples.add(triple);
			}
		} catch (NoSuchElementException e) {
			System.out.println("Exception: " + e);
		}
		return triples;
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		System.out.println("Proovime lugeda RDF-i!");
		String topic = request.getParameter("hub.topic");
		System.out.println("topic: " + topic);
		System.out.println("requestURI: " + request.getRequestURI());
		InputStream inputStream = request.getInputStream();
		Model model = ModelFactory.createDefaultModel();
		File resultsDir = new File("results");
		if(!resultsDir.exists()){
			resultsDir.mkdir();
		}
		File file = new File(resultsDir, topic != null ? topic : "results1");
		System.out.println(file.getAbsolutePath());
		System.out.println(inputStream.toString());
		try {
			model.read(inputStream, null, "RDF/JSON");
			if (file.exists()) {
				model.read(new FileInputStream(file), "");
			}
			System.out.println("Õnnestus!");
		} catch (JenaException e) {
			System.out.println("Exception " + e);
			e.printStackTrace();
		}

		model.write(new FileOutputStream(file));
	}
}
