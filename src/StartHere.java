import java.io.*;

import java.util.*;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.joda.time.DateTime;
import Graph.*;
import Trip.*;
public class StartHere {

	public static void main(String[] args) throws IOException, ClassNotFoundException {

		// Load the Graph
		ObjectInputStream oos_graph_read = new ObjectInputStream(new 
				FileInputStream("ObjectWarehouse/GraphObjects/NYCRoadGraph.obj"));
		DefaultDirectedWeightedGraph gr_t = new  
				DefaultDirectedWeightedGraph <GraphNode,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		gr_t =  (DefaultDirectedWeightedGraph<GraphNode, DefaultWeightedEdge>) oos_graph_read.readObject();
		oos_graph_read.close();
		
		//Load Kd Tree for search
		Set<GraphNode> nodes = gr_t.vertexSet();
		Iterator <GraphNode> node_itr = nodes.iterator();
		List<KdTree.XYZPoint> graphNodeList = new ArrayList<KdTree.XYZPoint>();
		while(node_itr.hasNext()){
			GraphNode tempNode = node_itr.next();
			graphNodeList.add(new KdTree.XYZPoint(""+tempNode.getId(), 
					tempNode.getLat(), tempNode.getLon(), ""+0));
		}
		KdTree kdtree = new KdTree<KdTree.XYZPoint>(graphNodeList);
		
		DateTime startTime = Constants.dt_formatter.parseDateTime("2013-01-01 10:00:00");
		DateTime endTime = Constants.dt_formatter.parseDateTime("2013-01-01 10:05:00");
		List<TaxiTrip>  trips = loadTrips(startTime,endTime);
		
		for(int j = 0 ; j < trips.size(); j ++){
			TaxiTrip trip_A = trips.get(j);
			
			//Extract destination lat long and convert to node
			KdTree.XYZPoint dest_A = new KdTree.XYZPoint(trip_A.getMedallion(), 
					trip_A.getDropOffLat(), trip_A.getDropOffLon(), ""+0);
			//Search for nearest vertex
			Collection<KdTree.XYZPoint> near_bys = kdtree.nearestNeighbourSearch(dest_A,0.06);
			Iterator<KdTree.XYZPoint> near_bys_itr =
					near_bys.iterator();
			GraphNode closestNode = near_bys_itr.next().toGraphNode();
		}
		

	}

	public static List<TaxiTrip> loadTrips(DateTime startTime, DateTime endTime) throws IOException {
		// TODO Auto-generated method stub
		List<TaxiTrip> trips = new ArrayList<TaxiTrip>();
		BufferedReader bf = new BufferedReader(new FileReader("ObjectWarehouse/TripData/TripDataID.csv"));
		String s = new String();
		s = bf.readLine();
		while((s=bf.readLine())!=null &&
				(s.length()!=0) ){
			String[] split_readline = s.split(",");
			DateTime trip_start_time =  Constants.dt_formatter.parseDateTime(split_readline[6]);

			TaxiTrip trip = new TaxiTrip();

			if(trip_start_time.compareTo(startTime)>0 &&
					trip_start_time.compareTo(endTime)<=0 	){
				trip = new TaxiTrip(split_readline[0],
						split_readline[6],
						split_readline[7],
						split_readline[8],
						split_readline[9],
						split_readline[10],
						split_readline[11],
						split_readline[12],
						split_readline[13],
						split_readline[14]);
				trips.add(trip);
			}

		}
		bf.close();
		return trips;

	}

}
