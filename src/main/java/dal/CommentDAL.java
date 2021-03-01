/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dal;

import entity.Comment;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author matineh
 */
public class CommentDAL extends GenericDAL<Comment> {
    
    public CommentDAL(){
        super(Comment.class);
    }

    @Override
    public List<Comment> findAll() {
       return findResults("Comment.findAll",null);//To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Comment findById(int id) {
        HashMap<String, Object> map=new HashMap<>();
        map.put("id",id);
        return findResult("Comment.findById",map); //To change body of generated methods, choose Tools | Templates.
    }
    
    public List<Comment> findByText(String text){
       HashMap<String, Object> map=new HashMap<>();
        map.put( "text", text );
        return findResults( "Comment.findByText", map );
       
     }
    
    public Comment findByCreated(Date created){
       HashMap<String, Object> map=new HashMap<>();
       map.put("created", created );
       return findResult("Comment.findByCreated",map);
      }
    
    public List<Comment> findByPoints(int points){
       HashMap<String, Object> map=new HashMap<>(); 
       map.put("points", points);
       return findResults("Comment.findByPoints",map);
     }
    public List<Comment> findByReplys(int replys){
         HashMap<String, Object> map=new HashMap<>(); 
         map.put("replys", replys);
        return findResults("Comment.findByReplys",map); 
    }
    public Comment findByUniqueId(String uniqueId){
         HashMap<String, Object> map=new HashMap<>();
         map.put("uniqueId", uniqueId);
        return findResult("Comment.findByUniqueId",map); 
    }
    
    public Comment findByIsReply(boolean isReply){
         HashMap<String, Object> map=new HashMap<>();
         map.put("isReply", isReply);
        return findResult("Comment.findByIsReply",map); 
        
    }
    
    
}
