package sqldump;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TripFileProcessorNoThread {
	
	private File currentFile;
	public long skippedTrips = 0;
	public long totalTrips = 0;
	String file_line ;
	
	public static SimpleDateFormat dt_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static DateTimeFormatter dt_formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	
	public TripFileProcessorNoThread(File currentFile){
		BufferedReader file_reader = null;
		try {
			file_reader = new BufferedReader(new FileReader(currentFile));
			file_line = file_reader.readLine();
			while((file_line=file_reader.readLine())!=null 
					&& file_line.length()!=0){
				totalTrips++;
				String[] file_line_split = file_line.split(",");
				if(file_line_split.length==19){
					checkAndDump(file_line_split);
				}else{
					System.out.println("Coloumns Abesnt -> "+file_line);
					skippedTrips++;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
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
		
		String VendorID = file_line_split[0];
		String tpep_pickup_datetime = file_line_split[1];
		String tpep_dropoff_datetime= file_line_split[2];
		String passenger_count= file_line_split[3];
		String trip_distance= file_line_split[4];
		String pickup_longitude= file_line_split[5];
		String pickup_latitude= file_line_split[6];
		String RateCodeID= file_line_split[7];
		String store_and_fwd_flag= file_line_split[8];
		String dropoff_longitude= file_line_split[9];
		String dropoff_latitude= file_line_split[10];
		String payment_type= file_line_split[11];
		String fare_amount= file_line_split[12];
		String extra= file_line_split[13];
		String mta_tax= file_line_split[14];
		String tip_amount= file_line_split[15];
		String tolls_amount= file_line_split[16];
		String improvement_surcharge= file_line_split[17];
		String total_amount= file_line_split[18];
		
		try{
			double pickup_longitudeD = Double.parseDouble(pickup_longitude);
			double pickup_latitudeD = Double.parseDouble(pickup_latitude);
			double dropoff_longitudeD = Double.parseDouble(dropoff_longitude);
			double dropoff_latitudeD = Double.parseDouble(dropoff_latitude);
			/*if(!ManhattanFilter.inManhattan(pickup_latitudeD, pickup_longitudeD)){
				skippedTrips++;
				return;
			}*/

			if(pickup_longitudeD==0 ||
					pickup_latitudeD==0 ||
					dropoff_longitudeD==0 ||
					dropoff_latitudeD==0 ){
				System.out.println("Lat Long ZERO -> "+file_line);
				skippedTrips++;
				return;
			}


			if(ManhattanFilter.distFrom(dropoff_latitudeD, dropoff_longitudeD, pickup_latitudeD, pickup_longitudeD)<0.1){
				System.out.println("SHORT TRIP -> "+file_line);
				skippedTrips++;
				return;
			}

			
		}catch(NumberFormatException nfe){
			skippedTrips++;
			System.out.println("Number Format Exception = "+ file_line);
		}



	}

}
