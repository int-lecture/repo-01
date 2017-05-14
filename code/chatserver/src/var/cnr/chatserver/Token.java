package var.cnr.chatserver;

import java.util.Date;

public class Token
{
	private String value;
	private Date expireDate;

	public Token(String value, Date expireDate)
	{
		this.value = value;
		this.expireDate = expireDate;
	}

	public String getValue()
	{
		return value;
	}

	public Date getExpireDate()
	{
		return expireDate;
	}
}
