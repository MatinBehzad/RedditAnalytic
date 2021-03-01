/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import dal.SubredditDAL;
import entity.Subreddit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import common.ValidationException;
import java.util.stream.Collectors;
import java.util.function.ObjIntConsumer;
import java.util.Objects;

/**
 *
 * @author matineh
 *
 */
public class SubredditLogic extends GenericLogic<Subreddit, SubredditDAL> {

    public static final String SUBSCRIBERS = "subscribers";
    public static final String URL = "url";
    public static final String NAME = "name";
    public static final String ID = "id";

    public SubredditLogic() {
        super(new SubredditDAL());
    }

    @Override
    public Subreddit createEntity(Map<String, String[]> parameterMap) {
        Objects.requireNonNull(parameterMap, "parameterMap cannot be null");
        //same as if condition below
        if (parameterMap == null) {
            throw new NullPointerException("parameterMap cannot be null");
        }

        //create a new Entity object
        Subreddit entity = new Subreddit();

        //ID is generated, so if it exists add it to the entity object
        //otherwise it does not matter as mysql will create an if for it.
        //the only time that we will have id is for update behaviour.
        if (parameterMap.containsKey(ID)) {
            try {
                entity.setId(Integer.parseInt(parameterMap.get(ID)[0]));
            } catch (java.lang.NumberFormatException ex) {
                throw new ValidationException(ex);
            }
        }
        ObjIntConsumer< String> validator = (value, length) -> {
            if (value == null || value.trim().isEmpty() || value.length() > length) {
                String error = "";
                if (value == null || value.trim().isEmpty()) {
                    error = "value cannot be null or empty: " + value;
                }
                if (value.length() > length) {
                    error = "string length is " + value.length() + " > " + length;
                }
                throw new ValidationException(error);
            }
        };
        String subscribers = parameterMap.get(SUBSCRIBERS)[0];
        String url = parameterMap.get(URL)[0];
        String name = parameterMap.get(NAME)[0];

        //validate the data
        validator.accept(url, 255);
        validator.accept(name, 100);

        //set values on entity
        entity.setName(name);
        entity.setUrl(url);
        entity.setSubscribers(Integer.parseInt(parameterMap.get(SUBSCRIBERS)[0]));

        // entity.setSubReddit(new SubredditLogic(Integer.parseInt(subscribers)));

        return entity;
    }

    private boolean findDuplicate(Subreddit entity) {
        List<Subreddit> boards = getAll();
        boards.remove(getWithId(entity.getId()));
        List duplicateEntries = boards.stream()
                .filter(e -> e.getId().equals(entity.getId()))
                .filter(e -> e.getUrl().equals(entity.getUrl()))
                .collect(Collectors.toList());
        return (!duplicateEntries.isEmpty());
    }

    public List<Subreddit> getSubredditWithSubscribers(int subscribers) {
        return get(() -> dal().findBySubscribers(subscribers));
    }

    public Subreddit getSubredditWithURL(String url) {
        return get(() -> dal().findByUrl(url));
    }
    public Subreddit getSubredditWithName(String name) {
        return get(() -> dal().findByUrl(name));
    }

    @Override
    public List<String> getColumnCodes() {
        return Arrays.asList(ID, NAME, URL, SUBSCRIBERS);//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("id", "name", "url", "subscribers"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<?> extractDataAsList(Subreddit e) {
        return Arrays.asList(e.getId(), e.getName(), e.getUrl(), e.getSubscribers()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Subreddit getWithId(int id) {
        return get(() -> dal().findById(id)); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<Subreddit> getAll() {
        return get(() -> dal().findAll()); //To change body of generated methods, choose Tools | Templates.
    }
}
