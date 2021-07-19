package Auth_Token_generator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import LogWritter.*;
import TestStatus.Result;

public class TokenGenerator {

//	static JSONObject jo;
	static JSONObject userCreds;
	
	static String token;
	
	static String authApplicable;

	static CloseableHttpResponse closeablehttpresponse;

	public static JSONObject LoadConfig(String ipPath) throws FileNotFoundException, IOException, ParseException {

		Object obj = new JSONParser().parse(
				new FileReader(ipPath));

		JSONObject jo = null;
		jo = (JSONObject) obj;

		authApplicable = (String) jo.get("AuthApplicable");
	
		if(authApplicable.contentEquals("Yes") || authApplicable.equalsIgnoreCase("yes")) {

			return jo;
		}
		
		else //Retun null if Configuration file says Auth not applicable
			return null;
	}
	


	@SuppressWarnings("static-access")
	public static String AuthTypeSelector(JSONObject jo) throws Exception, KeyStoreException {
		JSONObject authType = (JSONObject) jo.get("auth");
		JSONObject userCreds = (JSONObject) jo.get("userCreds");
		String type = (String) authType.get("Type");
		String endpoint = (String) authType.get("Endpoint");

		switch (type) {
		//This is an ACIS specific modification
		case "JWT":
			
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustStrategy() {
				public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
					return true;
				}
			});

			SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			
			CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();

			HttpPost httppost = new HttpPost(endpoint);
			TimeUnit.SECONDS.sleep(2);
			StringEntity entity = new StringEntity(userCreds.toString());
			httppost.addHeader("content-type", "application/json");
			httppost.addHeader("Accept", "application/json");
			httppost.setEntity(entity);
			try {
			 closeablehttpresponse = httpClient.execute(httppost);
			}
			catch (Exception e) {
				// e.printStackTrace();
				
				LogWritter.LogsWritter("Unable to reach to the Auth End point!");
	
			}
			int statuscode = closeablehttpresponse.getStatusLine().getStatusCode();
			String responseString = EntityUtils.toString(closeablehttpresponse.getEntity(), "UTF-8");

			if (responseString != null && statuscode == 200  ) {   				//Check if a valid response is generated or not & if status code is 200 or not
				LogWritter.LogsWritter("Auth Api has a response");
				JSONParser jsonParser = new JSONParser();
				Object respJson = jsonParser.parse(responseString);

				JSONArray authList = (JSONArray) respJson;
				JSONObject authData = (JSONObject)authList.get(0);
				JSONObject authDataObj = (JSONObject)authData.get("data");
				
				token = (String)authDataObj.get("token");
				
				LogWritter.LogsWritter("Generated token is :: ' " +token +" '");
				closeablehttpresponse.close();
				httpClient.close();

				//return token;
//				System.out.println(token);
				//String indented = respJson.toString(4);
			}
			
			else
			{
				token = "null";
			}
			
			break;
		case "OAuth":
			token=null;
			break;
		
		}
		return token;
	}
}
