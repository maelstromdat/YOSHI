package thesis.snapshots.communities.in;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.Community;

/**
 * Another attribute of informal networks is openness. 
 * The metric allowing to understand the degree of openness 
 * of a community is represented by the level of permissions granted to non-members. */
public class INOpenness
{
	Community community;
	GitHubClient client;
	
	private UserService userService;
	private RepositoryService repoService;	
	
	int countCollaborators;

	public INOpenness(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
		
		userService = new UserService(client);
		repoService = new RepositoryService(client);		
	}
	
	public List<User> getOrgPublicMembers() throws IOException
	{
		OrganizationService orgService = new OrganizationService(client);
		Repository repo = community.getRepository();
		User owner = repo.getOwner();
		List<User> publicMembers = orgService.getPublicMembers(owner.getCompany());
		return publicMembers;				
	}
	
	public List<User> getOrgMembers() throws IOException
	{
		OrganizationService orgService = new OrganizationService(client);
		Repository repo = community.getRepository();
		User owner = repo.getOwner();
		List<User> members = orgService.getMembers(owner.getCompany());
		return members;				
	}
	
	public List<User> repoMembers() throws IOException
	{
		Repository repo = community.getRepository();
		CollaboratorService collabService = new CollaboratorService(client);
		List<Contributor> contributors = repoService.getContributors(repo, false);
		
		Set<String> collaborators = new HashSet<String>();
		for(User user : collabService.getCollaborators(repo))
		{
			collaborators.add(user.getLogin());
		}
		countCollaborators = collaborators.size();
		
		List<User> nonCollaborators = new ArrayList<User>();
		for(Contributor contributor : contributors)
		{
			if(!collaborators.contains(contributor.getLogin()))
			{
				nonCollaborators.add(userService.getUser(contributor.getLogin()));
			}
		}
		
		return nonCollaborators;
	}

	public int getCountCollaborators()
	{
		return countCollaborators;
	}	
}
