/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.ValidationException;
import dal.RedditAccountDAL;
import entity.RedditAccount;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.ObjIntConsumer;

/**
 *
 * @author matineh
 */

public class RedditAccountLogic extends GenericLogic <RedditAccount, RedditAccountDAL> {
    
    
 public  static final String COMMENT_POINTS = "comment_points";
 public static final String LINK_POINTS  = "link_points";
 public  static final String CREATED = "created";
 public static final String  NAME = "name";
 public static final String ID = "id";
 
 
 RedditAccountLogic() {
        super( new RedditAccountDAL() );
   }

    @Override
    public List<String> getColumnNames() {
        return Arrays.asList( "ID", "name", "link_points", "comment_points","created" ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<String> getColumnCodes() {
         return Arrays.asList( ID,NAME ,LINK_POINTS  ,COMMENT_POINTS, CREATED ); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<?> extractDataAsList(RedditAccount e) {
        return Arrays.asList(e.getId(),e.getName(), e.getLinkPoints(), e.getCommentPoints(), e.getCreated()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RedditAccount createEntity(Map<String, String[]> parameterMap) {
        
        if (parameterMap == null) {
            throw new NullPointerException("parameterMap cannot be null");
        }
        RedditAccount entity = new RedditAccount();

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

        String name = parameterMap.get(NAME)[0];
        String link_points = parameterMap.get(LINK_POINTS)[0];
        String comment_points = parameterMap.get(COMMENT_POINTS)[0];
        String created = parameterMap.get(CREATED)[0];
        

        //validate the data
        validator.accept(name, 100);
        


        //set values on entity
        entity.setName(name);
        entity.setLinkPoints(Integer.parseInt(link_points));
        entity.setCreated(convertStringToDate(created));
        entity.setCommentPoints(Integer.parseInt(comment_points));
        
        return entity; //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<RedditAccount> getAll() {
       return get(() -> dal().findAll()); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RedditAccount getWithId(int id) {
        return get( () -> dal().findById( id )); //To change body of generated methods, choose Tools | Templates.
    }
    
    public RedditAccount getRedditAccountWithName(String name){
        
        return get(() -> dal().findByName(name));
        
    }
    public List<RedditAccount> getRedditAccountsWithLinkPoints(int linkPoints){
    
       return get(() -> dal().findByLinkPoints(linkPoints));
    }
   public List<RedditAccount> getRedditAccountsWithCommentPoints(int commentPoints){
       
       return get(() -> dal().findByCommentPoints(commentPoints));
   }
   public List<RedditAccount> getRedditAccountsWithCreated(Date created){
       
       return get(() -> dal(). findByCreated(created));
       
   }
}
