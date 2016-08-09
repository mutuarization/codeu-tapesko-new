package com.example.myproject;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import redis.clients.jedis.Jedis;


/**
 * Represents the results of a search query.
 *
 */
public class WikiSearch {
	
	// map from URLs that contain the term(s) to relevance score
	private Map<String, Integer> map;

	/**
	 * Constructor.
	 * 
	 * @param map
	 */
	public WikiSearch(Map<String, Integer> map) {
		this.map = map;
	}
	
	/**
	 * Looks up the relevance of a given URL.
	 * 
	 * @param url
	 * @return
	 */
	public Integer getRelevance(String url) {
		Integer relevance = map.get(url);
		return relevance==null ? 0: relevance;
	}
	
	/**
	 * Prints the contents in order of term frequency.
	 * 
	 * @param map
	 */
	private void print() {
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			System.out.println(entry);
		}
		if(entries.size()==0)
		{
			System.out.println("Query not in index.");
		}
	}
	
	/**
	 * Converts the contents to a String.
	 * 
	 * @param map
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer();
		
		List<Entry<String, Integer>> entries = sort();
		for (Entry<String, Integer> entry: entries) {
			buf.append(entry+"<br/>");
		}
		if(entries.size()==0)
		{
			buf.append("Query not in index.");
		}
		return buf.toString();
	}
	
	/**
	 * Computes the union of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch or(WikiSearch that) {
        // FILL THIS IN!
        Map<String,Integer> ormap = new HashMap<String,Integer>(this.map);
		for(String key: that.map.keySet())
		{
			int relevanceScore = totalRelevance(getRelevance(key),that.getRelevance(key));
			ormap.put(key,relevanceScore);
		}
		return new WikiSearch(ormap);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch and(WikiSearch that) {
        // FILL THIS IN!
        Map<String,Integer> andmap = new HashMap<String,Integer>();
       	for(String key: this.map.keySet())
		{
			if(that.map.containsKey(key))
			{
				int relevanceScore = totalRelevance(that.getRelevance(key),getRelevance(key)); 
				andmap.put(key,relevanceScore);
			}
		}
		return new WikiSearch(andmap);
	}
	
	/**
	 * Computes the intersection of two search results.
	 * 
	 * @param that
	 * @return New WikiSearch object.
	 */
	public WikiSearch minus(WikiSearch that) {
        // FILL THIS IN!
		Map<String,Integer> minusmap = new HashMap<String,Integer>(this.map);
        for(String key: this.map.keySet())
		{
			if(that.map.containsKey(key))
			{
				minusmap.remove(key);
			}
		}
		return new WikiSearch(minusmap);
	}
	
	/**
	 * Computes the relevance of a search with multiple terms.
	 * 
	 * @param rel1: relevance score for the first search
	 * @param rel2: relevance score for the second search
	 * @return
	 */
	protected int totalRelevance(Integer rel1, Integer rel2) {
		// simple starting place: relevance is the sum of the term frequencies.
		return rel1 + rel2;
	}

	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Integer>> sort() {
        // FILL THIS IN!
        List<Entry<String,Integer>> entries = new LinkedList<Entry<String,Integer>>(map.entrySet());
        Collections.sort(entries,new Comparator<Entry<String,Integer>>()
        {
        	@Override
        	public int compare(Entry<String,Integer> obj1,Entry<String,Integer> obj2)
        	{
        		return obj2.getValue().compareTo(obj1.getValue());
        	}
        });
		return entries;
	}

	/**
	 * Performs a search and makes a WikiSearch object.
	 * 
	 * @param term
	 * @param index
	 * @return
	 */
	public static WikiSearch search(String term, JedisIndex index) {
		Map<String, Integer> map = index.getCounts(term);
		return new WikiSearch(map);
	}

	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndex index = new JedisIndex(jedis); 
		//Assumption --> indexing already done.
		// search for a query
		Scanner sc = new Scanner(System.in);
		for(int i=0;i<3;i++)
		{
			System.out.println("Enter a query");
			String term1 = sc.next();
			WikiSearch search1 = search(term1, index);
			search1.print();
		}
		
	}
}
