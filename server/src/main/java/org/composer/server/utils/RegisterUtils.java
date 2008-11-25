package org.composer.server.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

import java.util.Properties;

import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Oct 29, 2008
 * Time: 12:57:42 AM
 * To change this template use File | Settings | File Templates.
 */
public final class RegisterUtils {
      private static Log log = LogFactory.getLog(RegisterUtils.class);
      private static final RegisterUtils INSTANCE = new RegisterUtils();
      private static Properties mailprops; 
      private static String emailUser;
      private static String emailUserPassword;

      private RegisterUtils () {
          try {
            Configuration config = new PropertiesConfiguration("smtp.properties");
            mailprops = new Properties();
            mailprops.put("mail.smtp.host", config.getString("host"));
            mailprops.put("mail.smtp.auth", "true");
            mailprops.put("mail.debug", "true");
            mailprops.put("mail.smtp.port", config.getString("port"));
            mailprops.put("mail.smtp.socketFactory.port", config.getString("port"));
            mailprops.put("mail.smtp.socketFactory.class", config.getString("ssl.factory"));
            mailprops.put("mail.smtp.socketFactory.fallback", "false");
            emailUser = config.getString("username");
            emailUserPassword = config.getString("password");
          } catch (Exception e) {
            log.error("Email Server Configuration Failed "+e);
          }
      }

      public static RegisterUtils getInstance() {
          return INSTANCE;
      }

      public static void emailAppKey(String emailId, String appName, String appKey) {
        try {
          // Get email for equalsId
          StringBuffer emailbody = new StringBuffer();
          emailbody.append(" *******************************************************\n");
          emailbody.append("   Cafepress/Composer Application Registration          \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your Application Name: "+appName+"                   \n");
//          emailbody.append("   - "+appName+"                                        \n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your Application Key: "+appKey+"                         \n");
//          emailbody.append("   - "+appKey+"                                         \n");
          emailbody.append("                                                        \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append(" *******************************************************\n");
          emailbody.append("\n");
          emailbody.append(" Instructions:\n");
          emailbody.append("\n");
          emailbody.append(" To complete your registration click on the link below:\n");
          emailbody.append(" http://composerlab.org/services/applications/activation/"+appName+"/"+appKey+"\n");
          emailbody.append("\n");
          emailbody.append("\n");
          emailbody.append("\n");

          // send instructions to the twitter user for completing the link
          sendEmail(emailId, appName+" - Cafepress/Composer Application Registration - ", emailbody.toString());

        } catch (Exception e) {
          log.error("Register.application Exception: "+e);
        }
    }

    public static void emailMemberKey(String emailId, String memberName, String memberKey) {
        try {
          // Get email for equalsId
          StringBuffer emailbody = new StringBuffer();
          emailbody.append(" *******************************************************\n");
          emailbody.append("   Cafepress/Composer Membership Registration           \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your Member Name: "+memberName+"                 \n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your Member Key: "+memberKey+"                      \n");
          emailbody.append("                                                        \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append(" *******************************************************\n");
          emailbody.append("\n");
          emailbody.append("\n");
          emailbody.append(" To complete your registration click on the link below:\n");
          emailbody.append("\n");
          emailbody.append(" http://composerlab.org/services/memberships/activation/"+memberName+"/"+memberKey+"\n");
          emailbody.append("\n");
          emailbody.append("\n");
          emailbody.append("\n");

          // send instructions to the twitter user for completing the link
          sendEmail(emailId, memberName+" - Cafepress/Composer Membership Registration - ", emailbody.toString());

        } catch (Exception e) {
          log.error("Register.member Exception: "+e);
        }
    }

    public static void emailUserKey(String emailId, String appName, String userId, String userKey) {
        try {
          // Get email for equalsId
          StringBuffer emailbody = new StringBuffer();
          emailbody.append(" *******************************************************\n");
          emailbody.append("   Cafepress/Composer User Registration                 \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append("                                                        \n");
          emailbody.append("   Application Name: "+appName+"                 \n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your User Id: "+userId+"                 \n");
          emailbody.append("                                                        \n");
          emailbody.append("   Your User Key: "+userKey+"                      \n");
          emailbody.append("                                                        \n");
          emailbody.append(" _______________________________________________________\n");
          emailbody.append(" *******************************************************\n");
          emailbody.append("\n");
          emailbody.append("\n");
          emailbody.append(" To complete your registration click on the link below:\n");
          emailbody.append("\n");
          emailbody.append(" http://composerlab.org/services/"+appName+"/users/activation/"+userId+"/"+userKey+"\n");
          emailbody.append("\n");
          emailbody.append("\n");
          emailbody.append("\n");

          // send instructions to the twitter user for completing the link
          sendEmail(emailId, appName+"::"+userId+" - Cafepress/Composer User Registration - ", emailbody.toString());

        } catch (Exception e) {
          log.error("Register.member Exception: "+e);
        }
    }

    private static boolean sendEmail(String toEmail, String subject, String body) {

      Session session = Session.getDefaultInstance(mailprops,
                new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(emailUser, emailUserPassword);
                    }
                });

      MimeMessage message = new MimeMessage( session );
      session.setDebug(true);

      try {
          message.setFrom(new InternetAddress(emailUser));
          message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
          message.setSubject(subject);
          message.setContent(body,"text/plain");

          Transport.send(message);

          return true;
      } catch (MessagingException ex){
          log.debug("Register.sendEmail MessagingException: "+toEmail+"\n Error: " + ex);
          return false;
      }
  }

    protected static MailAuthenticator getMailAuthenticator() {
      return new MailAuthenticator();
  }

  public static class MailAuthenticator extends Authenticator {

      public MailAuthenticator() {
      }

      public PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(emailUser, emailUserPassword);
      }

  }
}
