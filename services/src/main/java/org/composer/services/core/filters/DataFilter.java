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
import java.util.Enumeration;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.composer.engine.exceptions.GeneralException;


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

        composer = new Api("localhost", 3306, "composer", "composer", "cp1111");
        composer.loadStores();
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse res = (HttpServletResponse)response;

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

                System.out.println("STORE KEY: "+storeKey);

                JSONObject jsonrs = new JSONObject();

                jsonrs.put("data", composer.getData(storeKey, fpath));
                 
                response(res, jsonrs);
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
                    jsonobj.put("store-key", storeKey);

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
                    try {

                         String resp = composer.addData(storeKey, fpath, getData(request));

                         JSONObject rstate = JSONObject.fromObject();

                         JSONObject jsonrs;

                        if (rstate.containsKey("error"))
                            jsonrs = jsonMsg("error", "could not add data to "+fpath+" ERROR DESC: "+rstate.getString("error"));
                        else {
                             System.out.println("DATA INFO: "+composer.getData(storeKey, fpath));
                            jsonrs = new JSONObject();
                            jsonrs.put("data", composer.getData(storeKey, fpath));
                        }
                        
                        response(res, jsonrs);
                    } catch (GeneralException e) {
                        response(res, e.toJSON());
                    }
                }

            } else

            if ("DELETE".equals(req.getMethod())) {
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

    private void error(ServletResponse response, JSONObject data) throws ServletException, IOException {
        response.setContentType("application/json");

        PrintWriter out = response.getWriter();

        out.println(data);
    }

    private String getData(ServletRequest request) throws GeneralException {
        boolean nodata = true;

        try {

            Enumeration parms = request.getParameterNames();

            while(parms.hasMoreElements()) {
                String name = (String)parms.nextElement();

                if ("data".equalsIgnoreCase(name)) {
                    String[] values = request.getParameterValues(name);

                    if (values.length != 1) throw new GeneralException("data can only be submitted in one variable");
                    else return values[0];
                }
            }

            if (nodata) throw new GeneralException("no data was found");
        } catch (Exception e) {
            throw new GeneralException("processing error: "+e.toString());
        }

        return "";
    }

    private JSONObject jsonMsg (String name, String value) {
        JSONObject jsobj = new JSONObject();
        jsobj.put(name, value);

        return jsobj;
    }
}
