package thesis.snapshots.communities.data;

import java.awt.Container;
import java.io.IOException;
import java.util.Map;

import javax.swing.JFrame;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.fn.FormalNetwork;
import thesis.snapshots.communities.ic.InformalCommunity;
import thesis.snapshots.communities.in.InformalNetwork;
import thesis.snapshots.communities.map.DispersionMap;
import thesis.snapshots.communities.nop.NetworkOfPractice;
import thesis.snapshots.communities.utils.Statistics;

public class CommunitiesData
{
	FormalNetwork formalNetwork;
	InformalNetwork informalNetwork;
	InformalCommunity informalCommunity;
	NetworkOfPractice networkOfPractice;
	GitHubClient client;
	Community community;

	public CommunitiesData(String repoOwner, String repoName) throws IOException 
	{
		//OAuthentication
		client = new GitHubClient();
		// ask somebody to generate a new token in the name of YOSHI
		// 71df10f4a580add22f3d62d9bcd08c2a3ac8fdf4
		System.out.println("Building GitHub Client with necessary authentication...");
		client.setOAuth2Token("71df10f4a580add22f3d62d9bcd08c2a3ac8fdf4");
		client.setUserAgent("GitHubJava/1.2.0");
		System.out.println("Done.");

		community = new Community(repoOwner, repoName, client);

		System.out.println("Gathering Intel to establish formal networks...");
		formalNetwork = new FormalNetwork(client, community);
		System.out.println("Done.");
		System.out.println("Gathering Intel to establish informal networks...");
		informalNetwork = new InformalNetwork(client, community);
		System.out.println("Done.");
		System.out.println("Gathering Intel to establish networks of practice...");
		networkOfPractice = new NetworkOfPractice(client, community);
		System.out.println("Done.");
		System.out.println("Gathering Intel to establish informal communities...");
		informalCommunity = new InformalCommunity(client, community);
		System.out.println("Done.");

	}
	
	public String communitiesData() throws IOException
	{
		// FORMAL NETWORK attributes 		
		// number of milestones/period : OK 
		System.out.println("attempting to compute avgMilestonePeriod");
		float avgMilestonePeriod = formalNetwork.formality.fnGetMilestonesPerDay();
		System.out.println("Done.");
		
		// hierarchy degree : contributors / (collaborators + contributors) : OKish  
		System.out.println("attempting to compute hierarchyDegree");
		float hierarchDegree = formalNetwork.membership.fnHierarchyDegree();
		System.out.println("Done.");
		
		// INFORMAL NETWORK attributes 		
		// average no. of projects to which members collaborated : OK 
		System.out.println("attempting to compute avgCollabProjects");
		float avgCollabProjects = informalNetwork.informality.inInformalityCommunication();
		System.out.println("Done.");
		
		// max percent of members of the same organization : OK 
		System.out.println("attempting to compute inPercentageContributorCompanies");
		float inPercentageContributorCompanies = informalNetwork.informality.inCollaboratorCompanies();
		System.out.println("Done.");
		
		// number of milestones/period : REPEATED from FN
		float lackOfGovernance = avgMilestonePeriod;

		// NETWORK OF PRACTICE attributes 		
		// average distance : OK
		double[] distances = networkOfPractice.dispersion.nopMembersDistance();		
		System.out.println("attempting to compute avgDistance");
		double avgDistance = Statistics.getMean(distances);
		System.out.println("Done.");
		
		// standard deviation distance
		System.out.println("attempting to compute standardDevDistance");
		double standardDevDistance = Statistics.getStdDev(distances); 
		//investigate. Log was saved in the logs folder
		System.out.println("Done.");
		
		// cultural distance :  : OKish
		System.out.println("attempting to compute avgCulturalDistance");
		double avgCulturalDistance = networkOfPractice.dispersion.nopMembersCulturalDistance();
		System.out.println("Done.");
		
		// self-similarity, percentage of members with the same skill : OKish
		System.out.println("attempting to compute selfSimilarity");
		double selfSimilarity = networkOfPractice.similarity.nopSelfSimilar();
		System.out.println("Done.");
		
		// number of active members : OK
		System.out.println("attempting to compute activeMembers");
		int activeMembers = networkOfPractice.size.nopActiveMembers();
		System.out.println("Done.");

		// INFORMAL COMMUNITY attributes 
		// unique commenter if is responsible for 30% contributions : OK
		System.out.println("attempting to compute uniqueCommenters");
		boolean uniqueCommenterExists = informalCommunity.engagement.icUniqueCommenters();
		System.out.println("Done.");
		
		// engagement is high if, on avg, each member posts > 30 comments/month : OKish
		System.out.println("attempting to compute highEngagement");
		boolean highEngagement = informalCommunity.engagement.icHighEngagement();
		System.out.println("Done.");
		
		String BLANK_SPACE = ", ";
		StringBuilder sb = new StringBuilder().append(avgMilestonePeriod).append(BLANK_SPACE).
				append(hierarchDegree).append(BLANK_SPACE).
				
				append(avgCollabProjects).append(BLANK_SPACE).
				append(inPercentageContributorCompanies).append(BLANK_SPACE).
				append(lackOfGovernance).append(BLANK_SPACE).
				
				append(avgDistance).append(BLANK_SPACE).
				append(standardDevDistance).append(BLANK_SPACE).
				append(avgCulturalDistance).append(BLANK_SPACE).
				append(selfSimilarity).append(BLANK_SPACE).
				append(activeMembers).append(BLANK_SPACE).
				
				append(uniqueCommenterExists).append(BLANK_SPACE).
				append(highEngagement).append(BLANK_SPACE).
				append(avgDistance).append(BLANK_SPACE).
				append(avgCulturalDistance);

		return sb.toString();
	}

	public static void main(String[] args) throws IOException
	{
		// CommunitiesData communitiesData = new CommunitiesData("liferay", "liferay-portal");
		// CommunitiesData communitiesData = new CommunitiesData("eclipse", "egit-github");
		// has watchers: CommunitiesData communitiesData = new CommunitiesData("CloudifySource", "cloudify");
		
		// has issues: CommunitiesData communitiesData = new CommunitiesData("griddynamics", "yhadoop-common");
		// has milestones: issues required : 
		CommunitiesData current = new CommunitiesData(args[0], args[1]);
		
		current.communitiesData(); // aggregating all data from communities and layout in the console
		current.wordFrequency();
		//TODO: I assume these portions compute a dispersion map and the engagement value, how do we make it work?
		current.networkOfPracticeDispersionMap(); // aggregate network dispersion data and compute out in consol
		//current.informalCommunity.engagement.commentsContribution();
	}
	
	
	private void networkOfPracticeDispersionMap() throws IOException
	{
		System.out.println("Trying to determine network dispersion...");
		Map<String,String[]> locationMap = networkOfPractice.dispersion.dispersionMap();
		
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        content.add(new DispersionMap(locationMap));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
        System.out.println("Done.");
	}
	
	
	// added eventually to be useful using sentiment analysis
	// pretty simple method it might be improved, see people from FBK 
	// gets comment from the pull request, split comments and putting for each word its frequency
	// if it has access to a community object and a client object it can work stand alone
	public void wordFrequency() throws IOException
	{
		System.out.println("attempting to run WordFrequency");
		formalNetwork.wordFrequency.fnWordFrequency();
		System.out.println("Done.");
	}

	
	public FormalNetwork getFormalNetwork()
	{
		return formalNetwork;
	}
	
	public InformalNetwork getInformalNetwork()
	{
		return informalNetwork;
	}
	
	public InformalCommunity getInformalCommunity()
	{
		return informalCommunity;
	}
	
	public NetworkOfPractice getNetworkOfPractice()
	{
		return networkOfPractice;
	}
	
	public Community getCommunity()
	{
		return community;
	}	
	
	public GitHubClient getClient()
	{
		return client;
	}
	
}
