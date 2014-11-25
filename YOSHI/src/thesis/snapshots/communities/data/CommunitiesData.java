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
		//e5:6d:a3:7b:e0:05:9d:4a:42:71:5f:af:a4:ac:62:51
		client.setOAuth2Token("e56da37be0059d4a42715fafa4ac6251");
		community = new Community(repoOwner, repoName, client);		
		
		if(community.hasURL() && community.hasMilestones())
		{	
			formalNetwork = new FormalNetwork(client, community);
			informalNetwork = new InformalNetwork(client, community);
			networkOfPractice = new NetworkOfPractice(client, community);
			informalCommunity = new InformalCommunity(client, community);
		}
	}

	private void networkOfPracticeDispersionMap() throws IOException
	{
		Map<String,String[]> locationMap = networkOfPractice.dispersion.dispersionMap();
		
        final JFrame frame = new JFrame();
        Container content = frame.getContentPane();
        content.add(new DispersionMap(locationMap));
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
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
	
	public String communitiesData() throws IOException
	{
		// FORMAL NETWORK attributes 		
		// number of milestones/period : OK 
		System.out.println("attempting to compute avgMilestonePeriod");
		float avgMilestonePeriod = formalNetwork.formality.fnGetMilestonesPerDay();
		System.out.println("Done.");
		
		// hierarchy degree : contributors / (collaborators + contributors) : OKish  
		System.out.println("attempting to compute hierarchDegree");
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
		//TODO: this function and a few others start pumping numbers in the console without apparent reason... 
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
	// added eventually to be useful using sentiment analysis
	// pretty simple method it might be improved, see people from FBK 
	// gets comment from the pull request, split comments and putting for each word its frequency
	// if it has access to a community object and a client object it can work stand alone
	public void wordFrequency() throws IOException
	{
		System.out.println("attempting to determine WordFrequency");
		formalNetwork.wordFrequency.fnWordFrequency();
		System.out.println("Done.");
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
		System.out.println("I'm attempting to compute dispersion maps for the given repos...");
		current.networkOfPracticeDispersionMap(); // aggregate network dispersion data and compute out in console
		System.out.println("Done.");
		System.out.println("I'm attempting to compute community engagement for the given repos...");
		current.informalCommunity.engagement.commentsContribution();
		System.out.println("Done.");
	}
}
