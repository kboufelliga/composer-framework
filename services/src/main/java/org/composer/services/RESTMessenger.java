package org.composer.services;

import org.composer.server.core.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.composer.engine.annotations.DomainPath;
import org.composer.engine.annotations.ContextPath;

import org.composer.server.utils.KeyGen;
import org.composer.server.core.Application;
import org.composer.server.core.Membership;
import org.composer.engine.core.ResourceManager;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.AMQP;

/**
 * Created by IntelliJ IDEA.
 * User: kboufelliga
 * Date: Nov 17, 2008
 * Time: 10:12:19 PM
 * To change this template use File | Settings | File Templates.
 */
@Path("/messenger")
public class RESTMessenger {
    @Context UriInfo uriInfo;
    @Context SecurityContext security;

    private Log log = LogFactory.getLog(RESTManager.class);
    private ResourceManager resourceManager = ResourceManager.getInstance();
    private Application application = new Application(resourceManager);
    private Membership membership = new Membership(resourceManager);
    private Messenger messenger;

    public RESTMessenger() {
         try {
            BasicDataSource ds = new BasicDataSource();
            Configuration config = new PropertiesConfiguration("datasource.properties");

            ds.setDriverClassName(config.getString("className"));
            ds.setUsername(config.getString("username"));
            ds.setPassword(config.getString("password"));
            ds.setUrl(config.getString("url"));

            ResourceManager.setDataSource(ds);

            if (config.getString("databaseType") != null) {
                ResourceManager.setDatabaseType(config.getString("databaseType"));
            }

            Configuration rabbitConfig = new PropertiesConfiguration("rabbitmq.properties");
            messenger = new Messenger.Builder(rabbitConfig.getString("hostName"), rabbitConfig.getInt("portNumber"), rabbitConfig.getString("userName"), rabbitConfig.getString("userPassword")).virtualHost(rabbitConfig.getString("virtualHost")).build();
            
        } catch (Exception e) {
            log.error("datasource set up failed "+e);
        }

    }


    @DomainPath("/domain")
    @ContextPath("/context")
    @PUT
    @Produces({"application/xml", "application/json"})
    @Path("/send/{exchangeName}/{queueName}/{namespace}/{resourceName}")
    public Response send(@PathParam("exchangeName") String exchangeName, @PathParam("queueName") String queueName, @PathParam("namespace") String namespace, @PathParam("resourceName") String resourceName,@QueryParam("parms") String parmsList) {
        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
            Collection<String> parms = new ArrayList();

            JSONArray parmsArray = JSONArray.fromString(parmsList);
            for (int i=0; i < parmsArray.length(); i++) {
                parms.add(parmsArray.getString(i));
            }

            String messageData = resourceManager.read(namespace,resourceName, parms);

            Channel channel = messenger.createChannel();
            int ticket = channel.accessRequest("/data/commission");
            String routingKey = KeyGen.generateKey();

            channel.exchangeDeclare(ticket, exchangeName, "direct");
            channel.queueDeclare(ticket, queueName);
            channel.queueBind(ticket, queueName, exchangeName, routingKey);

            byte[] messageBodyBytes = messageData.getBytes();

            channel.basicPublish(ticket, exchangeName, routingKey,
                                 MessageProperties.PERSISTENT_TEXT_PLAIN,
                                 messageBodyBytes);
            AMQP.BasicProperties amqpProps = new AMQP.BasicProperties
                       (null, "application/json", null, null,
                        null, null, null, null,
                        resourceName, new Date(), null, null,
                        null, null);
            channel.basicPublish(ticket, exchangeName, routingKey,
                                             amqpProps,
                                             messageBodyBytes);

            JSONArray result = new JSONArray();
            JSONObject responseList = new JSONObject();
            responseList.put("ticket",ticket);
            responseList.put("routingKey",routingKey);
            result.put(responseList);

            return Response.ok(result.toString(),"application/json").build();
         } catch (Exception e) {
                log.error("exception writing response - "+e);
         }

         return Response.serverError().build();
    }

    @DomainPath("/cp/tests")
    @GET
    @Produces({"application/xml", "application/json"})
    @Path("/receive/{exchangeName}/{queueName}/{routingKey}")
    public Response receive(@PathParam("exchangeName") String exchangeName, @PathParam("queueName") String queueName,@PathParam("routingKey") String routingKey) {
        log.info("........... checking queue named: "+queueName);
        log.info("........... checking queue with routing key: "+routingKey);
        URI uri =  uriInfo.getAbsolutePath();
        Response.created(uri).build();

        try {
             Channel channel = messenger.createChannel();
             int ticket = channel.accessRequest("/data/commission");

             boolean noAck = false;
             GetResponse channelResponse = channel.basicGet(ticket, queueName, noAck);

             if (channelResponse == null) {
                log.info("................ GOT AN EMPTY RESPONSE FOR TICKET: "+ticket);

                return Response.ok(new JSONArray().toString(),"application/json").build();
             } else {
                AMQP.BasicProperties props = channelResponse.getProps();
                byte[] body = channelResponse.getBody();
                 log.info("................ RESPONSE: "+new String(body));

                long deliveryTag = channelResponse.getEnvelope().getDeliveryTag();
                 log.info("................ DELIVERY TAG: "+deliveryTag);


                channel.basicAck(deliveryTag, false);

                return Response.ok(new String(body),"application/json").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error("exception writing response - "+e);
        }

        return Response.serverError().build();
    }
}
