import java.io.*;

import java.io.UnsupportedEncodingException;

import sqldump.*;

public class FilteredData {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		File data_folder = new File(args[0]);
		File[] csv_files = data_folder.listFiles();
		
		for(File idx_file:csv_files){
			System.out.println(idx_file.getName());
			TripFileProcessorNoThread tripFileProcess = new TripFileProcessorNoThread(idx_file);
			System.out.println("Skipped Lines = "+tripFileProcess.skippedTrips);
			System.out.println("Total Lines = "+tripFileProcess.totalTrips);
			float error = (float) ((float)(tripFileProcess.skippedTrips/tripFileProcess.totalTrips)*100.00);
			System.out.println("Error % = "+error);
		}
		

	}

}
