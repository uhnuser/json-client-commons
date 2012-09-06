package ca.uhn.json.client;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

public class Client {

	protected static final int DEFAULT_JSON_TIMEOUT = 30000;		// 30 seconds
	protected static MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	protected HttpClient	client;
	protected String		url;
	protected String		clientId;
	protected String		pass;
	protected String 		auditSourceId;

	
	protected static final Logger log = Logger.getLogger(HttpClient.class);
	
	static {
		connectionManager.closeIdleConnections(DEFAULT_JSON_TIMEOUT);
	}
		
	public Client(String url, String clientId, String pass, String auditSourceId) {
		this.url = url;
		this.clientId = clientId;
		this.pass = pass;
		this.auditSourceId = auditSourceId;

		client = new HttpClient(connectionManager);
		client.getParams().setSoTimeout(DEFAULT_JSON_TIMEOUT);

	}
	
	public Client(String url) {
		this(url, null, null, null);
	}
	
	
	public void setTimeout(int seconds) {
		client.getParams().setSoTimeout(seconds * 1000);
	}
	
	public String callService(String data) throws HttpException, IOException {
		
		PostMethod httpPost = new PostMethod(url);
		httpPost.setRequestEntity(new StringRequestEntity(data, "application/json", "UTF-8"));
		log.debug("sent request: " + data);		
		int status = 0;
		try {
			status = client.executeMethod(httpPost);

			if (200 != status) {
				throw new IOException("Request failed: " + status);
			}

			InputStream in = httpPost.getResponseBodyAsStream();
			String jsonResponse = new String(IOUtils.toByteArray(in));
			
			log.debug("got response: " + jsonResponse);
			return jsonResponse;
			
		} finally {
			if (0 == status)
				log.error("JsonClient callService error, url: " + url);
			else if (200 != status)
				log.error("JsonClient callService got http error " + status + ", url: " + url);

			httpPost.releaseConnection();
		}
	}
}
