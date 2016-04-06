package hubclient.servlets;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

@SuppressWarnings("serial")
public class StreamServlet extends HttpServlet {
 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
    	response.setContentType("text/html");

    	String streamname = request.getParameter("streamname");


    	CloseableHttpClient httpclient = HttpClients.createDefault();
		HttpPut httpPut = new HttpPut("http://localhost:8175/streams/" + URLEncoder.encode(streamname, "UTF-8"));
		CloseableHttpResponse chResponse = httpclient.execute(httpPut);		
        try {
            System.out.println(chResponse.getStatusLine());
            HttpEntity entity = chResponse.getEntity();
            
	  		BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
	  		String output;
	  		System.out.println("Output from Server .... \n");
	  		while ((output = br.readLine()) != null) {
	  			System.out.println(output);
	  		}
	  		
            EntityUtils.consume(entity);
        } finally {
            chResponse.close();
        }  
    	
	  	
    	RequestDispatcher rd = getServletContext().getRequestDispatcher("/index.html");
    	rd.forward(request, response);
  	  
    }

}
