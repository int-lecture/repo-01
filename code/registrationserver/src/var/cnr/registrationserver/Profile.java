package var.cnr.registrationserver;

import java.util.ArrayList;
import java.util.Arrays;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Represents a user profile.
 * @author Christopher Rotter, Nico Gensheimer, Raphael Lubaschewski
 */
public class Profile
{
	/**
	 * The nickname of the user.
	 */
	private String name;

	/**
	 * The email address of the user.
	 */
	private String email;

	/**
	 * The nicknames of the user's contacts.
	 */
	private ArrayList<String> contacts = new ArrayList<>();

	/**
	 * Creates a new user profile.
	 * @param name		The nickname of the user.
	 * @param email		The email address of the user.
	 */
	public Profile(String name, String email)
	{
		this.name = name;
		this.email = email;
	}

	public String getName()
	{
		return name;
	}

	public String getEmail()
	{
		return email;
	}

	public String[] getContacts()
	{
		return (String[]) contacts.toArray();
	}

	/**
	 * Adds a nickname to the user's contacts.
	 * @param name	The nickname of the contact to be added.
	 */
	public void addContact(String name)
	{
		contacts.add(name);
	}

	/**
	 * Removes the nickname from the user's contacts.
	 * @param name	The nickname of the contact to be removed.
	 */
	public void removeContact(String name)
	{
		contacts.remove(name);
	}

	/**
	 * Converts this object into a JSON object.
	 * @return					The JSON object.
	 * @throws JSONException	Thrown when the object can't be converted.
	 */
	public JSONObject toJSONObject() throws JSONException
	{
		JSONObject obj = new JSONObject();
    	obj.put("name", name);
    	obj.put("email", email);
    	obj.put("contact", stringArrayToJSONArray(getContacts()));
		return obj;
	}

	/**
	 * Converts a string array into a JSON array.
	 * @param stringArray	The string array.
	 * @return				The JSON array.
	 */
	private JSONArray stringArrayToJSONArray(String[] stringArray)
	{
		JSONArray array = new JSONArray();

		for (int i = 0; i < stringArray.length; i++)
		{
			array.put(stringArray[i]);
		}

		return array;
	}
}
