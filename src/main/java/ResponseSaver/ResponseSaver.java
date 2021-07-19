package ResponseSaver;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import LogWritter.LogWritter;

public class ResponseSaver {

	static DateTimeFormatter dts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss.SSSSSSSSS");
	static LocalDateTime now = LocalDateTime.now();
	static String TimeStamp = dts.format(now);
	
	static LogWritter Logs;

	static String ApiName;
	static String location;

	static FileWriter writer;
	public static void FilePath(String path) {
		location =path;
	}

	public static void responseWritter(String resp, HashMap header, String Api) throws IOException {

		Logs = new LogWritter();

		ApiName = Api;

		CreateFile();

		respWritter(resp);

		headerWritter(header);

		RespClosure();
	}

	public static void CreateFile() throws IOException {
		try {

			String file_Name = location +"Response_" +ApiName + ".txt";
			File file = new File(file_Name);

			if (file.createNewFile()) {
				// System.out.println("File is created!");

			} else {
				Logs.LogsWritter("File ALready exists!! check the API names for repeatation");
			}

			writer = new FileWriter(file);

		} catch (IOException e) {
			//e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			//Logs.LogsWritter(stackTrace);
			Logs.LogsWritter("Something went wrong while creating or saving the Response file!");
			
		}
	}

	public static void respWritter(String data) throws IOException {

		writer.write("---------------------------------\r\n" + "Response_" + ApiName
				+ "\r\n---------------------------------\r\n" + "\r\n");

		writer.write("---------------------------------\r\n" + LocalDateTime.now()
				+ "\r\n---------------------------------\r\n" + data + "\r\n");
	}

	public static void headerWritter(HashMap data) throws IOException {

		writer.write("---------------------------------\r\n" + "Headers for the API: " + ApiName
				+ "\r\n---------------------------------\r\n" + "\r\n");

		writer.write("---------------------------------\r\n" + LocalDateTime.now()
				+ "\r\n---------------------------------\r\n" + data + "\r\n");
	}

	public static void RespClosure() throws IOException {

		writer.close();
	}
}
