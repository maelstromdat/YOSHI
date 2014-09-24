package thesis.snapshots.communities.ic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.WatcherService;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.data.Main;

/**
 * Another attribute of informal networks is openness. 
 * The metric allowing to understand the degree of openness 
 * of a community is represented by the level of permissions granted to non-members. */
public class ICEngagement
{
	Community community;
	GitHubClient client;
	private final int AVG_COMMENTS = 30;
	
	RepositoryService repoService;
	
	private Map<Date, Integer> monthlyComments;
	private Map<String, Integer> uniqueCommenters;

	public ICEngagement(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
		
		repoService = new RepositoryService(client);
	}
	
	/**
	 * High contribution in terms of comments
	 * Average per-month comments / number of active members(?)
	 * @throws IOException 
	 */
	public void commentsContribution() throws IOException
	{
		Repository repo = community.getRepository();
		IssueService issueService = new IssueService(client);
		PullRequestService pullRequestService = new PullRequestService(client);
		Set<String> repoMembers = getCollabContri();
		
		monthlyComments = new TreeMap<Date, Integer>();
		uniqueCommenters = new HashMap<String, Integer>();
		
		PageIterator<PullRequest> pageIterator = pullRequestService.pagePullRequests(repo, "closed");
		int countPullRequests = 0;
		
		while(pageIterator.hasNext())
		{
			try
			{
				Collection<PullRequest> pullRequests = pageIterator.next();

				for(PullRequest pullRequest : pullRequests)
				{
					List<Comment> issueComments;
					try
					{
						issueComments = issueService.getComments(repo, pullRequest.getNumber());
					}
					catch(Exception e)
					{
						issueComments = new ArrayList<Comment>();
					}
					
					if(issueComments.size() > 0)
					{
						for(Comment comment : issueComments)
						{
							Date created = comment.getCreatedAt();
							String commentCreatorLogin = comment.getUser().getLogin();
							Date monthCreated = new Date(created.getYear(), created.getMonth(), 1);
							
							if(commentCreatorLogin != null && repoMembers.contains(commentCreatorLogin))
							{
								int countComments = 1;
								if(monthlyComments.containsKey(monthCreated))
								{
									countComments+=monthlyComments.get(monthCreated);						
								}					
								monthlyComments.put(monthCreated, countComments);
							
								int countUserComments = 1;
								if(uniqueCommenters.containsKey(commentCreatorLogin))
								{
									countUserComments+=uniqueCommenters.get(commentCreatorLogin);
								}						
								uniqueCommenters.put(commentCreatorLogin, countUserComments);
							}
						}	
					}
				}
				
				countPullRequests+=pullRequests.size();
				
				if(countPullRequests > Main.MAX_LIMIT-1)
				{
					break;
				}
			}
			catch(Exception e)
			{				
				System.err.println("Catch exception in ICEngagement, commentsContribution");
			}
		}

		CommitService commitService = new CommitService(client);
		PageIterator<CommitComment> pageComments = commitService.pageComments(repo);
		
		while(pageComments.hasNext())
		{
			try
			{
				Collection<CommitComment> commitComments = pageComments.next();
				
				for(CommitComment comment : commitComments)
				{
					Date created = comment.getCreatedAt();
					Date monthCreated = new Date(created.getYear(), created.getMonth(), 1);
					String commentCreatorLogin = comment.getUser().getLogin();
					
					if(commentCreatorLogin != null && repoMembers.contains(commentCreatorLogin))
					{
						int countComments = 1;
						if(monthlyComments.containsKey(monthCreated))
						{
							countComments+=monthlyComments.get(monthCreated);						
						}			
						monthlyComments.put(monthCreated, countComments);
					
						int countUserComments = 1;
						if(uniqueCommenters.containsKey(commentCreatorLogin))
						{
							countUserComments+=uniqueCommenters.get(commentCreatorLogin);
						}						
						uniqueCommenters.put(commentCreatorLogin, countUserComments);
					}
				}
			}
			catch(Exception e)
			{	
				System.err.println("Catch exception in ICEngagement, commentsContribution");
			}
		}

		//for(Date date : monthlyComments.keySet())	{	System.out.println(monthlyComments.get(date));	}
		/*for(Date date : monthlyComments.keySet())	{	System.out.println(date+" : "+monthlyComments.get(date));	}
		for(String user : uniqueCommenters.keySet())	{	System.out.println(user+" : "+uniqueCommenters.get(user));	}
		*/		
	}
	
	/** 
	 * Unique commenters within a project
	 * Total numbers of members that are unique commenters
	 */
	public boolean icUniqueCommenters() throws IOException
	{
		if(uniqueCommenters == null ||  monthlyComments == null)
		{ 
			commentsContribution();
		}
		
		int totalComments = 0;
		int maxComments = 0;
		for(String user : uniqueCommenters.keySet())
		{
			int comments = uniqueCommenters.get(user);
			totalComments+=comments;
			if(comments > maxComments)
			{
				maxComments = comments;
			}
		}
		
		float percentage = (float) maxComments / totalComments;
		if(percentage >= 0.3f)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	//  on 	average, each member posts no less than 30 comments/month
	public boolean icHighEngagement() throws IOException
	{

		if(uniqueCommenters == null ||  monthlyComments == null)
		{ 
			commentsContribution();
		}		
		
		int countMonths = monthlyComments.size();
		for(String user : uniqueCommenters.keySet())
		{
			int comments = uniqueCommenters.get(user);
			float avgComments = (float) comments / countMonths;
			if(avgComments < AVG_COMMENTS)
			{
				return false;
			}
		}
		return true;
	}
	
	public Map<Date, Integer> icGraphHighEngagement() throws IOException
	{
		if(uniqueCommenters == null ||  monthlyComments == null)
		{ 
			commentsContribution();
		}	
		return monthlyComments;
	}
	
	public Map<String, Integer> icGraphUniqueCommenters() throws IOException
	{
		if(uniqueCommenters == null ||  monthlyComments == null)
		{ 
			commentsContribution();
		}		
		
		// sort commenters by number of comments
		Map<String, Integer> uniqueCommentersTree = new TreeMap<String, Integer>();
		uniqueCommentersTree.putAll(uniqueCommenters);
		
		return uniqueCommentersTree;		
	}
	
	
	private Set<String> getCollabContri() throws IOException
	{
		Set<String> repoMembers = new HashSet<String>();
		CollaboratorService collaboratorService = new CollaboratorService(client);
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
		
		for(User user : collaborators)
		{
			if(user.getLogin() != null)
			{
				repoMembers.add(user.getLogin());
			}
		}
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				repoMembers.add(contributor.getLogin());
			}
		}
		return repoMembers;
	}	
}
