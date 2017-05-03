package var.cnr.chatserver;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.*;
import org.codehaus.jettison.json.*;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

/**
 * A chat server built on the RESTful API that can receive, store and send messages in JSON format.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
@Path("")
public class ChatServer
{
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
	public Response getMessages(@PathParam("user_id") String userId)
	{

		return getMessages(userId, null);
	}

	/**
	 * Retrieves all available messages for a user with a sequence number greater than the parameter sequenceNumber,
	 * sends them to the client and deletes them.
	 * @param userId			The user ID of the user.
	 * @param sequenceNumber	The number of the message that the client received last.
	 * @return					A HTTP response with all available messages in JSON format attached.
	 */
	@GET
	@Path("/messages/{user_id}/{sequene_number}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMessages(@PathParam("user_id") String userId, @PathParam("sequence_number") String sequenceNumber)
	{
		Message[] messages;

		try
		{
			messages = readFile(userId + ".txt");

			if (messages == null)
			{
				return Response.status(Response.Status.NO_CONTENT).build();
			}
		}
		catch (Exception e)
		{
			return Response.status(Response.Status.NO_CONTENT).build();
		}

		Message[] unreadMessages;

		if (sequenceNumber != null)
		{
			int sequence = Integer.parseInt(sequenceNumber);
			int unread = messages[messages.length - 1].getSequence() - sequence;
			unreadMessages = new Message[unread];
			System.arraycopy(messages, messages.length - unread, unreadMessages, 0, unread);
		}
		else
		{
			unreadMessages = messages;
		}

		return Response.status(Response.Status.OK).entity(messagesToJSONArray(unreadMessages).toString()).build();
	}


	/**
	 * Stores the message in a file so the chat partner can retrieve it. If the message was in correct format,
	 * the server will answer with HTTP code 201, the message's sequence number and the server time, in JSON format.
	 * @param msg	The message for the chat partner, in JSON format.
	 * @return		A HTTP response.
	 */
	@PUT
	@Path("/send")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response sendMessage(String msg)
	{
		try
		{
			Message[] message = new Message[1];
			message[0] = new Message(new JSONObject(msg));
			String fileName = message[0].getTo() + ".txt";
			message[0].setSequence(getUserSequence(fileName) + 1);
			writeToFile(fileName, message);

			JSONObject obj = new JSONObject();
			obj.put("date", message[0].getDate());
			obj.put("sequence", message[0].getSequence());
			return Response.status(Response.Status.CREATED).entity(obj).build();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
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
	private void writeToFile(String fileName, Message[] messages) throws IOException
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
	 * Returns the last sequence number of a user's stored messages or 0 if there are none.
	 * @param fileName		The name of the file.
	 * @return				The last sequence number or 0 if the file doesn't exist.
	 * @throws IOException	Thrown when the file can't be read.
	 */
	private int getUserSequence(String fileName) throws IOException
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

			return messages[messages.length - 1].getSequence();
		}
		else
		{
			return 0;
		}
	}
}
