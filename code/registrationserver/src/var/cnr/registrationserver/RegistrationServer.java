package var.cnr.registrationserver;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PseudoColumnUsage;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
 * A registration server built on the RESTful API that can create and store user profiles.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
@Path("")
public class RegistrationServer
{
	/** String for date parsing in ISO 8601 format. */
	public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static final String MONGO_URL = "mongodb://141.19.142.55:27017";

	 /** URI to the MongoDB instance. */
    private static MongoClientURI connectionString =
            new MongoClientURI(MONGO_URL);

    /** Client to be used. */
    private static MongoClient mongoClient = new MongoClient(connectionString);

    /** Mongo database. */
    private static MongoDatabase database = mongoClient.getDatabase("chat");

	public static void main(String[] args) throws IllegalArgumentException, IOException
	{

		final String baseUri = "http://localhost:5002/";
		final String packageName = "var.cnr.registrationserver";
		final Map<String, String> initParams = new HashMap<String, String>();
		initParams.put("com.sun.jersey.config.property.packages", packageName);

		System.out.println("Starte grizzly...");
		SelectorThread threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
		System.out.printf("Grizzly läuft unter %s%n", baseUri);
	}

	/**
	 * Registers a new user if the nickname doesn't already exist.
	 * @param profile	A JSON object containing all information relevant for sign-up.
	 * @return			A HTTP response indicating success or failure of registration.
	 */
	@PUT
	@Path("/register")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(String profile)
	{

		 MongoCollection<Document> collection = database.getCollection("profiles");
		 List<Document> documents = new ArrayList<>();

//			zu testzwecken!
//			String profiltest = "{ \"user\": \"glatzo\", \"pseudonym\": \"glatze\", \"password\": \"123\"}";

		try
		{
			JSONObject jsnobj = new JSONObject(profile);
			String user = jsnobj.getString("user");
			String pseudonym = jsnobj.getString("pseudonym");
			String password = jsnobj.getString("password");
			String secPassword = SecurityHelper.hashPassword(password);

			collection.find(and(eq("user",user),eq("pseudonym",pseudonym)))
	         .forEach((Block<Document>) e -> documents.add(e));

			if (documents.isEmpty())
			{

				collection.insertOne(new Profile(pseudonym, user, secPassword).toDocument());

//				nicknames.put(user, pseudonym);
//				passwords.put(user, password);
//				profiles.put(pseudonym, new Profile(pseudonym, user));
			}
			else
			{
				//Hier sollte der Statuscode I�m a Teapot 418 gesendet werden laut Aufgabe
				return Response
						.status(Response.Status.NOT_ACCEPTABLE)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
						.build();
			}

		  //Erstelleung des JSON Objekts mit der "Success" Nachricht an den Client
		  JSONObject obj = new JSONObject();
		  obj.put("success", "true");

		  return Response
				  .status(Response.Status.OK)
				  .entity(obj.toString())
				  .header("Access-Control-Allow-Origin", "*")
				  .header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
				  .build();
		}
		catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
					.build();
		}
	}

	/**
	 * Retrieves the own profile of the user who requests it, if the token is valid.
	 * @param request	The profile request, in JSON format.
	 * @return			A HTTP response containing the profile in JSON format, if successful.
	 */
	@POST
	@Path("/profile")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getProfile(String request)
	{
		System.out.println("test");

		MongoCollection<Document> collection = database.getCollection("profiles");
		List<Document> documents = new ArrayList<>();

		try
		{
			JSONObject jsonObj = new JSONObject(request);
			String nickname = jsonObj.getString("getownprofile");
			String token = jsonObj.getString("token");

			if (!validateToken(token, nickname))
			{
				return Response
						.status(Response.Status.UNAUTHORIZED)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
						.build();
			}

			collection.find(eq("pseudonym",nickname))
	         .forEach((Block<Document>) e -> documents.add(e));

			if (!documents.isEmpty())
			{

				return Response
						.status(Response.Status.OK)
						.entity(documentToJSONObject(documents).toString())
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
						.build();
			}
			else
			{
				return Response
						.status(Response.Status.NO_CONTENT)
						.header("Access-Control-Allow-Origin", "*")
						.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
						.build();
			}
		}
		catch (JSONException e)
		{
			return Response
					.status(Response.Status.BAD_REQUEST)
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, OPTIONS, POST, DELETE, PUT")
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

				if (new SimpleDateFormat(ISO8601).parse(responseObj.getString("expire-date")).after(new Date()))
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
	 * Converts the given list<document> to JSONObject and returns the Object
	 */
	static 	public JSONObject documentToJSONObject( List<Document> documents) {

		try {
			JSONObject obj = new JSONObject();
			  for (Document document : documents) {
		            obj.put("user", document.getString("user"));
		            obj.put("pseudonym", document.getString("pseudonym"));
		            obj.put("password", document.getString("password"));

		        }


			  return obj;

		} catch (Exception e2) {
			System.out.println(e2);
			return null;
		}

	}



}






