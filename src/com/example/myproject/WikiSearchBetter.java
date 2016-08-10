package com.example.myproject;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.Map.Entry;

import redis.clients.jedis.Jedis;

public class WikiSearchBetter {
	private JedisIndexBetter index;

	public WikiSearchBetter(JedisIndexBetter index) {
		this.index = index;
	}

	/**
	 * Uses cosine similarity
	 */
	public List<String> searchTopK(String query, int k) {
		Map<String, Double> scores = new HashMap<String, Double>();
		Map<String, Double> length = new HashMap<String, Double>();
		List<String> queryList = processQuerry(query);

		//Cosine Similarity
		for(String term: queryList) {
			Set<String> urlsWithWord = index.getURLs(term);
			double weightTermQuery = 1 + Math.log10(Collections.frequency(queryList, term));
			for(String url: urlsWithWord) {
				double currScore = weightTermQuery * index.getTFIDF(term, url);
				if(!scores.containsKey(url)) {
					scores.put(url, 0.0);
					length.put(url, 0.0);
				}
				scores.put(url, scores.get(url) + currScore);
				length.put(url, length.get(url) + Math.pow(currScore, 2));
			}
		}

//		//Normalize scores
//		for(String url: length.keySet()) {
//			double len = Math.sqrt(length.get(url));
//			if(len != 0) {
//				scores.put(url, scores.get(url) / len);
//			}
//			
//		}

		List<String> topK = new ArrayList<String>();
		for(Entry<String, Double> entry: sort(scores)) {
			if(topK.size() == k) {
				break;
			}
			topK.add(entry.getKey());
		}
		return topK;
	}

	/**
	 * Splits querry into words
	 */

	private List<String> processQuerry(String query) {
		ArrayList<String> queryList = new ArrayList<String>();
		query = query.toLowerCase();
		for(String str: query.split("\\s+")) {
			str = str.replaceAll("[^a-zA-Z0-9]", "");
			if(str.equals("")) continue;
			queryList.add(str);
		}
		return queryList;

	}
	
	/**
	 * Sort the results by relevance.
	 * 
	 * @return List of entries with URL and relevance.
	 */
	public List<Entry<String, Double>> sort(Map<String,Double> map) {
        List<Entry<String,Double>> entries = new LinkedList<Entry<String,Double>>(map.entrySet());
        Collections.sort(entries,new Comparator<Entry<String,Double>>()
        {
        	@Override
        	public int compare(Entry<String,Double> obj1,Entry<String,Double> obj2)
        	{
        		return obj2.getValue().compareTo(obj1.getValue());
        	}
        });
		return entries;
	}
	
	/**
	 * Converts the contents to a String.
	 * 
	 * @param map
	 */
	public String toString(List<String> urls, String term) {
		StringBuffer buf = new StringBuffer();
		int resultsLimit = 30;
		int resultsShown =  (urls.size() > resultsLimit)? resultsLimit : urls.size() ;
		
		buf.append("Showing top " + resultsShown + " of " + urls.size() + " results for \""+ term +"\".<br/><br/>");
		for (String url: urls) {
			buf.append("<a target=\"_blank\" href=\""+url+"\">"+getTitle(url)+"<br/>");
		}
		if(urls.size()==0)
		{
			buf.append("Query not in index.");
		}
		return buf.toString();
	}
	
	/*
	 * Given a wiki url, gets the title
	 * 
	 * @param url
	 * @ return title
	 */
	private String getTitle(String url) {
		String[] urlSections = url.split("/");
		String decoded = URLDecoder.decode(urlSections[urlSections.length - 1]);
		String[] titleSections = decoded.split("_");
		String title = titleSections[0];
		for (int i = 1; i < titleSections.length; i++) {
			title += " " + titleSections[i];
		}
		return title;
	}
	
	public static void main(String[] args) throws IOException {
		
		// make a JedisIndex
		Jedis jedis = JedisMaker.make();
		JedisIndexBetter index = new JedisIndexBetter(jedis); 
		//Assumption --> indexing already done.
		// search for a query

		WikiSearchBetter search = new WikiSearchBetter(index);
		List<String> results = search.searchTopK("object oriented", 10);
		for(String s: results) {
			System.out.println(s);
		}
		
	}

}
