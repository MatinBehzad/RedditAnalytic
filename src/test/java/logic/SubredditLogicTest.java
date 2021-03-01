/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import javax.persistence.EntityManagerFactory;

import common.TomcatStartUp;
import common.ValidationException;
import dal.EMFactory;
import entity.Account;
import entity.Subreddit;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author user
 */
public class SubredditLogicTest {

    private SubredditLogic logic;
    private Subreddit expectedSubreddit;

    @BeforeAll
    final static void setUpBeforeClass() throws Exception {
        TomcatStartUp.createTomcat("/RedditAnalytic", "common.ServletListener");
    }

    @AfterAll
    final static void tearDownAfterClass() throws Exception {
        TomcatStartUp.stopAndDestroyTomcat();
    }

    @BeforeEach
    final void setUp() throws Exception {

        logic = LogicFactory.getFor("Subreddit");
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the account to not rely on any logic functionality , just for testing

        Subreddit subreddit = new Subreddit();
        subreddit.setName("Junit");
        subreddit.setSubscribers(2);
        subreddit.setUrl("JunitTestUrl");

        //get an instance of EntityManager
        EntityManagerFactory pm = EMFactory.getEMF();
        EntityManager em = pm.createEntityManager();
        //start a Transaction
        em.getTransaction().begin();
        //add an account to hibernate, account is now managed.
        //we use merge instead of add so we can get the updated generated ID.
        expectedSubreddit = em.merge(subreddit);
        //commit the changes
        em.getTransaction().commit();
        //close EntityManager
        em.close();
    }

    @AfterEach
    final void tearDown() throws Exception {
        if (expectedSubreddit != null) {
            logic.delete(expectedSubreddit);
        }
    }

    @Test
    final void testGetAll() {
        //get all the accounts from the DB
        List<Subreddit> list = logic.getAll();
        //store the size of list, this way we know how many accounts exits in DB
        int originalSize = list.size();

        //make sure account was created successfully
        assertNotNull(expectedSubreddit);
        //delete the new account
        logic.delete(expectedSubreddit);

        //get all accounts again
        list = logic.getAll();
        //the new size of accounts must be one less
        assertEquals(originalSize - 1, list.size());
    }

    /**
     * helper method for testing all account fields
     *
     * @param expected
     * @param actual
     */
    private void assertSubredditEquals(Subreddit expected, Subreddit actual) {

        //assert all field to guarantee they are the same
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getSubscribers(), actual.getSubscribers());
        assertEquals(expected.getUrl(), actual.getUrl());
    }

    @Test
    final void testGetWithUrl() {
        Subreddit returnedSubreddit = logic.getSubredditWithURL(expectedSubreddit.getUrl());
        assertSubredditEquals(expectedSubreddit, returnedSubreddit);

    }

    @Test
    final void testGetWithId() {
        //using the id of test account get another account from logic
        Subreddit returnedSubreddit = logic.getWithId(expectedSubreddit.getId());

        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertSubredditEquals(expectedSubreddit, returnedSubreddit);
    }

    @Test
    final void testGetRedditSubredditWithSubscribers() {
        List<Subreddit> returnedSubreddit = logic.getSubredditWithSubscribers(expectedSubreddit.getSubscribers());
        int i = returnedSubreddit.size();
//        assertNotNull(expectedSubreddit);
        logic.delete(expectedSubreddit);
        returnedSubreddit = logic.getSubredditWithSubscribers(expectedSubreddit.getSubscribers());
        int originalSize = returnedSubreddit.size();
        //the two accounts (testAcounts and returnedAccounts) must be the same
        assertEquals(originalSize - 1<0?0:originalSize - 1, returnedSubreddit.size());
    }

    @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put(SubredditLogic.ID, new String[]{Integer.toString(expectedSubreddit.getId())});
        sampleMap.put(SubredditLogic.NAME, new String[]{expectedSubreddit.getName()});
        sampleMap.put(SubredditLogic.SUBSCRIBERS, new String[]{Integer.toString(expectedSubreddit.getSubscribers())});
        sampleMap.put(SubredditLogic.URL, new String[]{expectedSubreddit.getUrl()});

        Subreddit returnedSubreddit = logic.createEntity(sampleMap);

        assertSubredditEquals(expectedSubreddit, returnedSubreddit);
    }

    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = (Map<String, String[]> map) -> {
            map.clear();
            sampleMap.put(SubredditLogic.ID, new String[]{Integer.toString(expectedSubreddit.getId())});
            sampleMap.put(SubredditLogic.NAME, new String[]{expectedSubreddit.getName()});
            sampleMap.put(SubredditLogic.SUBSCRIBERS, new String[]{Integer.toString(expectedSubreddit.getSubscribers())});
            sampleMap.put(SubredditLogic.URL, new String[]{expectedSubreddit.getUrl()});

        };

        //idealy every test should be in its own method
        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.ID, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.ID, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.NAME, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.NAME, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.SUBSCRIBERS, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.SUBSCRIBERS, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.URL, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.URL, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

    }

   
    @Test
    final void testCreateEntityBadLengthValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = (Map<String, String[]> map) -> {
            map.clear();
            sampleMap.put(SubredditLogic.ID, new String[]{Integer.toString(expectedSubreddit.getId())});
            sampleMap.put(SubredditLogic.NAME, new String[]{expectedSubreddit.getName()});
            sampleMap.put(SubredditLogic.SUBSCRIBERS, new String[]{Integer.toString(expectedSubreddit.getSubscribers())});
            sampleMap.put(SubredditLogic.URL, new String[]{expectedSubreddit.getUrl()});
        };

        IntFunction<String> generateString = (int length) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            //from 97 inclusive to 123 exclusive
            return new Random().ints('a', 'z' + 1).limit(length)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        };

        //idealy every test should be in its own method
        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.ID, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.ID, new String[]{"12b"});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.NAME, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.NAME, new String[]{generateString.apply(101)});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.SUBSCRIBERS, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.SUBSCRIBERS, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(SubredditLogic.URL, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(SubredditLogic.URL, new String[]{generateString.apply(257)});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

    }

    @Test
    final void testGetColumnNames() {

        List<String> list = logic.getColumnNames();
        assertEquals(Arrays.asList("id", "name", "url", "subscribers"), list);
    }

    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals(Arrays.asList(SubredditLogic.ID, SubredditLogic.NAME, SubredditLogic.URL, SubredditLogic.SUBSCRIBERS), list);
    }

    @Test
    final void testExtractDataAsList() {
        List<?> list = logic.extractDataAsList(expectedSubreddit);
        assertEquals(expectedSubreddit.getId(), list.get(0));
        assertEquals(expectedSubreddit.getName(), list.get(1));
        assertEquals(expectedSubreddit.getUrl(), list.get(2));
        assertEquals(expectedSubreddit.getSubscribers(), list.get(3));

    }
}
