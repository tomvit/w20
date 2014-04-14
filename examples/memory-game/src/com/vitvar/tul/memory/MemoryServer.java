package com.vitvar.tul.memory;

import memoryinterface.MemoryInterface;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;

public class MemoryServer {

	private Server server = null;
	
	public MemoryServer(MemoryInterface memoryGame, int port) {
    	server = new Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(port);
        server.addConnector(connector);
 
        String webDir = this.getClass().getClassLoader().
        	getResource("com/vitvar/tul/memory/resources").toExternalForm();
        
        // setup a static resource handler
        ResourceHandler staticResources = new ResourceHandler();
        staticResources.setWelcomeFiles(new String[]{ "index.html" });
        staticResources.setResourceBase(webDir);        
 
        // register all handlers
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[] { new AJAXMemoryInterface(memoryGame.getClass()), staticResources });
        server.setHandler(handlers);		
	}
	
    public void start() throws Exception {
        server.start();
        server.join();
    }
    
	public static void main(String[] args) throws Exception {
		String memoryClass = "com.vitvar.example.MemoryExample";
		if (args.length > 0) {
			memoryClass = args[0];
			System.out.println("Will use custom memory interface class " + memoryClass);		
		} else
			System.out.println("Custom memory interface class was not specified, defaulting to com.vitvar.example.MemoryExample");
		
		MemoryInterface gameInstnace = null;
		try {
			gameInstnace = (MemoryInterface)Class.forName(memoryClass).newInstance();
		} catch (ClassNotFoundException e) {
			System.out.println("The class " + memoryClass + 
					" cannot be found! Check the class is in a jar file that is on the class path.");
			System.exit(1);
		}
		MemoryServer server = new MemoryServer(gameInstnace, 8080);
		server.start();
	}
   

}
