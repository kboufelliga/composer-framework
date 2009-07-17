package org.composer.clients;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import junit.framework.TestCase;
import net.sf.json.JSONArray;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 15, 2008
 * Time: 1:34:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestApiClient extends TestCase {
    private ApiClient api;

    @Override
    protected void setUp() {
        
        api = new ApiClient
                   .Builder("http://localhost",8080)
                   .credentials("cafepress", "cafe1111")
                   .build();
    }


    public void testStorage() {

        try {
           // run the test in a loop for
           // 10 seconds

           JSONArray rsnm = api.put("creditcard.name");
           JSONArray rsnb = api.put("creditcard.number");
           JSONArray rsem = api.put("creditcard.expiration.month");
           JSONArray rsey = api.put("creditcard.expiration.year");
           JSONArray rssc = api.put("creditcard.security.code");

           JSONArray metalist = api.get("creditcard");

           Iterator<String> list = metalist.iterator();
           boolean foundnm = false;
           boolean foundnb = false;
           boolean foundem = false;
           boolean foundey = false;
           boolean foundsc = false;

           while (list.hasNext()) {
               String prop = list.next();

               if (prop.equals("creditcard.name")) {
                   foundnm = true;
               }
               if (prop.equals("creditcard.number")) {
                   foundnb = true;
               }
               if (prop.equals("creditcard.expiration.month")) {
                   foundem = true;
               }
               if (prop.equals("creditcard.expiration.year")) {
                   foundey = true;
               }
               if (prop.equals("creditcard.security.code")) {
                   foundsc = true;
               }
           }

           assertTrue(foundnm);
           assertTrue(foundnb);
           assertTrue(foundem);
           assertTrue(foundey);
           assertTrue(foundsc);    
           
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testStorageLoop() throws InterruptedException {
        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            private int repeat = 5;

            public void run() {

            if (repeat > 0) {
                System.out.println("Test# "+repeat);

                testStorage();

                repeat--;
            } else {
                System.out.println("Completed Test Loop!");

                timer.cancel();
            }
            }
        };

        //Note every seconds here is relative to the termination of the execution
        //basically it will repeat the exectution of the task every second until
        //exhausting the repeat the numbers

        timer.schedule(task, 0, 1*1000);

        //Give it time to execute
        Thread.sleep(4*1000);
    }

    @Override
    protected void tearDown() {
        api = null;
    }
}