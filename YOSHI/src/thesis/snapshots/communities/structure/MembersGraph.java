package thesis.snapshots.communities.structure;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.data.Main;
public class MembersGraph
{
	public static final String GRAPH_FILE = "graph.txt";
	GitHubClient client;
	Community community;
	
	private UserService userService;
	private RepositoryService repoService;	
	
	Map<String, Set<String>> connections;
	Map<String, List<String>> contributorsProjects;
	Map<String, Integer> mapUserNumber;
	
	public MembersGraph(GitHubClient client, Community community)
	{
		this.client = client;
		this.community = community;
		
		userService = new UserService(client);
		repoService = new RepositoryService(client);	
	}
	
	public void prepareJungGraph() throws IOException
	{		
		commonProjects();
		followers();
		commenters();
		writeJungGraph();
	}
	
	public void prepareGephiGraph() throws IOException
	{
		commonProjects();
		followers();
		commenters();
		writeGephiGraph();
	}
	
	private void commonProjects() throws IOException 
	{
		connections = new HashMap<String, Set<String>>();
		contributorsProjects = new HashMap<String, List<String>>();
		mapUserNumber = new HashMap<String, Integer>();
		String currentRepoName = community.getRepository().getName();
		
		List<String> members = getCollabContri();		
		for(String userLogin : members)
		{
			List<Repository> repositories = repoService.getRepositories(userLogin);
			List<String> repoNames = new ArrayList<String>();
			Set<String> userLinks = new HashSet<String>();
			
			for(Repository repo : repositories)
			{
				String repoName = repo.getName();
				if(!repoName.equals(currentRepoName))
				{
					repoNames.add(repo.getName());
				}
			}
			
			contributorsProjects.put(userLogin, repoNames);
			connections.put(userLogin, userLinks);
		}

		Object[] users = contributorsProjects.keySet().toArray();
		for(int i = 0; i < users.length; i++)
		{
			String user = users[i].toString();
			mapUserNumber.put(user, i);
		}
		
		for(int i = 0; i < users.length - 1; i++)
		{
			String user = users[i].toString();
			Set<String> userLinks = connections.get(user);
			List<String> projects = contributorsProjects.get(user);
			
			for(int j = i + 1; j < users.length; j++)
			{
				String diffUser = users[j].toString();
				Set<String> diffUserLinks = connections.get(diffUser);
				List<String> diffUserProjects = new ArrayList<String>(contributorsProjects.get(diffUser)); 
				
				diffUserProjects.retainAll(projects);
				if(diffUserProjects.size() > 0)
				{						
					userLinks.add(diffUser);
					diffUserLinks.add(user);
					connections.put(diffUser, diffUserLinks);
				}
			}
			connections.put(user, userLinks);
		}
		
	}
	
	private void followers() throws IOException
	{
		for(String user : contributorsProjects.keySet())
		{
			Set<String> currConnections = connections.get(user);
			List<User> followers = userService.getFollowers(user);
			List<User> following = userService.getFollowing(user);
			
			for(User follower : followers)
			{
				String currFollower = follower.getLogin();
				if(contributorsProjects.containsKey(currFollower))
				{
					currConnections.add(currFollower);
					
					Set<String> currFollowerConnections = connections.get(currFollower);
					currFollowerConnections.add(user);
					connections.put(currFollower, currFollowerConnections);
				}					
			}
			
			for(User followed : following)
			{
				String currFollower = followed.getLogin();
				if(contributorsProjects.containsKey(currFollower))
				{
					currConnections.add(currFollower);
					
					Set<String> currFollowedConnections = connections.get(currFollower);
					currFollowedConnections.add(user);					
					connections.put(currFollower, currFollowedConnections);
				}
			}
			
			connections.put(user, currConnections);
		}	
	}
	
	private void commenters() throws IOException
	{
		Repository repo = community.getRepository();
		IssueService issueService = new IssueService(client);
		PullRequestService pullRequestService = new PullRequestService(client);
		
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
					
					String pullRequestCreator = pullRequest.getUser().getLogin();
					if(issueComments.size() > 0 && pullRequestCreator != null && contributorsProjects.containsKey(pullRequestCreator))
					{
						Set<String> requestCreatorLinks = connections.get(pullRequestCreator);				
						
						for(Comment comment : issueComments)
						{
							String pullRequestCommenter = comment.getUser().getLogin();
							
							if(pullRequestCommenter != null && contributorsProjects.containsKey(pullRequestCommenter))
							{
								Set<String> requestCommenterLinks = connections.get(pullRequestCommenter);
								requestCommenterLinks.add(pullRequestCreator);
								connections.put(pullRequestCommenter, requestCommenterLinks);
								
								requestCreatorLinks.add(pullRequestCommenter);
							}
						}
						
						connections.put(pullRequestCreator, requestCreatorLinks);
					}				
				}
				
				countPullRequests+=pullRequests.size();
				if(countPullRequests > Main.MAX_LIMIT)
				{
					break;
				}
			}
			catch(Exception e)
			{			
				e.printStackTrace();
				System.err.println("Catch exception in MembersGraph, commenters");
			}
		}
	}
	
	private List<String> getCollabContri() throws IOException
	{
		CollaboratorService collaboratorService = new CollaboratorService(client);
		RepositoryService repositoryService = new RepositoryService(client);
		
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repositoryService.getContributors(community.getRepository(), false);
		List<String> members = new ArrayList<String>();
		
		for(User user : collaborators)
		{
			if(user.getLogin() != null)
			{
				members.add(user.getLogin());
			}
		}
		
		for(Contributor user : contributors)
		{
			if(user.getLogin() != null)
			{
				members.add(user.getLogin());
			}
		}
		return members;
	}	
	
	private void display()
	{
		for(String user : connections.keySet())
		{
			Set<String> links = connections.get(user);
			System.out.print(user+": ");
			for(String link : links)
			{
				System.out.print(link+" ");
			}
			System.out.println();
		}
		System.out.println("***** End Of Display *****");
	}
	
	public void writeJungGraph()
	{
		try {
			//File file = new File("src/thesis/snapshots/communities/structure/social.net");
			File file = new File("src/"+SocialStructure.JU_PATH);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file);
			
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();			
	        String NEWLINE = System.getProperty("line.separator");

			String content = "*Vertices "+connections.size()+NEWLINE;
			sb.append(content);
			for(int i = 1; i <= connections.size(); i++)
			{
				content = i+" "+"\""+i+"\""+NEWLINE;
				sb.append(content);
			}
			
			content = "*Edges"+NEWLINE;
			sb.append(content);
			
			int weight = 1;
			for(String user : connections.keySet())
			{
				Set<String> links = connections.get(user);
				int userId = mapUserNumber.get(user);
				
				for(String link : links)
				{
					int userLinkId = mapUserNumber.get(link);
					content = (userId+1)+" "+userLinkId+" "+weight+NEWLINE;
					sb.append(content);
				}
			}			
			
			bw.write(sb.toString());
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void writeGephiGraph() 
	{
		try {
			String fileName = community.getRepository().getName();
			File file = new File("src/"+SocialStructure.GE_PATH+fileName);
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file);
			
			BufferedWriter bw = new BufferedWriter(fw);
			StringBuilder sb = new StringBuilder();			
	        String NEWLINE = System.getProperty("line.separator");

			String content = "graph"+NEWLINE+"["+NEWLINE+"directed 0"+NEWLINE;
			sb.append(content);
			for(int i = 0; i < connections.size(); i++)
			{
				content = "node"+NEWLINE+"["+NEWLINE+"id "+i+NEWLINE+
						"source "+"\""+"github"+"\""+NEWLINE+"]"+NEWLINE;
				sb.append(content);
			}
			
			for(String user : connections.keySet())
			{
				Set<String> links = connections.get(user);
				int userId = mapUserNumber.get(user);
				
				for(String link : links)
				{
					int userLinkId = mapUserNumber.get(link);
					content = "edge"+NEWLINE+"["+NEWLINE+"source "+userId+NEWLINE+"target "+userLinkId+NEWLINE+"]"+NEWLINE;
					sb.append(content);
				}
			}		
			
			content="]"+NEWLINE;
			sb.append(content);
			
			bw.write(sb.toString());
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
}
