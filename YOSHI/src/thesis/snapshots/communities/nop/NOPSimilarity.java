package thesis.snapshots.communities.nop;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.Community;

/**
 * Self-similarity can be evaluated by
 * considering the similarity between the topics of interest
 * and the stated skills of collaborating developers. 
 * In order to be considered self-similar, a community needs 
 * to show a very high percentage of members with the same skill.
 */
public class NOPSimilarity
{
	Community community;
	GitHubClient client;
	private UserService userService;
	private RepositoryService repoService;	

	public NOPSimilarity(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
		
		userService = new UserService(client);
		repoService = new RepositoryService(client);		
	}
	
	public Map<String, Integer> membersSimilarity() throws IOException
	{
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), true);
		Map<Contributor, Set<String>> contributorsLang = new HashMap<Contributor, Set<String>>();
				
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				List<Repository> repositories = repoService.getRepositories(contributor.getLogin());
				Set<String> repoLanguages = new HashSet<String>();
				
				for(Repository repo : repositories)
				{
					if(repo.getLanguage() != null)
					{
						repoLanguages.add(repo.getLanguage());
					}
				}			
				contributorsLang.put(contributor, repoLanguages);
			}
		}
		
		// take into account also number of contributors
		Map<String, Integer> langFreq = new HashMap<String, Integer>();
		for(Set<String> languages : contributorsLang.values())
		{
			for(String lang : languages)
			{
				int countLang = 1;
				if(langFreq.containsKey(lang))
				{
					countLang+=langFreq.get(lang);
				}
				langFreq.put(lang, countLang);
			}			
		}
			
		return langFreq;
	}
	
	public double nopSelfSimilar() throws IOException
	{
		List<User> users = getCollabContri();
		Map<String, Integer> languageFreq = new HashMap<String, Integer>();
				
		for(User user : users)
		{
			if(user.getLogin() != null)
			{
				List<Repository> repositories = repoService.getRepositories(user.getLogin());
				Set<String> languages = new HashSet<String>();
				
				for(Repository repo : repositories)
				{
					if(repo.getLanguage() != null)
					{
						languages.add(repo.getLanguage());
					}
				}
				
				for(String language : languages)
				{
					int count = 1;
					if(languageFreq.containsKey(language))
					{
						count+=languageFreq.get(language);
					}
					languageFreq.put(language, count);
				}
			}
		}
		
		int maxFreq = 0;
		for(String language : languageFreq.keySet())
		{
			int freq = languageFreq.get(language);
			if(maxFreq < freq)
			{
				maxFreq = freq;
			}	
			
			//System.out.println(freq);
		}
		
		double percentage = (double) maxFreq / users.size();
		return percentage;
	}	
	
	private List<User> getCollabContri() throws IOException
	{
		CollaboratorService collaboratorService = new CollaboratorService(client);
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				User user = userService.getUser(contributor.getLogin());
				collaborators.add(user);
			}
		}
		return collaborators;
	}	
}
