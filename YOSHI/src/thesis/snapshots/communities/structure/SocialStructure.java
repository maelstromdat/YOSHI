package thesis.snapshots.communities.structure;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import javax.swing.JFrame;

import org.eclipse.egit.github.core.client.GitHubClient;
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

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;

/**
 * Or, do OSS projects have some
 * latent1 structure of their own? Are there dynamic, self-
 * organizing subgroups that spontaneously form and evolve?
 */
public class SocialStructure
{
	MembersGraph membersGraph;
	NewmanClusterer newmanClusterer;
	GitHubClient client;
	Community community;
	public static final String JU_PATH = "thesis/snapshots/communities/structure/social.net";
	public static final String GE_PATH = "thesis/snapshots/communities/structure/";	
	public static final String GE_PATH_NEW = "thesis/snapshots/communities/structure/qualities.gml";
	public static final String GE_SHORT_PATH = "thesis/snapshots/communities/structure/";
	
	GraphModel graphModel;
    AttributeModel attributeModel; 
	
    int edgeCount, nodeCount;
    
	public SocialStructure(CommunitiesData communitiesData, GitHubClient client, String repoOwner, String repoName) throws IOException 
	{
		this.client = client;
		
		community = communitiesData.getCommunity();
		membersGraph = new MembersGraph(client, community);
	}
	
	public MembersGraph getMembersGraph()
	{
		return membersGraph;
	}
	
	public void prepareJungGraph() throws IOException
	{
		membersGraph.prepareJungGraph();
	}
	
	public void prepareGephiGraph() throws IOException
	{
		membersGraph.prepareGephiGraph();
	}
	
	public void createJungGraph()
	{
		NewmanClusterer clusterer = new NewmanClusterer(JU_PATH);
		clusterer.start();
		// Add a restart button so the graph can be redrawn to fit the size of the frame
		JFrame jf = new JFrame();
		jf.getContentPane().add(clusterer);
		
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.pack();
		jf.setVisible(true);
	}
	
	public void createGephiGraph() {        
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
        	File file = new File(getClass().getResource("/"+GE_SHORT_PATH+community.getRepository().getName()+".gml").toURI());

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
        nodeCount = graph.getNodeCount();
        edgeCount = graph.getEdgeCount();

        //Partition with 'source' column, which is in the data
        PartitionController partitionController = Lookup.getDefault().lookup(PartitionController.class);
        Partition p = partitionController.buildPartition(attributeModel.getNodeTable().getColumn("source"), graph);
        NodeColorTransformer nodeColorTransformer = new NodeColorTransformer();
        nodeColorTransformer.randomizeColors(p);
        partitionController.transform(p, nodeColorTransformer);

        //Export
        ExportController ec = Lookup.getDefault().lookup(ExportController.class);
        try {
            ec.exportFile(new File("partition1_"+community.getRepository().getName()+".pdf"));
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
            ec.exportFile(new File("partition2_"+community.getRepository().getName()+".pdf"));
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
	
	public int getNodecount()
	{
		return nodeCount;
	}
	
	public int getEdgecount()
	{
		return edgeCount;
	}
	
	private void forceRefresh()
	{
		try
		{
			File fileIn = new File("src/"+SocialStructure.GE_PATH);
			File fileOut = new File("src/"+SocialStructure.GE_PATH_NEW);
			 
			// if file doesnt exists, then create it
			if (!fileIn.exists()) {
				fileIn.createNewFile();
			}

			if (!fileOut.exists()) {
				fileOut.createNewFile();
			}
			
			FileReader fr = new FileReader(fileIn);
			BufferedReader br = new BufferedReader(fr);
			
			FileWriter fw = new FileWriter(fileOut);
			BufferedWriter bw = new BufferedWriter(fw);

			String line = null;
            while((line = br.readLine()) != null) 
            {
                bw.write(line+"\n");
            }
			
            br.close();
			bw.close();
			fr.close();		
			fw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
}
