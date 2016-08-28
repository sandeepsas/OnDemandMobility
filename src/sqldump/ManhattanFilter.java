package sqldump;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Interval;

import Trip.Constants;

/*North Latitude: 40.882214 South Latitude: 40.680396 
 * East Longitude: -73.907000 West Longitude: -74.047285*/
public class ManhattanFilter {
	
	private static final double NORTH_MANHATTAN_LAT = 40.8822 ;
	private static final double SOUTH_MANHATTAN_LAT = 40.6804 ;
	private static final double EAST_MANHATTAN_LNG = -73.9070;
	private static final double WEST_MANHATTAN_LNG = -74.0473;
	
	public static boolean inManhattan(double lat, double lng){
		
		boolean lat_check = (lat <=NORTH_MANHATTAN_LAT && lat >=SOUTH_MANHATTAN_LAT);
		boolean lon_check = (lng >=WEST_MANHATTAN_LNG && lng <=EAST_MANHATTAN_LNG);
		return (lat_check && lon_check);
	}
	
		  /*"highway" = "primary" OR
			"highway" = "secondary" OR
			"highway" = "tertiary" OR
			"highway" = "residential" OR
			"highway" = "unclassified" OR
			"highway" = "road" OR
			"highway" = "living street"
			*/
	
	public static boolean isWeekday(String dateTime)
	{
		DateTime f_date = Constants.dt_formatter.parseDateTime(dateTime);
		int dayOfWeek = f_date.getDayOfWeek();

		if(dayOfWeek>5)
			return false;

		return true;
	}
	public static boolean isWeekday(DateTime dateTime)
	{
		int dayOfWeek = dateTime.getDayOfWeek();

		if(dayOfWeek>5)
			return false;

		return true;
	}

	public static Duration CalculateTripDuration(String pickup_datetime,
			String dropoff_datetime) {

		DateTime f_pickup_datetime = Constants.dt_formatter.parseDateTime(pickup_datetime);
		DateTime f_dropoff_datetime = Constants.dt_formatter.parseDateTime(dropoff_datetime);
		
		if(f_pickup_datetime.compareTo(f_dropoff_datetime)>=0){
			return new Duration(0);
		}
		Duration duration = new Interval(f_pickup_datetime, f_dropoff_datetime).toDuration();		

		return duration;
	}

	/*Distance calculator*/
	public static double distFrom(double lat1, double lng1, double lat2, double lng2) {
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
				* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		return dist;
	}

	public static boolean inLaG(double lat1, double lng1) {
		double lat2 = Constants.LaG_lat;
		double lng2 = Constants.LaG_lng;
		double earthRadius = 3958.75;
		double dLat = Math.toRadians(lat2-lat1);
		double dLng = Math.toRadians(lng2-lng1);
		double sindLat = Math.sin(dLat / 2);
		double sindLng = Math.sin(dLng / 2);
		double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
				* Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
		double dist = earthRadius * c;

		if(dist<1)
			return true;
		else
			return false;

	}

}
