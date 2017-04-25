package var.cnr.chatserver;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.*;
import org.codehaus.jettison.json.*;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

@Path("")
public class ChatServer
{
	public static void main(String[] args) throws IllegalArgumentException, IOException
	{
		final String baseUri = "http://localhost:9998/";
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

	@GET
	@Path("/messages/{user_id}/{sequene_number}")
	@Produces("application/json")
	public Response getMessage(@PathParam("user_id") String userId, @PathParam("sequence_number") String sequenceNumber)
	{
		List<Message> messages;

		try
		{
			messages = readFile(userId + ".txt");
		}
		catch (Exception e)
		{
			return Response.noContent().build();
		}

		List<Message> unreadMessages;

		if (sequenceNumber != null)
		{
			int sequence = Integer.parseInt(sequenceNumber);
			unreadMessages = messages.subList(sequence - messages.get(0).getSequence() + 1, messages.size());
		}
		else
		{
			unreadMessages = messages;
		}

		new File(userId + ".txt").delete();
		return Response.ok(messagesToJSONArray((Message[]) unreadMessages.toArray())).build();
	}

	@PUT
	@Path("/send")
	@Consumes("application/json")
	public Response sendMessage(JSONObject object)
	{
		System.out.println("test");
		try
		{
			Message message = new Message(object);
			String fileName = message.getFrom() + message.getTo() + ".txt";
			List<Message> messageList = readFile(fileName);
			messageList.add(message);
			writeToFile(fileName, (Message[]) messageList.toArray());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return Response.created(null).build();
	}

	private JSONArray messagesToJSONArray(Message[] messages)
	{
		JSONArray array = new JSONArray();

		for (int i = 0; i < messages.length; i++)
			array.put(messages[i]);

		return array;
	}

	private List<Message> readFile(String fileName) throws JsonParseException, IOException, ParseException
	{
		List<Message> messageList = new ArrayList<Message>();
		JsonFactory factory = new JsonFactory();

		JsonParser parser = factory.createJsonParser(new File(fileName));
		JsonToken token = parser.nextToken();

		while(token != JsonToken.END_ARRAY)
		{
			String from = null, to = null, text = null;
			Date date = null;
			int sequence = -1;

			while(token!= JsonToken.END_OBJECT)
			{
				String fieldName = parser.getCurrentName();

				if(fieldName.equals("from"))
				{
					token = parser.nextToken();
					from = parser.getText();
				}
				else if(fieldName.equals("to"))
				{
					token = parser.nextToken();
					to = parser.getText();
				}
				else if(fieldName.equals("text"))
				{
					token = parser.nextToken();
					text = parser.getText();
				}
				else if(fieldName.equals("date"))
				{
					token = parser.nextToken();
					date = new SimpleDateFormat(Message.ISO8601).parse(parser.getText());
				}
				else if(fieldName.equals("sequence"))
				{
					token = parser.nextToken();
					sequence = parser.getIntValue();
				}

				token = parser.nextToken();
			}

			parser.close();
			messageList.add(new Message(from, to, date, text, sequence));
		}

		return messageList;
	}

	private void writeToFile(String fileName, Message[] messages) throws JsonGenerationException, IOException, JSONException
	{
		JsonFactory factory = new JsonFactory();
		JsonGenerator generator = factory.createJsonGenerator(new File(fileName), JsonEncoding.UTF8);
		generator.writeStartArray();

		for (int i = 0; i < messages.length; i++)
		{
			generator.writeObject(messages[i].toJSONObject());
		}

		generator.writeEndArray();
		generator.close();
	}
}
