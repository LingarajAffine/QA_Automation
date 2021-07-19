package TestStatus;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;

import LogWritter.LogWritter;

public class Result {

	static LogWritter Logs = new LogWritter();
	static String apiName;
	public static int pCount = 0, fCount = 0, sCount = 0;
	public static ArrayList<String> statusPass = new ArrayList<String>();
	public static ArrayList<String> statusFail = new ArrayList<String>();
	public static ArrayList<String> statusSkipped = new ArrayList<String>();

	public static void GetTestName(String APIName) {
		apiName = APIName;
	}

	public static void Pass() throws Exception {
		try {
			Logs.LogsWritter(" '" + apiName + "' " + " -- Test Pass!");
			statusPass.add(apiName);
			pCount++;
		} catch (IOException e) {
			// e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			// Logs.LogsWritter(stackTrace);
			Logs.LogsWritter("Something went while writting Pass test cases!");
		}
	}

	public static void Fail() throws Exception {

		try {
			Logs.LogsWritter(" '" + apiName + "' " + " --Test Fail! (:");
			statusFail.add(apiName);
			fCount++;
		} catch (IOException e) {
			// e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			// Logs.LogsWritter(stackTrace);
			Logs.LogsWritter("Something went while writting Failed test cases!");
		}
	}

	public static void Skipped() throws Exception {

		// Logs.LogsWritter(" '" + apiName + "' " + " --Test Skipped! (:");
		statusSkipped.add(apiName);
		sCount++;
	}

}
