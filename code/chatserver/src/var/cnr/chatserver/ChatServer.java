package var.cnr.chatserver;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

import java.io.*;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.codehaus.jettison.json.*;

import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

/**
 * A chat server built on the RESTful API that can receive, store and send messages in JSON format.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
@Path("")
public class ChatServer
{
	private static final String MONGO_URL = "mongodb://141.19.142.55:27017";

	 /** URI to the MongoDB instance. */
    private static MongoClientURI connectionString = new MongoClientURI(MONGO_URL);

    /** Client to be used. */
    private static MongoClient mongoClient = new MongoClient(connectionString);

    /** Mongo database. */
    private static MongoDatabase database = mongoClient.getDatabase("chat");

	/**
	 * Contains the last sequence number for all users.
	 */
	//private static HashMap<String, Integer> userSequenceNumbers = new HashMap<>();

	/**
	 * The thread lock is used to prevent inconsistent file access through multiple threads.
	 */
	private static Object threadLock = new Object();

	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		final String baseUri = "http://localhost:" + args[0] + "/";
		final String packageName = "var.cnr.chatserver";
		final Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("com.sun.jersey.config.property.packages", packageName);

		System.out.println("Starte grizzly...");
		SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
		System.out.printf("Grizzly läuft unter %s%n", baseUri);
		System.out.println("[ENTER] drücken, um Grizzly zu beenden");
		System.in.read();
		threadSelector.stopEndpoint();
		System.out.println("Grizzly wurde beendet");
		System.exit(0);
	}

	/**
	 * Retrieves all available messages for a user, sends them to the client and deletes them.
	 * @param userId	The user ID of the user.
	 * @return			A HTTP response with all available messages in JSON format attached.
	 */
	@GET
	@Path("/messages/{user_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessages(@PathParam("user_id") String userId, @Context HttpHeaders header)
	{
		return getMessages(userId, null, header);
	}

	/**
	 * Retrieves all available messages for a user, sends them to the client and deletes those with a sequence number
	 * lower than or equal to the parameter sequenceNumber.
	 * @param userId			The user ID of the user.
	 * @param sequenceNumber	The number of the message that the client received last.
	 * @return					A HTTP response with all available messages in JSON format attached.
	 */
	@GET
	@Path("/messages/{user_id}/{sequene_number}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessages(@PathParam("user_id") String userId, @PathParam("sequence_number") String sequenceNumber, @Context HttpHeaders header)
	{
		MultivaluedMap<String, String> headerValues = header.getRequestHeaders();
		String token = headerValues.getFirst("Authorization");

		if (!validateToken(token, userId))
		{
			return Response
					.status(Response.Status.UNAUTHORIZED)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}

		Message[] messages;

		synchronized (threadLock)
		{
			MongoCollection<Document> collection = database.getCollection("messages");
			List<Document> documents = new ArrayList<>();
			collection.find(eq("user",userId)).forEach((Block<Document>) e -> documents.add(e));
			messages = Message.documentsToMessages((Document[]) documents.toArray());

			if (messages.length == 0)
			{
				return Response
						.status(Response.Status.NO_CONTENT)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
						.build();
			}

			int sequence = 0;

			if (sequenceNumber != null)
			{
				Integer.parseInt(sequenceNumber);
			}

			for (Document document : documents)
			{
				if (document.getInteger("sequence") <= sequence)
				{
					collection.deleteOne(document);
				}
			}
		}

		return Response
				.status(Response.Status.OK).entity(messagesToJSONArray(messages).toString())
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.build();
	}


	/**
	 * Stores the message in a file so the chat partner can retrieve it. If the message was in correct format,
	 * the server will answer with HTTP code 201, the message's sequence number and the server time, in JSON format.
	 * @param request	The message for the chat partner, in JSON format.
	 * @return			A HTTP response.
	 */
	@PUT
	@Path("/send")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessage(String request)
	{
		try
		{
			JSONObject jsonRequest = new JSONObject(request);
			String token = jsonRequest.getString("token");
			Message message = new Message(jsonRequest);

			if (!validateToken(token, message.getFrom()))
			{
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
						.build();
			}

			synchronized (threadLock)
			{
				message.setSequence(increaseUserSequence( message.getTo()));

				MongoCollection<Document> collection = database.getCollection("messages");
				collection.insertOne(message.toDocument());
			}

			JSONObject obj = new JSONObject();
			obj.put("date", message.getDate());
			obj.put("sequence", message.getSequence());
			return Response
					.status(Response.Status.CREATED).entity(obj.toString())
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response
					.status(Response.Status.BAD_REQUEST)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}
	}

	private boolean validateToken(String token, String nickname)
	{
		JSONObject obj = new JSONObject();
		try
		{
			obj.put("token", token);
			obj.put("pseudonym", nickname);
		}
		catch (JSONException e)
		{
			e.printStackTrace();
		}

		Client client = new Client();
		WebResource webResource = client.resource("http://141.19.142.55:5001/auth");
		ClientResponse response = webResource.accept(MediaType.APPLICATION_JSON).entity(obj).post(ClientResponse.class);
	    int status = response.getStatus();
	    String textEntity = response.getEntity(String.class);
	    System.out.println(status);

		if (status == 200)
		{
			try
			{
				JSONObject responseObj = new JSONObject(textEntity);
				  System.out.println(responseObj.getString("expire-date"));

				if (new SimpleDateFormat(Message.ISO8601).parse(responseObj.getString("expire-date")).after(new Date()))
				{
					return true;
				}
			}
			catch (JSONException | ParseException e)
			{
				e.printStackTrace();
			}
		}

		return false;
	}

	/**
	 * Converts Messages to JSONArray.
	 * @param messages	An array of messages.
	 * @return			A JSONArray object.
	 */
	private JSONArray messagesToJSONArray(Message[] messages)
	{
		JSONArray array = new JSONArray();

		for (int i = 0; i < messages.length; i++)
		{
			try
			{
				array.put(messages[i].toJSONObject());
			}
			catch (JSONException e)
			{
				e.printStackTrace();
			}
		}

		return array;
	}

	/**
	 * Increases the sequence number of a user and returns the previous sequence number.
	 * @param userId		The id of the user.
	 * @return				The last sequence number.
	 */
	private int increaseUserSequence(String userId)
	{
		int sequence = 0;

		MongoCollection<Document> collection = database.getCollection("userSequences");
		List<Document> documents = new ArrayList<>();
		collection.find(eq("user",userId)).forEach((Block<Document>) e -> documents.add(e));

		if (!documents.isEmpty())
		{
			Document first = documents.get(0);
			sequence = first.getInteger("sequence");
			collection.deleteOne(first);
		}

		collection.insertOne(new Document().append("user", userId).append("sequence", sequence + 1));
		return sequence;
	}
}