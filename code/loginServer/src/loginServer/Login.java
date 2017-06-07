package loginServer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import org.apache.commons.codec.binary.Base64;

@Path("/")
public class Login {

	public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";

	private static Map<String, String> passwordList = new HashMap<>();
	private static Map<String, String> tokenList = new HashMap<>();

	/*
	 * Verbesserung: DB und SQL statt password liste.
	 * Wie kann man das Expire-Date selber setzen, mit vorgefertigten Funktionen.
	 * Token mit JWT oder Random erstellen.
	 * Email auf gültigkeit Testen.
	 * 
	 * RestAssured zum Testen von RestServices
	 */
	
	
	
	@POST
	@Path("/login")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response login(String jsonString) throws JSONException, ParseException {

		JSONObject jsonObject = new JSONObject(jsonString);
		// Wenn die JsonDatei falsch formatiert ist, dann Sende 400/Bad-Request.
		if (!jsonObject.optString("name").isEmpty() || !jsonObject.optString("password").isEmpty()
				|| !jsonObject.optString("pseudonym").isEmpty()) {
			// Wenn das Passwort falsch ist dann sende Unautorisiert ohne
			// Message.
			if(passwordList.containsKey(jsonObject.optString("pseudonym"))){
				String password = jsonObject.optString("password");
				String pseudonym = jsonObject.optString("pseudonym");
				if (checkPassword(pseudonym, password)) {
					JSONObject tokenObject = new JSONObject();
					//SimpleDateFormat statt Date();
					//Token mit JWT erstellen oder über eine Liste von vorgefertigten Tokens.
					SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
					tokenObject.put("token", createToken(pseudonym,"testtoken")).put("expire-date", sdf.format(new Date(new Date().getTime()+5*60*1000)));
					return Response.ok().entity(tokenObject.toString()).build();
				}else{
					return Response.status(Response.Status.UNAUTHORIZED).build();
					
				}
			
			} else {
				passwordList.put(jsonObject.optString("pseudonym"), createToken(jsonObject.optString("pseudonym"),jsonObject.optString("password")));
				return Response.ok().status(Response.Status.CREATED).build();
			}
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}

	@POST
	@Path("/auth")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response auth(String jsonString) throws JSONException {
		JSONObject jsonObject = new JSONObject(jsonString);
		if (!jsonObject.optString("token").isEmpty() || !jsonObject.optString("pseudonym").isEmpty()) {
			// Token muss mit pseudonym übereinstimmen. -> HashMap
			if(tokenList.get(jsonObject.optString("pseudonym")).equals(jsonObject.optString("token"))){
				JSONObject successJsonObject = new JSONObject();
				//SimpleDateFormat formatierung statt Date();
				SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);
				successJsonObject.put("success", "true").put("expire-Date", sdf.format(new Date(new Date().getTime()+5*60*1000)));
				return Response.ok().entity(successJsonObject.toString()).build();
			}else{
				return Response.status(Response.Status.UNAUTHORIZED).build();
			}
			
		} else {
			return Response.status(Response.Status.BAD_REQUEST).build();
		}
	}


	private String createToken(String pseudonym,String stringToEncode) {
		byte[] encoded = Base64.encodeBase64(stringToEncode.getBytes());
		String token = new String(encoded);
		tokenList.put(pseudonym, token);
		return token;
	}

	private boolean checkPassword(String user, String password) {
		String check = passwordList.get(user);
		byte[] decoded = Base64.decodeBase64(check);
		String compare = new String(decoded).toString();
		if (password.equals(compare)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/**
	 * Dient nur darum ein name und password hinzuzufügen
	 * @return
	 */
	@PUT
	@Path("/setuppassword")
	public Response setupPassword() {
		passwordList.put("dennis", createToken("dennis","test"));
		return Response.ok().build();

	}


}