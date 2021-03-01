/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dal;

import entity.Subreddit;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author matineh
 */
public class SubredditDAL extends GenericDAL<Subreddit> {

    public SubredditDAL() {

        super(Subreddit.class);
    }

    @Override
    public List<Subreddit> findAll() {

        return findResults("Subreddit.findAll", null);
    }

    @Override
    public Subreddit findById(int id) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("id", id);
        return findResult("Subreddit.findById", map);
    }

    public Subreddit findByName(String name) {

        HashMap<String, Object> map = new HashMap<>();
        map.put("name", name);
        return findResult("Subreddit.findByName", map);

    }

    public Subreddit findByUrl(String url) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("url", url);
        return findResult("Subreddit.findByUrl", map);
    }

    public List<Subreddit> findBySubscribers(int subscribers) {

        HashMap<String, Object> map = new HashMap<>();
        map.put(" subscribers ", subscribers);
        return findResults("Subreddit.findBySubscribers", map);
    }
}
