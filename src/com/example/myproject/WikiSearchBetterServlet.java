package com.example.myproject;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import redis.clients.jedis.Jedis;

/**
 * Servlet implementation class WikiSearchBetterServlet
 */
public class WikiSearchBetterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public WikiSearchBetterServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String term = req.getParameter("term"); 
		String results;
		
		int resultsLimit = 30;
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndexBetter index = new JedisIndexBetter(jedis); 
		//Assumption --> indexing already done.
		// search for a query
		WikiSearchBetter search = new WikiSearchBetter(index);
		
		// set the results string
		if (term.equals("")) {
			results = "Your search was empty<br/>We're not sure what you seek, so<br/>Here is a haiku<br/>:)";
		} else {
			results = search.toString(search.searchTopK(term, resultsLimit), term);
		}
		
		req.setAttribute("resultsId", results); // add to request
		//resp.setContentType("text/plain");
		//resp.getWriter().println("Hello, world!!!!!");
		jedis.quit();
		try {
			RequestDispatcher RequetsDispatcherObj = req.getRequestDispatcher("/home.jsp");
			RequetsDispatcherObj.forward(req, resp);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
