package Probability;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import Graph.DirectedEdge;
import Graph.GraphNode;
import Graph.Pair;
import Trip.KdTree;
import Trip.KdTree.XYZPoint;
import osmProcessor.OsmConstants;
import osmProcessor.RoadGraph;
import sqldump.TripFileProcessor;
import sqldump.TripFileProcessorNoThread;

public class GenerateFiles {
	static Connection c = null;

	public static void main(String[] args) throws XmlPullParserException, IOException, SQLException {
		
		/*Load map data*/
		RoadGraph g = new RoadGraph();

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput ( new FileReader ("OSMData/Manhattan.osm"));

		g.osmGraphParser(xpp);
		
		LinkedList<GraphNode> nodes = g.nodes;
		LinkedList<DirectedEdge> edges = g.edges;
		System.out.println("Map Parsing Ended");
		/*Iterate through edges, get mid point of road and load kd tree*/
		KdTree<XYZPoint> sbTree = new KdTree<XYZPoint>();
		List<XYZPoint> roadEdgeMidPointList = new ArrayList<XYZPoint>(); 
		
		ListIterator<DirectedEdge> listIterator_t = edges.listIterator();
		long streetID = 1;
		while (listIterator_t.hasNext()) {
			DirectedEdge single_edge = listIterator_t.next();
			Pair<Double,Double>mid_LATLONG = OsmConstants.midPoint(single_edge.from().getLat(),
					single_edge.from().getLon(), single_edge.to().getLat(), single_edge.to().getLon());
			roadEdgeMidPointList.add(new XYZPoint(""+streetID,mid_LATLONG.getR(),mid_LATLONG.getL(),""));
			streetID++;
		}
		sbTree = new KdTree<XYZPoint>(roadEdgeMidPointList);
		System.out.println("Tree Loaded");
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
		Manhattan2SQL mnsql = new Manhattan2SQL(new File("manhattan.csv"),c, sbTree);
		System.out.println("************COMMITTING************");
		c.commit();
		System.out.println("!!!!!!! DONE !!!!!!!!!!!!!!!!");

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
					" LINKID           TEXT    NOT NULL, " + 
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
		System.out.println("Created database successfully");
	}


}
