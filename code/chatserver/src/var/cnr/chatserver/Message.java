package var.cnr.chatserver;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Chat message.
 */
class Message
{
	/** String for date parsing in ISO 8601 format. */
	public static final String ISO8601 = "yyyy-MM-dd'T'HH:mm:ssZ";

    /** From. */
    private String from;

    /** To. */
    private String to;

    /** Date. */
    private Date date;

    /** Text. */
    private String text;

    /** Sequence number. */
    private int sequence;

    /**
     * Create a new message.
     *
     * @param from From.
     * @param to To.
     * @param date Date.
     * @param text Contents.
     * @param sequence Sequence-Number.
     */
    public Message(String from, String to, Date date, String text, int sequence)
    {
        this.from = from;
        this.to = to;
        this.date = date;
        this.text = text;
        this.sequence = sequence;
    }

    /**
     * Create a new message.
     *
     * @param from From.
     * @param to To.
     * @param date Date.
     * @param text Contents.
     */
    public Message(String from, String to, Date date, String text)
    {
        this(from, to, date, text, 0);
    }

    public Message(JSONObject message) throws ParseException, JSONException
    {
    	from = message.getString("from");
    	to = message.getString("to");
    	date = new SimpleDateFormat(ISO8601).parse(message.getString("date"));
    	text = message.getString("text");
    	sequence = message.getInt("sequence");
    }

    private String getDate()
    {
    	return new SimpleDateFormat(ISO8601).format(date);
    }

    public String getFrom()
    {
    	return from;
    }

    public String getTo()
    {
    	return to;
    }

    public int getSequence()
    {
    	return sequence;
    }

    public JSONObject toJSONObject() throws JSONException
    {
    	JSONObject obj = new JSONObject();
    	obj.put("from", from);
    	obj.put("to", to);
		obj.put("date", getDate());
		obj.put("text", text);
		obj.put("sequence", sequence);
		return obj;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        SimpleDateFormat sdf = new SimpleDateFormat(ISO8601);

        return String.format("{ 'from': '%s', 'to': '%s', 'date': '%s', 'text': '%s'}".replace('\'',  '"'),
                from, to, sdf.format(new Date()), text);
    }
}