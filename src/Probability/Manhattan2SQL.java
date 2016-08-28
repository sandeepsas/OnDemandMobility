package Probability;

import java.io.*;
import java.sql.*;
import java.text.*;
import java.util.*;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import Trip.KdTree;
import Trip.KdTree.XYZPoint;
import sqldump.ManhattanFilter;

public class Manhattan2SQL{

	private File currentFile;
	private long skippedTrips = 0;
	private Connection cnn;
	KdTree<XYZPoint> tree;

	public static SimpleDateFormat dt_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static DateTimeFormatter dt_formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	public Manhattan2SQL(File currentFile, Connection c, KdTree<XYZPoint> ktree){
		this.currentFile = currentFile;
		this.cnn = c;
		this.tree = ktree;
		runFile();
	}

	public void runFile() {
		BufferedReader file_reader = null;
		try {
			file_reader = new BufferedReader(new FileReader(currentFile));
			String file_line = file_reader.readLine();
			while((file_line=file_reader.readLine())!=null 
					&& file_line.length()!=0){
				String[] file_line_split = file_line.split(",");
				if(file_line_split.length==8){
					checkAndDump(file_line_split);
				}else{
					skippedTrips++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				file_reader.close();
				System.out.println("Skipped Lines from "+currentFile.getName()+" = "+skippedTrips);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void checkAndDump(String[] file_line_split) throws ParseException {

		String tripID = file_line_split[0];
		long pickup_datetime = getUNIXTime(file_line_split[1]);
		long dropoff_datetime = getUNIXTime(file_line_split[2]);
		String passenger_count = file_line_split[7];

		try{
			double pickup_longitude = Double.parseDouble(file_line_split[3]);
			double pickup_latitude = Double.parseDouble(file_line_split[4]);
			double dropoff_longitude = Double.parseDouble(file_line_split[5]);
			double dropoff_latitude = Double.parseDouble(file_line_split[6]);
			
			String linkID = getLinkID(pickup_latitude,pickup_longitude);
			if(!ManhattanFilter.inManhattan(pickup_latitude, pickup_longitude)){
				skippedTrips++;
				return;
			}

			if(pickup_longitude==0 ||
					pickup_latitude==0 ||
					dropoff_longitude==0 ||
					dropoff_latitude==0 ){
				skippedTrips++;
				return;
			}


			if(ManhattanFilter.distFrom(dropoff_latitude, dropoff_longitude, pickup_latitude, pickup_longitude)<0.1){
				skippedTrips++;
				return;
			}

			String sql = "INSERT INTO MHTRIPS (LINKID,PICKUPDATETIME,DROPOFFDATETIME,"
					+ "PICKUPLAT,PICKUPLNG,DROPOFFLAT,DROPOFFLNG,PASSCNT) " +
					"VALUES ("+linkID+","+ pickup_datetime+","+dropoff_datetime+","+pickup_latitude+","+
					pickup_longitude+","+dropoff_latitude+","+ dropoff_longitude+","+passenger_count  +")"; 
			//System.out.println(sql);
			Statement stmt = null;

			try {
				stmt = cnn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}catch(NumberFormatException nfe){
			skippedTrips++;
			System.out.println("Number Format Exception = "+ file_line_split);
		}



	}

	private String getLinkID(double latitude, double longitude) {
		String linkID = "";
		KdTree.XYZPoint nearestNode = getNNNode(new KdTree.XYZPoint("",
				latitude, longitude,""+0));
		linkID = nearestNode.linearID;
		return  linkID;
	}

	private long getUNIXTime(String pickup_datetime) {
		long time = dt_formatter.parseDateTime(pickup_datetime).getMillis();
		return time/1000L;
	}
	
	/*Returns a nearest node*/
	public KdTree.XYZPoint getNNNode(KdTree.XYZPoint node){

		//Search for nearest vertex
		Collection<KdTree.XYZPoint> near_bys = tree.nearestNeighbourSearch(node,0.06);
		Iterator<KdTree.XYZPoint> near_bys_itr =
				near_bys.iterator();
		KdTree.XYZPoint elt = near_bys_itr.next();

		return elt;

	}


}
