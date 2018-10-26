package geotools;

import java.util.ArrayList;

// http://stackoverflow.com/questions/4287780/detecting-whether-a-gps-coordinate-falls-within-a-polygon-on-a-map

public class GeoPolygonCheck {
	private static double PI = 3.14159265;
	private static double TWOPI = 2 * PI;

	public static void main(String args[]) {

		// Testing
		double latitude;
		double longitude;
		ArrayList<String> poly = new ArrayList<String>();

		poly.add("53.100000,76.150000"); // bottom left
		poly.add("53.100000,76.070000"); // bottom right
		poly.add("53.1325000,76.070000"); // top right
		poly.add("53.1325000,76.150000"); // top left

		latitude = 53.131135;
		longitude = 76.105257;

		try {
			System.out.println("Is lat-long inside polygon?: " + IsLatLongInsidePolygon(poly, latitude, longitude));
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	public static boolean IsLatLongInsidePolygon(ArrayList<String> PolyLatLongPair, double latitude, double longitude) throws Exception {
		ArrayList<Double> lat_array = new ArrayList<Double>();
		ArrayList<Double> long_array = new ArrayList<Double>();

		for (String s : PolyLatLongPair) {
			lat_array.add(Double.parseDouble(s.split(",")[0]));
			long_array.add(Double.parseDouble(s.split(",")[1]));
		}
		
		for(int i = 0; i<lat_array.size(); i++)
			if(!is_valid_gps_coordinate(lat_array.get(i), long_array.get(i)))
				throw new Exception("Invalid coordinate in polygon!");
		
		if(!is_valid_gps_coordinate(latitude, longitude))
			throw new Exception("Invalid coordinate to check against!");

		return coordinate_is_inside_polygon(latitude, longitude, lat_array, long_array);
	}

	private static boolean coordinate_is_inside_polygon(double latitude, double longitude, ArrayList<Double> lat_array, ArrayList<Double> long_array) {
		int i;
		double angle = 0;
		double point1_lat;
		double point1_long;
		double point2_lat;
		double point2_long;
		int n = lat_array.size();

		for (i = 0; i < n; i++) {
			point1_lat = lat_array.get(i) - latitude;
			point1_long = long_array.get(i) - longitude;
			point2_lat = lat_array.get((i + 1) % n) - latitude;
			// you should have paid more attention in high school geometry.
			point2_long = long_array.get((i + 1) % n) - longitude;
			angle += Angle2D(point1_lat, point1_long, point2_lat, point2_long);
		}

		if (Math.abs(angle) < PI)
			return false;
		else
			return true;
	}

	private static double Angle2D(double y1, double x1, double y2, double x2) {
		double dtheta, theta1, theta2;

		theta1 = Math.atan2(y1, x1);
		theta2 = Math.atan2(y2, x2);
		dtheta = theta2 - theta1;
		while (dtheta > PI)
			dtheta -= TWOPI;
		while (dtheta < -PI)
			dtheta += TWOPI;

		return (dtheta);
	}

	public static boolean is_valid_gps_coordinate(double latitude, double longitude) {
		if (latitude > -90 && latitude < 90 && longitude > -180 && longitude < 180) {
			return true;
		}
		return false;
	}
}
