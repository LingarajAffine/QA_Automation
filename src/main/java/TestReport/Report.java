package TestReport;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import LogWritter.LogWritter;
import ReadTestCase.*;
import TestStatus.*;

public class Report {

	static DateTimeFormatter dts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss");
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss.SSSSSSSSS");
	static LocalDateTime now = LocalDateTime.now();
	static String TimeStamp = dts.format(now);
	static LogWritter Logs;
	static int tCount;
	static int eCount;
	static int sCount;
	static int pCount;
	static int fCount;
	
	public static void counts(){
		
		tCount=TestCaseReader.Total;
		eCount=TestCaseReader.Executed;
		sCount=TestCaseReader.Skipped;
		pCount=Result.pCount;
		fCount=Result.fCount;
		sCount=Result.sCount;
		
	}
	
	public static void status() throws IOException{
		ArrayList<String> statusP= Result.statusPass;
		ArrayList<String> statusf= Result.statusFail;
		ArrayList<String> statusS= Result.statusSkipped;

		try {
			reportWritter(statusP,statusf,statusS);
		} catch (IOException e) {
			//e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			LogWritter.LogsWritter("Unable to fetch the Pass, Fail & skipped test counts!!");
			//Logs.LogsWritter(stackTrace);
		}
		
		try {
			ReportClosure(); // Closing the log writter file
		} catch (IOException e) {
			//e.printStackTrace();
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			pw.flush();
			String stackTrace = sw.toString();
			LogWritter.LogsWritter(stackTrace);
		}
	}
	
	static FileWriter writer;
	static FileWriter writer1;

	public static void CreateFile(String path)throws IOException {
		try {

			File file = new File(path + TimeStamp + " -Report.txt");
			
			if (file.createNewFile()) {
				// System.out.println("File is created!");

			} else {
				LogWritter.LogsWritter("File already exists.");
			}

			writer = new FileWriter(file);

		} catch (IOException e) {
			//e.printStackTrace();
			LogWritter.LogsWritter("Issue Occured while creating the Report File!");
		}
		//To write the failed test cases
		try {
			File Ffile = new File(path + TimeStamp + " -FailTest_Report.txt");
		if (Ffile.createNewFile()) {
			// System.out.println("File is created!");

		} else {
			LogWritter.LogsWritter("File already exists.");
		}

		writer1 = new FileWriter(Ffile);

	} catch (IOException e) {
		//e.printStackTrace();
		LogWritter.LogsWritter("Issue Occured while creating Failed the Report File!");
	}
		
	}
	
	public static void reportWritter(ArrayList<String> pdata, ArrayList<String> fdata, ArrayList<String> sdata) throws IOException{
		writer.write(" \r\n"+"Total number of test cases is: " + tCount+" \r\n");
		writer.write("Total number of Executed test cases is: " + eCount+" \r\n");
		writer.write("Total number of Skipped test cases is: " + sCount+" \r\n");
		writer.write("Total number of Passed test cases is: " + pCount+" \r\n");
		writer.write("Total number of Failed test cases is: " + fCount+" \r\n");

		writer.write("-----------------------------------------------");
		
		writer.write(" \r\n"+"Passed Test Cases are: " +" \r\n");
		for (int i=0;i<pdata.size();i++){
			writer.write(" \t"+pdata.get(i) +  "\r\n");
		}
		
		writer.write("-----------------------------------------------");

		writer.write(" \r\n"+"Failed Test Cases are: " +" \r\n");
		for (int i=0;i<fdata.size();i++){
			writer.write(" \t"+fdata.get(i)+  "\r\n");
			writer1.write(" \t"+fdata.get(i)+  "\r\n");
		}
		
		writer.write("-----------------------------------------------");

		writer.write(" \r\n"+"Skipped Test Cases are: " +" \r\n");
		for (int i=0;i<sdata.size();i++){
			writer.write(" \t"+sdata.get(i)+  "\r\n");
		}

	}
	
	public static void ReportClosure() throws IOException {
		writer.close();
		writer1.close();
	}
}
