package hubclient.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class SubscriberServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// POST
	// curl - vd \
	// "hub.mode=subscribe&hub.callback=http://10.1.1.8:4567/sub1&hub.topic=foo&hub.verify=sync&hub.verify=async&hub.lease_seconds=86400"
	// \ http://guest:guest@localhost:15670/subscribe/x/amq.direct
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");

		String mode = "subscribe";
		String streamname = request.getParameter("streamname");
		String topic = request.getParameter("topic");
		String leaseSeconds = request.getParameter("lease_seconds");
		String verify = "async";
		String callback = "http://localhost:8080/endpoint/" + topic;
		String token = "";

		CloseableHttpClient httpclient = HttpClients.createDefault();

//		HttpPost httpPost = new HttpPost(
//				"http://guest:guest@localhost:15670/subscribe/x/"+ URLEncoder.encode(streamname, "UTF-8"));
		HttpPost httpPost = new HttpPost(
		"http://guest:guest@localhost:15670/subscribe/x/amq.direct");

		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		nvps.add(new BasicNameValuePair("hub.mode", mode));
		nvps.add(new BasicNameValuePair("hub.topic", topic));
		if (leaseSeconds != null && !leaseSeconds.isEmpty()) {
			nvps.add(new BasicNameValuePair("hub.leaseSeconds", leaseSeconds));
		}
		nvps.add(new BasicNameValuePair("hub.verify", verify));
		nvps.add(new BasicNameValuePair("hub.callback", callback));
		httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		CloseableHttpResponse chResponse = httpclient.execute(httpPost);
		try {
			System.out.println("get status line: " + chResponse.getStatusLine());
//			HttpEntity entity = chResponse.getEntity();
//			BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
//			token = br.readLine();
//			System.out.println("SubscriberServlet - doPost token: " + token);
//			EntityUtils.consume(entity);
		} finally {
			chResponse.close();
		}
		
		RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
		rd.forward(request, response);

	}

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		response.setContentType("text/html");

		String token = request.getParameter("hub.challenge");

		if (token != null) {
			System.out.println("SubscriberServlet - doGet token: " + token);
			
			response.setHeader("Content-Type", "text/plain");

			PrintWriter writer = response.getWriter();
			writer.write(token);
			writer.flush();
			writer.close();
			
		} else {
			System.out.println("no token, instead: " + request.getQueryString());
		}

	}

}
