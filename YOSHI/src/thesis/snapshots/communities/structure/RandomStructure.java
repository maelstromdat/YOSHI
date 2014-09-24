package thesis.snapshots.communities.structure;

import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.EdgeDefault;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.partition.api.Partition;
import org.gephi.partition.api.PartitionController;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.statistics.plugin.ClusteringCoefficient;
import org.gephi.statistics.plugin.Degree;
import org.gephi.statistics.plugin.GraphDistance;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;

public class RandomStructure
{
	public static final String PATH = "thesis/snapshots/communities/structure/random.net";
	
	RandomGraph randomGraph;
	NewmanClusterer clusterer;
	
	GraphModel graphModel;
    AttributeModel attributeModel; 	
	
	public RandomStructure(int vertices, int edges)
	{
		this.randomGraph = new RandomGraph(vertices, edges);
		this.clusterer = new NewmanClusterer(PATH);
	}
	
	public void prepareGephiGraph(String name) throws IOException
	{
		randomGraph.createGephiGraph(name);
	}
	
	public void createGephiGraph(String name) {        
        //Init a project - and therefore a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        Workspace workspace = pc.getCurrentWorkspace();

        //Get controllers and models
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
        attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();

        //Import file
        Container container;
        try {
        	File file = new File(getClass().getResource("/"+SocialStructure.GE_SHORT_PATH+name+".gml").toURI());
       	
            container = importController.importFile(file);
            container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
        } catch (Exception ex) {
            ex.printStackTrace();
            return;
        }

        //Append imported data to GraphAPI
        importController.process(container, new DefaultProcessor(), workspace);

        //See if graph is well imported
        DirectedGraph graph = graphModel.getDirectedGraph();        
        /*System.out.println("Nodes: " + graph.getNodeCount());
        System.out.println("Edges: " + graph.getEdgeCount());*/

        //Partition with 'source' column, which is in the data
        PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
        Partition p = partitionController.buildPartition(attributeModel.getNodeTable().getColumn("source"), graph);
        NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
        nodeColorTransformer.randomizeColors(p);
        partitionController.transform(p, nodeColorTransformer);

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("partition-"+name+".pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }

        //Run modularity algorithm - community detection
        Modularity modularity = new Modularity();
        modularity.execute(graphModel, attributeModel);

        //Partition with 'modularity_class', just created by Modularity algorithm
        AttributeColumn modColumn = attributeModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        Partition p2 = partitionController.buildPartition(modColumn, graph);
        //System.out.println(p2.getPartsCount() + " partitions found");
        NodeColorTransformer nodeColorTransformer2 = new NodeColorTransformer();
        nodeColorTransformer2.randomizeColors(p2);
        partitionController.transform(p2, nodeColorTransformer2);

        //Export
        try {
            ec.exportFile(new File("partition2-"+name+".pdf"));
        } catch (IOException ex) {
            ex.printStackTrace();
            return;
        }
    }
	
	public double scClusteringCoefficient()
	{
		ClusteringCoefficient clusteringCoefficient = new ClusteringCoefficient();
		clusteringCoefficient.execute(graphModel, attributeModel);
		double avgClusteringCoefficient = clusteringCoefficient.getAverageClusteringCoefficient();	
		return avgClusteringCoefficient;
	}
	
	public double scDegree()
	{
		Degree degree = new Degree();
		degree.execute(graphModel, attributeModel);
		double avgDegree = degree.getAverageDegree();	
		return avgDegree;
	}	
	
	public double scModularity()
	{
		Modularity modularity = new Modularity();
		modularity.execute(graphModel, attributeModel);
		double modular = modularity.getModularity();	
		return modular;
	}	
	
	public double scClosenessCentrality()
	{
		GraphDistance graphDistance = new GraphDistance();
		graphDistance.execute(graphModel, attributeModel);
		double closenessCentrality = graphDistance.getPathLength();
		//double closenessCentrality = graphDistance.getDiameter();
		return closenessCentrality;
	}

	public static void main(String[] args) throws IOException
	{
		String[] names = { 
		"khan-exercises", 
		"SignalR", 
		"MonoGame", 
		"scikit-learn", 
		"html5-boilerplate",
		"bundler",
		"pyrocms",
		"cucumber",
		"salt",
		"composer", 
		"hammer.js", 
		"gollum", 
		"netty", 
		"Modernizrv", 
		"refinerycms", 
		"cloud9",  
		"pdf.jsv", 
		"boto", 
		"Arduino", 
		"ember.js",
		"yii",
		"mongoid", 
		"android",
		"bootstrap"};	
		
		int[] nodes = {129,  52,  132,  181,  158,  271,   209,  242,   386,   225,   17, 93,   74,  109,  312,   70,  110,  278,  39, 225,   169,  268,   78,   51};
		int[] edges = {1309, 346, 1481, 4680, 3494, 32892, 4837, 14132, 10848, 19204, 31, 2003, 622, 2671, 20064, 678, 1545, 4367, 83, 17456, 2861, 21281, 1285, 695};
		
		
		for(int i = 0; i < names.length; i++)
		{
			String currentName = "random"+names[i];
			RandomStructure structure = new RandomStructure(nodes[i], edges[i]/2);
			//structure.prepareGephiGraph(currentName);
			
			structure.createGephiGraph(currentName);
			double avgClusteringCoefficient = structure.scClusteringCoefficient();
			double avgDegree = structure.scDegree();
			double modularity = structure.scModularity();
			double closenessCentrality = structure.scClosenessCentrality();
			
			StringBuilder sb = new StringBuilder().append(names[i]).append(" ").
					append(avgClusteringCoefficient).append(" ").
					append(avgDegree).append(" ").
					append(modularity).append(" ").
					append(closenessCentrality);		
			System.out.println(sb);
		}		
	}
}
