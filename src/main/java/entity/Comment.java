/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 *
 * @author matineh
 */
@Entity
@Table( name = "Comment", catalog = "redditanalytic", schema = "" )
@NamedQueries( {
    
    @NamedQuery( name = "Comment.findAll", query = "SELECT a FROM Comment a" ),
    @NamedQuery( name = "Comment.findById", query = "SELECT a FROM Comment a WHERE a.id = :id" ),
    @NamedQuery( name = "Comment.findByText", query = "SELECT a FROM Comment a WHERE a.text = :text" ),
    @NamedQuery( name = "Comment.findByCreated", query = "SELECT a FROM Comment a WHERE a.created = :created" ),
    @NamedQuery( name = "Comment.findByPoints", query = "SELECT a FROM Comment a WHERE a.points = :points" ),
    @NamedQuery( name = "Comment.findByReplys", query = "SELECT a FROM Comment a WHERE a.replys = :replys" ),
    @NamedQuery( name = "Comment.findByUniqueId", query = "SELECT a FROM Comment a WHERE a.uniqueId = :uniqueId" ),
    @NamedQuery( name = "Comment.findByIsReply", query = "SELECT a FROM Comment a WHERE a.isReply = :isReply" ),
    
})
public class Comment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    @Basic( optional = false )
    @Column( name = "id" )
    private Integer id;
    @Basic( optional = false )
    @NotNull
    @Size( min = 1, max = 10 )
    @Column( name = "unique_id" )
    private String uniqueId;
    @Basic( optional = false )
    @NotNull
    @Size( min = 1, max = 1000 )
    @Column( name = "text" )
    private String text;
    @Basic( optional = false )
    @NotNull
    @Size( min = 1, max = 30 )
    @Column( name = "created" )
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date created;
    @Basic( optional = false )
    @NotNull
    @Size( min = 1, max = 20 )
    @Column( name = "points" )
    private int points;
    @Basic( optional = false )
    @NotNull
    @Size( min = 1, max = 20 )
    @Column( name = "replys" )
    private int replys;
    @Basic( optional = false )
    @NotNull
    @Size(max = 1)
    @Column( name = "is_reply" )
    private boolean isReply;
    @JoinColumn( name = "post_id", referencedColumnName = "id" )
    @ManyToOne( optional = false, fetch = FetchType.LAZY )
    @Size(max = 30)
    private Post postId;
    @JoinColumn( name = "reddit_account_id", referencedColumnName = "id" )
    @ManyToOne( optional = false, fetch = FetchType.LAZY )
    @Size(max = 20)
    private RedditAccount redditAccountId;
    
    
public Comment(){
      
  }
public Comment(Integer id){
    
    this.id=id;
    
}
public Comment(Integer id,String text,Date created,int points,int replys,boolean isReply){
        this.id=id;
        this.text=text;
        this.created=created;
        this.points=points;
        this.replys=replys;
        this.isReply=isReply;
}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
     public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
     public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
    public int getReplys() {
        return replys;
    }

    public void setReplys(int replys) {
        this.replys = replys;
    }
    public boolean getIsReply() {
        return isReply;
    }

    public void setIsReply(boolean isReply) {
        this.isReply = isReply;
    }
    public Post getPostId() {
        return postId ;
    }

    public void setPostId(Post postId) {
        this.postId= postId;
    }
    public  RedditAccount getRedditAccountId() {
        return redditAccountId;
    }

    public void setRedditAccountId(RedditAccount redditAccountId) {
        this.redditAccountId= redditAccountId;
    }
    

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Comment)) {
            return false;
        }
        Comment other = (Comment) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Comment[ id=" + id + " ]";
    }
    
}
