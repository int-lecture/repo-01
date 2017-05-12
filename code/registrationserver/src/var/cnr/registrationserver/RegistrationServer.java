package var.cnr.registrationserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

/**
 * A registration server built on the RESTful API that can create and store user profiles.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
public class RegistrationServer
{
	/**
	 * Contains all registered users' nicknames. Key is email address.
	 */
	private static HashMap<String,String> nicknames = new HashMap<>();

	/**
	 * Contains all registered users' passwords. Key is email address.
	 */
	private static HashMap<String,String> passwords = new HashMap<>();

	/**
	 * Contains all registered users' profiles. Key is nickname.
	 */
	private static HashMap<String,Profile> profiles = new HashMap<>();

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
//			zu testzwecken!
//			String profiltest = "{ \"user\": \"glatzo\", \"pseudonym\": \"glatze\", \"password\": \"123\"}";

		try
		{
			JSONObject jsnobj = new JSONObject(profile);
			String user = jsnobj.getString("user");
			String pseudonym = jsnobj.getString("pseudonym");
			String password = jsnobj.getString("password");

			if (!nicknames.containsKey(user))
			{
				nicknames.put(user, pseudonym);
				passwords.put(user, password);
				profiles.put(pseudonym, new Profile(pseudonym, user));
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
		catch (JSONException e)
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
		try
		{
			JSONObject jsonObj = new JSONObject(request);
			String nickname = jsonObj.getString("name");
			String token = jsonObj.getString("token"); // TODO: validate token

			if (profiles.containsKey(nickname))
			{
				return Response.status(Response.Status.OK).entity(profiles.get(nickname).toJSONObject().toString()).build();
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
}






