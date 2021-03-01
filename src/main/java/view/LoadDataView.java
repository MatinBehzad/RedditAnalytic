/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package view;

import entity.Comment;
import entity.Post;
import entity.RedditAccount;
import entity.Subreddit;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import logic.CommentLogic;

import logic.LogicFactory;
import logic.PostLogic;
import logic.RedditAccountLogic;

import logic.SubredditLogic;
import reddit.DeveloperAccount;
import reddit.wrapper.AccountWrapper;

import reddit.wrapper.CommentSort;

import reddit.wrapper.PostWrapper;
import reddit.wrapper.RedditWrapper;
import reddit.wrapper.SubSort;
import reddit.wrapper.SubredditWrapper;

/**
 *
 * @author Brahim
 */
@WebServlet(name = "LoadDataView", urlPatterns = {"/LoadDataView"})
public class LoadDataView extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SubredditLogic c = LogicFactory.getFor("Subreddit");
        List<Subreddit> list = c.getAll();
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {

            out.println("<!DOCTYPE html>");
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Subreddit  Logic</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<div style=\"text-align: center;\">");
            out.println("<div style=\"display: inline-block; text-align: left;\">");
            out.println("<form method=\"post\">");
            out.println("Subreddit:<br>");
            out.printf("<select name=\"%s\" value=\"\"><br>", SubredditLogic.NAME);
            for (int i = 0; i < list.size(); i++) {
                out.printf("<option value=" + list.get(i).getName() + ">" + list.get(i).getName() + "</option>");
            }
            out.println("</select>");
             out.println("<br>");
            out.println("URL:<br>");
            out.printf("<input type=\"text\" name=\"%s\" value=\"\"><br>", SubredditLogic.URL);
            out.println("<br>");
            out.println("<input type=\"submit\" name=\"view\" value=\"Add and View\">");
            out.println("<input type=\"submit\" name=\"add\" value=\"Add\">");
            out.println("</form>");

            out.println("<pre>");
            out.println("Submitted keys and values:");
            out.println(toStringMap(request.getParameterMap()));
            out.println("</pre>");
            out.println("</div>");
            out.println("</div>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    private String toStringMap(Map<String, String[]> m) {
        StringBuilder builder = new StringBuilder();
        for (String k : m.keySet()) {
            builder.append("Key=").append(k)
                    .append(", ")
                    //  .append( "Value/s=" ).append( Arrays.toString( m.get( k ) ) )
                    .append(System.lineSeparator());
        }
        return builder.toString();
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log("GET");
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        log("POST");

        SubredditWrapper currentSubreddit;
        String clientID = "-OTjK3ANaaAtHQ";
        String clientSecret = "PPjVzF7FmHXqxv87a-N9pXWBzdY";
        String redditUser = "Brasergeo99";
        String algonquinUser = "bra0037";

        DeveloperAccount dev = new DeveloperAccount()
                .setClientID(clientID)
                .setClientSecret(clientSecret)
                .setRedditUser(redditUser)
                .setAlgonquinUser(algonquinUser);

        //create a new scraper
        RedditWrapper scrap = new RedditWrapper();
        //authenticate and set up a page for wallpaper subreddit with 5 posts soreted by HOT order
        scrap.authenticate(dev).setLogger(false);
        //declaring variables to make connection with other classe
        Subreddit subreddit = null;
        String p;

        Comment subway = null;

        SubredditLogic logic = LogicFactory.getFor("Subreddit");

        if (!request.getParameter(SubredditLogic.URL).isEmpty()) {
            p = request.getParameter(SubredditLogic.URL);

            scrap.configureCurentSubreddit(p, 2, SubSort.HOT);
            currentSubreddit = scrap.getCurrentSubreddit();
            Map<String, String[]> paramMap = new HashMap<>();
            String[] n = {currentSubreddit.getName()};
            paramMap.put(SubredditLogic.NAME, n);
            String[] u = {currentSubreddit.getReletiveUrl()};
            paramMap.put(SubredditLogic.URL, u);
            String[] s = {Integer.toString(currentSubreddit.getSubscribers())};
            paramMap.put(SubredditLogic.SUBSCRIBERS, s);
            subreddit = logic.createEntity(paramMap);
            logic.add(subreddit);
        } else {
            scrap.configureCurentSubreddit(request.getParameter(SubredditLogic.NAME), 2, SubSort.HOT);
            //TODO use the subreddit logic to get the subreddit with the given 
            subreddit = logic.getSubredditWithName(request.getParameter(SubredditLogic.NAME));
        }
        final Subreddit finalSub = subreddit;

        RedditAccountLogic rLogic = LogicFactory.getFor("RedditAccount");
        PostLogic r = LogicFactory.getFor("Post");
        CommentLogic z = LogicFactory.getFor("Comment");
        //create a lambda that accepts post
        Consumer<PostWrapper> saveData = ((PostWrapper post) -> {

            AccountWrapper aw = post.getAuthor();
            RedditAccount acc = rLogic.getRedditAccountWithName(aw.getName());
            if (acc == null) {
                Map<String, String[]> map = new HashMap<>(6);
                map.put(RedditAccountLogic.COMMENT_POINTS, new String[]{Integer.toString(aw.getLinkKarma())});
                map.put(RedditAccountLogic.LINK_POINTS, new String[]{Integer.toString(aw.getLinkKarma())});
                map.put(RedditAccountLogic.CREATED, new String[]{rLogic.convertDateToString(aw.getCreated())});
                map.put(RedditAccountLogic.NAME, new String[]{aw.getName()});

                acc = rLogic.createEntity(map);
                rLogic.add(acc);
            }

            Post a = r.getPostWithUniqueId(post.getUniqueID());

            if (a == null) {
                Map<String, String[]> map = new HashMap<>(6);
                map.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(post.getCommentCount())});

                map.put(PostLogic.TITLE, new String[]{(post.getTitle())});
                map.put(PostLogic.UNIQUE_ID, new String[]{(post.getUniqueID())});
                map.put(PostLogic.COMMENT_COUNT, new String[]{Integer.toString(post.getCommentCount())});
                map.put(PostLogic.CREATED, new String[]{rLogic.convertDateToString(post.getCreated())});
                a = r.createEntity(map);
                a.setRedditAccountId(acc);
                a.setSubredditId(finalSub);
                r.add(a);
            }
            final Post finalP = a;
            post.configComments(2, 2, CommentSort.CONFIDENCE);

            post.processComments(comment -> {
                if (comment.isPinned() || comment.getDepth() == 0) {
                    return;
                }
//                comment.getAuthor(); comment author
                AccountWrapper l = comment.getAuthor();
                //finish getting the account for the comment and add it to db
                RedditAccount t = rLogic.getRedditAccountWithName(aw.getName());
                if (t == null) {
                    Map<String, String[]> map = new HashMap<>(6);
                    map.put(RedditAccountLogic.COMMENT_POINTS, new String[]{Integer.toString(l.getLinkKarma())});
                    map.put(RedditAccountLogic.LINK_POINTS, new String[]{Integer.toString(l.getLinkKarma())});
                    map.put(RedditAccountLogic.CREATED, new String[]{rLogic.convertDateToString(l.getCreated())});
                    map.put(RedditAccountLogic.NAME, new String[]{l.getName()});

                    t = rLogic.createEntity(map);
                    rLogic.add(t);
                }
                Comment w = z.getCommentWithUniqueId(comment.getUniqueID());
                //this is for the comment. not the redditaccount

                if (w == null) {
                    Map<String, String[]> map = new HashMap<>(6);
                    map.put(CommentLogic.POINTS, new String[]{Integer.toString(comment.getVotes())});
                    map.put(CommentLogic.TEXT, new String[]{comment.getText()});
                    map.put(CommentLogic.CREATED, new String[]{rLogic.convertDateToString(comment.getCreated())});
                    map.put(PostLogic.UNIQUE_ID, new String[]{(comment.getUniqueID())});
                    map.put(CommentLogic.IS_REPLY, new String[]{Integer.toString(comment.getReplyCount())});

                    w = z.createEntity(map);
                    w.setRedditAccountId(t);
                    w.setPostId(finalP);
                    z.add(w);
                }

//                comment itself
            });
        });
        //At this point you will create the lambda for RedditWrapper::proccessCurrentPage. You can
//also just make a method and pass it as method reference
        scrap.requestNextPage().proccessCurrentPage(saveData);
       // processRequest(request, response);

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Sample of Load data View Normal";
    }

    private static final boolean DEBUG = true;

    @Override
    public void log(String msg) {
        if (DEBUG) {
            String message = String.format("[%s] %s", getClass().getSimpleName(), msg);
            getServletContext().log(message);
        }
    }

    @Override
    public void log(String msg, Throwable t) {
        String message = String.format("[%s] %s", getClass().getSimpleName(), msg);
        getServletContext().log(message, t);
    }
}
