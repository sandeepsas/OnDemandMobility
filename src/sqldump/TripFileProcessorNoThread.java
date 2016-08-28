package sqldump;

import java.io.*;

import java.text.*;
import java.text.SimpleDateFormat;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TripFileProcessorNoThread {
	
	private File currentFile;
	public long skippedTrips = 0;
	public long totalTrips = 0;
	public long tripID = 1;
	String file_line ;
	
	PrintWriter writer;
	
	public static SimpleDateFormat dt_format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	public static DateTimeFormatter dt_formatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
	
	public TripFileProcessorNoThread(File currentFile) throws FileNotFoundException, UnsupportedEncodingException{
		//writer = new PrintWriter("manhattanTrips.csv", "UTF-8");
		File outputWriter = new File("manhattanTrips.csv");
		if(outputWriter.exists()){
			outputWriter.delete();
		}
		
		writer = new PrintWriter(new FileOutputStream(
				outputWriter, true ));
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
				writer.close();
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
			if(!ManhattanFilter.inManhattan(pickup_latitudeD, pickup_longitudeD)){
				skippedTrips++;
				return;
			}

			if(pickup_longitudeD==0 ||
					pickup_latitudeD==0 ||
					dropoff_longitudeD==0 ||
					dropoff_latitudeD==0 ){
				//System.out.println("Lat Long ZERO -> "+file_line);
				skippedTrips++;
				return;
			}


			if(ManhattanFilter.distFrom(dropoff_latitudeD, dropoff_longitudeD, pickup_latitudeD, pickup_longitudeD)<0.1){
				//System.out.println("SHORT TRIP -> "+file_line);
				skippedTrips++;
				return;
			}
			
			if(ManhattanFilter.distFrom(dropoff_latitudeD, dropoff_longitudeD, pickup_latitudeD, pickup_longitudeD)>250){
				//System.out.println("VERY LONG TRIP -> "+file_line);
				skippedTrips++;
				return;
			}

			
		}catch(NumberFormatException nfe){
			skippedTrips++;
			System.out.println("Number Format Exception = "+ file_line);
		}finally{
			StringBuilder sb = new StringBuilder();
			sb.append(tripID+",");
			sb.append(tpep_pickup_datetime+",");
			sb.append(tpep_dropoff_datetime+",");
			sb.append(pickup_longitude+",");
			sb.append(pickup_latitude+",");
			sb.append(dropoff_longitude+",");
			sb.append(dropoff_latitude+",");
			sb.append(passenger_count);
			writer.println(sb.toString());
			tripID++;
		}



	}

}
