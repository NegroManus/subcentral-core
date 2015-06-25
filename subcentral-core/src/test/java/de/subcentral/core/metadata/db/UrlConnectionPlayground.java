package de.subcentral.core.metadata.db;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

class UrlConnectionPlayground
{
    public static void main(String[] args) throws Exception
    {
	URL url = new URL("http://www.orlydb.com/?q=Web+Therapy+S04E03");
	URLConnection urlc = url.openConnection();
	urlc.setRequestProperty("User-Agent", "Mozilla 5.0 (Windows; U; " + "Windows NT 5.1; en-US; rv:1.8.0.11) ");
	urlc.setRequestProperty("Cookie", "userId=igbrown; sessionId=SID77689211949; isAuthenticated=true");

	System.out.println(urlc.getRequestProperties());
	System.out.println(urlc);

	InputStream is = urlc.getInputStream();
	int c;
	while ((c = is.read()) != -1)
	{
	    System.out.print((char) c);
	}
    }
}
