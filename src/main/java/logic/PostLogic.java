/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.ValidationException;
import dal.PostDAL;
import entity.Post;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.ObjIntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author kosio
 */
public class PostLogic extends GenericLogic<Post, PostDAL> {

    public static final String ID = "id";
    public static final String REDDIT_ACCOUNT_ID = "reddit_account_id";
    public static final String SUBREDDIT_ID = "subreddit_id";
    public static final String UNIQUE_ID = "unique_id";
    public static final String POINTS = "points";
    public static final String COMMENT_COUNT = "comment_count";
    public static final String TITLE = "title";
    public static final String CREATED = "created";

    PostLogic() {
        super(new PostDAL());
    }

//    +getAll() : List<Post>
    @Override
    public List<Post> getAll() {
        return get(() -> dal().findAll());
    }

    //+getWithId(id : int) : Post
    @Override
    public Post getWithId(int id) {
        return get(() -> dal().findById(id));
    }
//+getPostWithUniqueId(uniqueId : String) : Post

    public Post getPostWithUniqueId(String uniqueId) {
        return get(() -> dal().findByUniqueId(uniqueId));
    }
//+getPostWithPoints(points : int) : List<Post>

    public List<Post> getPostWithPoints(int points) {
        return get(() -> dal().findByPoints(points));
    }
//+getPostsWithCommentCount(commentCount : int) : List<Post>

    public List<Post> getPostsWithCommentCount(int commentCount) {
        return get(() -> dal().findByCommentCount(commentCount));
    }
//+getPostsWithAuthorID(id : int) : List<Post>

    public List<Post> getPostsWithAuthorID(int id) {//ID OR AUTHOR ID?
        return get(() -> dal().findByAuthor(id));
    }
//+getPostsWithTitle(title : String) : List<Post>

    public List<Post> getPostsWithTitle(String title) {
        return get(() -> dal().findByTitle(title));
    }
//+getPostsWithCreated(created : Date) : List<Post>

    public List<Post> getPostsWithCreated(Date created) {
        return get(() -> dal().findByCreated(created));
    }
//+createEntity(parameterMap : Map<String, String[]>) : Post

    @Override
    public Post createEntity(Map<String, String[]> parameterMap) {

        Objects.requireNonNull(parameterMap, "parameterMap cannot be null");
        if (parameterMap == null) {
            throw new NullPointerException("parameterMap cannot be null");
        }

        Post entity = new Post();

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

        String reddit_account_id = parameterMap.get( REDDIT_ACCOUNT_ID )[ 0 ];
        String subreddit_id = parameterMap.get( SUBREDDIT_ID )[ 0 ];
        String unique_id = parameterMap.get(UNIQUE_ID)[0];
        int p = Integer.parseInt(parameterMap.get(POINTS)[0]);
        String comment_count = parameterMap.get(COMMENT_COUNT)[0];
        String title = parameterMap.get(TITLE)[0];
        String created = parameterMap.get(CREATED)[0];

        //REVIEW
        validator.accept(unique_id, 10);
        validator.accept(title, 255);

        entity.setUniqueId(unique_id);
        entity.setPoints(p);
        try {
            entity.setCommentCount(Integer.parseInt(comment_count));
        } catch (java.lang.NumberFormatException ex) {
            throw new ValidationException("CommentCount should be a number ");
        }
        entity.setTitle(title);
        entity.setCreated(convertStringToDate(created));

        return entity;
    }
//To change body of generated methods, choose Tools | Templates.

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("ID", "REDDIT_ACCOUNT_ID", "SUBREDDIT_ID", "UNIQUE_ID", "POINTS", "COMMENT_COUNT", "TITLE", "CREATED"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getColumnCodes() {
        return Arrays.asList(ID, REDDIT_ACCOUNT_ID, SUBREDDIT_ID, UNIQUE_ID, POINTS, COMMENT_COUNT, TITLE, CREATED); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<?> extractDataAsList(Post e) {
        return Arrays.asList(e.getId(), e.getRedditAccountId(), e.getSubredditId(), e.getUniqueID(), e.getPoints(), e.getCommentCount(), e.getTitle(), e.getCreated()); //To change body of generated methods, choose Tools | Templates.
    }

}
