package com.example.myproject;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import redis.clients.jedis.Jedis;

@SuppressWarnings("serial")
public class WikiSearchServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String term = req.getParameter("term"); 
		
		System.out.print(term);
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		//Assumption --> indexing already done.
		// search for a query
		WikiSearch search1 = WikiSearch.search(term, index);
		String results = search1.toString();
		
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
}
