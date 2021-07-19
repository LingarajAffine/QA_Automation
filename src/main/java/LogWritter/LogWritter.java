package LogWritter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogWritter {

	static DateTimeFormatter dts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss.SSSSSSSSS");
	static LocalDateTime now = LocalDateTime.now();
	static String TimeStamp = dts.format(now);
	
	static FileWriter writer;

	public void CreateFile(String path) {
		try {

			File file = new File(path + TimeStamp + " -TestRunLogs.txt");

			if (file.createNewFile()) {
				// System.out.println("File is created!");

			} else {
				System.out.println("File already exists.");
			}

			writer = new FileWriter(file);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void LogsWritter(String logData) throws IOException {

		if (logData.equals("********************************")) {
			writer.write(" \r\n" + logData + "\r\n");
		}

		else {
			writer.write(LocalDateTime.now() + " \r\n" + logData + "\r\n");
		}
	}

	public void logClosure() throws IOException {
		writer.close();
	}
}
