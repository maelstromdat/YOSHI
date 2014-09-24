package thesis.snapshots.communities.quality;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.User;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.fn.FormalNetwork;
import thesis.snapshots.communities.ic.InformalCommunity;
import thesis.snapshots.communities.in.InformalNetwork;
import thesis.snapshots.communities.nop.NetworkOfPractice;
import thesis.snapshots.communities.structure.MembersGraph;
import thesis.snapshots.communities.utils.Pair;
/**
 * [1] Quality: Community cohesiveness
 * 	[?] Clustering coefficient
 * 	[?] • how closely community members interact
(1) * 	1. Formal Networks : 1.1 Membership: countNonTeamCommits, countTeamCommits
(2) * 	2. Informal community : 2.1 Engagement : average per-month comments / number of active members
(3) * 	3. Informal network: 3.2 Openness : contributors - collaborators
(4) * 	3. Informal network: 3.1 Informality : collaboration: following/followers
(5) * 	3. Informal network: 3.2 Openness : pull requests comments
(6) * 	4. Network of practice : 4.2 Similarity : 	languages used in repositories to which they contribute
(7) * 	from : MembersGraph : commonProjects, followers
 * 
 * @author leta
 *
 */
public class CommCohesiveness
{
	CommunitiesData communitiesData;
	FormalNetwork formalNetwork;
	InformalNetwork informalNetwork;
	InformalCommunity informalCommunity;
	NetworkOfPractice networkOfPractice;
	MembersGraph membersGraph;
	
	public CommCohesiveness (CommunitiesData communitiesData)
	{
		this.communitiesData = communitiesData;
		this.formalNetwork = communitiesData.getFormalNetwork();
		this.informalNetwork = communitiesData.getInformalNetwork();
		this.informalCommunity = communitiesData.getInformalCommunity();
		this.networkOfPractice = communitiesData.getNetworkOfPractice();
	}
	
	//(1) * 	1. Formal Networks : 1.1 Membership: countNonTeamCommits, countTeamCommits
	public void cohesivenessAttr1_1() throws IOException
	{
		int countTeamCommits = 0;//formalNetwork.membership.getCountCommits();
		System.out.println(countTeamCommits);
	}
	
	public void cohesivenessAttr1_2() throws IOException
	{
		int countNonTeamCommits = 0;//formalNetwork.membership.getCountNonTeamCommits();
		System.out.println(countNonTeamCommits);
	}
		
	//(2) * 	2. Informal community : 2.1 Engagement : average per-month comments / number of active members
	public void cohesivenessAttr2() throws IOException
	{
		/* FIXME
		Map<Date, Integer> monthlyComments = informalCommunity.engagement.commentsContribution();
		Set<User> uniqueCommenters = informalCommunity.engagement.icUniqueCommenters();
		
		for(Date date : monthlyComments.keySet())
		{
			System.out.println(date+" : "+monthlyComments.get(date));
		}
		for(User user : uniqueCommenters)
		{
			System.out.println(user.getLogin()+" : "+user.getName());
		}
		*/
	}
	
	//(3) * 	3. Informal network: 3.2 Openness : contributors - collaborators
	public void cohesivenessAttr3() throws IOException
	{
		List<User> orgMembers = informalNetwork.openness.getOrgMembers();
		List<User> nonCollaborators = informalNetwork.openness.repoMembers();
		int countCollaborators = informalNetwork.openness.getCountCollaborators();
		
		displayList(orgMembers);
		displayList(nonCollaborators);
	}
	
	//(4) * 	3. Informal network: 3.1 Informality : collaboration: following/followers
	/*
	public void cohesivenessAttr4() throws IOException
	{
		informalNetwork.informality.getMemberCollaboration();
		
		Map<User, Integer> followers = informalNetwork.informality.getFollowers();
		Map<User, Integer> following = informalNetwork.informality.getFollowing();
		
		displayMap(followers);
		displayMap(following);
	}
	*/
	
	//(5) * 	3. Informal network: 3.2 Openness : pull requests comments
	/*
	public void cohesivenessAttr5() throws IOException
	{
		Set<Pair<User, User>> membersInteract = informalNetwork.openness.pullRequestsComments();
		for(Pair interaction : membersInteract)
		{
			User commiter = (User) interaction.getFirst();
			User commenter = (User) interaction.getSecond();
			System.out.println(commiter.getLogin()+" "+commenter.getLogin());
		}
	}	
	*/
	
	//(6) * 	4. Network of practice : 4.2 Similarity : 	languages used in repositories to which they contribute
	public void cohesivenessAttr6() throws IOException
	{
		 Map<String, Integer> langFreq = networkOfPractice.similarity.membersSimilarity();
		 displayMap(langFreq);		
	}
	
	//(7) * 	from : MembersGraph : commonProjects, followers
	public void cohesivenessAttr7() throws IOException
	{
		// smth about members graph connections
	}
	
	
	private void displayList(List list)
	{
		for(Object listElement : list)
		{
			System.out.println(listElement);
		}
	}
	
	private void displayMap(Map map)
	{
		for(Object key : map.keySet())
		{
			System.out.println(key+" : "+map.get(key));
		}
	}
}
