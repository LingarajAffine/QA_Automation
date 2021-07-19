
package RequestType;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.simple.JSONArray;

//import org.json.JSONArray;
import LogWritter.LogWritter;
import ResponseSaver.ResponseSaver;
import TestStatus.Result;
import Validator.ExpctedResultValidation;

public class Request {

	static LogWritter Logs;
	static JSONObject respJson;
	static CloseableHttpResponse closeablehttpresponse;
	static String respString;
	static String token;
	static String endPoint;
	static JSONArray Qparam;
	static int qpsize;
	static String key;

	// Get the URI query Parameters
	public static void getQueryParam() throws IOException {

		// get the size of array & assign the value to qpsize
		qpsize = Qparam.size();

		if (qpsize > 0) {
			endPoint = endPoint + "?";
			for (int i = 0; i < qpsize; i++) {
				// String operation for filtering out the key and valaue
				String s1 = Qparam.get(i).toString();

				String s2 = s1.replace("", "");
				s2 = s2.replace("\"", "");
				String s3 = s2.substring(1);
				s3 = s3.substring(0, s3.length() - 1);
				String[] arrStr = s3.split(":", 2);
				endPoint = endPoint + arrStr[0] + "=" ;
				
				if (arrStr[1].contains(" ")) 
				{
					
					endPoint = endPoint + URLEncoder.encode(arrStr[1],"UTF-8")  + "&";
				}
				
				else {
					endPoint = endPoint + arrStr[1] + "&";
				}
			}
			
			endPoint = endPoint +"token" + "=" + token;
			// URLEncoder.encode(endPoint, "UTF-8");
		} 
		
		else 
		{
			endPoint = endPoint + "?"+"token" + "=" + token;
		}

	}

	// Load the generated token here
	public static void getToken(String tk) {
		token = tk;
	}

	public static void POST(org.json.simple.JSONObject aPIsObject) throws Exception {

		// Get Api Name
		String APIName = (String) aPIsObject.get("APIName");
		endPoint = (String) aPIsObject.get("endPoint");

		String TestType = (String) aPIsObject.get("TestType");
		String reqType = (String) aPIsObject.get("requestType");

		Qparam = (JSONArray) aPIsObject.get("QueryParameters");

		// load the query params to the function for extraction
		getQueryParam();

		// Read the Body object part
		org.json.simple.JSONObject bodyObject = (org.json.simple.JSONObject) aPIsObject.get("Body");

		// Read the Expected result part
		org.json.simple.JSONObject expResultObject = (org.json.simple.JSONObject) aPIsObject.get("expected_result");

		Logs = new LogWritter();

		// SSL Connection
		// ------------------------------------------------------------------------------------------------------------------
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});

		SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

//        CloseableHttpClient httpclient = HttpClients.createDefault();                                                                    
//        CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();            
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();

		// -------------------------------------------------------------------------------------------------------------------

		TimeUnit.SECONDS.sleep(2);
		HttpPost httppost = new HttpPost(endPoint);
		TimeUnit.SECONDS.sleep(2);
		StringEntity entity = new StringEntity(bodyObject.toString());
		httppost.addHeader("content-type", "application/json");
		httppost.addHeader("Accept", "application/json");
		httppost.setEntity(entity); // Entity as Payload

		TimeUnit.SECONDS.sleep(2);
		try {
			closeablehttpresponse = httpClient.execute(httppost);
		} catch (Exception e) {
			// e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			// Logs.LogsWritter(stackTrace);
			Logs.LogsWritter("Unable to get a closeable HTTP response!");
			//Result.Fail();

		}
		TimeUnit.SECONDS.sleep(2);

		int statuscode = closeablehttpresponse.getStatusLine().getStatusCode();
		Logs.LogsWritter("Status Code of API: " + APIName + " is : " + statuscode);

//Actual response string
		String responseString = EntityUtils.toString(closeablehttpresponse.getEntity(), "UTF-8");

		// System.out.println(respString);

		if (responseString != null) {
			Logs.LogsWritter("Response Generated for API: " + APIName);

			if (responseString.startsWith("[")) { // if the response json starts with square brecket
				// Removing the array implementation
				String x = responseString.substring(1);
				respString = x.substring(0, x.length() - 1);
				respJson = new JSONObject(respString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

			else { // If the response starts with curly brace
				respJson = new JSONObject(responseString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

		} else {
			Logs.LogsWritter("Something went wrong, unable to fetch a valid response JSON!");
			//Result.Fail();
		}

		// System.out.println("Response Status code is :" + statuscode);
		// return respJson;

	}
//--------------------------------------------------------------- PUT Method ---------------------------------------------------------------

	public static void PUT(org.json.simple.JSONObject aPIsObject) throws Exception {
		// Get Api Name
		String APIName = (String) aPIsObject.get("APIName");
		endPoint = (String) aPIsObject.get("endPoint");
		String TestType = (String) aPIsObject.get("TestType");
		String reqType = (String) aPIsObject.get("requestType");

		Qparam = (JSONArray) aPIsObject.get("QueryParameters");

		// load the query params to the function for extraction
		getQueryParam();

		// Read the Body object part
		org.json.simple.JSONObject bodyObject = (org.json.simple.JSONObject) aPIsObject.get("Body");

		// Read the Expected result part
		org.json.simple.JSONObject expResultObject = (org.json.simple.JSONObject) aPIsObject.get("expected_result");
		Logs = new LogWritter();

		// SSL Connection
		// ------------------------------------------------------------------------------------------------------------------
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});

		SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

//	        CloseableHttpClient httpclient = HttpClients.createDefault();                                                                    
//	        CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();            
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();

		// -------------------------------------------------------------------------------------------------------------------

		HttpPut httpPut = new HttpPut(endPoint);
		TimeUnit.SECONDS.sleep(2);
		StringEntity entity = new StringEntity(bodyObject.toString());
		httpPut.addHeader("content-type", "application/json");
		httpPut.addHeader("Accept", "application/json");
		httpPut.setEntity(entity); // Entity as Payload
		CloseableHttpResponse closeablehttpresponse = httpClient.execute(httpPut);
		// Get the response
		String responseString = EntityUtils.toString(closeablehttpresponse.getEntity(), "UTF-8");

		int statuscode = closeablehttpresponse.getStatusLine().getStatusCode();
		Logs.LogsWritter("Status Code of API: " + APIName + " is : " + statuscode);
		if (responseString != null ) {
			Logs.LogsWritter("Response Generated for API: " + APIName);

			if (responseString.startsWith("[")) {
				// Removing the array implementation
				String x = responseString.substring(1);
				respString = x.substring(0, x.length() - 1);
				respJson = new JSONObject(respString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

			else {
				respJson = new JSONObject(responseString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

		} else {
			Logs.LogsWritter("Something went wrong, unable to fetch a valid response JSON!");
			//Result.Fail();
		}
	}

//------------------------------------- GET Method ------------------------------------------------------------------------

	public static void GET(org.json.simple.JSONObject aPIsObject) throws Exception {
		// Get Api Name
		String APIName = (String) aPIsObject.get("APIName");
		endPoint = (String) aPIsObject.get("endPoint");
		String TestType = (String) aPIsObject.get("TestType");
		String reqType = (String) aPIsObject.get("requestType");

		Qparam = (JSONArray) aPIsObject.get("QueryParameters");

		// load the query params to the function for extraction
		getQueryParam();

		// Read the Body object part
		org.json.simple.JSONObject bodyObject = (org.json.simple.JSONObject) aPIsObject.get("Body");

		// Read the Expected result part
		org.json.simple.JSONObject expResultObject = (org.json.simple.JSONObject) aPIsObject.get("expected_result");
		Logs = new LogWritter();

		// SSL Connection
		// ----------------------------------------------------------------------
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustStrategy() {
			public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				return true;
			}
		});

		SSLConnectionSocketFactory sslSF = new SSLConnectionSocketFactory(builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

//        CloseableHttpClient httpclient = HttpClients.createDefault();                                                                    
//        CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();            
		CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslSF).build();

		// -------------------------------------------------------------------------------------------------------------------

		HttpGet httpget = new HttpGet(endPoint);
		TimeUnit.SECONDS.sleep(2);
		StringEntity entity = new StringEntity(bodyObject.toString());
		httpget.addHeader("content-type", "application/json");
		httpget.addHeader("Accept", "application/json");
		CloseableHttpResponse closeablehttpresponse = httpClient.execute(httpget);
		// Get the response
		String responseString = EntityUtils.toString(closeablehttpresponse.getEntity(), "UTF-8");

		int statuscode = closeablehttpresponse.getStatusLine().getStatusCode();
		Logs.LogsWritter("Status Code of API: " + APIName + " is : " + statuscode);
		if (responseString != null) {
			Logs.LogsWritter("Response Generated for API: " + APIName);

			if (responseString.startsWith("[")) {
				// Removing the array implementation
				String x = responseString.substring(1);
				respString = x.substring(0, x.length() - 1);
				respJson = new JSONObject(respString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

			else {
				respJson = new JSONObject(responseString);

				String indented = respJson.toString(4);
				Header[] headerArray = closeablehttpresponse.getAllHeaders();

				// hashmap obj
				HashMap allheader = new HashMap<String, String>();

				// to store the header in hashmap
				for (Header header : headerArray) {
					allheader.put("\r\n" + header.getName(), header.getValue());
				}

				ResponseSaver.responseWritter(indented, allheader, APIName);

				ExpctedResultValidation.Test(respJson, expResultObject,TestType);

			}

		} else {
			Logs.LogsWritter("Something went wrong, unable to fetch a valid response JSON!");
			//Result.Fail();
		}
	}

	public void DELETE() {

	}

}
