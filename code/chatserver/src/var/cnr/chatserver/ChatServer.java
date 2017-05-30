package var.cnr.chatserver;

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

import org.codehaus.jettison.json.*;

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
	/**
	 * Contains the last sequence number for all users.
	 */
	private static HashMap<String, Integer> userSequenceNumbers = new HashMap<>();

	/**
	 * The thread lock is used to prevent inconsistent file access through multiple threads.
	 */
	private static Object threadLock = new Object();

	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		final String baseUri = "http://localhost:5000/";
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

		try
		{
			synchronized (threadLock)
			{
				messages = readFile(userId + ".txt");
			}

			if (messages == null)
			{
				return Response
						.status(Response.Status.NO_CONTENT)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
						.build();
			}
		}
		catch (Exception e)
		{
			return Response
					.status(Response.Status.NO_CONTENT)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.build();
		}

		Message[] unconfirmedMessages;

		if (sequenceNumber != null)
		{
			int unconfirmed = messages[messages.length - 1].getSequence() - Integer.parseInt(sequenceNumber);
			unconfirmedMessages = new Message[unconfirmed];

			for (int i = 0; i < unconfirmedMessages.length; i++)
			{
				unconfirmedMessages[i] = messages[messages.length - unconfirmedMessages.length + i];
			}
		}
		else
		{
			unconfirmedMessages = messages;
		}

		try
		{
			synchronized (threadLock)
			{
				writeToFile(userId, unconfirmedMessages);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
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

			String fileName = message.getTo() + ".txt";
			message.setSequence(increaseUserSequence(fileName));

			synchronized (threadLock)
			{
				writeToFile(fileName, message);
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

	private synchronized boolean validateToken(String token, String nickname)
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
		WebResource webResource = client.resource("http://141.19.142.57:5001/auth");
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
	 * Reads a file if it exists, converts the data to messages and returns them. The file is then deleted.
	 * @param fileName		The name of the file.
	 * @return				An array of messages or null if the file doesn't exist.
	 * @throws IOException	Thrown when the file can't be read.
	 */
	private Message[] readFile(String fileName) throws IOException
	{
		File file = new File(fileName);

		if (file.exists())
		{
			byte[] countBytes = new byte[4];
			FileInputStream inStream = new FileInputStream(fileName);
			inStream.read(countBytes, 0, 4);
			int count = ByteBuffer.wrap(countBytes).getInt();
			byte[][] messageBytes = new byte[count][];
			Message[] messages = new Message[count];

			for (int i = 0; i < count; i++)
			{
				byte[] messageLengthBytes = new byte[4];
				inStream.read(messageLengthBytes, 0, 4);
				int messageLength = ByteBuffer.wrap(messageLengthBytes).getInt();
				messageBytes[i] = new byte[messageLength];
				inStream.read(messageBytes[i], 0, messageLength);

				messages[i] = Message.Deserialize(messageBytes[i]);
			}

			inStream.close();
			file.delete();

			return messages;
		}
		else
		{
			return null;
		}
	}

	/**
	 * Writes messages into a file. If the file already exists, the messages will be appended.
	 * @param fileName		The name of the file.
	 * @param messages		An array of messages.
	 * @throws IOException	Thrown when the file can't be written.
	 */
	private void writeToFile(String fileName, Message... messages) throws IOException
	{

		File file = new File(fileName);
		Message[] existingMessages = new Message[0];

		if (file.exists())
		{
			existingMessages = readFile(fileName);
		}

		int count = messages.length + existingMessages.length;
		int[] messageLengths = new int[count];
		byte[][] messageBytes = new byte[count][];

		for (int i = 0; i < existingMessages.length; i++)
		{
			messageBytes[i] = existingMessages[i].Serialize();
			messageLengths[i] = messageBytes[i].length;
		}

		for (int i = existingMessages.length; i < count; i++)
		{
			messageBytes[i] = messages[i - existingMessages.length].Serialize();
			messageLengths[i] = messageBytes[i].length;
		}

		FileOutputStream outStream = new FileOutputStream(fileName);
		byte[] countBytes = ByteBuffer.allocate(4).putInt(count).array();
		outStream.write(countBytes);

		for (int i = 0; i < count; i++)
		{
			byte[] messageLengthBytes = ByteBuffer.allocate(4).putInt(messageLengths[i]).array();
			outStream.write(messageLengthBytes);
			outStream.write(messageBytes[i]);
		}

		outStream.close();
	}

	/**
	 * Increases the sequence number of a user and returns the previous sequence number.
	 * @param userId		The id of the user.
	 * @return				The last sequence number.
	 */
	private synchronized int increaseUserSequence(String userId)
	{
		if (userSequenceNumbers.containsKey(userId))
		{
			int seq = userSequenceNumbers.get(userId);
			userSequenceNumbers.put(userId, seq + 1);
			return seq;
		}
		else
		{
			userSequenceNumbers.put(userId, 1);
			return 0;
		}
	}
}