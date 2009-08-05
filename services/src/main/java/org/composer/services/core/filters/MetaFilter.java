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
    private static String SP_REGEX = "\\s+|/+";
    //private static String GP_REGEX = "(\w.[(\w,+)*])";
    private static String REPLACE = ".";
    private static Api composer;

    public void init(FilterConfig filterConfig) throws ServletException {
        this.config = filterConfig;
        context = config.getServletContext();
        filterName = config.getFilterName();

        composer = new Api("localhost", 3306, "composer", "composer", "cp1111");
        System.out.println("COMPOSER LOADING STORES RESULT: " + composer.loadStores());
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

        /*** NOTE THAT FOR NOW ALL REQUESTS REQUIRE A KEY SET IN A COOKIE
         *   THE CLIENT HANDLES ALL THAT LOGIC.
         *   THIS IS A SHORT TERM LIMITATION.
         ***/
        String path = req.getServletPath().substring(6);
        String fpath = formatPath(path);

        System.out.println("received request path: " +fpath + " and method: "+ req.getMethod());
        
        String storeKey = getStoreKey(req.getCookies());

        if ("".equals(storeKey) && !("PUT".equals(req.getMethod()) && "store.registration".equalsIgnoreCase(fpath))) {

            JSONObject jserror = new JSONObject();
            jserror.put("error", "requires a store key");

            error(res, jserror);
        } else {

            if ("GET".equals(req.getMethod())) {
                String uplinks = composer.getUplinks(storeKey, fpath);

                if (uplinks.trim().equals("[]")) {
                    composer.addLink(storeKey, fpath);
                }

                JSONArray jsonrs = JSONArray.fromObject(composer.getUplinks(storeKey, fpath));
                
                JSONObject jsresp = new JSONObject();
                jsresp.put(fpath, jsonrs);

                response(res, jsresp);
            } else

            if ("PUT".equals(req.getMethod())) {
            } else

            if ("POST".equals(req.getMethod())) {
                if ("store.registration".equalsIgnoreCase(fpath)) {
                    Cookie cookie = setStoreKey();

                    res.addCookie(cookie);

                    storeKey = cookie.getValue();

                    composer.addDataStore(storeKey,"public");

                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("storeKey", storeKey);

                    System.out.println("SENDING RESPONSE: "+jsonobj.toString());

                    response(res, jsonobj);

                } else

                if ("store.open".equalsIgnoreCase(fpath)) {
                   composer.addDataStore(storeKey,"public");

                   JSONObject jsonobj = new JSONObject();
                    jsonobj.put("store-key", storeKey);

                    System.out.println("SENDING RESPONSE: "+jsonobj.toString());

                    response(res, jsonobj);
                } else {
                    composer.addLink(storeKey, fpath);

                    JSONArray jsonrs = JSONArray.fromObject(composer.getUplinks(storeKey, fpath));

                    JSONObject jsresp = new JSONObject();
                    jsresp.put(fpath, jsonrs);

                    response(res, jsresp);
                }

            } else

            if ("DELETE".equals(req.getMethod())) {
               //Api.removeLink(storeKey, fpath);
            }

            else {
                chain.doFilter(request,response);
            }
        }
    }

    public void destroy() {
    }

    private void response(ServletResponse response, JSONObject data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();
        
        out.println(data);
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

        Pattern p = Pattern.compile(SP_REGEX);

        Matcher m = p.matcher(uri);
        
        StringBuffer sb = new StringBuffer();

        while(m.find()){
            m.appendReplacement(sb,REPLACE);
        }

        m.appendTail(sb);


        return sb.toString();
    }

    private void error(ServletResponse response, JSONObject data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        out.println(data);
    }
}
