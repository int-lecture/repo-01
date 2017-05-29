package var.cnr.registrationserver;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.gt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.PseudoColumnUsage;
import java.util.ArrayList;
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
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;



/**
 * A registration server built on the RESTful API that can create and store user profiles.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
@Path("")
public class RegistrationServer
{

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
		System.out.println("[ENTER] drücken, um Grizzly zu beenden");
		System.in.read();
		threadSelector.stopEndpoint();
		System.out.println("Grizzly wurde beendet");
		System.exit(0);
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
				return Response.status(Response.Status.NOT_ACCEPTABLE).build();
			}

		  //Erstelleung des JSON Objekts mit der "Success" Nachricht an den Client
		  JSONObject obj = new JSONObject();
		  obj.put("success", "true");

		  return Response.status(Response.Status.OK).entity(obj.toString()).build();
		}
		catch (JSONException | NoSuchAlgorithmException | InvalidKeySpecException e)
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
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
			String token = jsonObj.getString("token"); // TODO: validate token

			collection.find(eq("pseudonym",nickname))
	         .forEach((Block<Document>) e -> documents.add(e));

			if (!documents.isEmpty())
			{

				return Response.status(Response.Status.OK).entity(documentToJSONObject(documents).toString()).build();
			}
			else
			{
				return Response.status(Response.Status.NO_CONTENT).build();
			}
		}
		catch (JSONException e)
		{
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
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






