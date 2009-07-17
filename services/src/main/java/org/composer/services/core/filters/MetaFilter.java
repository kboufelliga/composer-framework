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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;


public class MetaFilter implements Filter {
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
        composer = new Api("localhost", 3306, "composer", "composer", "cp1111");

    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        System.out.println(
                    "URI: "+req.getRequestURI()+"\n"+
                    "CONTEXT PATH: "+req.getContextPath()+"\n"+
                    "HEADER NAMES: "+req.getHeaderNames().toString()+"\n"+
                    "PATH TRANSLATED: "+req.getPathTranslated()+"\n"+
                    "REQUEST URL: "+req.getRequestURL()+"\n"+
                    "SERVLET PATH: "+req.getServletPath()+"\n"+
                    "METHOD: "+req.getMethod()+"\n"+
                    "REQUEST URL: "+req.getRequestURL()+"\n"+
                    "(Reported by " + filterName + ".)");
                //metadata(response, req.getServletPath());

        String path = req.getServletPath().substring(6);
        String fpath = formatPath(path);

        String storeKey = getStoreKey(req.getCookies());
System.out.println("COOKIE VALUE: "+storeKey);
        if (storeKey.equals("")) {
            /*** NEW COOKIE ***/
            Cookie cookie = setStoreKey();

            res.addCookie(cookie);

            storeKey = cookie.getValue();

            /*** PROMPT USER TO OPEN A STORE***/
            //should get the query string store_key value or create a new one
            if (req.getMethod().equals("PUT") && fpath.equalsIgnoreCase("store.registration")) {
                    composer.addDataStore(storeKey,"public");

                    register(res, storeKey);
            } else {
                createStore(res,"");
            }
        } else {

            /*** ADD DEFAULT META AND DATA STORES ***/
            //composer.addDataStore(storeKey);
            
            
            /*** GET=READ ***/
            if (req.getMethod().equals("GET")) {
                //composer.addDataStore(storeKey);

                String uplinks = composer.getUplinks(storeKey, fpath);
           
                if (uplinks.trim().equals("[]")) {
                    composer.addLink(storeKey, fpath);
                    //createPrompt(res,fpath);
                } //else {
                    JSONArray jsonrs = JSONArray.fromObject(composer.getUplinks(storeKey, fpath));
                    response(res, jsonrs);
                //}
            } else

            /*** PUT=CREATE ***/
            if (req.getMethod().equals("PUT")) {
                /*** NOTE THAT THIS SHOULD BE HANDLED BY THE ACTION SERVLET
                *   WE COULD CREATE AN ACTION MAP FILER SUCH AS
                *   forward to /action/#{form action}
                */
                if (path.equalsIgnoreCase("store.registration")) {
                    //should get the query string store_key value or create a new one
                    composer.addDataStore(storeKey,"public");

                    register(res, storeKey);
                } else {
                    composer.addLink(storeKey, fpath);
                
                    JSONArray jsonrs = JSONArray.fromObject(composer.getUplinks(storeKey, fpath));

                    response(res, jsonrs);
                }
            } else
        
            /*** DELETE=DELETE ***/
            if (req.getMethod().equals("DELETE")) {
                //Api.removeLink(storeKey, fpath);
            } else

            /*** POST=UPDATE ***/
            if (req.getMethod().equals("POST")) {

                System.out.println("PROCESSING POST REQUEST: "+fpath);

                if ("store.registration".equalsIgnoreCase(fpath)) {

                    System.out.println("ADDING META STORE: "+fpath);

                    composer.addMetaStore(storeKey);

                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("store-key", storeKey);

                    System.out.println("SENDING RESPONSE: "+jsonobj.toString());

                    response(res, jsonobj);

                } else {
                    String result = composer.addLink(storeKey, fpath);

                    System.out.println("ADDLINK RESPONSE: "+result);

                    System.out.println("SENDING RESPONSE: "+composer.getUplinks(storeKey, fpath));

                    JSONArray jsonobj = JSONArray.fromObject(composer.getUplinks(storeKey, fpath));
                    response(res, jsonobj);
                }
                
            } else {

                chain.doFilter(request,response);
            }
        }
    }

    public void destroy() {
    }

    private void createStore(ServletResponse response, String data) throws ServletException, IOException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        String docType =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
        "Transitional//EN\">\n";

        out.println
        (docType +
           "<HTML>\n" +
           "<HEAD><TITLE>COMPOSER API METADATA </TITLE></HEAD>\n" +
           "<BODY BGCOLOR=\"WHITE\">\n" +
           "<H1>JSONHUB METADATA</H1>\n" +
           " need to create a store  <form method=\"POST\" action=\"/api/meta/store.registration\"><input type=\"submit\" value=\"Create\"/></form>\n" +
           "</BODY></HTML>");
    }

    private void createPrompt(ServletResponse response, String data) throws ServletException, IOException {
        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        String docType =
        "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
        "Transitional//EN\">\n";

        out.println
        (docType +
           "<HTML>\n" +
           "<HEAD><TITLE>COMPOSER API METADATA </TITLE></HEAD>\n" +
           "<BODY BGCOLOR=\"WHITE\">\n" +
           "<H1>JSONHUB METADATA</H1>\n" +
           data + " definition does not exists. <form method=\"POST\"><input type=\"submit\" value=\"Create\"/></form>\n" +
           "</BODY></HTML>");
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
           "<HEAD><TITLE>COMPOSER API METADATA DEFINITION</TITLE></HEAD>\n" +
           "<BODY BGCOLOR=\"WHITE\">\n" +
           "<H1>JSONHUB METADATA</H1>\n" +
           ": " + data + "\n" +
           "</BODY></HTML>");
    }

    private void response(ServletResponse response, JSONArray data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        
        out.println(data);
    }

    private void response(ServletResponse response, JSONObject data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        out.println(data);
    }


    private void register(ServletResponse response, String data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        JSONObject jsonrs = new JSONObject();

        jsonrs.put("jsonhub-store-key", data);

        out.println(jsonrs);
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
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        Cookie cookie = new Cookie("jsonhub-store-key",key);
        //cookie.setDomain(".jsonhub.com");
        cookie.setComment("JSONHUB RESTful DATA - PUBLIC STORE KEY");
        //cookie.setMaxAge(60*60*24*365); //one year lifetime
        cookie.setMaxAge(60*5); //5 min lifetime

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
