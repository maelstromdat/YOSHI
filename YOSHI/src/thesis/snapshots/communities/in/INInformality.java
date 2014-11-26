package thesis.snapshots.communities.in;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * The key-attribute for informal networks is represented by informality of
 * communication between involved members. 
 * Informality is the result of loose ties among members. To establish whether there is a
 *  relationship between two members we looked for a continuative collaboration involving both of them.
 *  Based on what we observed, frequent interactions between two developers only results from a formal
 *  evaluation relationship in between.
 *  Average number of projects in which two members have previously collaborated
 *    
 *  Maximum percentage of community members of the same organization
 *  represents no more than 5% of the involved developers.
		 *  Organization mmember is a user that belongs to at least 1 team in the organization. 
		 *  If the authenticated user is also an owner of this organization then both concealed and public members will be returned. 
		 *  If the requester is not an owner of the organization the query will be redirected to the public members list.
 */
public class INInformality
{
	private Community community;
	private GitHubClient client;
	
	private UserService userService;
	private RepositoryService repoService;
	
	List<User> repoMembers;

	public INInformality(Community community, GitHubClient client) throws IOException
	{
		this.community = community;		
		this.client = client;

		this.userService = new UserService(client);
		this.repoService = new RepositoryService(client);
	}

	public float inInformalityCommunication() throws IOException 
	{		
		repoMembers = getCollabContri();
		float avgCollabProjects = 0.0f;
		Map<String, List<String>> collaboratorProjects = new HashMap<String, List<String>>();
		for(User user : repoMembers)
		{
				List<Repository> repositories = repoService.getRepositories(user.getLogin());
				List<String> repoNames = new ArrayList<String>();
				
				for(Repository repo : repositories)
				{
					repoNames.add(repo.getName());
				}						
				collaboratorProjects.put(user.getLogin(), repoNames);
		}
		System.out.println("Done.");
		System.out.println("computing collaboratorProjects");
		List<String> users = new ArrayList<String>(collaboratorProjects.keySet()); 
		for(String user : users)
		{
			List<String> projects = collaboratorProjects.get(user);
			int commonProjects = 0;
			
			for(String diffUser : users)
			{
				if(!user.equals(diffUser))
				{
					List<String> diffProjects = new ArrayList<String>(collaboratorProjects.get(diffUser)); 
					diffProjects.retainAll(projects);
					commonProjects+= diffProjects.size();
				}
			}
			avgCollabProjects+=commonProjects;			
		}
		System.out.println("Done.");
		
		avgCollabProjects = (float)avgCollabProjects / users.size();
		return avgCollabProjects;
	}
	
	/**
	 *  the maximum percentage of community members of the 
	 *  same organization represents no more than 5% of the involved developers
	 */
	public float inCollaboratorCompanies() throws IOException
	{
		System.out.println("attempting to compute collaboratorCompanies");
		float percentage = 0.0f;
		OrganizationService organizationService = new OrganizationService(client);
		Map<String, Integer> userCompany = new HashMap<String, Integer>();
		
		for(User user : repoMembers)
		{
			String companyName = user.getCompany();
			if(companyName != null)
			{
				if(userCompany.containsKey(companyName))
				{
					int countUsers = userCompany.get(companyName);
					userCompany.put(companyName, countUsers + 1);
				}
				else
				{
					userCompany.put(companyName, 1);
				}				
			}

			List<User> organizations = organizationService.getOrganizations(user.getLogin());								
			for(User organization : organizations)
			{
				companyName = organization.getLogin();
				if(companyName != null)
				{
					if(userCompany.containsKey(companyName))
					{
						int countUsers = userCompany.get(companyName);
						userCompany.put(companyName, countUsers + 1);
					}
					else
					{
						userCompany.put(companyName, 1);
					}				
				}				
			}
		}
		
		int maxGroupSize = 0;
		int totalGroupSize = 0;
		for(String company : userCompany.keySet())
		{
			int size = userCompany.get(company);
			if(maxGroupSize < size)
			{
				maxGroupSize = size;
			}
			totalGroupSize+=size;
		}
		percentage = (float)maxGroupSize / totalGroupSize;
		System.out.println("Done.");
		return percentage;
	}
	
	private List<User> getCollabContri() throws IOException
	{
		System.out.println("attempting to compute collaboratingContributors");
		List<User> repoMembers = new ArrayList<User>();
		CollaboratorService collaboratorService = new CollaboratorService(client);
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
		
		for(User user : collaborators)
		{
			if(user.getLogin() != null)
			{
				repoMembers.add(user);
			}
		}
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				User user = userService.getUser(contributor.getLogin());
				repoMembers.add(user);
			}
		}
		System.out.println("Done.");
		return repoMembers;
	}		
}
