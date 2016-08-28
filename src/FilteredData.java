import java.io.*;

import sqldump.*;

public class FilteredData {

	public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
		
		File data_folder = new File(args[0]);
		File[] csv_files = data_folder.listFiles();
		
		File outputWriter = new File("manhattanTrips.csv");
		if(outputWriter.exists()){
			outputWriter.delete();
		}
		
		PrintWriter writer = new PrintWriter(outputWriter);
		
		for(File idx_file:csv_files){
			System.out.println(idx_file.getName());
			TripFileProcessorNoThread tripFileProcess = new TripFileProcessorNoThread(idx_file, writer);
			System.out.println("Skipped Lines = "+tripFileProcess.skippedTrips);
			System.out.println("Total Lines = "+tripFileProcess.totalTrips);
			float error = (float) ((float)((float)tripFileProcess.skippedTrips/(float)tripFileProcess.totalTrips)*100.00);
			System.out.println("Error % = "+error);
		}
		writer.close();

	}

}
