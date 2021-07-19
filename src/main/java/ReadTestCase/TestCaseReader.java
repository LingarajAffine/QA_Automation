package ReadTestCase;

import java.io.File;
import java.io.FileReader;
import ResponseSaver.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import LogWritter.*;
import TestReport.*;
import TestStatus.*;
import RequestType.*;
import TestReport.*;
import Auth_Token_generator.*;

public class TestCaseReader {

	static DateTimeFormatter dts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
	static LocalDateTime now = LocalDateTime.now();
	static String TimeStamp = dts.format(now);
	public static int Total, Executed, Skipped;

	static LogWritter Logs;
	static String AuthApplicable;
	static String token;
	static JSONObject jo;
	static String endPoint;
	static String ipPath = "E:\\ACIS API Test\\Input files\\";
	static String path = "E:\\ACIS API Test\\Results\\";

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {

		// create object of logs
		Logs = new LogWritter();
		String Output_dir = null;
	

		// create the directory with timestamp
		File theDir = new File(path + TimeStamp);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
			// System.out.println("creating directory: " + theDir.getName());
			Output_dir = path + theDir.getName() + "\\";
			// System.out.println(Output_dir);
			boolean result = false;

			try {
				theDir.mkdir();
				result = true;
			} catch (SecurityException se) {
				LogWritter.LogsWritter("Something went wrong while creating the new directory for Test Run!");
				// handle it
			}
			if (result) {
				// System.out.println("DIR created");
			}
		}

		// create the files
		try {
			Logs.CreateFile(Output_dir); // Logs fIle (This is dummy)
		} catch (Exception e) {
			LogWritter.LogsWritter("Something went wrong with log file creation!");
		}

		// Report file creation
		try {
			Report.CreateFile(Output_dir); // Report file
		} catch (Exception e) {
			LogWritter.LogsWritter("Something went wrong with Report file creation!");
		}

		// Response savings file creation
		try {
			ResponseSaver.FilePath(Output_dir); // Response file
		} catch (Exception e) {
			LogWritter.LogsWritter("Something went wrong with Response saver file creation!");
		}
		LogWritter.LogsWritter("----------------Logs---------------\r\n");

		Total = 0;
		Executed = 0;
		Skipped = 0;

		try {
			configFileLoader();
		} catch (Exception e) {

			LogWritter.LogsWritter("Something went wrong with Token generation!");
		}
		// JSON parser object to parse read file
		JSONParser jsonParser = new JSONParser();

		try (FileReader reader = new FileReader(ipPath+"ACIS_API_TestCases_Module.json")) {
			// Read JSON file
			Object obj = jsonParser.parse(reader);

			JSONArray APIList = (JSONArray) obj;
			// System.out.println(APIList);

			int i = 0;
			Iterator It = APIList.iterator();
			do {
				JSONObject Api = (JSONObject) APIList.get(i);
				parseAPIsObject((JSONObject) Api);
				i++;
			} while (It.hasNext());

		} catch (IndexOutOfBoundsException e1) {
			// e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e1.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			LogWritter.LogsWritter("IndexOutOfBoundsException");
		} catch (Exception e) {
			// e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			LogWritter.LogsWritter(stackTrace);
		}

		LogWritter.LogsWritter("No. of total Test Cases: " + Total);
		LogWritter.LogsWritter("No. of Executed Test Cases: " + Executed);
		LogWritter.LogsWritter("No. of skipped Test Cases: " + Skipped);

		Report.counts();

		Report.status();

		Logs.logClosure();
	}
// The End

	// Parcing here
	private static void parseAPIsObject(JSONObject APIs) throws Exception {
		Total++;

		// Get Apis object within list
		JSONObject APIsObject = (JSONObject) APIs.get("Apis");

		// Get Api Name
		String APIName = (String) APIsObject.get("APIName");
		
		String Test_Id = (String) APIsObject.get("Test_id");
		
		Result.GetTestName(Test_Id +" - "+ APIName); // Api Name sent to report
		if (APIName != null) {

			// Get skip value
			String skip = (String) APIsObject.get("skip");

			if (skip.equalsIgnoreCase("No")) { // Api will be executed

				Executed++; // Gets the count of Executes

				LogWritter.LogsWritter("********************************");

				LogWritter.LogsWritter("API :" + " '" + APIName + "' " + "  is executed");

				String TestType = (String) APIsObject.get("TestType");

				LogWritter.LogsWritter("Test case type is: " + TestType);

				// Get the end point
				endPoint = (String) APIsObject.get("endPoint");

				// endpoint is added to the logs

				LogWritter.LogsWritter("The End point is :" + " '" + endPoint + "' ");

				String reqType = (String) APIsObject.get("requestType");

				LogWritter.LogsWritter("Request Type is :" + " '" + reqType + "' ");

				JSONArray QueryParameters = (JSONArray) APIsObject.get("QueryParameters");

				// int k = QueryParameters.size();
				LogWritter.LogsWritter("The Query parameters are : " + QueryParameters);

				// Read the Body object part
				JSONObject bodyObject = (JSONObject) APIsObject.get("Body");

				// String body = bodyObject.toString(body, 4);
				LogWritter.LogsWritter("The body is : " + bodyObject);

				// Read the Expected result part
				JSONObject expResultObject = (JSONObject) APIsObject.get("expected_result");

				switch (reqType) {

				case "POST":
					try {
						Request.POST(APIsObject);
					} catch (Exception e) {

						// e.printStackTrace();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						pw.flush();
						String stackTrace = sw.toString();
						Logs.LogsWritter(stackTrace);
						// LogWritter.LogsWritter("Response is generated, Please check the expected
						// result inputs.");
						try {
							Result.Fail();
						} catch (Exception e1) {
							// e1.printStackTrace();
							StringWriter sw1 = new StringWriter();
							PrintWriter pw1 = new PrintWriter(sw);
							e1.printStackTrace(pw);
							pw.flush();
							String stackTrace1 = sw.toString();
							// Logs.LogsWritter(stackTrace);
							LogWritter.LogsWritter("Unable to write the Fail Message!!");
						}
					}
					break;
				case "GET":
					try {
						Request.GET(APIsObject);
					} catch (Exception e) {

						// e.printStackTrace();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						pw.flush();
						String stackTrace = sw.toString();
						Logs.LogsWritter(stackTrace);
						// LogWritter.LogsWritter("Response is generated, Please check the expected
						// result inputs.");
						try {
							Result.Fail();
						} catch (Exception e1) {
							// e1.printStackTrace();
							StringWriter sw1 = new StringWriter();
							PrintWriter pw1 = new PrintWriter(sw);
							e1.printStackTrace(pw);
							pw.flush();
							String stackTrace1 = sw.toString();
							// Logs.LogsWritter(stackTrace);
							LogWritter.LogsWritter("Unable to write the Fail Message!!");
						}
					}
					break;

				case "PUT":

					try {
						Request.PUT(APIsObject);
					} catch (Exception e) {

						// e.printStackTrace();
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						pw.flush();
						String stackTrace = sw.toString();
						Logs.LogsWritter(stackTrace);
						// LogWritter.LogsWritter("Response is generated, Please check the expected
						// result inputs.");
						try {
							Result.Fail();
						} catch (Exception e1) {
							// e1.printStackTrace();
							StringWriter sw1 = new StringWriter();
							PrintWriter pw1 = new PrintWriter(sw);
							e1.printStackTrace(pw);
							pw.flush();
							String stackTrace1 = sw.toString();
							// Logs.LogsWritter(stackTrace);
							LogWritter.LogsWritter("Unable to write the Fail Message!!");
						}
					}
					break;

				case "DELETE":
					break;
				}

				LogWritter.LogsWritter("********************************");

				// may be objects needs to be cleared if same memory is not
				// released

			} else {

				Skipped++;
				Result.Skipped(); // If Api Skipped write to Result >> Report
				LogWritter.LogsWritter("********************************");
				// call the log writter file for skipped test cases
				LogWritter.LogsWritter(APIName + ": API is Skipped for Test");
				LogWritter.LogsWritter("********************************");
			}
		}

	}

	public static void configFileLoader() throws Exception {

		// -------------------------------------------------------------(2)--------------------------------------

		// Load the configuration file for auth model to Auth token generator
		try {
			// SkipConfig = TokenGenerator.LoadConfig();
			jo = TokenGenerator.LoadConfig(ipPath);

			if (jo != null) {
				AuthApplicable = (String) jo.get("AuthApplicable");
			}

			else {
				AuthApplicable = "null";
			}
		} catch (Exception e) {
			LogWritter.LogsWritter("Something went wrong with loading & fetching data from Configuration file.");
			LogWritter.LogsWritter("This might result in Test fail!");

		}
		// check if auth model is applicable or not?

		if (AuthApplicable.contentEquals("Yes") || AuthApplicable.equalsIgnoreCase("yes")) {

			// Load config objects(Send the object to tokenGenerator for getting the token)
			try {
				token = TokenGenerator.AuthTypeSelector(jo);
			} catch (Exception e) {
				LogWritter.LogsWritter("Something went wrong with token generation!");
				Logs.logClosure();
			}
			if (token.equalsIgnoreCase("null")) {
				LogWritter.LogsWritter("Unable to generate Auth. token!");
				LogWritter.LogsWritter("Test Aborted!!!!");
				Report.ReportClosure();
				ResponseSaver.RespClosure();
				Logs.logClosure();
				System.exit(0);
			}

			else {
				Request.getToken(token);
			}
		}

	}

}
