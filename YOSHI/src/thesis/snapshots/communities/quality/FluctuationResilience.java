package thesis.snapshots.communities.quality;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.utils.Statistics;

public class FluctuationResilience
{
	GitHubClient client;
	Map<String, Integer> follow;
	
	Community community;	
	String user;
	String repository;

	public FluctuationResilience(CommunitiesData communitiesData, GitHubClient client, String user, String repository)
	{
		this.community = communitiesData.getCommunity();
		this.client = client;
		
		this.user = user;
		this.repository = repository;
	}		
	
	private Map<String, Integer> userFollowers() throws IOException
	{
		UserService userService = new UserService(client);
		setFollowCollabContri();
		
		for(String user : follow.keySet())
		{
			List<User> followedBy = userService.getFollowers(user);
			List<User> follows = userService.getFollowing(user);
			
			int countFollow = 0;			
			for(User follower : followedBy)
			{
				String followerLogin = follower.getLogin();
				if(followerLogin != null)
				{
					if(follow.containsKey(followerLogin))
					{
						countFollow++;
					}
				}
			}
			
			for(User following : follows)
			{
				String followingLogin = following.getLogin();
				if(followingLogin != null)
				{
					if(follow.containsKey(followingLogin))
					{
						countFollow++;
					}
				}
			}
			
			follow.put(user, countFollow);
		}
		
		return follow;
	}		
	
	public double[] collabContriFollowers() throws IOException
	{
		if(follow == null)
		{
			userFollowers();
		}
		
		double[] countFollow = new double[follow.size()];
		int i = 0;
		for(String user : follow.keySet())
		{
			countFollow[i] = follow.get(user);
			i++;
		}
		return countFollow;		
	}
	
	public double frCollabContriFollowers() throws IOException
	{
		double[] values = collabContriFollowers();
		double avgCCFollowers = Statistics.getMean(values);
		avgCCFollowers/=follow.size();
		return avgCCFollowers;
	}
	
	
	private void setFollowCollabContri() throws IOException
	{
		CollaboratorService collaboratorService = new CollaboratorService(client);
		RepositoryService repositoryService = new RepositoryService(client);
		
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repositoryService.getContributors(community.getRepository(), false);
		
		follow = new HashMap<String, Integer>();
		for(User user : collaborators)
		{
			if(user.getLogin() != null)
			{
				follow.put(user.getLogin(), 0);
			}
		}
		
		for(Contributor user : contributors)
		{
			if(user.getLogin() != null)
			{
				follow.put(user.getLogin(), 0);
			}
		}
	}		
	// Not used
	/**
	public Set<Pair<User, User>> pullRequestsComments() throws IOException
	{
		Repository repo = community.getRepository();
		IssueService issuService = new IssueService(client);
		PullRequestService pullRequestService = new PullRequestService(client);

		List<PullRequest> pullRequests = pullRequestService.getPullRequests(repo, "closed");
		Set<Pair<User, User>> membersInteract = new HashSet<Pair<User, User>>();

		for(PullRequest pullRequest : pullRequests)
		{
			List<Comment> issueComments = issuService.getComments(repo, pullRequest.getNumber());
			if(issueComments.size() > 0)
			{
				for(Comment comment : issueComments)
				{
					User committer = pullRequest.getUser();
					User commenter = comment.getUser();
					if(!commiter.getLogin().equals(commenter.getLogin()))
					{
						Pair<User, User> interaction = new Pair<User, User>(committer, commenter);	
						membersInteract.add(interaction);
					}
				}				
			}
		}
		
		// TODO : remove
		for(Pair interaction : membersInteract)
		{
			User committer = (User) interaction.getFirst();
			User commenter = (User) interaction.getSecond();
			System.out.println(commiter.getLogin()+" "+commenter.getLogin());
		}
		return membersInteract;
	}	
	*/
}
