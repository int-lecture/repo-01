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
			System.out.printf("Grizzly lÃ¤uft unter %s%n", baseUri);
			System.out.println("[ENTER] drÃ¼cken, um Grizzly zu beenden");
			System.in.read();
			threadSelector.stopEndpoint();
			System.out.println("Grizzly wurde beendet");
			System.exit(0);
		}


		HashMap<String,String> userPseudoHashMap = new HashMap<>();
		HashMap<String,String> userPasswordHashMap = new HashMap<>();



		@PUT
		@Path("/register")
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.APPLICATION_JSON)
		public Response register(String profil){


//			zu testzwecken!
//			String profiltest = "{ \"user\": \"glatzo\", \"pseudonym\": \"glatze\", \"password\": \"123\"}";

			try {
				
				JSONObject jsnobj = new JSONObject(profil);
				String user = (String) jsnobj.get("user");
				String pseudonym = (String) jsnobj.get("pseudonym");
				String password = (String) jsnobj.get("password");
				boolean contains = false;

				  for(String key : userPseudoHashMap.keySet())
				    {
					  if (key.contains(user)) {
						  contains = true;
						  if (userPseudoHashMap.get(key).contains(pseudonym)) {
							  contains = true;
						}

					}

				    }
				  if (!contains) {
					  userPasswordHashMap.put(user, password);
					  userPseudoHashMap.put(user, pseudonym);
				}else{
					
					//Hier sollte der Statuscode I´m a Teapot 418 gesendet werden laut Aufgabe
					return Response.status(Response.Status.FORBIDDEN).build();
				}
				  
				  //Erstelleung des JSON Objekts mit der "Success" Nachricht an den Client
				  JSONObject successJsonObj = new JSONObject();
				  successJsonObj.put("success", "true");
				  
				return Response.status(Response.Status.OK).entity(successJsonObj).build();
			} catch (Exception e) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		


		}

		@POST
		@Path("/profile")
		@Produces(MediaType.APPLICATION_JSON)
		@Consumes(MediaType.APPLICATION_JSON)
		public Response profileRequest(String profil){
			
			try {
				JSONObject jsonObj = new JSONObject(profil);
				//Auswertung des JsonStrings von dem Wert getownprofile
				//muss dann mit dem DB eintrag abgeglichen werden.
//				String ownprofile = jsonObj.get(getownprofile);
				
				JSONObject profilJsonObj = new JSONObject();
				
				
				//Hier müssen dann die Daten aus der DB gezogen werden (name, email, contact)
//				profilJsonObj.put(key, value)
				return Response.status(Response.Status.OK).entity(profilJsonObj).build();
				 
			} catch (Exception e) {
				return Response.status(Response.Status.BAD_REQUEST).build();
			}
		}
		}

		




