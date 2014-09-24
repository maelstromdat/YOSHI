package thesis.snapshots.communities.quality;

import java.io.IOException;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.fn.FormalNetwork;
import thesis.snapshots.communities.structure.MembersGraph;
import thesis.snapshots.communities.structure.RandomStructure;
import thesis.snapshots.communities.structure.SocialStructure;

public class Qualities
{
	GitHubClient client;
	public CommCohesiveness commCohesiveness;
	public CommunitiesData communitiesData;
	public ProjectCharacteristics prjCharacteristics;
	public TaskAllocation taskAllocation;
	public TeamCharacteristics teamCharacteristics;
	public FluctuationResilience fluctuationResilience;
	public SocialStructure socialStructure;
	
	public RandomStructure randomStructure;
	
	String repoOwner;
	String repoName;
	
	public Qualities(String repoOwner, String repoName) throws IOException 
	{
		this.communitiesData = new CommunitiesData(repoOwner, repoName);
		this.client = communitiesData.getClient();
		this.repoName = repoName;
		this.repoOwner = repoOwner;
		
		commCohesiveness = new CommCohesiveness(communitiesData);
		prjCharacteristics = new ProjectCharacteristics(communitiesData, client, repoOwner, repoName);
		taskAllocation = new TaskAllocation(communitiesData, client, repoOwner, repoName);
		teamCharacteristics = new TeamCharacteristics(communitiesData, client, repoOwner, repoName);
		fluctuationResilience = new FluctuationResilience(communitiesData, client, repoOwner, repoName);
		socialStructure = new SocialStructure(communitiesData, client, repoName, repoOwner);
	}
	
	public String getQualities() throws IOException
	{
		String qualities = projectCharacterictics()+", "+
				taskAllocation()+", "+
				fluctuationResilience()+", "+
				teamCharacteristics()+", "+
				socialStructure() + ", "+
				// added a method to evaluate a type estimation using the qualities and characteristics found so far
				determineType(projectCharacterictics(),taskAllocation(),fluctuationResilience(),teamCharacteristics(),socialStructure());
		return qualities;
	}
	
	public String projectCharacterictics() throws IOException
	{
		prjCharacteristics.eventsEvolution();
		
		double monthlyEventsStdDev = prjCharacteristics.pcMonthlyEventsStdDev();
		double reopenedIssuesPercentage = prjCharacteristics.pcReopenedIssuesPercentage();
		double milestonesPerDay = prjCharacteristics.pcMilestonesPerDay();
		boolean hasWiki = prjCharacteristics.pcHasWiki();
		
		
		StringBuilder sb = null;
		sb= new StringBuilder().append(monthlyEventsStdDev).append(", ").
		append(reopenedIssuesPercentage).append(", ").
		append(milestonesPerDay).append(", ").
		append(hasWiki);
		
		/*System.out.println(sb.toString());*/
		return sb.toString();		
	}
	
	public String taskAllocation() throws IOException
	{
		taskAllocation.commitsEvolution();
		taskAllocation.collaborationFiles();
		taskAllocation.writeData3();
		taskAllocation.writeData4();
		
		double avgUserCommits = taskAllocation.taUserCommits();
		double avgFileContributors = taskAllocation.taFileChangedUsers();

		int activeMembers = taskAllocation.activeMembers();		
			
		StringBuilder sb = new StringBuilder().append(avgUserCommits).append(", ").
				append(avgFileContributors).append(", ").
				append(activeMembers);

		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public String fluctuationResilience() throws IOException
	{
		// user can collaborate with other for different files; 
		// medium number of collaborations a user has / max number of possible collabs (total users) 
		double avgUserCollaborationFiles = taskAllocation.taUserCollaborationFiles();

		double avgCCFollowers = fluctuationResilience.frCollabContriFollowers();

		StringBuilder sb = new StringBuilder().append(avgUserCollaborationFiles).append(", ").append(avgCCFollowers);

		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public String teamCharacteristics() throws IOException
	{
		teamCharacteristics.evaluateTeamCharacterics();
		taskAllocation.commitsEvolution();
		double avgCommiterLonvigity = taskAllocation.taCommiterLongivity();
		double avgSubscriptions = teamCharacteristics.tcAvgSubscriptions();
		double avgBloggers = teamCharacteristics.tcAvgBloggers();
		
		StringBuilder sb = new StringBuilder().append(avgCommiterLonvigity).append(", ").
				append(avgSubscriptions).append(", ").
				append(avgBloggers);

		System.out.println(sb.toString());
		return sb.toString();
	}
	
	public String socialStructure() throws IOException
	{
		socialStructure.prepareGephiGraph();
		socialStructure.createGephiGraph();
		
		/** https://wiki.gephi.org/index.php/Avg_Clustering_Coefficient */
		double avgClusteringCoefficient = socialStructure.scClusteringCoefficient();

		double avgDegree = socialStructure.scDegree();
		/** https://wiki.gephi.org/index.php/Modularity */
		
		double modularity = socialStructure.scModularity();
		
		/** Path length : https://wiki.gephi.org/index.php/Avg_Path_Length*/
		/** diameter : The maximal distance between all pairs of nodes. */
		double closenessCentrality = socialStructure.scClosenessCentrality();
		
		StringBuilder sb = new StringBuilder().append(avgClusteringCoefficient).append(", ").
				append(avgDegree).append(", ").
				append(modularity).append(", ").
				append(closenessCentrality);
		return sb.toString();
		
		/** check this : https://wiki.gephi.org/index.php/HITS ? */
	}	
	
	public String determineType(String projectCharacterictics, String taskAllocation, String fluctuationResilience, String teamCharacteristics, String socialStructure) throws IOException {
		StringBuilder type = null;
		// this function should compute a type estimation from YOSHI characteristics which have been determined for the community under analysis
		// EXPLANATION: 
		// not implemented yet, currently returning null type only, should distinguish between all possible types available;
		// you have all the values received as strings so far you need only compute them against the known constraints 
		return type.toString();
	}

	public static void main(String[] args) throws IOException
	{
		//CommunitiesData communitiesData = new CommunitiesData("liferay", "liferay-portal");
		//CommunitiesData communitiesData = new CommunitiesData("eclipse", "egit-github");
		// has issues: CommunitiesData communitiesData = new CommunitiesData("griddynamics", "yhadoop-common");
		
		Qualities qualities = new Qualities("griddynamics", "yhadoop-common");
		qualities.socialStructure();
	}	
}

