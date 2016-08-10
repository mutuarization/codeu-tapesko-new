package com.example.myproject;

import java.io.IOException;
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

		//Normalize scores
		for(String url: length.keySet()) {
			double len = Math.sqrt(length.get(url));
			if(len != 0) {
				scores.put(url, scores.get(url) / len);
			}
			
		}

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
