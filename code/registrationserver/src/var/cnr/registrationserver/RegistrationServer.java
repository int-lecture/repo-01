package var.cnr.regeistrationserver;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jettison.json.*;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import org.codehaus.jettison.json.JSONObject;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;


public class RegistrationServer {


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



		@PUT
		@Path("/register")
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.APPLICATION_JSON)
		public Response register(JSONObject profil){

			try {

				String user = (String) profil.get("user");
				String pseudonym = (String) profil.get("pseudonym");
				String password = (String) profil.get("password");

				return Response.status(Response.Status.OK).build();
			} catch (Exception e) {
				// TODO: handle exception
			}
			return null;


		}



		}




