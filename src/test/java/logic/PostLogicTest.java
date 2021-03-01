/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package logic;

import common.TomcatStartUp;
import common.ValidationException;
import dal.EMFactory;
import entity.Post;
import entity.RedditAccount;
import entity.Subreddit;
import java.util.Arrays;
import java.util.Date;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author kosio
 */
public class PostLogicTest {

    private PostLogic logic;
    private Post expectedEntity;
    private final String TEST_DATE = "2020-11-08 00:00:00";

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
        logic = LogicFactory.getFor("Post");
        /* **********************************
         * ***********IMPORTANT**************
         * **********************************/
        //we only do this for the test.
        //always create Entity using logic.
        //we manually make the account to not rely on any logic functionality , just for testing

        Date date = logic.convertStringToDate(TEST_DATE);

        Post entity = new Post();

        RedditAccountLogic raLogic = LogicFactory.getFor("RedditAccount");
        RedditAccount ra = raLogic.getWithId(1);
        entity.setRedditAccountId(ra);

        SubredditLogic sLogic = LogicFactory.getFor("Subreddit");
        Subreddit subreddit = sLogic.getWithId(1);
        entity.setSubredditId(subreddit);

        entity.setUniqueId("TestUID");
        entity.setPoints(22);
        entity.setCommentCount(0);
        entity.setTitle("TestTitle");
        entity.setCreated(date);

        EntityManager em = EMFactory.getEMF().createEntityManager();
        em.getTransaction().begin();
        expectedEntity = em.merge(entity);
        em.getTransaction().commit();
        em.close();

    }

    @AfterEach
    final void tearDown() throws Exception {
        if (expectedEntity != null) {
            logic.delete(expectedEntity);
        }
    }

    @Test
    final void testGetAll() {
        List<Post> list = logic.getAll();
        int originalSize = list.size();

        assertNotNull(expectedEntity);
        logic.delete(expectedEntity);

        list = logic.getAll();
        assertEquals(originalSize - 1, list.size());
    }

    private void assertPostEquals(Post expected, Post actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRedditAccountId().getId(), actual.getRedditAccountId().getId());
        assertEquals(expected.getSubredditId().getId(), actual.getSubredditId().getId());
        assertEquals(expected.getUniqueID(), actual.getUniqueID());
        assertEquals(expected.getPoints(), actual.getPoints());
        assertEquals(expected.getCommentCount(), actual.getCommentCount());
        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getCreated(), actual.getCreated());
    }

    @Test
    final void testGetWithId() {
        Post returnedPost = logic.getWithId(expectedEntity.getId());
        assertPostEquals(expectedEntity, returnedPost);

    }

    @Test
    final void testGetPostWithUniqueId() {
        Post returnedPost = logic.getPostWithUniqueId(expectedEntity.getUniqueID());
        assertPostEquals(expectedEntity, returnedPost);
    }

    @Test //REVIEW!!
    final void testGetPostWithPoints() {
        int foundFull = 0;
        List<Post> returnedPosts = logic.getPostWithPoints(expectedEntity.getPoints());
        for (Post post : returnedPosts) {
            assertEquals(expectedEntity.getPoints(), post.getPoints());
            if (post.getId().equals(expectedEntity.getId())) {
                assertPostEquals(expectedEntity, post);
                foundFull++;
            }
        }
        assertEquals(1, foundFull, "if zero means not found, if more than one means duplicate");
    }

    @Test //REVIEW
    final void testGetPostWithCommentCount() {
        int foundFull = 0;
        List<Post> returnedPosts = logic.getPostsWithCommentCount(expectedEntity.getCommentCount());
        for (Post post : returnedPosts) {
            assertEquals(expectedEntity.getCommentCount(), post.getCommentCount());
            if (post.getId().equals(expectedEntity.getId())) {
                assertPostEquals(expectedEntity, post);
                foundFull++;
            }
        }
        assertEquals(1, foundFull, "if zero means not found, if more than one means duplicate");
    }

    @Test
    final void testGetPostWithAuthorID() {
        int foundFull = 0;
        List<Post> returnedPosts = logic.getPostsWithAuthorID(expectedEntity.getRedditAccountId().getId());
        for (Post post : returnedPosts) {
            assertEquals(expectedEntity.getRedditAccountId().getId(), post.getRedditAccountId().getId());
            if (post.getId().equals(expectedEntity.getId())) {
                assertPostEquals(expectedEntity, post);
                foundFull++;
            }
        }
        assertEquals(1, foundFull, "if zero means not found, if more than one means duplicate");
    }

    @Test
    final void testGetPostWithTitle() {
        int foundFull = 0;
        List<Post> returnedPosts = logic.getPostsWithTitle(expectedEntity.getTitle());
        for (Post post : returnedPosts) {
            assertEquals(expectedEntity.getTitle(), post.getTitle());
            if (post.getId().equals(expectedEntity.getId())) {
                assertPostEquals(expectedEntity, post);
                foundFull++;
            }
        }
        assertEquals(1, foundFull, "if zero means not found, if more than one means duplicate");
    }

    @Test
    final void testGetPostWithCreated() {
        int foundFull = 0;
        long errorRangeInMilliseconds = 1000;
        List<Post> returnedPost = logic.getPostsWithCreated(expectedEntity.getCreated());
        for (Post post : returnedPost) {
            long timeInMilliseconds1 = expectedEntity.getCreated().getTime();
            long timeInMilliseconds2 = post.getCreated().getTime();
            assertTrue(Math.abs(timeInMilliseconds1 - timeInMilliseconds2) < errorRangeInMilliseconds);

            if (post.getId().equals(expectedEntity.getId())) {
                assertPostEquals(expectedEntity, post);
                foundFull++;
            }
        }
        assertEquals(1, foundFull, "if zero means not found, if more than one means duplicate");
    }

    @Test
    final void testCreateEntity() {
        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put(PostLogic.ID, new String[]{Integer.toString(expectedEntity.getId())});
        sampleMap.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(expectedEntity.getRedditAccountId().getId())});
        sampleMap.put(PostLogic.SUBREDDIT_ID, new String[]{Integer.toString(expectedEntity.getSubredditId().getId())});
        sampleMap.put(PostLogic.UNIQUE_ID, new String[]{expectedEntity.getUniqueID()});
        sampleMap.put(PostLogic.POINTS, new String[]{Integer.toString(expectedEntity.getPoints())});
        sampleMap.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(expectedEntity.getCommentCount())});
        sampleMap.put(PostLogic.TITLE, new String[]{expectedEntity.getTitle()});
        sampleMap.put(PostLogic.CREATED, new String[]{TEST_DATE});

        Post returnedPost = logic.createEntity(sampleMap);

        RedditAccountLogic raLogic = LogicFactory.getFor("RedditAccount");
        RedditAccount ra = raLogic.getWithId(1);
        returnedPost.setRedditAccountId(ra);

        SubredditLogic sLogic = LogicFactory.getFor("Subreddit");
        Subreddit subreddit = sLogic.getWithId(1);
        returnedPost.setSubredditId(subreddit);

        assertPostEquals(expectedEntity, returnedPost);
    }

    @Test
    final void testGetColumnNames() {
        List<String> list = logic.getColumnNames();
        assertEquals(Arrays.asList("ID", "REDDIT_ACCOUNT_ID", "SUBREDDIT_ID", "UNIQUE_ID", "POINTS", "COMMENT_COUNT", "TITLE", "CREATED"), list);
    }

    @Test
    final void testGetColumnCodes() {
        List<String> list = logic.getColumnCodes();
        assertEquals(Arrays.asList(PostLogic.ID, PostLogic.REDDIT_ACCOUNT_ID, PostLogic.SUBREDDIT_ID,
                PostLogic.UNIQUE_ID, PostLogic.POINTS, PostLogic.COMMENT_COUNT, PostLogic.TITLE, PostLogic.CREATED), list);
    }

    @Test
    final void testExtractDateAsList() {
        List<?> list = logic.extractDataAsList(expectedEntity);
        assertEquals(expectedEntity.getId(), list.get(0));
        assertEquals(expectedEntity.getRedditAccountId().getId(), ((RedditAccount) list.get(1)).getId());
        assertEquals(expectedEntity.getSubredditId().getId(), ((Subreddit) list.get(2)).getId());
        assertEquals(expectedEntity.getUniqueID(), list.get(3));
        assertEquals(expectedEntity.getPoints(), list.get(4));
        assertEquals(expectedEntity.getCommentCount(), list.get(5));
        assertEquals(expectedEntity.getTitle(), list.get(6));
        assertEquals(expectedEntity.getCreated(), list.get(7));

    }

    @Test
    final void testCreateEntityAndAdd() {

        Map<String, String[]> sampleMap = new HashMap<>();

        sampleMap.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{"1"});
        sampleMap.put(PostLogic.SUBREDDIT_ID, new String[]{"1"});
        sampleMap.put(PostLogic.UNIQUE_ID, new String[]{"1"});
        sampleMap.put(PostLogic.POINTS, new String[]{"1"});
        sampleMap.put(PostLogic.COMMENT_COUNT, new String[]{"1"});
        sampleMap.put(PostLogic.TITLE, new String[]{"Test"});
        sampleMap.put(PostLogic.CREATED, new String[]{TEST_DATE});

        Post returnedPost = logic.createEntity(sampleMap);

        RedditAccountLogic raLogic = LogicFactory.getFor("RedditAccount");
        RedditAccount ra = raLogic.getWithId(1);
        returnedPost.setRedditAccountId(ra);

        SubredditLogic sLogic = LogicFactory.getFor("Subreddit");
        Subreddit subreddit = sLogic.getWithId(1);
        returnedPost.setSubredditId(subreddit);

        logic.add(returnedPost);

        returnedPost = logic.getPostWithUniqueId(returnedPost.getUniqueID());

        assertEquals(sampleMap.get(PostLogic.REDDIT_ACCOUNT_ID)[0], Integer.toString(returnedPost.getRedditAccountId().getId()));
        assertEquals(sampleMap.get(PostLogic.SUBREDDIT_ID)[0], Integer.toString(returnedPost.getSubredditId().getId()));
        assertEquals(sampleMap.get(PostLogic.UNIQUE_ID)[0], returnedPost.getUniqueID());
        assertEquals(sampleMap.get(PostLogic.POINTS)[0], Integer.toString(returnedPost.getPoints()));
        assertEquals(sampleMap.get(PostLogic.COMMENT_COUNT)[0], Integer.toString(returnedPost.getCommentCount()));
        assertEquals(sampleMap.get(PostLogic.TITLE)[0], returnedPost.getTitle());
        assertEquals(logic.convertStringToDate(sampleMap.get(PostLogic.CREATED)[0]), returnedPost.getCreated());

        logic.delete(returnedPost);
    }

    @Test
    final void testCreateEntityNullAndEmptyValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = (Map<String, String[]> map) -> {
            map.clear();
            map.put(PostLogic.ID, new String[]{Integer.toString(expectedEntity.getId())});
            map.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(expectedEntity.getRedditAccountId().getId())});
            map.put(PostLogic.SUBREDDIT_ID, new String[]{Integer.toString(expectedEntity.getSubredditId().getId())});
            map.put(PostLogic.UNIQUE_ID, new String[]{expectedEntity.getUniqueID()});
            map.put(PostLogic.POINTS, new String[]{Integer.toString(expectedEntity.getPoints())});
            map.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(expectedEntity.getCommentCount())});
            map.put(PostLogic.TITLE, new String[]{expectedEntity.getTitle()});
            map.put(PostLogic.CREATED, new String[]{logic.convertDateToString(expectedEntity.getCreated())});
        };

        //idealy every test should be in its own method
        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.ID, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.ID, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.REDDIT_ACCOUNT_ID, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.REDDIT_ACCOUNT_ID, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.SUBREDDIT_ID, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.SUBREDDIT_ID, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.UNIQUE_ID, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.UNIQUE_ID, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.POINTS, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.POINTS, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.COMMENT_COUNT, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.COMMENT_COUNT, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.TITLE, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.TITLE, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.CREATED, null);
        assertThrows(NullPointerException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.CREATED, new String[]{});
        assertThrows(IndexOutOfBoundsException.class, () -> logic.createEntity(sampleMap));
    }

    @Test
    final void testCreateEntityBadLengthValues() {
        Map<String, String[]> sampleMap = new HashMap<>();
        Consumer<Map<String, String[]>> fillMap = (Map<String, String[]> map) -> {
            map.clear();
            map.put(PostLogic.ID, new String[]{Integer.toString(expectedEntity.getId())});
            map.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(expectedEntity.getRedditAccountId().getId())});
            map.put(PostLogic.SUBREDDIT_ID, new String[]{Integer.toString(expectedEntity.getSubredditId().getId())});
            map.put(PostLogic.UNIQUE_ID, new String[]{expectedEntity.getUniqueID()});
            map.put(PostLogic.POINTS, new String[]{Integer.toString(expectedEntity.getPoints())});
            map.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(expectedEntity.getCommentCount())});
            map.put(PostLogic.TITLE, new String[]{expectedEntity.getTitle()});
            map.put(PostLogic.CREATED, new String[]{logic.convertDateToString(expectedEntity.getCreated())});
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
        sampleMap.replace(PostLogic.ID, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.ID, new String[]{"12b"});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.UNIQUE_ID, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.UNIQUE_ID, new String[]{generateString.apply(11)});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

        fillMap.accept(sampleMap);
        sampleMap.replace(PostLogic.TITLE, new String[]{""});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));
        sampleMap.replace(PostLogic.TITLE, new String[]{generateString.apply(256)});
        assertThrows(ValidationException.class, () -> logic.createEntity(sampleMap));

    }

    @Test
    final void testCreateEntityEdgeValues() {
        IntFunction<String> generateString = (int length) -> {
            //https://www.baeldung.com/java-random-string#java8-alphabetic
            return new Random().ints('a', 'z' + 1).limit(length)
                    .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                    .toString();
        };

        Map<String, String[]> sampleMap = new HashMap<>();
        sampleMap.put(PostLogic.ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.SUBREDDIT_ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.UNIQUE_ID, new String[]{generateString.apply(1)});
        sampleMap.put(PostLogic.POINTS, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.TITLE, new String[]{generateString.apply(1)});
        sampleMap.put(PostLogic.CREATED, new String[]{TEST_DATE});

        //idealy every test should be in its own method
        Post returnedPost = logic.createEntity(sampleMap);
        
        RedditAccountLogic raLogic = LogicFactory.getFor("RedditAccount");
        RedditAccount ra = raLogic.getWithId(1);
        returnedPost.setRedditAccountId(ra);

        SubredditLogic sLogic = LogicFactory.getFor("Subreddit");
        Subreddit subreddit = sLogic.getWithId(1);
        returnedPost.setSubredditId(subreddit);
        
        
        assertEquals(Integer.parseInt(sampleMap.get(PostLogic.ID)[0]), returnedPost.getId());
        assertEquals(sampleMap.get(PostLogic.REDDIT_ACCOUNT_ID)[0], Integer.toString(returnedPost.getRedditAccountId().getId()));
        assertEquals(sampleMap.get(PostLogic.SUBREDDIT_ID)[0], Integer.toString(returnedPost.getSubredditId().getId()));
        assertEquals(sampleMap.get(PostLogic.UNIQUE_ID)[0], returnedPost.getUniqueID());
        assertEquals(sampleMap.get(PostLogic.POINTS)[0], Integer.toString(returnedPost.getPoints()));
        assertEquals(sampleMap.get(PostLogic.COMMENT_COUNT)[0], Integer.toString(returnedPost.getCommentCount()));
        assertEquals(sampleMap.get(PostLogic.TITLE)[0], returnedPost.getTitle());
        assertEquals(logic.convertStringToDate(sampleMap.get(PostLogic.CREATED)[0]), returnedPost.getCreated());


        sampleMap = new HashMap<>();
        sampleMap.put(PostLogic.ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.REDDIT_ACCOUNT_ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.SUBREDDIT_ID, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.UNIQUE_ID, new String[]{generateString.apply(10)});
        sampleMap.put(PostLogic.POINTS, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(1)});
        sampleMap.put(PostLogic.TITLE, new String[]{generateString.apply(255)});
        sampleMap.put(PostLogic.CREATED, new String[]{TEST_DATE});
        
        //idealy every test should be in its own method
        returnedPost = logic.createEntity(sampleMap);
        
        returnedPost.setRedditAccountId(ra);
        returnedPost.setSubredditId(subreddit);
        
        assertEquals(Integer.parseInt(sampleMap.get(PostLogic.ID)[0]), returnedPost.getId());
        assertEquals(sampleMap.get(PostLogic.REDDIT_ACCOUNT_ID)[0], Integer.toString(returnedPost.getRedditAccountId().getId()));
        assertEquals(sampleMap.get(PostLogic.SUBREDDIT_ID)[0], Integer.toString(returnedPost.getSubredditId().getId()));
        assertEquals(sampleMap.get(PostLogic.UNIQUE_ID)[0], returnedPost.getUniqueID());
        assertEquals(sampleMap.get(PostLogic.POINTS)[0], Integer.toString(returnedPost.getPoints()));
        assertEquals(sampleMap.get(PostLogic.COMMENT_COUNT)[0], Integer.toString(returnedPost.getCommentCount()));
        assertEquals(sampleMap.get(PostLogic.TITLE)[0], returnedPost.getTitle());
        assertEquals(logic.convertStringToDate(sampleMap.get(PostLogic.CREATED)[0]), returnedPost.getCreated());
    }
}
