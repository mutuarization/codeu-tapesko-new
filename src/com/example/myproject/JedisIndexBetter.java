package com.example.myproject;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.jsoup.select.Elements;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * Represents a Redis-backed web search index.
 * 
 */
@SuppressWarnings("unused")
public class JedisIndexBetter extends JedisIndex{
	/**
	 * Constructor.
	 * 
	 * @param jedis
	 */
	public JedisIndexBetter(Jedis jedis) {
		super(jedis);
	}

	/**
	 * Gets tfidf score given word and url
	 */
	public double getTFIDF(String term, String url) {
		int urlsWithWord = getURLs(term).size();
		int numUrls = termCounterKeys().size();
		double idf = 0.0;
		double tf = 0.0;
		if(urlsWithWord != 0) {
			idf = Math.log10(numUrls/(double)urlsWithWord);
			tf = 1 + Math.log10(getCount(url, term));
		}
		
		return tf * idf;
	} 
	
	/**
	 * Gets all tfidf counts for a given term
	 */
	public Map<String, Double> getTFIDFCounts(String term) {
		Map<String, Double> counts = new HashMap<String, Double>();
		Set<String> urlset = getURLs(term);
		for(String url: urlset) {
			counts.put(url, getTFIDF(term, url));
		}
		return counts;
	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Jedis jedis = JedisMaker.make();
		JedisIndexBetter index = new JedisIndexBetter(jedis);
		
		//index.deleteTermCounters();
		//index.deleteURLSets();
		//index.deleteAllKeys();
//		loadIndex(index);
//		
		Map<String, Integer> map = index.getCountsFaster("java");
		for (Entry<String, Integer> entry: map.entrySet()) {
			System.out.println(entry);
		}
		System.out.println("------------------------------------");
		Map<String, Double> testmap = index.getTFIDFCounts("java");
		for(Entry<String, Double> entry: testmap.entrySet()) {
			System.out.println(entry);
		}
		
	}

	/**
	 * Stores two pages in the index for testing purposes.
	 * 
	 * @return
	 * @throws IOException
	 */
	private static void loadIndex(JedisIndex index) throws IOException {
		WikiFetcher wf = new WikiFetcher();
		//Not true --> tried indexing whole of wikipedia hence Index contains more than just these two pages
		String url = "https://en.wikipedia.org/wiki/Java_(programming_language)";
		Elements paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
		
		url = "https://en.wikipedia.org/wiki/Programming_language";
		paragraphs = wf.readWikipedia(url);
		index.indexPage(url, paragraphs);
	}
}
