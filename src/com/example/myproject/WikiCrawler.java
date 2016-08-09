package com.example.myproject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.nodes.Node;

import redis.clients.jedis.Jedis;


public class WikiCrawler {
	// keeps track of where we started
	private final String source;
	
	// the index where the results go
	private JedisIndex index;
	
	// queue of URLs to be indexed
	private Queue<String> queue = new LinkedList<String>();
	
	// fetcher used to get pages from Wikipedia
	final static WikiFetcher wf = new WikiFetcher();

	/**
	 * Constructor.
	 * 
	 * @param source
	 * @param index
	 */
	public WikiCrawler(String source, JedisIndex index) {
		this.source = source;
		this.index = index;
		queue.offer(source);
	}

	/**
	 * Returns the number of URLs in the queue.
	 * 
	 * @return
	 */
	public int queueSize() {
		return queue.size();	
	}

	/**
	 * Gets a URL from the queue and indexes it.
	 * @param b 
	 * 
	 * @return Number of pages indexed.
	 * @throws IOException
	 */
	public String crawl() throws IOException {
        // FILL THIS IN!
        if(queue.isEmpty()){
        	return null;
        }
        String url = queue.remove();
    	System.out.println("crawling "+url);
        Elements paragraphs;
       	if(index.isIndexed(url))
    	{
       		System.out.println(url+" is already indexed");
    		return null;
    	}
    	paragraphs = wf.fetchWikipedia(url);
        index.indexPage(url,paragraphs);
        queueInternalLinks(paragraphs);	        
		return url;
	}
	
	/**
	 * Parses paragraphs and adds internal links to the queue.
	 * 
	 * @param paragraphs
	 */
	// NOTE: absence of access level modifier means package-level
	void queueInternalLinks(Elements paragraphs) {
        // FILL THIS IN!
		for(Element paragraph: paragraphs)
		{
			Iterable<Node> iterator = new WikiNodeIterable(paragraph);
			for(Node node: iterator)
			{
				if(node instanceof Element)
				{
					String link = node.attr("href");
					if(link.startsWith("/wiki/"))
					{
						queue.add("https://en.wikipedia.org" + ((Element)node).attr("href"));	
					}
				}
			}
		}
	}

	public void startCrawl() throws IOException {
		
		// make a WikiCrawler
		
		
		// for testing purposes, load up the queue
		Elements paragraphs = wf.fetchWikipedia(source);
		queueInternalLinks(paragraphs);

		// loop until we index a new page
		String res;
		do {
			res = crawl();

		} while (res == null);
	}
}
