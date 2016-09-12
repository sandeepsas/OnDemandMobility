package Probability;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;

import Trip.Constants;
import Trip.TaxiTrip;

/*StreetID starts from 1
 * 
 * # Streets = 61650
 * # Nodes = 51195
 * */

public class GenTrainData {

	static Connection c = null;
	public static DateTimeFormatter dt_formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

	static long SIZE_EDGES = 61650;
	public static Map<String, Integer> map_time_slot;
	static PrintWriter writer;

	public static DateTime startTime;
	public static DateTime endTime;

	public static void main(String[] args) throws IOException {

		openDatabase();
		writer = new PrintWriter(new File ("ObjectWarehouse/grid_train.csv"));

		startTime = Constants.dt_formatter.parseDateTime("2016-01-01 00:00:00");
		endTime = Constants.dt_formatter.parseDateTime("2016-01-31 00:00:01");

		while(startTime.isBefore(endTime)){
			
			System.out.println("Processing = "+ startTime.toString(dt_formatter) );

			/*CREATE EMPTY HASHMAP FOR TIME INTERVALS*/
			map_time_slot = new LinkedHashMap<String,Integer>();
			DateTime startTime_t = Constants.time_formatter_HM.parseDateTime("0000");
			DateTime endTime_t = Constants.time_formatter_HM.parseDateTime("2359");
			int cnt=0;
			while(startTime_t.isBefore(endTime_t)){
				map_time_slot.put(startTime_t.toString(Constants.time_formatter_HM).trim()+
						startTime_t.plusMinutes(10).toString(Constants.time_formatter_HM).trim(), cnt );
				startTime_t = startTime_t.plusMinutes(10);
				cnt++;
			}
			/*QUERY FOR SPECIFIC TIME INTERVAL ON GRID #*/
			DateTime intervalEnd = startTime.plusMinutes(10);
			StringBuilder sbr = new StringBuilder();
			ListMultimap<Long, TaxiTrip>  tripMap = loadTrips(getUNIXTime(startTime.toString(dt_formatter)),
					getUNIXTime(intervalEnd.toString(dt_formatter)));
			
			
			for(long grid_index = 1; grid_index<= SIZE_EDGES; grid_index++){
				
				List<TaxiTrip> trips = tripMap.get(grid_index);
				List<String> pickupTimeList = new ArrayList<String>();

				
				for(int i=0;i<trips.size();i++){
					//Populate the pickup time array and send for probability calculation
					pickupTimeList.add(trips.get(i).getPickupDate());
				}
				Double probability = computeProbability(startTime,intervalEnd,pickupTimeList);
				String f_date = (startTime).toString(Constants.time_formatter_HM);
				/*Find the time Slot*/
				int timeSlot = getTimeSlot(f_date);
				//System.out.println("Time Slot = "+timeSlot+"\t PickUpTime = "+startTime.toString(Constants.dt_formatter)+"\t probability = "+probability);
				sbr.append(probability+",");

			}
			String print_sbr = sbr.toString();
			print_sbr = print_sbr.replaceAll(", $", "");
			writer.println(print_sbr);
			//System.out.println(print_sbr);
			/*Increment Start time*/
			startTime = startTime.plusMinutes(10);
		}
		writer.close();
		//out.close();

	}
	private static Double computeProbability(DateTime startTime2,
			DateTime intervalEnd2,
			List<String> pickupTimeList) {
		List<DateTime> dateList = new ArrayList<DateTime>();
		List<DateTime> timePickUpList = new ArrayList<DateTime>();

		for(String i: pickupTimeList){
			dateList.add(Constants.dt_formatter.parseDateTime(i));
			timePickUpList.add((Constants.dt_formatter.parseDateTime(i)).plusMinutes(2));
		}

		double requestPendingDuration = 0;
		long intervalStart = startTime2.getMillis();
		long intervalEnd = intervalEnd2.getMillis();

		boolean interval_flag = false;
		for(int k=0;k< timePickUpList.size();k++){
			long timeDiff=0;
			if(timePickUpList.get(k).isBefore(intervalEnd)){

				if(k<1){
					timeDiff =  (timePickUpList.get(k).getMillis()-intervalStart);
				}else{
					timeDiff = (timePickUpList.get(k).getMillis()-intervalStart);
				}
				requestPendingDuration+=timeDiff;
				intervalStart = timePickUpList.get(k).getMillis();
			}else{
				interval_flag = true;
				requestPendingDuration = 10*60*1000;
				break;
			}
		}
		//System.out.println("\n\t Pending Duration in millis = "+requestPendingDuration);
		double probability = requestPendingDuration/(60*10*1000);
		//System.out.println("\n\t Probability = "+probability);
		return round(probability,2);
	}
	
	public static ListMultimap<Long, TaxiTrip> loadTrips(long startTime, long endTime) throws IOException {

		ListMultimap<Long, TaxiTrip> tripMap = ArrayListMultimap.create();

		String sql = "SELECT * FROM MHTRIPS WHERE PICKUPDATETIME>="+startTime+""
				+ " AND DROPOFFDATETIME<"+endTime+";"; 

		/*Query DB*/
		try{
			Statement stmt = c.createStatement();
			ResultSet rs  = stmt.executeQuery(sql);

			while(rs.next()){
				StringBuilder sb = new StringBuilder();
				DateTime pdt = new DateTime(rs.getInt("PICKUPDATETIME")*1000L);
				sb.append(pdt.toString(Constants.dt_formatter));
				DateTime ddt = new DateTime(rs.getInt("DROPOFFDATETIME")*1000L);
				sb.append(pdt.toString(Constants.dt_formatter));
				sb.append(", "+rs.getInt("LINKID"));


				TaxiTrip trip = new TaxiTrip("",
						pdt.toString(Constants.dt_formatter),
						ddt.toString(Constants.dt_formatter),
						rs.getString("PASSCNT"),
						"",
						"",
						rs.getString("PICKUPLNG"),
						rs.getString("PICKUPLAT"),
						rs.getString("DROPOFFLNG"),
						rs.getString("DROPOFFLAT"));
				tripMap.put(rs.getLong("LINKID"), trip);
			}
			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return tripMap;

	}
	private static int getTimeSlot(String pickup_datetime) {
		DateTime start = Constants.time_formatter_HM.parseDateTime(pickup_datetime);

		int hh = start.getHourOfDay();
		int mm = start.getMinuteOfHour();
		int mm_a = mm - (mm%10);
		String st_key;

		if((hh<10) && (mm_a<10)){
			st_key = ""+0+hh+0+mm_a;
		}else if((hh<10)){
			st_key = ""+0+hh+mm_a;
		}else if(mm_a<10){
			st_key = ""+hh+0+mm_a;
		}else{

			st_key = ""+hh+mm_a;
		}


		//Find to which interval the start time belongs to

		DateTime end = start.plusMinutes(10);

		int e_hh = end.getHourOfDay();
		int e_mm = end.getMinuteOfHour();
		int e_mm_a = e_mm - (e_mm%10);

		String e_st_key;

		if((e_hh<10) && (e_mm_a<10)){
			e_st_key = ""+0+e_hh+0+e_mm_a;
		}else if((e_hh<10)){
			e_st_key = ""+0+e_hh+e_mm_a;
		}else if(e_mm_a<10){
			e_st_key = ""+e_hh+0+e_mm_a;
		}else{
			e_st_key = ""+e_hh+e_mm_a;
		}



		//String e_st_key = ""+e_hh+e_mm;

		String key = st_key+ e_st_key;
		/*String key = pickup_datetime.trim() +
				end.toString(Constants.time_formatter_HM).trim();*/
		//System.out.println(key);
		int res = map_time_slot.get(key);
		return res;
	}

	private static void openDatabase() {

		try {
			Class.forName("org.sqlite.JDBC");
			c = DriverManager.getConnection("jdbc:sqlite:manhattantrips.db");
			System.out.println("Opened database successfully");
		}catch ( Exception e ) {
			System.err.println( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
	}

	private static long getUNIXTime(String pickup_datetime) {
		long time = dt_formatter.parseDateTime(pickup_datetime).getMillis();
		return time/1000L;
	}
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}

}

