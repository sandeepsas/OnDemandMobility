package sqldump;

import java.io.*;
import java.sql.*;
import java.util.*;

import org.joda.time.*;

public class Dump2SQL {

	private static final File data_folder = new File("/media/sandeep/WORKSPACE/Data_Repo/Taxi-Trajectories/FOIL2013/Trip-Files/");

	private static final File[] csv_files = data_folder.listFiles();

	static Connection c = null;

	public static void main(String[] args) throws IOException, SQLException {
		System.out.println("Run started at"+ LocalDateTime.now() );

		/*Create SQL database*/
		File dbFile = new File("manhattantrips.db");
		if(dbFile.exists()){
			System.out.println("Deleting Existing DB");
			dbFile.delete();
		}else{
			c = DriverManager.getConnection("jdbc:sqlite:manhattantrips.db");
		}
		
		
		createDatabase();
		openDatabase();

		List<Thread> ls = new ArrayList<Thread>();

		for(File idx_file:csv_files){
			System.out.println(idx_file.getName());
			Thread t = new Thread(new TripFileProcessor(idx_file,c));
			ls.add(t);
			t.start();
			/*try {
				
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
		for(int i = 0; i < ls.size(); i++){
			try {
				ls.get(i).join();
				System.out.println("Remaining threads to be Joined = "+ (ls.size()-i));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			c.commit();
			c.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}


	}

	private static void openDatabase() {

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:manhattantrips.db");
			c.setAutoCommit(false);
			System.out.println("Opened database successfully");
		}catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}

	private static void createDatabase() {
		Connection c = null;
		Statement stmt = null;
		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:manhattantrips.db");
			stmt = c.createStatement();
			String sql = "CREATE TABLE MHTRIPS " +
					"(ID INTEGER PRIMARY KEY AUTOINCREMENT," +
					" MEDALLION           TEXT    NOT NULL, " + 
					" PICKUPDATETIME           INTEGER    , " + 
					" DROPOFFDATETIME            INTEGER , " + 
					" PICKUPLAT        REAL, " + 
					" PICKUPLNG        REAL, " + 
					" DROPOFFLAT        REAL, " + 
					" DROPOFFLNG        REAL, " + 
					" PASSCNT         INT)"; 
			stmt.executeUpdate(sql);
			stmt.close();
			c.close();

		} catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		System.out.println("Opened database successfully");
	}
}