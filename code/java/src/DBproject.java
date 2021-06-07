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
		if(rs.next()){
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
                        String query = "INSERT INTO Doctor VALUES (";
                        System.out.print("\tEnter Doctor's name: ");
                        String name = in.readLine();
                        System.out.print("\tEnter Doctor's specialty: ");
                        String specialty = in.readLine();
                        System.out.print("\tEnter Doctor's unique ID: ");
                        String doctor_ID = in.readLine();
                        System.out.print("\tEnter Doctor's department ID: ");
                        String did = in.readLine();
			System.out.print("\n");
                        query += doctor_ID + ", \'" + name + "\', \'" + specialty + "\', " + did + ");";
                        esql.executeUpdate(query);
			System.out.print("Succesfully added Doctor!\n");
                }
                catch(Exception e) {
			System.out.print("Unable to add doctor. Please check if the Doctor ID is unique or the department ID exists.\n");
			System.err.println (e.getMessage());
                }
		System.out.print("\n");
	}

	public static void AddPatient(DBproject esql) {//2
		try {	
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
                        System.out.print("\tEnter Patient's unique ID: ");
                        String patient_ID = in.readLine();
                        System.out.print("\tEnter Patient's number of appointments: ");
                        String numAppointments = in.readLine();
			while(true) {
                                try {
                                        x = Integer.parseInt(numAppointments);
                                        if(x < 0) {
                                                System.out.print("\t\tPlease enter an appointment value 0 or above: ");
                                                numAppointments = in.readLine();
                                        }
                                        else { break; }
                                }
                                catch(NumberFormatException e3) {
                                	System.out.print("\t\tPlease enter an appointment value 0 or above: ");
                                        numAppointments = in.readLine();
                                }
                        }
                        System.out.print("\n");
                        query += patient_ID + ", \'" + name + "\', \'" + gender + "\', " + age + ", \'" + address + "\', " + numAppointments + ");";
                        esql.executeUpdate(query);
                        System.out.print("Succesfully added Patient!\n");
                }
                catch(Exception e) {
                        System.out.print("Unable to add patient. Please check if the Patient ID is unique.\n");
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}

	public static void AddAppointment(DBproject esql) {//3
		try{
                        String query = "INSERT INTO Appointment VALUES (";
                        System.out.print("\tEnter the date of the appointment (YYYY-MM-DD): ");
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
                        System.out.print("\tEnter the appointment's unique ID: ");
                        String appnt_ID = in.readLine();
                        System.out.print("\tEnter the appointment's status (PA, AC, AV, WL): ");
                        String status = in.readLine();
			while(!(status.equals("PA") || status.equals("AC") || status.equals("AV") || status.equals("WL"))) {
				System.out.print("\t\tPlease enter a valid status value (PA, AC, AV, WL): ");
                        	status = in.readLine();
			}
                        System.out.print("\n");
                        query += appnt_ID + ", DATE \'" + adate + "\', \'" + time_slot + "\', \'" + status + "\');";
                        esql.executeUpdate(query);
                        System.out.print("Succesfully added Appointment!\n");
                }
                catch(Exception e) {
                        System.out.print("Unable to add appointment. Please check if the Appointment ID is unique and that the date is correctly formatted.\n");
                        System.err.println (e.getMessage());
                }
                System.out.print("\n");
	}


	public static void MakeAppointment(DBproject esql) {//4
		// Given a patient, a doctor and an appointment of the doctor that s/he wants to take, add an appointment to the DB
	}

	public static void ListAppointmentsOfDoctor(DBproject esql) {//5
		// For a doctor ID and a date range, find the list of active and available appointments of the doctor
	}

	public static void ListAvailableAppointmentsOfDepartment(DBproject esql) {//6
		// For a department name and a specific date, find the list of available appointments of the department
	}

	public static void ListStatusNumberOfAppointmentsPerDoctor(DBproject esql) {//7
		// Count number of different types of appointments per doctors and list them in descending order
	}

	
	public static void FindPatientsCountWithStatus(DBproject esql) {//8
		// Find how many patients per doctor there are with a given status (i.e. PA, AC, AV, WL) and list that number per doctor.
	}
}
