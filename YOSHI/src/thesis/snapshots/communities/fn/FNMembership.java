package thesis.snapshots.communities.fn;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.RepositoryService;

import thesis.snapshots.communities.data.Community;

/**
 * Another attribute of Formal Networks is represented by the rigor adopted to select 
 * the members of the community. This attribute is be referred to as Membership importance. 
 * To understand the importance of membership, it is necessary to verify weather non-members 
 * are allowed to access the project and actively participate in its evolution. 
 * If the project is private, then a selection process is adopted to filter contributing members. 
 */
public class FNMembership
{
	Community community;
	GitHubClient client;

	int countNonTeamCommits;
	int countCommits;
	boolean computed;
	
	public FNMembership(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
		//computed = false;
	}
	
	/**
	 * All actions against teams require at a minimum an authenticated user who 
	 * is a member of the Owners team in the :org being managed. 
	 * Additionally, OAuth users require “user” scope. 
	 * Can't get team members
	 */	
	public float fnHierarchyDegree() throws IOException
	{
		float hierarchyDegree = 0.0f;
		Repository repository = community.getRepository();
		//System.out.println("attempting to reach GitHub Repository");
		// repo contributors
		System.out.println("attempting to compute contributors");
		RepositoryService repoService = new RepositoryService(client);
		CollaboratorService colabService = new CollaboratorService(client);

		List<Contributor> contributors = repoService.getContributors(repository, false);
		List<User> collaborators = colabService.getCollaborators(repository);
		Set<String> repoCollaborators = new HashSet<String>();
		Set<String> repoContributors = new HashSet<String>();
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				repoContributors.add(contributor.getLogin());
			}
		
		}
		System.out.println("Done.");
		
		System.out.println("attempting to compute collaborators");
		for(User collaborator : collaborators)
		{
			if(collaborator.getLogin() != null)
			{
				repoCollaborators.add(collaborator.getLogin());
			}
			
		}
		System.out.println("Done.");
		
		// contributors which are not team repo members
		repoContributors.removeAll(repoCollaborators);
		hierarchyDegree = (float)repoContributors.size() / (repoContributors.size() + repoCollaborators.size());
		return hierarchyDegree;
	}
	
	// Not used
	/**
	 *
	 * its getting issues and for the person who created the pull request and the person who commented on it
	 * pull request creator and pull request commenter, this goes in the direction of social-code graphs
	 *
	public void contributorsMembership() throws IOException
	{
		Repository repository = community.getRepository();
		
		// repo teams members
		TeamService teamService = new TeamService(client);
		List<Team> teams = teamService.getTeams(repository);
		Set<User> repoTeamMembers = new HashSet<User>();
		for(Team team : teams)
		{
			List<User> teamMembers = teamService.getMembers(team.getId());
			repoTeamMembers.addAll(teamMembers);			
		}	
		
		// repo contributors
		RepositoryService repoService = new RepositoryService(client);
		UserService userService = new UserService(client);
		List<Contributor> contributors = repoService.getContributors(repository, false);
		Set<User> repoContributors = new HashSet<User>();
		
		for(Contributor contributor : contributors)
		{
			User user = userService.getUser(contributor.getLogin());
			repoContributors.add(user);
		}
		
		// contributors which are not team repo members
		repoContributors.removeAll(repoTeamMembers);			
			
		CommitService commitService = new CommitService(client);
		List<Commit> commits = new ArrayList<Commit>();
		try
		{
			commitService.getCommits(community.getRepository());
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
			
		countNonTeamCommits = 0;
		countCommits = commits.size();
		for(Commit commit : commits)
		{
			User user = userService.getUser(commit.getCommitter().getName());
			if(repoContributors.contains(user))
			{
				countNonTeamCommits++;
			}
		}
		computed = true;
	}

	public int getCountNonTeamCommits() throws IOException
	{
		if(!computed)
		{
			fnHierarchyDegree();
		}
		return countNonTeamCommits;
	}

	public int getCountCommits() throws IOException
	{
		if(!computed)
		{
			fnHierarchyDegree();
		}
		return countCommits;
	}
	*/
}
