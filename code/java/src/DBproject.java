/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */

public class DBproject{
	//reference to physical database connection
	private Connection _connection = null;
	static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	
	public DBproject(String dbname, String dbport, String user, String passwd) throws SQLException {
		System.out.print("Connecting to database...");
		try{
			// constructs the connection URL
			String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
			System.out.println ("Connection URL: " + url + "\n");
			
			// obtain a physical connection
	        this._connection = DriverManager.getConnection(url, user, passwd);
	        System.out.println("Done");
		}catch(Exception e){
			System.err.println("Error - Unable to Connect to Database: " + e.getMessage());
	        System.out.println("Make sure you started postgres on this machine");
	        System.exit(-1);
		}
	}
	
	/**
	 * Method to execute an update SQL statement.  Update SQL instructions
	 * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
	 * 
	 * @param sql the input SQL string
	 * @throws java.sql.SQLException when update failed
	 * */
	public void executeUpdate (String sql) throws SQLException { 
		// creates a statement object
		Statement stmt = this._connection.createStatement ();

		// issues the update instruction
		stmt.executeUpdate (sql);

		// close the instruction
	    stmt.close ();
	}//end executeUpdate

	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and outputs the results to
	 * standard out.
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQueryAndPrintResult (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		/*
		 *  obtains the metadata object for the returned result set.  The metadata
		 *  contains row and column info.
		 */
		ResultSetMetaData rsmd = rs.getMetaData ();
		int numCol = rsmd.getColumnCount ();
		int rowCount = 0;
		
		//iterates through the result set and output them to standard out.
		boolean outputHeader = true;
		while (rs.next()){
			if(outputHeader){
				for(int i = 1; i <= numCol; i++){
					System.out.print(rsmd.getColumnName(i) + "\t");
			    }
			    System.out.println();
			    outputHeader = false;
			}
			for (int i=1; i<=numCol; ++i)
				System.out.print (rs.getString (i) + "\t");
			System.out.println ();
			++rowCount;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the results as
	 * a list of records. Each record in turn is a list of attribute values
	 * 
	 * @param query the input query string
	 * @return the query result as a list of records
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException { 
		//creates a statement object 
		Statement stmt = this._connection.createStatement (); 
		
		//issues the query instruction 
		ResultSet rs = stmt.executeQuery (query); 
	 
		/*
		 * obtains the metadata object for the returned result set.  The metadata 
		 * contains row and column info. 
		*/ 
		ResultSetMetaData rsmd = rs.getMetaData (); 
		int numCol = rsmd.getColumnCount (); 
		int rowCount = 0; 
	 
		//iterates through the result set and saves the data returned by the query. 
		boolean outputHeader = false;
		List<List<String>> result  = new ArrayList<List<String>>(); 
		while (rs.next()){
			List<String> record = new ArrayList<String>(); 
			for (int i=1; i<=numCol; ++i) 
				record.add(rs.getString (i)); 
			result.add(record); 
		}//end while 
		stmt.close (); 
		return result; 
	}//end executeQueryAndReturnResult
	
	/**
	 * Method to execute an input query SQL instruction (i.e. SELECT).  This
	 * method issues the query to the DBMS and returns the number of results
	 * 
	 * @param query the input query string
	 * @return the number of rows returned
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	public int executeQuery (String query) throws SQLException {
		//creates a statement object
		Statement stmt = this._connection.createStatement ();

		//issues the query instruction
		ResultSet rs = stmt.executeQuery (query);

		int rowCount = 0;

		//iterates through the result set and count nuber of results.
		while(rs.next()){
			rowCount++;
		}//end while
		stmt.close ();
		return rowCount;
	}
	
	/**
	 * Method to fetch the last value from sequence. This
	 * method issues the query to the DBMS and returns the current 
	 * value of sequence used for autogenerated keys
	 * 
	 * @param sequence name of the DB sequence
	 * @return current value of a sequence
	 * @throws java.sql.SQLException when failed to execute the query
	 */
	
	public int getCurrSeqVal(String sequence) throws SQLException {
		Statement stmt = this._connection.createStatement ();
		
		ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
		if (rs.next()) return rs.getInt(1);
		return -1;
	}

	/**
	 * Method to close the physical connection if it is open.
	 */
	public void cleanup(){
		try{
			if (this._connection != null){
				this._connection.close ();
			}//end if
		}catch (SQLException e){
	         // ignored.
		}//end try
	}//end cleanup

	/**
	 * The main execution method
	 * 
	 * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
	 */
	public static void main (String[] args) {
		if (args.length != 3) {
			System.err.println (
				"Usage: " + "java [-classpath <classpath>] " + DBproject.class.getName () +
		            " <dbname> <port> <user>");
			return;
		}//end if
		
		DBproject esql = null;
		
		try{
			System.out.println("(1)");
			
			try {
				Class.forName("org.postgresql.Driver");
			}catch(Exception e){

				System.out.println("Where is your PostgreSQL JDBC Driver? " + "Include in your library path!");
				e.printStackTrace();
				return;
			}
			
			System.out.println("(2)");
			String dbname = args[0];
			String dbport = args[1];
			String user = args[2];
			
			esql = new DBproject (dbname, dbport, user, "");
			
			boolean keepon = true;
			while(keepon){
				System.out.println("MAIN MENU");
				System.out.println("---------");
				System.out.println("1. Add Doctor");
				System.out.println("2. Add Patient");
				System.out.println("3. Add Appointment");
				System.out.println("4. Make an Appointment");
				System.out.println("5. List appointments of a given doctor");
				System.out.println("6. List all available appointments of a given department");
				System.out.println("7. List total number of different types of appointments per doctor in descending order");
				System.out.println("8. Find total number of patients per doctor with a given status");
				System.out.println("9. < EXIT");
				
				switch (readChoice()){
					case 1: AddDoctor(esql); break;
					case 2: AddPatient(esql); break;
					case 3: AddAppointment(esql); break;
					case 4: MakeAppointment(esql); break;
					case 5: ListAppointmentsOfDoctor(esql); break;
					case 6: ListAvailableAppointmentsOfDepartment(esql); break;
					case 7: ListStatusNumberOfAppointmentsPerDoctor(esql); break;
					case 8: FindPatientsCountWithStatus(esql); break;
					case 9: keepon = false; break;
				}
			}
		}catch(Exception e){
			System.err.println (e.getMessage ());
		}finally{
			try{
				if(esql != null) {
					System.out.print("Disconnecting from database...");
					esql.cleanup ();
					System.out.println("Done\n\nBye !");
				}//end if				
			}catch(Exception e){
				// ignored.
			}
		}
	}

	public static int readChoice() {
		int input;
		// returns only if a correct value is given.
		do {
			System.out.print("Please make your choice: ");
			try { // read the integer, parse it and break.
				input = Integer.parseInt(in.readLine());
				break;
			}catch (Exception e) {
				System.out.println("Your input is invalid!");
				continue;
			}//end try
		}while (true);
		return input;
	}//end readChoice

	public static void AddDoctor(DBproject esql) {//1
		try{
			int doctor_id = esql.executeQuery("SELECT * FROM Doctor;");
                        String query = "INSERT INTO Doctor VALUES (";
                        System.out.print("\tEnter Doctor's name: ");
                        String name = in.readLine();
                        System.out.print("\tEnter Doctor's specialty: ");
                        String specialty = in.readLine();
                        System.out.print("\tEnter Doctor's department ID: ");
                        String did = in.readLine();
			System.out.print("\n");
                        query += doctor_id + ", \'" + name + "\', \'" + specialty + "\', " + did + ");";
                        esql.executeUpdate(query);
			System.out.print("Succesfully added Doctor!\n");
                }
                catch(Exception e) {
			System.out.print("Unable to add doctor. Check if the Department ID is valid.\n");
			System.err.println (e.getMessage());
                }
		System.out.print("\n");
	}

	public static void AddPatient(DBproject esql) {//2
		try {	
			int patient_ID = esql.executeQuery("SELECT * FROM Patient;");
                        String query = "INSERT INTO Patient VALUES (";
                        System.out.print("\tEnter Patient's name: ");
                        String name = in.readLine();
                        System.out.print("\tEnter Patient's gender (M/F): ");
                        String gender = in.readLine();
			while(!(gender.equals("M") || gender.equals("F"))) {
				System.out.print("\t\tPlease enter a gender value of 'M' or 'F': ");
                                gender = in.readLine();
			}
			System.out.print("\tEnter Patient's age: ");
                        String age = in.readLine();
			int x;
			while(true) {
				try {
   					x = Integer.parseInt(age);
					if(x < 0) {
						System.out.print("\t\tPlease enter a positive integer value for age: ");
						age = in.readLine();
					}
					else { break; }
				}
				catch(NumberFormatException e2) {
					System.out.print("\t\tPlease enter a positive integer value for age: ");
                                        age = in.readLine();
				}
			}
			System.out.print("\tEnter Patient's address: ");
                        String address = in.readLine();
                        System.out.print("\n");
                        query += patient_ID + ", \'" + name + "\', \'" + gender + "\', " + age + ", \'" + address + "\', 0);";
                        esql.executeUpdate(query);
                        System.out.print("Succesfully added Patient!\n");
                }
                catch(Exception e) {
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}

	public static void AddAppointment(DBproject esql) {//3
		try{
			int appnt_ID = esql.executeQuery("SELECT * FROM Appointment;");
                        String query = "INSERT INTO Appointment VALUES (";
                        System.out.print("\tEnter the date of the appointment (MM/DD/YYYY or YYYY-MM-DD): ");
                        String adate = in.readLine();
                        System.out.print("\tEnter the time slot of the appointment in military time (HH:MM-HH:MM): ");
                        String time_slot = in.readLine();
			int h1;
			int m1;
			int h2;
			int m2;
			while(true) {
				try {
                                        if(time_slot.substring(2,3).equals(":")) {
						h1 = Integer.parseInt(time_slot.substring(0,2));
						m1 = Integer.parseInt(time_slot.substring(3,5));
						if(time_slot.substring(8,9).equals(":")) {
							h2 = Integer.parseInt(time_slot.substring(6,8));
                                                	m2 = Integer.parseInt(time_slot.substring(9,11));
						}
						else {
							h2 = Integer.parseInt(time_slot.substring(6,7));
                                                	m2 = Integer.parseInt(time_slot.substring(8,10));
						}
					}
					else {
						h1 = Integer.parseInt(time_slot.substring(0,1));
						m1 = Integer.parseInt(time_slot.substring(2,4));
						if(time_slot.substring(7,8).equals(":")) {
                                                        h2 = Integer.parseInt(time_slot.substring(5,7));
                                                        m2 = Integer.parseInt(time_slot.substring(8,10));
                                                }
                                                else {
                                                        h2 = Integer.parseInt(time_slot.substring(5,6));
                                                        m2 = Integer.parseInt(time_slot.substring(7,9));
                                                }
					}
                                        if(h1 >= 0 && h1 < 25 && h2 >= 0 && h2 < 25 && m1 >= 0 && m1 < 60 && m2 >= 0 && m2 < 60) {
						break;
					}
					else {
						System.out.print("\t\tPlease enter a valid time slot (HH:MM-HH:MM): ");
                                        	time_slot = in.readLine();
					}
                                }
                                catch(NumberFormatException e2) {
                                        System.out.print("\t\tPlease enter a valid time slot (HH:MM-HH:MM): ");
                                        time_slot = in.readLine();
                                }
			}
                        System.out.print("\n");
                        query += appnt_ID + ", DATE \'" + adate + "\', \'" + time_slot + "\', \'AV\');";
                        esql.executeUpdate(query);
                        System.out.print("Succesfully added new available Appointment!\n");
                }
                catch(Exception e) {
                        System.out.print("Unable to add appointment. Please check that the date is correctly formatted.\n");
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
		try {
			System.out.print("\tEnter Doctor's ID: ");
			String doc_id = in.readLine();
			System.out.print("\tEnter Appointment's ID: ");
			String appt_id = in.readLine();
			String query = "SELECT * FROM has_appointment H WHERE H.appt_id=" + appt_id + " AND H.doctor_id=" + doc_id + ";";
			int rows = esql.executeQuery(query);
			if(rows == 0) {
				System.out.print("Doctor with ID " + doc_id + " does not have Appointment with ID " + appt_id + "\n\n");
				return;
			}
			query = "SELECT A.status FROM Appointment A WHERE A.appnt_ID=" + appt_id + ";";
			List<List<String>> list = esql.executeQueryAndReturnResult(query);
			String status = list.get(0).get(0);
			String newStatus = "";
			if(status.equals("AV")) {
				newStatus = "AC";
			}
			else if(status.equals("AC") || status.equals("WL")) {
				newStatus = "WL";
			}
			else {
				System.out.print("This Appointment has passed. Please enter a different appointment\n\n");
				return;
			}
			System.out.print("\tEnter Patient Details:\n");
			System.out.print("\t\tEnter Patient's name: ");
			String name = in.readLine();
			System.out.print("\t\tEnter Patient's age: ");
			String age = in.readLine();
			int x;
			while(true) {
				try {
					x = Integer.parseInt(age);
					if(x < 0) {
						System.out.print("\t\t\tPlease enter a positive integer value for age: ");
						age = in.readLine();
					}
					else { break; }
				}
				catch(NumberFormatException e2) {
					System.out.print("\t\t\tPlease enter a positive integer value for age: ");
					age = in.readLine();
				}
			}
			System.out.print("\t\tEnter Patient's gender: ");
			String gender = in.readLine();
			while(!(gender.equals("M") || gender.equals("F"))) {
				System.out.print("\t\t\tPlease enter a gender value of 'M' or 'F': ");
				gender = in.readLine();
			}
			System.out.print("\t\tEnter Patient's address: ");
			String address = in.readLine();
			list = esql.executeQueryAndReturnResult("SELECT * FROM Patient P WHERE P.name=\'" + name + "\' AND P.age=" + age + " AND P.gtype=\'" + gender + "\' AND P.address=\'" + address + "\';");
			int patient_id = -1;
			if(list.size()==0) {
				patient_id = esql.executeQuery("SELECT * FROM Patient;");
				query = "INSERT INTO Patient VALUES (";
				query += patient_id + ", \'" + name + "\', \'" + gender + "\', " + age + ", \'" + address + "\', 1);";
				esql.executeUpdate(query);
			}
			else {
				patient_id = Integer.parseInt(list.get(0).get(0));
				query = "UPDATE Patient SET number_of_appts=number_of_appts+1 WHERE patient_ID=" + patient_id + ";";
				esql.executeUpdate(query);
			}
			query = "SELECT DP.hid FROM Department DP, Doctor DC WHERE DC.doctor_ID=" + doc_id + " AND DP.dept_ID=DC.did;";
			list = esql.executeQueryAndReturnResult(query);
			int hos_id = Integer.parseInt(list.get(0).get(0));
			query = "INSERT INTO searches VALUES(";
			query += hos_id + ", " + patient_id + ", " + appt_id + ");";
			esql.executeUpdate(query);
			query = "UPDATE Appointment SET status=\'" + newStatus + "\' WHERE appnt_ID=" + appt_id + ";";
			esql.executeUpdate(query);
			System.out.print("\nSuccesfully made Appointment!\n\n");
		}
		catch(Exception e) {
            		System.err.println (e.getMessage());
        	}
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
		try{
                        String query = "SELECT A.appnt_ID AS id, A.adate AS App_Date, A.time_slot AS Time_slot, A.status AS Status FROM has_appointment H, Appointment A WHERE H.doctor_id=";
                        System.out.print("\tEnter Doctor's ID: ");
                        String id = in.readLine();
                        System.out.print("\tEnter beginning of date range (MM/DD/YYYY or YYYY-MM-DD): ");
                        String begin = in.readLine();
                        System.out.print("\tEnter end of date range (MM/DD/YYYY or YYYY-MM-DD): ");
                        String end = in.readLine();
                        System.out.print("\n");
                        query += id + " AND H.appt_id=A.appnt_ID AND (A.adate BETWEEN ";
			query += "\'" + begin + "\' AND \'" + end + "\') AND (A.status=\'AV\' OR A.status=\'AC\');";
                        int rows = esql.executeQueryAndPrintResult(query);
			if(rows == 0) {
				System.out.print("There are no available or active appointments for this doctor in the specified date range, or the doctor ID does not exist.\n");
			}
                }
                catch(Exception e) {
			System.out.print("Invalid Query. Did you check if your dates are formatted correctly?\n");
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
		try{
                        String query = "SELECT A.appnt_ID AS id, A.adate AS App_Date, A.time_slot AS Time_slot, A.status AS Status FROM Department DP, Doctor DC, has_appointment H, Appointment A WHERE DP.name=";
                        System.out.print("\tEnter Department Name: ");
                        String name = in.readLine();
                        System.out.print("\tEnter a date (YYYY-MM-DD): ");
                        String date = in.readLine();
                        System.out.print("\n");
                        query += "\'" + name + "\' AND DP.dept_ID=DC.did AND DC.doctor_ID=H.doctor_id AND H.appt_id=A.appnt_ID AND A.adate=";
                        query += "\'" + date + "\' AND A.status=\'AV\';";
                        int rows = esql.executeQueryAndPrintResult(query);
                        if(rows == 0) {
                                System.out.print("There are no available appointments for this department on the specified date or the department does not exist.\n");
                        }
                }
                catch(Exception e) {
			System.out.print("Invalid Query. Did you check if your date is formatted correctly?\n");
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
		try{
			String query = "SELECT D.doctor_ID AS ID, D.name AS doctor_name, A.status, COUNT(*) AS numAppointments ";
			query += "FROM Doctor D, Appointment A, has_appointment H ";
			query += "WHERE D.doctor_ID=H.doctor_id AND A.appnt_ID=H.appt_id ";
			query += "GROUP BY D.doctor_ID, D.name, A.status ";
			query += "ORDER BY numAppointments DESC;";
			esql.executeQueryAndPrintResult(query);
		}
		catch(Exception e) {
			System.err.println (e.getMessage());
        	}
		System.out.print("\n");
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
		try{
                        String query = "SELECT D.doctor_ID, D.name, A.status, COUNT(S.pid) AS numPatients FROM Doctor D, Appointment A, has_appointment H, searches S ";
			query += "WHERE D.doctor_ID=H.doctor_id AND A.appnt_ID=H.appt_id AND S.aid=A.appnt_ID AND A.status=\'";
                        System.out.print("\tEnter Patient Status (AV, AC, PA, WL): ");
                        String status = in.readLine();
			while(!(status.equals("AV") || status.equals("AC") || status.equals("WL") || status.equals("PA"))) {
                                System.out.print("\t\tPlease enter a status value of AV, AC, WL, or PA: ");
                                status = in.readLine();
                        }
                        
                        System.out.print("\n");
                        query +=  status + "\' GROUP BY D.doctor_ID, D.name, A.status;";
                      
                        int rows = esql.executeQueryAndPrintResult(query);
			if(rows == 0) {
                                System.out.print("There are no patients for any doctor with the given status.\n");
                        }
                }
        	catch(Exception e) {
            		System.err.println (e.getMessage());
        	}
        	System.out.print("\n");
	}
}
