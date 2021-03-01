/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.ValidationException;
import dal.CommentDAL;
import entity.Comment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.ObjIntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author matineh
 */
public class CommentLogic extends GenericLogic<Comment, CommentDAL> {

    public static final String REPLYS = "replys";
    public static final String IS_REPLY = "is_reply";
    public static final String POINTS = "points";
    public static final String CREATED = "created";
    public static final String TEXT = "text";
    public static final String ID = "id";
    public static final String UNIQUE_ID = "unique_id";
    public static final String REDDIT_ACCOUNT_ID = "reddit_account_id";
    public static final String POST_ID = "post_id";

    CommentLogic() {
        super(new CommentDAL());
    }

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList("ID", " REPLYS", "IS_REPLY", "POINTS", "CREATED", "TEXT", "UNIQUE_ID", "REDDIT_ACCOUNT_ID", "POST_ID"); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getColumnCodes() {
        return Arrays.asList(ID, REPLYS, IS_REPLY, POINTS, CREATED, TEXT, UNIQUE_ID, REDDIT_ACCOUNT_ID, POST_ID); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<?> extractDataAsList(Comment e) {
        return Arrays.asList(e.getId(), e.getReplys(), e.getIsReply(), e.getPoints(), e.getCreated(), e.getText(), e.getUniqueId(), e.getRedditAccountId(), e.getPostId()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Comment createEntity(Map<String, String[]> parameterMap) {

        if (parameterMap == null) {
            throw new NullPointerException("parameterMap cannot be null");
        }
        Comment entity = new Comment();

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

        String replys = parameterMap.get(REPLYS)[0];
        String is_reply = parameterMap.get(IS_REPLY)[0];
        String points = parameterMap.get(POINTS)[0];
        String created = parameterMap.get(CREATED)[0];
        String text = parameterMap.get(TEXT)[0];
        String uniqueId = parameterMap.get(UNIQUE_ID)[0];
//        

        //validate the data
        validator.accept(uniqueId, 10);
        validator.accept(text, 1000);
        validator.accept(is_reply, 1);


        //set values on entity
        entity.setReplys(Integer.parseInt(replys));
        
        if(is_reply.equals("1")){
            entity.setIsReply(true);
        }
        else{
            entity.setIsReply(false);
        }
        
        entity.setPoints(Integer.parseInt(points));
        entity.setCreated(convertStringToDate(created));
        entity.setText(text);
        entity.setUniqueId(uniqueId);


        return entity;
    }

    @Override
    public List<Comment> getAll() {
        return get(() -> dal().findAll()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Comment getWithId(int id) {
        return get(() -> dal().findById(id)); //To change body of generated methods, choose Tools | Templates.
    }

    public Comment getCommentWithUniqueId(String uniqueId) {

        return get(() -> dal().findByUniqueId(uniqueId));

    }

    public List<Comment> getCommentsWithText(String text) {

        return get(() -> dal().findByText(text));
    }

    public List<Comment> getCommentsWithCreated(Date created) {

        return (List<Comment>) get(() -> dal().findByCreated(created));

    }

    public List<Comment> getCommentsWithPoints(int points) {

        return get(() -> dal().findByPoints(points));

    }

    public List<Comment> getCommentsWithReplys(int replys) {

        return get(() -> dal().findByReplys(replys));

    }

    public List<Comment> getCommentsWithIsReply(boolean isReply) {

        return (List<Comment>) get(() -> dal().findByIsReply(isReply));

    }

}
