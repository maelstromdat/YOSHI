package thesis.snapshots.communities.quality;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.MarkdownService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.eclipse.egit.github.core.service.WatcherService;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;

public class TeamCharacteristics
{
	GitHubClient client;
	
	Community community;	
	String user;
	String repository;
	
	int countWatchers;
	int countBloggers;
	Set<String> users;
	
	public TeamCharacteristics(CommunitiesData communitiesData, GitHubClient client, String user, String repository)
	{
		this.community = communitiesData.getCommunity();
		this.client = client;
		
		this.user = user;
		this.repository = repository;
	}	
	
	public void evaluateTeamCharacterics() throws IOException
	{
		users = getCollabContri();
		Repository repo = community.getRepository();
		countWatchers = repo.getWatchers();
	}
	
	public double tcAvgSubscriptions()
	{
		double avgSubscriptions = (double) countWatchers / users.size();
		return avgSubscriptions;
	}
	
	public double tcAvgBloggers()
	{
		double avgBloggers = (double) countBloggers / users.size();
		return avgBloggers;
	}
	
	private Set<String> getCollabContri() throws IOException
	{
		Set<String> repoMembers = new HashSet<String>();
		CollaboratorService collaboratorService = new CollaboratorService(client);
		UserService userService = new UserService(client);
		RepositoryService repoService = new RepositoryService(client);
		
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
		
		countBloggers = 0;
		for(User user : collaborators)
		{
			if(user.getLogin() != null)
			{
				repoMembers.add(user.getLogin());
			}
			
			if(user.getBlog() != null)
			{
				countBloggers++;
			}
		}
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				repoMembers.add(contributor.getLogin());
				
				User user = userService.getUser(contributor.getLogin());
				if(user.getBlog() != null)
				{
					countBloggers++;
				}
			}
		}
		return repoMembers;
	}
}
