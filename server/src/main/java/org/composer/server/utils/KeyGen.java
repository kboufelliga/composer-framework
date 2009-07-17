package org.composer.server.utils;

import java.util.UUID;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 21, 2008
 * Time: 11:14:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class KeyGen {
    private static final String[] LETTERS ={"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","a","b","c","d","e","f","d","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"};
    
    public static String generateShortKey() {
        StringBuffer uidbuffer = new StringBuffer();
        
        Random rand = new Random();

        for (int i=0; i <7; i++) {
          uidbuffer.append(LETTERS[rand.nextInt(LETTERS.length)]);
        }

        return uidbuffer.toString();

    }

    public static String generateKey() {
        return UUID.randomUUID().toString();     
    }
}
