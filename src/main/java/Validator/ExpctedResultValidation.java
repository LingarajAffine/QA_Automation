package Validator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import org.json.JSONObject;

import LogWritter.LogWritter;
import TestStatus.Result;
import Utilities.TestUtil;

public class ExpctedResultValidation {

	static LogWritter Logs;
	static String ExpResValue;
	static String ActualResult;

	public static void Test(JSONObject jsonobj, org.json.simple.JSONObject expResultObject, String TestType)
			throws Exception {

		Logs = new LogWritter();
		String errorMsg, errorStatus;
		int passCount = 0; // for Every key & value that matches it will be used to compare with the total
		int failCount = 0; // no. of validation key & values

//		// Sonic specific validation code , may not be applicable for other application-// 
//		errorStatus = TestUtil.getValueByJpath(jsonobj, "status");						//	
//																						//
//		if (errorStatus.equals("error")) {												//
//			errorMsg = TestUtil.getValueByJpath(jsonobj, "data/errorMessage");			//
//			Logs.LogsWritter("Error Message : " + " '" + errorMsg + "' ");				//
//																						//		
//			Result.Fail();																//
//			Logs.LogsWritter("Validation is done!");									//
//			return;																		//					
//		}																				//	 
//		//------------------------------------------------------------------------------//

//		else {		

		Set<?> keys = expResultObject.keySet();

		ArrayList<String> keysSet = new ArrayList<String>();

		Iterator<?> itr = keys.iterator();
		do {
			try {
				String key = itr.next().toString();

				keysSet.add(key);
			} catch (Exception e) {
				LogWritter.LogsWritter("No Expected Result validatons!!");
			}

		} while (itr.hasNext());

		if (keysSet.size() > 0) {

			for (int i = 0; i < keysSet.size(); i++) {

				ExpResValue = (String) expResultObject.get(keysSet.get(i));
				try {
					ActualResult = TestUtil.getValueByJpath(jsonobj, keysSet.get(i));
				} catch (Exception e) {
					LogWritter
							.LogsWritter("Unable to fetch the value for provided expected result for '" + keysSet.get(i)
									+ "'  , Please check with the expected result parameter in the input file.");
					failCount++;
					//Result.Fail();
				}

				if (ExpResValue.equalsIgnoreCase(ActualResult)) {
					passCount++;
				}

				// Null value check
//				else if (ExpResValue.equalsIgnoreCase(null) && ActualResult.equalsIgnoreCase(null)) {
//					passCount++;
//				}

				// Soft validation with non-null value
				else if (ExpResValue.equalsIgnoreCase("soft_validation") && !(ActualResult.equals(""))) {
					passCount++;
				}

				// Soft validation with type & null value
				else if (ExpResValue.contains("soft_validation/")) {

					// Add code
					String svType = ExpResValue;
					String[] DataType;
					DataType = svType.split("/"); // Split & get the data type after "soft_validation"

					switch (DataType[1]) {

					case "Number": // when the value in the respponse received as a number
						// Check the value is a Number or not
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.Integer")) {
							passCount++;
							break;
						}
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.Double")) {
							passCount++;
							break;
						}
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.Long")) {
							passCount++;
							break;
						}
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.Float")) {
							passCount++;
							break;
						}

						// Since The number is type "string" in response
						// ---------------------------------------------
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.String")) {

							int i1 = 0, i2 = 0;
							try {
								try {
									Double Val = Double.parseDouble(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Integer Val = Integer.parseInt(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Float Val = Float.parseFloat(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Double Val = Double.parseDouble(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}

							} catch (Exception e) {
								if (i1 == 4) { // if the number is unable to be converted any of above data type test
												// fails
									LogWritter.LogsWritter("The value for : " + keysSet.get(i)
											+ " was expected to be in number format but probably is not,"
											+ "The actual Value is: '" + ActualResult + " '");
									failCount++;
								}
							}
							if (i2 > 0 && i2 < 5) { // If number is able to converted to any of above data type test
													// pass
								passCount++;
								break;
							}
						}

						else {
							LogWritter.LogsWritter(
									"The value for : " + keysSet.get(i) + " was expected to be in number format but,"
											+ "The actual Value is: '" + ActualResult + " '");
							failCount++;
							break;
						}

					case "NumberText": // when response is Number as a Text
						// Action
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.String")) {
							int i1 = 0, i2 = 0;
							try {
								try {
									Double Val = Double.parseDouble(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Integer Val = Integer.parseInt(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Float Val = Float.parseFloat(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}
								try {
									Double Val = Double.parseDouble(ActualResult);
									i2++;
								} catch (NumberFormatException e) {
									i1++;
								}

							} catch (Exception e) {
								if (i1 == 4) { // if the number is unable to be converted any of above data type test
												// fails
									LogWritter.LogsWritter("The value for : " + keysSet.get(i)
											+ " was expected to be in number format but probably is not,"
											+ "The actual Value is: '" + ActualResult + " '");
									failCount++;
								}
							}
							if (i2 > 0 && i2 < 5) { // If number is able to converted to any of above data type test
													// pass
								passCount++;
								break;
							}

						} else {
							LogWritter.LogsWritter("The value for : " + keysSet.get(i)
									+ " was expected to be in number as String format but," + "The actual Value is: '"
									+ ActualResult + " '");
							failCount++;
							break;
						}

					case "Text": // Will test data in String format, will fail if empty or null value receiver
						// Action
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.String")
								&& !(ActualResult.equals("")) && !(ActualResult.equals("null"))) {
							passCount++;
							break;
						} else {
							LogWritter.LogsWritter(
									"The value for : " + keysSet.get(i) + " was expected to be in String format but,"
											+ "The actual Value is: '" + ActualResult + " '");
							failCount++;
							break;
						}

					case "Boolean":
						// Check if the Value matches to "true" or "false"
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.String")) {
							if (ActualResult.equalsIgnoreCase("true") || ActualResult.equalsIgnoreCase("false")) {
								passCount++;
								break;
							} else {
								LogWritter.LogsWritter("The value for : " + keysSet.get(i)
										+ " was expected to be in Boolean i.e 'True/False' but,"
										+ "The actual Value is: '" + ActualResult + " '");
								failCount++;
								break;
							}

						} else {
							failCount++;
							break;
						}

					case "BooleanText": // when the value in the response is a expected boolean & received as a text
						// Check if the Value matches to "true" or "false"
						if (((Object) ActualResult).getClass().getTypeName().contentEquals("java.lang.String")) {
							if (ActualResult.equalsIgnoreCase("true") || ActualResult.equalsIgnoreCase("false")) {
								passCount++;
								break;
							} else {
								LogWritter.LogsWritter("The value for : " + keysSet.get(i)
										+ " was expected to be in Boolean i.e 'True/False' but,"
										+ "The actual Value is: '" + ActualResult + " '");
								failCount++;
								break;
							}

						} else {
							failCount++;
							break;
						}

					}

				}

				else {
					failCount++;
					LogWritter.LogsWritter("Expected Result for the Key :" + "' " + (keysSet.get(i)) + " '" + " is :"
							+ "' " + ExpResValue + " '" + ",But the Actual Result is :" + "' " + ActualResult + " '");
					LogWritter.LogsWritter("Validation is done!");
					break;
				}
			}

			// Only if the pass count of key set matches && all values are as expected only
			// then test cases passes
//			if (passCount == keysSet.size()) {
//				Result.Pass();
//				Logs.LogsWritter("Validation is done!");
//			}
//			else{
//				Result.Fail();
//				Logs.LogsWritter("Validation is done!");
//			}

		}

		if (keysSet.size() <= 0 && TestType.equalsIgnoreCase("Positive")) {
			LogWritter.LogsWritter(
					"Since no validation points is added & has a JSON response with status code 200, Test Pass!");
			Result.Pass();
			return;
		}
		if (passCount == keysSet.size() && TestType.equalsIgnoreCase("Positive")) {
			Result.Pass();
			LogWritter.LogsWritter("Validation is done!");
			return;
		}
		if (failCount > 0 && TestType.equalsIgnoreCase("Negative")) {
			Result.Pass();
			LogWritter.LogsWritter("Validation is done! For the negative test cases data validation is failing, Hence Test Pass!");
			return;
		}
		if (passCount == keysSet.size() && TestType.equalsIgnoreCase("Negative")) {
			Result.Fail();
			LogWritter.LogsWritter("For the Negative Test case the Test shouldn't give a valid response!");
			return;
		} else {
			Result.Fail();
			LogWritter.LogsWritter("Validation is done!");
			return;
		}
	}
}
