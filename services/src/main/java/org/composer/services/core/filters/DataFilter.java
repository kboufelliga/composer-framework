package org.composer.services.core.filters;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Jun 11, 2009
 * Time: 6:37:17 PM
 * To change this template use File | Settings | File Templates.
 */


import org.composer.engine.Api;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONObject;


public class DataFilter implements Filter {
    protected FilterConfig config;
    private ServletContext context;
    private String filterName;
    private static String REGEX = "\\s+|/+";
    private static String REPLACE = ".";
    private static Api composer;
    

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        context = config.getServletContext();
        filterName = config.getFilterName();
        //composer = new Api("localhost", 3306, "composer", "composer", "cp1111");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        String storeKey = getStoreKey(req.getCookies());

        System.out.println("DATAFILTER Cookie value: "+storeKey);

        if (storeKey.equals("")) {
            //create a new key and include in the response
            Cookie cookie = setStoreKey();

            res.addCookie(cookie);

            storeKey = cookie.getValue();

            //composer.addDataStore(storeKey);
            //should prompt the user to re-submit the request
        }

       // for now only accept letters, periods, spaces, forward slashes
       String path = req.getServletPath().substring(6);
       String fpath = formatPath(path);

       System.out.println("GET PATH: "+path);

        //GET=READ
        if (req.getMethod().equals("GET")) {
           //metadata(res, composer.getData(storeKey, fpath));
        }

        //POST=CREATE
        if (req.getMethod().equals("POST")) {
           /*** NOTE THAT THIS SHOULD BE HANDLED BY THE ACTION SERVLET
            *   WE COULD CREATE AN ACTION MAP FILER SUCH AS
            *   forward to /action/#{form action}
            */
           if (path.equalsIgnoreCase("store.registration")) {

               System.out.println("CREATING STORE WITH KEY: "+storeKey);

               //composer.addDataStore(storeKey,"public");

               register(res, storeKey);
           } else {
                
               //composer.addData(storeKey, fpath, "data...");
           
               metadata(res, composer.getData(storeKey, fpath));
           }
        }

        //PUT=UPDATE
        if (req.getMethod().equals("PUT")) {
           /*** NOTE THAT THIS SHOULD BE HANDLED BY THE ACTION SERVLET
            *   WE COULD CREATE AN ACTION MAP FILER SUCH AS
            *   forward to /action/#{form action}
            */
           if (path.equalsIgnoreCase("store.registration")) {

               System.out.println("CREATING STORE WITH KEY: "+storeKey);

               //composer.addDataStore(storeKey,"public");

               register(res, storeKey);
           } else {
                //composer.addData(storeKey, fpath, "data...");

                //metadata(res, composer.getData(storeKey, fpath));
           }

        }
        //DELETE=DELETE

        //POST=UPDATE

            //else
                //chain.doFilter(request,response);
    }

    public void destroy() {
    }

    private void metadata(ServletResponse response, String data) throws ServletException, IOException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        String docType =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
        "Transitional//EN\">\n";

        out.println
        (docType +
           "<HTML>\n" +
           "<HEAD><TITLE>Access Prohibited</TITLE></HEAD>\n" +
           "<BODY BGCOLOR=\"WHITE\">\n" +
           "<H1>JSONHUB METADATA</H1>\n" +
           ": " + data + "\n" +
           "</BODY></HTML>");
    }

    private void register(ServletResponse response, String data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        JSONObject jsonrs = new JSONObject();

        jsonrs.put("jsonhub-storekey", data);

        out.println(jsonrs);
    }



    private void createLink (String storeKey, String link, ServletResponse response) throws ServletException, IOException {

    }

    private String getStoreKey (Cookie[] cookies) {
        if (cookies != null)
            for (Cookie cookie: cookies) {
                if (cookie.getName().equalsIgnoreCase("jsonhub-store-key"))
                    return cookie.getValue();
            }

        return "";
    }

    private Cookie setStoreKey () {
        String key = UUID.randomUUID().toString().replaceAll("-","");
        Cookie cookie = new Cookie("jsonhub-store-key",key);
        //cookie.setDomain(".jsonhub.com");
        cookie.setComment("JSONHUB RESTful DATA - PUBLIC STORE KEY");
        //cookie.setMaxAge(60*60*24*365); //one year lifetime
        cookie.setMaxAge(5*60); //5 minutes lifetime

        return cookie;
    }

    private String formatPath(String uri) {

        Pattern p = Pattern.compile(REGEX);

        Matcher m = p.matcher(uri);

        StringBuffer sb = new StringBuffer();

        while(m.find()){
            m.appendReplacement(sb,REPLACE);
        }

        m.appendTail(sb);


        return sb.toString();
    }

}
