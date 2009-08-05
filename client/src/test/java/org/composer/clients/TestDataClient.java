package org.composer.clients;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 15, 2008
 * Time: 1:34:26 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestDataClient extends TestCase {
    private DataClient api;

    @Override
    protected void setUp() {

        api = new DataClient
                   .Builder("http://localhost",8080)
                   .credentials("", "")
                   .build();
    }


    public void testStorage() {

        try {

            // should have the option for one declaration such as
            // api.put"credicard...name,number,expiration.month,expiration.year,security.code")

           JSONObject rsnm = api.post("creditcard.name","additional abc name");
           JSONObject rsnb = api.post("creditcard.number","15-1234567891011");
           JSONObject rsem = api.post("creditcard.expiration.month","july-22");
           JSONObject rsey = api.post("creditcard.expiration.year","2011");
           JSONObject rssc = api.post("creditcard.security.code","928");

           JSONObject dataobj = api.get("creditcard");

           System.out.println("CREDITCARD DATA: "+dataobj.toString());

           assertTrue(dataobj.has("creditcard.name"));
           assertTrue(dataobj.has("creditcard.number"));
           assertTrue(dataobj.has("creditcard.expiration"));
           assertTrue(dataobj.has("creditcard.expiration.month"));
           assertTrue(dataobj.has("creditcard.expiration.year"));
           assertTrue(dataobj.has("creditcard.security"));
           assertTrue(dataobj.has("creditcard.security.code"));


           JSONArray datalist = dataobj.getJSONArray("creditcard.name");

           Iterator<String> list = datalist.iterator();
           boolean foundnm = false;
           
           while (list.hasNext()) {
               String value = list.next();

               if (value.equals("additional abc name")) {
                   foundnm = true;
               }
           }

           assertTrue(foundnm);
           
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    public void testStorageLoop() throws InterruptedException {
        final Timer timer = new Timer();

        TimerTask task = new TimerTask() {
            private int repeat = 0;

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
        Thread.sleep(5*1000);
    }

    @Override
    protected void tearDown() {
        api = null;
    }
}