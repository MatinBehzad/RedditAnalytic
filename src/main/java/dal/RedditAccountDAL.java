/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dal;

import entity.RedditAccount;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author matineh
 */
public class RedditAccountDAL extends GenericDAL<RedditAccount> {
    
   
  public RedditAccountDAL(){
      super(RedditAccount.class);
  }
  @Override
  public List<RedditAccount> findAll(){
      return findResults("RedditAccount.findAll",null);
  }
  @Override
  public RedditAccount findById(int id){
    HashMap<String, Object> map=new HashMap<>();
        map.put("id",id);
        return findResult("RedditAccount.findById",map);
 } 
public RedditAccount findByName(String name){
    
     HashMap<String, Object> map=new HashMap<>();
     map.put( "name", name );
     return findResult( "RedditAccount.findByName", map );
    }
public List<RedditAccount> findByLinkPoints( int linkPoints){
    HashMap<String, Object> map=new HashMap<>(); 
         map.put("linkPoints",linkPoints );
        return findResults("RedditAccount.findByLinkPoints",map); 
    
}
public List<RedditAccount> findByCommentPoints(int commentPoints){
    HashMap<String, Object> map=new HashMap<>(); 
         map.put("commentPoints",commentPoints );
        return findResults("RedditAccount.findByCommentPoints",map); 
}
public List<RedditAccount> findByCreated(Date created){
    
    HashMap<String, Object> map=new HashMap<>();
       map.put("created", created );
       return findResults("RedditAccount.findByCreated",map);
 } 

 }
