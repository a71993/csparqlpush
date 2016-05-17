package hubclient.servlets;

import hubclient.ListenerEchoSocket;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
 
@SuppressWarnings("serial")
public class MyEchoServlet extends WebSocketServlet {
 
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.getPolicy().setIdleTimeout(1000000);
        factory.register(ListenerEchoSocket.class);
    }
}