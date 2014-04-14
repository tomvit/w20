package com.vitvar.tul.memory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import memoryinterface.MemoryInterface;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class AJAXMemoryInterface extends AbstractHandler {
	
	private Hashtable<String, GameInstance> games = new Hashtable<String, GameInstance>();
	private Class<? extends MemoryInterface> memoryInterfaceClass = null;
	
	public AJAXMemoryInterface(Class<? extends MemoryInterface> memoryInterfaceClass) {
		this.memoryInterfaceClass = memoryInterfaceClass;
	}
	
	private MemoryInterface createGameInstance() throws Exception {
		return memoryInterfaceClass.newInstance();
	}
	
	private GameInstance getGameInstance(String id) throws BadRequestException {
		GameInstance g = games.get(id);
		if (id == null)
			throw new BadRequestException("Invalid game id (" + id + ")!");
		return g;
	}
	
	private void sendJSON(HttpServletResponse response, int status, String json) throws IOException {
        response.setStatus(status);				
		response.setContentType("application/json");
		response.getWriter().println(json);		
	}
	
	private void sendJSON(HttpServletResponse response, String json) throws IOException {
		sendJSON(response, HttpServletResponse.SC_OK, json);
	}
	
	private String convertStreamToString(java.io.InputStream is) {
	    try {
	        return new java.util.Scanner(is).useDelimiter("\\A").next();
	    } catch (java.util.NoSuchElementException e) {
	        return "";
	    }
	}
	
	private String parse(String pattern, String expr, int g) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(expr);
		if (m.matches())
			return m.group(g);
		else
			return null;
	}
	
	private String parse(String pattern, String expr) {
		return parse(pattern, expr, 1);
	}
	
	private String[] parseRequest(HttpServletRequest request, String method, 
		String pathRegEx) {
		if (request.getMethod().equals(method)) {
			Pattern p = Pattern.compile(pathRegEx);
			Matcher m = p.matcher(request.getPathInfo());
			if (m.matches()) {
				String[] r = new String[m.groupCount()];
				for (int i = 0; i < r.length; i++)
					r[i] = m.group(1);
				return r;
			}
		}
		return null;
	}
	
	private String getGameStatus(boolean stream, GameInstance g) throws Exception {
		String status = (stream ? "data: " : "") + "{ \"cards\" : [ \n";
		int[] cards = g.getGameInstance().getCardsOnBoard();
		
		for (int i = 0; i < cards.length; i++) 
			status += (stream ? "data: " : "") + " { \"value\"" + " : " + cards[i] + ", \"turned\" : " + 
				((g.getTurnedCard1() == i || g.getTurnedCard2() == i) ? "true" : "false") + " }" + (i < cards.length - 1 ? ",\n" : "\n"); 
		status += (stream ? "data: " : "") + "] " + ", \"players\" : [ \n";  
		
		for (int i = 0; i < g.getGameInstance().getNumPlayers(); i++) 
			status += (stream ? "data: " : "") + " { \"name\" : \"Player_"   + (i + 1) + "\"" + 
				       ", \"active\" : " + new Boolean(g.getGameInstance().getActivePlayerInx() == i).toString() + "" +
					   ", \"numCards\" : " + g.getGameInstance().getNumCardsPlayer(i) + " } " + (i <  g.getGameInstance().getNumPlayers() - 1 ? ",\n" : "\n");;
				
		status += (stream ? "data: " : "") + "], \"game_over\" : " + g.getGameInstance().gameOver() + 
			", \"winner\" : " + g.getGameInstance().winner() + " }" + (stream ? "\n\n" : "");
		
		return status;
	}
		
    public void handle(String target, Request baseRequest, HttpServletRequest request, 
		HttpServletResponse response) throws IOException, ServletException {
	    // the default; will be overridden in case the request will be handled
	    baseRequest.setHandled(false);
	    String[] params = null;
	    String data = "";
	    
	    System.out.println(request.getMethod() + " " + request.getPathInfo());
	    
	    try {
		    // create a new game resource
		    if (parseRequest(request, "POST", "/api/games/?") != null) {
		    	// generate game id
		    	GameInstance g = new GameInstance(new Date().getTime(), createGameInstance());
		    	data = convertStreamToString(request.getInputStream());
		    	String numCards = parse(".*\"numCards\"[\\s]*:[\\s]*([0-9]+).*", data);
		    	g.getGameInstance().setNumCards(Integer.parseInt(numCards));
		    	games.put(g.getKey(), g);
		        response.setStatus(HttpServletResponse.SC_CREATED);				
		        response.setHeader("Location", "/api/games/" + g.getKey());
				response.getWriter().print("");
				baseRequest.setHandled(true);
				return;
		    }
		    
		    if ((params = parseRequest(request, "GET", "/api/games/([a-fA-F0-9]+)/realtime/status/?")) != null) {
		    	response.setContentType("text/event-stream; charset=utf-8");
		    	response.setHeader("cache-control", "no-chace");
		    	
		    	GameInstance g = getGameInstance(params[0]);
		    	String status = "";
		    	long ms = System.currentTimeMillis(); 
		    	// push data to the client for 20 seconds
		        // client should reconnect when the connection is closed
		        while (System.currentTimeMillis() - ms < 60000) {
		        	String newStatus = getGameStatus(true, g);
		        	if (!status.equals(newStatus)) {
			            response.getWriter().print(newStatus);
			            response.getWriter().flush();
			            status = newStatus;
		        	}	
		            try {
		                Thread.sleep(200);
		            } catch (InterruptedException e) {
		                // do nothing;
		            }
		        }
		    }
		    
		    // make a single turn
		    if ((params = parseRequest(request, "POST", "/api/games/([a-fA-F0-9]+)/turns/?")) != null) {
		    	GameInstance g = getGameInstance(params[0]);
		    	data = convertStreamToString(request.getInputStream());
	    		String cardInx = parse(".*\"CardInx\"[\\s]*:[\\s]*([0-9]+).*", data);
	    		g.turnCard(Integer.parseInt(cardInx));
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		baseRequest.setHandled(true);
		    }		    

		    // apply the turns - make a move
		    if ((params = parseRequest(request, "POST", "/api/games/([a-fA-F0-9]+)/moves/?")) != null) {
		    	GameInstance g = getGameInstance(params[0]);
		    	g.move();
	    		response.setStatus(HttpServletResponse.SC_OK);
	    		baseRequest.setHandled(true);		    	
		    }
		    
	    } catch (BadRequestException e1) {
	    	System.out.println(data);
            System.out.println(e1.toString());
            e1.printStackTrace();
	    	response.sendError(HttpServletResponse.SC_BAD_REQUEST, e1.getMessage());
            baseRequest.setHandled(true);	 
	    } catch (Exception e) {
	    	System.out.println(data);
            System.out.println(e.toString());
            e.printStackTrace();
	    	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            baseRequest.setHandled(true);	    		    	
	    }		        
    }	

}
