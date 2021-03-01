/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.TomcatStartUp;
import common.ValidationException;
import dal.EMFactory;
import entity.Comment;
import entity.Post;
import entity.RedditAccount;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import static org.hibernate.type.InstantType.FORMATTER;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 * @author matineh
 */

public class CommentLogicTest {
    
    private CommentLogic logic;
    private Comment expectedEntity;
    
    
    @BeforeAll
    final static void setUpBeforeClass() throws Exception {
        TomcatStartUp.createTomcat( "/RedditAnalytic", "common.ServletListener" );
    }

    @AfterAll
    final static void tearDownAfterClass() throws Exception {
        TomcatStartUp.stopAndDestroyTomcat();
    }
    
    @BeforeEach
    final void setUp() throws Exception {

        logic = LogicFactory.getFor( "Comment" );
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the account to not rely on any logic functionality , just for testing
      
    
        
    PostLogic pLogic = LogicFactory.getFor( "Post" );
    Post pTest = pLogic.getWithId(1);
    
    
    RedditAccountLogic rLogic = LogicFactory.getFor( "RedditAccount" );
    RedditAccount rTest = rLogic.getWithId(1);
    
    Date date=logic.convertStringToDate("2020-11-08 00:00:00");
    
    Comment entity=new Comment();
    
    
        entity.setRedditAccountId(rTest);
        entity.setPostId(pTest);
        entity.setUniqueId("junit1");
        entity.setText("junit5");
        entity.setCreated(date);
        entity.setPoints(2);
        entity.setReplys(4);
        entity.setIsReply(true);

        //get an instance of EntityManager
        EntityManagerFactory pm = EMFactory.getEMF();
        EntityManager em = pm.createEntityManager();
        //start a Transaction
        em.getTransaction().begin();
        //add an account to hibernate, account is now managed.
        //we use merge instead of add so we can get the updated generated ID.
        expectedEntity = em.merge(entity );
        
        //commit the changes
        em.getTransaction().commit();
        //close EntityManager
        em.close();
    }
    
    @AfterEach
    final void tearDown() throws Exception {
        if( expectedEntity != null ){
            logic.delete( expectedEntity );
        }
    }
    
    @Test
    final void testGetAll() {
        //get all the accounts from the DB
        List<Comment> list = logic.getAll();
        //store the size of list, this way we know how many accounts exits in DB
        int originalSize = list.size();
        //make sure account was created successfully
        assertNotNull( expectedEntity );
        //delete the new account
        logic.delete( expectedEntity );
        //get all accounts again
        list = logic.getAll();
        //the new size of accounts must be one less
        assertEquals( originalSize - 1, list.size() );
    }
    
    private void assertCommentEquals( Comment expected, Comment actual ) {
        //assert all field to guarantee they are the same
         long errorRangeInMilliSeconds = 10000;
        assertEquals( expected.getId(), actual.getId() );
        assertEquals( expected.getRedditAccountId().getId(), actual.getRedditAccountId().getId());
        assertEquals( expected.getPostId().getId(), actual.getPostId().getId());
        assertEquals( expected.getUniqueId(), actual.getUniqueId());
        assertEquals( expected.getText(), actual.getText());
        long timeInMilliSeconds1 = expected.getCreated().getTime() ;
        long timeInMilliSeconds2 = actual.getCreated().getTime(); 
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
        assertEquals( expected.getPoints(), actual.getPoints());
        assertEquals( expected.getIsReply(), actual.getIsReply());
    }
    
    @Test
    final void testGetWithId() {
        //using the id of test account get another account from logic
        Comment returnedComment = logic.getWithId( expectedEntity.getId() );
       //the two accounts (testAcounts and returnedAccounts) must be the same
        assertCommentEquals( expectedEntity, returnedComment);
    }
    
     @Test
    final void testGetCommentWithUniqueId() {
        Comment returnedComment = logic.getCommentWithUniqueId(expectedEntity.getUniqueId());

        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertCommentEquals( expectedEntity, returnedComment );
    }
    
   @Test
    final void testGetCommentsWithText(){
        
        int foundFull = 0;
        List<Comment> returnedComments = logic.getCommentsWithText(expectedEntity.getText());
        for( Comment comment: returnedComments ) {
            //all accounts must have the same password
            
            assertEquals( expectedEntity.getText(), comment.getText());
            //exactly one account must be the same
            if( comment.getId().equals( expectedEntity.getId() ) ){
                assertCommentEquals( expectedEntity, comment );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
        
    }
    
    
     @Test
    final void testGetCommentsWithCreated(){
        
        int foundFull = 0;
         long errorRangeInMilliSeconds = 10000;
        List<Comment> returnedComments = logic.getCommentsWithText(expectedEntity.getText());
        for( Comment comment: returnedComments ) {
            //all accounts must have the same password
            long timeInMilliSeconds1 = expectedEntity.getCreated().getTime() ;
            long timeInMilliSeconds2 = comment.getCreated().getTime(); 
            assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
            //exactly one account must be the same
            if( comment.getId().equals( expectedEntity.getId() ) ){
                assertCommentEquals( expectedEntity, comment );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
        
    }
    
    @Test
    final void testGetCommentsWithPoints(){
        
        int foundFull = 0;
        List<Comment> returnedComments = logic.getCommentsWithText(expectedEntity.getText());
        for( Comment comment: returnedComments ) {
            //all accounts must have the same password
            assertEquals( expectedEntity.getPoints(), comment.getPoints());
            //exactly one account must be the same
            if( comment.getId().equals( expectedEntity.getId() ) ){
                assertCommentEquals( expectedEntity, comment );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
        
    }
    
    @Test
    final void testGetCommentsWithReplys(){
        
        int foundFull = 0;
        List<Comment> returnedComments = logic.getCommentsWithText(expectedEntity.getText());
        for( Comment comment: returnedComments ) {
            //all accounts must have the same password
            assertEquals( expectedEntity.getReplys(), comment.getReplys());
            //exactly one account must be the same
            if( comment.getId().equals( expectedEntity.getId() ) ){
                assertCommentEquals( expectedEntity, comment );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
        
    }
    
    @Test
    final void testCommentsWithIsReply(){
        
        int foundFull = 0;
        List<Comment> returnedComments = logic.getCommentsWithText(expectedEntity.getText());
        for( Comment comment: returnedComments ) {
            //all accounts must have the same password
            assertEquals( expectedEntity.getIsReply(), comment.getIsReply());
            //exactly one account must be the same
            if( comment.getId().equals( expectedEntity.getId() ) ){
                assertCommentEquals( expectedEntity, comment );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
        
    }
    
       @Test
    final void testCreateEntityAndAdd() {
       
       Map<String, String[]> sampleMap = new HashMap<>();
       sampleMap.put( CommentLogic.UNIQUE_ID, new String[]{ "Hap" } );
       sampleMap.put( CommentLogic.TEXT, new String[]{ "Hello" } );
       sampleMap.put( CommentLogic.CREATED, new String[]{"2020-11-08 00:00:04"});
       sampleMap.put( CommentLogic.POINTS, new String[]{ "1" } );
       sampleMap.put( CommentLogic.REPLYS, new String[]{ "0" } );
       sampleMap.put( CommentLogic.IS_REPLY, new String[]{ "1" } );
       sampleMap.put( CommentLogic.POST_ID, new String[]{ "1" } );
       sampleMap.put( CommentLogic.REDDIT_ACCOUNT_ID, new String[]{ "1" } );
       
      
       
       PostLogic pLogic = LogicFactory.getFor( "Post" );
       Post p = pLogic.getWithId(1);
  
       RedditAccountLogic rLogic = LogicFactory.getFor( "RedditAccount" );
       RedditAccount r = rLogic.getWithId(1);
      
       Comment returnedComment = logic.createEntity( sampleMap );
       
       returnedComment.setRedditAccountId(r);
       returnedComment.setPostId(p);
       
       logic.add( returnedComment );
        
     returnedComment = logic.getCommentWithUniqueId(returnedComment.getUniqueId());

      assertEquals( sampleMap.get( CommentLogic.UNIQUE_ID )[ 0 ], returnedComment.getUniqueId());
      assertEquals( sampleMap.get( CommentLogic.TEXT)[ 0 ], returnedComment.getText());
       long errorRangeInMilliSeconds = 10000;
      long timeInMilliSeconds1 = returnedComment.getCreated().getTime() ; 
      long timeInMilliSeconds2=logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime();
      assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
      assertEquals( Integer.parseInt(sampleMap.get( CommentLogic.POST_ID)[ 0 ]),returnedComment.getRedditAccountId().getId());
      assertEquals( Integer.parseInt(sampleMap.get( CommentLogic.REDDIT_ACCOUNT_ID)[ 0 ]), returnedComment.getPostId().getId());
      assertEquals( sampleMap.get( CommentLogic.POINTS )[ 0 ],Integer.toString(returnedComment.getPoints()));
      assertEquals( sampleMap.get( CommentLogic.REPLYS)[ 0 ], Integer.toString(returnedComment.getReplys()));
      
    
      boolean test=returnedComment.getIsReply();
      if(test == true){
          
          assertEquals( sampleMap.get( CommentLogic.IS_REPLY)[ 0 ], "1");
          
      }
      
      else{
          assertEquals( sampleMap.get( CommentLogic.IS_REPLY)[ 0 ], "0");
      }
      
      
     // assertEquals( RedditAccount(sampleMap.get( CommentLogic.REDDIT_ACCOUNT_ID)[ 0 ]), returnedComment.getRedditAccountId());
     // assertEquals( sampleMap.get( CommentLogic.POST_ID)[ 0 ], returnedComment.getPostId());
      
      logic.delete( returnedComment );
    }
    
    @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( CommentLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
        sampleMap.put( CommentLogic.UNIQUE_ID, new String[]{ expectedEntity.getUniqueId() } );
        sampleMap.put( CommentLogic.TEXT, new String[]{ expectedEntity.getText()} );
        sampleMap.put( CommentLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );
        sampleMap.put( CommentLogic.POINTS, new String[]{ Integer.toString(expectedEntity.getPoints())} );
        sampleMap.put( CommentLogic.REPLYS, new String[]{ Integer.toString(expectedEntity.getReplys())} );
        String test=Boolean.toString(expectedEntity.getIsReply());
        if(test=="true"){
            
            sampleMap.put( CommentLogic.IS_REPLY, new String[]{"1"} );
        }
        else
        {
             sampleMap.put( CommentLogic.IS_REPLY, new String[]{"0"} );
        }
        
       PostLogic pLogic = LogicFactory.getFor( "Post" );
       Post p = pLogic.getWithId(1);
  
       RedditAccountLogic rLogic = LogicFactory.getFor( "RedditAccount" );
       RedditAccount r = rLogic.getWithId(1);
       
       Comment returnedComment = logic.createEntity( sampleMap );
       
       returnedComment.setRedditAccountId(r);
       returnedComment.setPostId(p);

       assertCommentEquals( expectedEntity, returnedComment );
    }
    
    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( CommentLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( CommentLogic.UNIQUE_ID, new String[]{ expectedEntity.getUniqueId() } );
            map.put( CommentLogic.TEXT, new String[]{ expectedEntity.getText()} );
            map.put( CommentLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated())} );
            map.put( CommentLogic.POINTS, new String[]{ Integer.toString(expectedEntity.getPoints())});
            map.put( CommentLogic.REPLYS, new String[]{ Integer.toString(expectedEntity.getReplys())} );
            
            
            String test=Boolean.toString(expectedEntity.getIsReply());
             if(test=="true"){
            
            sampleMap.put( CommentLogic.IS_REPLY, new String[]{"1"} );
            }
           else
           {
             sampleMap.put( CommentLogic.IS_REPLY, new String[]{"0"} );
           }
            
          };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.UNIQUE_ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.UNIQUE_ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.TEXT, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.TEXT, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.CREATED, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.CREATED, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.POINTS, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.POINTS, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.REPLYS, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.REPLYS, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.IS_REPLY, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.IS_REPLY, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
        
    }
    
    
     @Test
    final void testCreateEntityBadLengthValues() {
        
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( CommentLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( CommentLogic.UNIQUE_ID, new String[]{ expectedEntity.getUniqueId() } );
            map.put( CommentLogic.TEXT, new String[]{ expectedEntity.getText()} );
            map.put( CommentLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated())} );
            map.put( CommentLogic.POINTS, new String[]{ Integer.toString(expectedEntity.getPoints())});
            map.put( CommentLogic.REPLYS, new String[]{ Integer.toString(expectedEntity.getReplys())} );
            String test=Boolean.toString(expectedEntity.getIsReply());
             if("true".equals(test)){
            
            sampleMap.put( CommentLogic.IS_REPLY, new String[]{"1"} );
            }
           else
           {
             sampleMap.put( CommentLogic.IS_REPLY, new String[]{"0"} );
           }
            
          };
      
            IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            //from 97 inclusive to 123 exclusive
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.ID, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.ID, new String[]{ "12b" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.UNIQUE_ID, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.UNIQUE_ID, new String[]{ generateString.apply( 11 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.TEXT, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.TEXT, new String[]{ generateString.apply( 1001 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( CommentLogic.IS_REPLY, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( CommentLogic.IS_REPLY, new String[]{ generateString.apply( 2 ) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
    }
    
    @Test
    final void testCreateEntityEdgeValues() {
        IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };

        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( CommentLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( CommentLogic.UNIQUE_ID, new String[]{ generateString.apply( 1 ) } );
        sampleMap.put( CommentLogic.TEXT, new String[]{ generateString.apply( 1 ) } );
        sampleMap.put( CommentLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated())  } );
        sampleMap.put( CommentLogic.POINTS, new String[]{ Integer.toString(2)} );
        sampleMap.put( CommentLogic.REPLYS, new String[]{Integer.toString(2)} );
        sampleMap.put( CommentLogic.POST_ID, new String[]{ Integer.toString(1)} );
        sampleMap.put( CommentLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(1)} );
        
        String test=Boolean.toString(expectedEntity.getIsReply());
        if("true".equals(test)){
            sampleMap.put( CommentLogic.IS_REPLY, new String[]{"1"} );
        }
        else
        {
            sampleMap.put( CommentLogic.IS_REPLY, new String[]{"0"} );
        }
        

        //idealy every test should be in its own method

        Comment returnedComment= logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( CommentLogic.ID )[ 0 ] ), returnedComment.getId() );
        assertEquals( sampleMap.get( CommentLogic.UNIQUE_ID )[ 0 ], returnedComment.getUniqueId());
        assertEquals( sampleMap.get( CommentLogic.TEXT)[ 0 ], returnedComment.getText() );
        long errorRangeInMilliSeconds = 10000;
        long timeInMilliSeconds1 = returnedComment.getCreated().getTime() ; 
        long timeInMilliSeconds2=logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime();
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
        assertEquals( sampleMap.get( CommentLogic.POINTS)[ 0 ], Integer.toString(returnedComment.getPoints()));
        assertEquals( sampleMap.get( CommentLogic.REPLYS )[ 0 ], Integer.toString(returnedComment.getReplys()));
        boolean b= returnedComment.getIsReply();
        if(b==true){
        assertEquals( sampleMap.get( CommentLogic.IS_REPLY )[ 0 ], "1");  
        }
        else{
        assertEquals( sampleMap.get( CommentLogic.IS_REPLY )[ 0 ], "0");    
        }
        
        sampleMap = new HashMap<>();
        sampleMap.put(CommentLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put(CommentLogic.UNIQUE_ID, new String[]{ generateString.apply( 10 ) } );
        sampleMap.put(CommentLogic.TEXT, new String[]{ generateString.apply( 1000 ) } );
        sampleMap.put(CommentLogic.IS_REPLY, new String[]{"1"} );
        sampleMap.put(CommentLogic.POINTS, new String[]{Integer.toString( 1 ) } );
        sampleMap.put(CommentLogic.REPLYS, new String[]{Integer.toString( 1 ) } );
        sampleMap.put(CommentLogic.CREATED, new String[]{logic.convertDateToString(returnedComment.getCreated())});
    
        
        
        //idealy every test should be in its own method
        returnedComment = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( CommentLogic.ID )[ 0 ] ), returnedComment.getId() );
        assertEquals( sampleMap.get( CommentLogic.UNIQUE_ID )[ 0 ], returnedComment.getUniqueId());
        assertEquals( sampleMap.get( CommentLogic.TEXT)[ 0 ], returnedComment.getText() );
        timeInMilliSeconds1 = returnedComment.getCreated().getTime() ; 
        timeInMilliSeconds2=logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime();
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
        assertEquals( sampleMap.get( CommentLogic.POINTS)[ 0 ], Integer.toString(returnedComment.getPoints()));
        assertEquals( sampleMap.get( CommentLogic.REPLYS )[ 0 ], Integer.toString(returnedComment.getReplys()));
        boolean c= returnedComment.getIsReply();
        if(c==true){
        assertEquals( sampleMap.get( CommentLogic.IS_REPLY )[ 0 ], "1");  
        }
        else{
        assertEquals( sampleMap.get( CommentLogic.IS_REPLY )[ 0 ], "0");    
        }
    }
    
     @Test
    final void testGetColumnNames() {
        List<String> list = logic.getColumnNames();
        assertEquals( Arrays.asList("ID", " REPLYS", "IS_REPLY", "POINTS", "CREATED", "TEXT", "UNIQUE_ID", "REDDIT_ACCOUNT_ID", "POST_ID"), list );
        
    }

    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals(  Arrays.asList(CommentLogic.ID, CommentLogic.REPLYS, CommentLogic.IS_REPLY, CommentLogic.POINTS, CommentLogic.CREATED, CommentLogic.TEXT, CommentLogic.UNIQUE_ID, CommentLogic.REDDIT_ACCOUNT_ID, CommentLogic.POST_ID), list );
    }

    @Test
    final void testExtractDataAsList() {
        
        
        List<?> list = logic.extractDataAsList( expectedEntity );
        assertEquals( expectedEntity.getId(), list.get( 0 ) );
        assertEquals( expectedEntity.getReplys(), list.get( 1 ) );
        assertEquals( expectedEntity.getIsReply(), list.get( 2) );
        assertEquals( expectedEntity.getPoints(), list.get( 3 ) );
        assertEquals( expectedEntity.getCreated(), list.get(4) );
        assertEquals( expectedEntity.getText(), list.get( 5) );
        assertEquals( expectedEntity.getUniqueId(), list.get(6) );
        
        
        
    }
}

 
