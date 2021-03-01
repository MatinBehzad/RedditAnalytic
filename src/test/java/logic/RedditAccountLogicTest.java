/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.TomcatStartUp;
import common.ValidationException;
import dal.EMFactory;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author matineh
 */
public class RedditAccountLogicTest {
    
    private RedditAccountLogic logic;
    private RedditAccount expectedEntity;

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

        logic = LogicFactory.getFor( "RedditAccount" );
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the account to not rely on any logic functionality , just for testing
        Date date=logic.convertStringToDate("2020-11-08 00:00:00");
        RedditAccount entity = new RedditAccount();
        entity.setName("Junit 5 Test" );
        entity.setLinkPoints(1);
        entity.setCommentPoints(1);
        entity.setCreated(date);

        //get an instance of EntityManager
        EntityManagerFactory pm = EMFactory.getEMF();
        EntityManager em = pm.createEntityManager();
        //start a Transaction
        em.getTransaction().begin();
        //add an account to hibernate, account is now managed.
        //we use merge instead of add so we can get the updated generated ID.
        expectedEntity = em.merge( entity );
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
        List<RedditAccount> list = logic.getAll();
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

    /**
     * helper method for testing all account fields
     *
     * @param expected
     * @param actual
     */
    private void assertAccountEquals( RedditAccount expected, RedditAccount actual ) {
         long errorRangeInMilliSeconds = 10000;
        //assert all field to guarantee they are the same
        assertEquals( expected.getId(), actual.getId() );
        assertEquals( expected.getName(), actual.getName());
        assertEquals( expected.getLinkPoints(), actual.getLinkPoints() );
        long timeInMilliSeconds1 = expected.getCreated().getTime() ;
        long timeInMilliSeconds2 = actual.getCreated().getTime(); 
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
        assertEquals( expected.getCommentPoints(), actual.getCommentPoints());
    }

    @Test
    final void testGetWithId() {
        //using the id of test account get another account from logic
        RedditAccount returnedAccount = logic.getWithId( expectedEntity.getId() );

        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertAccountEquals( expectedEntity, returnedAccount );
    }

    @Test
    final void testGetRedditAccountWithName() {
        RedditAccount returnedAccount = logic.getRedditAccountWithName(expectedEntity.getName() );

        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertAccountEquals( expectedEntity, returnedAccount );
    }

    @Test
    final void testGetRedditAccountsWithLinkPoints() {
        int foundFull = 0;
        List<RedditAccount> returnedAccounts = logic.getRedditAccountsWithLinkPoints(expectedEntity.getLinkPoints());
        for( RedditAccount account: returnedAccounts ) {
            //all accounts must have the same password
            assertEquals( expectedEntity.getLinkPoints(), account.getLinkPoints());
            //exactly one account must be the same
            if( account.getId().equals( expectedEntity.getId() ) ){
                assertAccountEquals( expectedEntity, account );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }

    @Test
    final void testGetRedditAccountsWithCommentPoints() {
        int foundFull = 0;
        List<RedditAccount> returnedAccounts = logic.getRedditAccountsWithCommentPoints(expectedEntity.getCommentPoints());
        for( RedditAccount account: returnedAccounts ) {
            //all accounts must have the same password
            assertEquals( expectedEntity.getCommentPoints(), account.getCommentPoints());
            //exactly one account must be the same
            if( account.getId().equals( expectedEntity.getId() ) ){
                assertAccountEquals( expectedEntity, account );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }
    
     @Test
    final void testGetRedditAccountsWithCreated() {
        long errorRangeInMilliSeconds = 10000;
        int foundFull = 0;
        List<RedditAccount> returnedAccounts = logic.getRedditAccountsWithCreated(expectedEntity.getCreated());
        for( RedditAccount account: returnedAccounts ) {
            //all accounts must have the same password
            long timeInMilliSeconds1 = expectedEntity.getCreated().getTime() ;
            long timeInMilliSeconds2 = account.getCreated().getTime(); 
            assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
            //exactly one account must be the same
            if( account.getId().equals( expectedEntity.getId() ) ){
                assertAccountEquals( expectedEntity, account );
                foundFull++;
            }
        }
        assertEquals( 1, foundFull, "if zero means not found, if more than one means duplicate" );
    }

  

    @Test
    final void testCreateEntityAndAdd() {
        
        long errorRangeInMilliSeconds = 10000;
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( RedditAccountLogic.NAME, new String[]{ "Test" } );
        sampleMap.put( RedditAccountLogic.LINK_POINTS, new String[]{"1"} );
        sampleMap.put( RedditAccountLogic.COMMENT_POINTS, new String[]{"1"} );
        sampleMap.put( RedditAccountLogic.CREATED, new String[]{"2020-11-08 00:00:04"} );
        

        RedditAccount returnedAccount = logic.createEntity( sampleMap );
        logic.add( returnedAccount );

        returnedAccount = logic.getRedditAccountWithName(returnedAccount.getName());

        assertEquals( sampleMap.get( RedditAccountLogic.NAME )[ 0 ], returnedAccount.getName() );
        assertEquals( sampleMap.get( RedditAccountLogic.LINK_POINTS )[ 0 ],  Integer.toString(returnedAccount.getLinkPoints()) );
        assertEquals( sampleMap.get( RedditAccountLogic.COMMENT_POINTS )[ 0 ],  Integer.toString(returnedAccount.getCommentPoints()) );
         long timeInMilliSeconds1 = returnedAccount.getCreated().getTime() ;
         long timeInMilliSeconds2 = logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime(); 
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
        

        logic.delete( returnedAccount );
    }

    @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( RedditAccountLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
        sampleMap.put( RedditAccountLogic.NAME, new String[]{ expectedEntity.getName()} );
        sampleMap.put( RedditAccountLogic.LINK_POINTS, new String[]{ Integer.toString(expectedEntity.getLinkPoints())} );
        sampleMap.put( RedditAccountLogic.COMMENT_POINTS, new String[]{ Integer.toString(expectedEntity.getCommentPoints()) } );
        sampleMap.put( RedditAccountLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );
        

        RedditAccount returnedAccount = logic.createEntity( sampleMap );

        assertAccountEquals( expectedEntity, returnedAccount );
    }

    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( RedditAccountLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( RedditAccountLogic.NAME, new String[]{ expectedEntity.getName() } );
            map.put( RedditAccountLogic.LINK_POINTS, new String[]{  Integer.toString(expectedEntity.getLinkPoints() ) } );
            map.put( RedditAccountLogic.COMMENT_POINTS, new String[]{ Integer.toString(expectedEntity.getCommentPoints()) } );
            map.put( RedditAccountLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );
        };

        //idealy every test should be in its own method
        fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.ID, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.ID, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.NAME, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.NAME, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.LINK_POINTS, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.LINK_POINTS, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );

        fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.COMMENT_POINTS, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.COMMENT_POINTS, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
        
         fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.CREATED, null );
        assertThrows( NullPointerException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.CREATED, new String[]{} );
        assertThrows( IndexOutOfBoundsException.class, () -> logic.createEntity( sampleMap ) );
    }

    @Test
    final void testCreateEntityBadLengthValues() {
       Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = ( Map<String, String[]> map ) -> {
            map.clear();
            map.put( RedditAccountLogic.ID, new String[]{ Integer.toString( expectedEntity.getId() ) } );
            map.put( RedditAccountLogic.NAME, new String[]{ expectedEntity.getName() } );
            map.put( RedditAccountLogic.LINK_POINTS, new String[]{  Integer.toString(expectedEntity.getLinkPoints() ) } );
            map.put( RedditAccountLogic.COMMENT_POINTS, new String[]{ Integer.toString(expectedEntity.getCommentPoints()) } );
            map.put( RedditAccountLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );
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
        sampleMap.replace( AccountLogic.ID, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( AccountLogic.ID, new String[]{ "12b" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );


        fillMap.accept( sampleMap );
        sampleMap.replace( RedditAccountLogic.NAME, new String[]{ "" } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
        sampleMap.replace( RedditAccountLogic.NAME, new String[]{ generateString.apply( 101) } );
        assertThrows( ValidationException.class, () -> logic.createEntity( sampleMap ) );
    }

    @Test
    final void testCreateEntityEdgeValues() {
        
        long errorRangeInMilliSeconds = 10000;
        IntFunction<String> generateString = ( int length ) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            return new Random().ints( 'a', 'z' + 1 ).limit( length )
                    .collect( StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append )
                    .toString();
        };

        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put( RedditAccountLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.NAME, new String[]{ generateString.apply( 1 ) } );
        sampleMap.put( RedditAccountLogic.LINK_POINTS, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.COMMENT_POINTS, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );

        //idealy every test should be in its own method
        RedditAccount returnedAccount = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( RedditAccountLogic.ID )[ 0 ] ), returnedAccount.getId() );
        assertEquals( sampleMap.get( RedditAccountLogic.NAME)[ 0 ], returnedAccount.getName() );
        assertEquals( sampleMap.get( RedditAccountLogic.COMMENT_POINTS )[ 0 ], Integer.toString(returnedAccount.getCommentPoints()) );
        assertEquals( sampleMap.get( RedditAccountLogic.LINK_POINTS )[ 0 ], Integer.toString(returnedAccount.getLinkPoints()));
         long timeInMilliSeconds1 = returnedAccount.getCreated().getTime() ; 
        long timeInMilliSeconds2=logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime();
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);

        sampleMap = new HashMap<>(); 
        
        sampleMap.put( RedditAccountLogic.ID, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.NAME, new String[]{ generateString.apply( 100 ) } );
        sampleMap.put( RedditAccountLogic.LINK_POINTS, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.COMMENT_POINTS, new String[]{ Integer.toString( 1 ) } );
        sampleMap.put( RedditAccountLogic.CREATED, new String[]{ logic.convertDateToString(expectedEntity.getCreated()) } );

        //idealy every test should be in its own method
        returnedAccount = logic.createEntity( sampleMap );
        assertEquals( Integer.parseInt( sampleMap.get( RedditAccountLogic.ID )[ 0 ] ), returnedAccount.getId() );
        assertEquals( sampleMap.get( RedditAccountLogic.NAME)[ 0 ], returnedAccount.getName() );
        assertEquals( sampleMap.get( RedditAccountLogic.COMMENT_POINTS )[ 0 ], Integer.toString(returnedAccount.getCommentPoints()) );
        assertEquals( sampleMap.get( RedditAccountLogic.LINK_POINTS )[ 0 ], Integer.toString(returnedAccount.getLinkPoints()));
        timeInMilliSeconds1 = returnedAccount.getCreated().getTime() ; 
        timeInMilliSeconds2=logic.convertStringToDate(sampleMap.get( CommentLogic.CREATED)[ 0 ]).getTime();
        assertTrue(Math.abs(timeInMilliSeconds1-timeInMilliSeconds2)< errorRangeInMilliSeconds);
    }

    
     
    @Test
    final void testGetColumnNames() {
       
        List<String> list = logic.getColumnNames();
        assertEquals( Arrays.asList( "ID", "name", "link_points", "comment_points","created" ), list );
    }

    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals( Arrays.asList( RedditAccountLogic.ID, RedditAccountLogic.NAME, RedditAccountLogic.LINK_POINTS, RedditAccountLogic.COMMENT_POINTS,RedditAccountLogic.CREATED ), list );
    }

    @Test
    final void testExtractDataAsList() {
        List<?> list = logic.extractDataAsList( expectedEntity );
        assertEquals( expectedEntity.getId(), list.get( 0 ) );
        assertEquals( expectedEntity.getName(), list.get( 1 ) );
        assertEquals( expectedEntity.getLinkPoints(), list.get( 2 ) );
        assertEquals( expectedEntity.getCommentPoints(), list.get( 3 ) );
        assertEquals( expectedEntity.getCreated(), list.get( 4 ) );
    }
    
}
