/*
 * This class generate DefaultDirectedGraph from OSM data
 * 
 * Bounding Box of OSM NYC is set to North Latitude: 40.882214 South Latitude: 40.680396
 * East Longitude: -73.907000 West Longitude: -74.047285
 * 
 * @Author: Sandeep Sasidharan
 * 
 * */

package osmProcessor;

import java.io.*;
import java.util.*;


import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.joda.time.LocalDateTime;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import Graph.*;

public class RoadGraphGenerator {

	public static void main(String[] args) throws FileNotFoundException, IOException, XmlPullParserException{
		System.out.println("Run started at"+ LocalDateTime.now() );
		RoadGraph g = new RoadGraph();

		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(true);
		XmlPullParser xpp = factory.newPullParser();
		xpp.setInput ( new FileReader ("OSMData/Manhattan.osm"));

		g.osmGraphParser(xpp);

		System.out.println("Parsing ended at"+ LocalDateTime.now() );
		LinkedList<GraphNode> nodes = g.nodes;
		LinkedList<DirectedEdge> edges = g.edges;
		GraphNode hub_node = g.LGA_NODE;
		
		System.out.println(hub_node);
		System.out.println(hub_node.getId());
		System.out.println(hub_node.getId()+","+hub_node.getLat()+","+hub_node.getLon());
				
		System.out.println("Graph filling started at"+ LocalDateTime.now() );
		//Construct Graph
		DefaultDirectedWeightedGraph <GraphNode,DefaultWeightedEdge> gr_t = new  
				DefaultDirectedWeightedGraph <GraphNode,DefaultWeightedEdge>(DefaultWeightedEdge.class);

		ListIterator<GraphNode> nodeIterator_t = nodes.listIterator();
		//Adding vertices
		while (nodeIterator_t.hasNext()) {
			GraphNode single_node= nodeIterator_t.next();
			gr_t.addVertex(single_node);
		}
		//adding edges

		ListIterator<DirectedEdge> listIterator_t = edges.listIterator();
		while (listIterator_t.hasNext()) {
			DirectedEdge single_edge = listIterator_t.next();
			float weight = single_edge.getWeight();
			// Loop detection

			if(single_edge.from().getId() == single_edge.to().getId()){
				continue;
			}
			if(weight<=0){
				continue;
			}

			//Add single edge for Oneway, Two for others
			if(single_edge.isOneway()){
				//check if edge already present
				DefaultWeightedEdge temp_e = gr_t.getEdge(single_edge.from(),single_edge.to());
				if(temp_e == null){
					DefaultWeightedEdge e1 = gr_t.addEdge(single_edge.from(),single_edge.to()); 
					gr_t.setEdgeWeight(e1,weight);
				}

			}else{
				//check if edge already present
				DefaultWeightedEdge temp_i = gr_t.getEdge(single_edge.from(),single_edge.to());
				if(temp_i == null){
					DefaultWeightedEdge e1 = gr_t.addEdge(single_edge.from(),single_edge.to()); 
					gr_t.setEdgeWeight(e1, weight); 
				}
				DefaultWeightedEdge temp_o = gr_t.getEdge(single_edge.to(),single_edge.from());
				if(temp_o == null){
					DefaultWeightedEdge e2 = gr_t.addEdge(single_edge.to(),single_edge.from()); 
					gr_t.setEdgeWeight(e2, weight); 
				}
			}
		}
		System.out.println("Graph filling ended at"+ LocalDateTime.now() );

		System.out.println("Serialization started at"+ LocalDateTime.now() );

		ObjectOutputStream oos_graph = new ObjectOutputStream(new FileOutputStream("ObjectWarehouse/GraphObjects/MHRoadGraph.obj"));
		oos_graph.writeObject(gr_t);
		oos_graph.close();
	}

}
