package thesis.snapshots.communities.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MilestoneService;
import org.eclipse.egit.github.core.service.RepositoryService;

public class Community
{
	GitHubClient client;
	String repoOwner;
	String repoName;
	
	private RepositoryService repoService;
	
	private Repository repo;
	
	public Community(String repoOwner, String repoName, GitHubClient client) throws IOException
	{
		this.repoOwner = repoOwner;
		this.repoName = repoName;
		this.client = client;
	}
	
	public boolean hasURL()
	{
		RepositoryService repoService = new RepositoryService(client);
		try
		{
			repoService.getRepository(repoOwner, repoName);
			return true;
		} 
		catch (IOException e)
		{
			System.err.println("Catch exception in Community hasURL");
			e.printStackTrace();
			return false;
		}			
	}
	
	public boolean hasMilestones()
	{
		Repository repository = getRepository();
		MilestoneService milestoneService = new MilestoneService(client);
		List<Milestone> milestones = new ArrayList<Milestone>();

		try
		{
			milestones = milestoneService.getMilestones(repository, "closed");
			return (milestones.size() > 1);
		} 
		catch (Exception e)
		{
			System.err.println("Catch exception in Community hasMilestones");
			return false;
		}
	}
	
	public Repository getRepository()
	{
		RepositoryService repoService = new RepositoryService(client);
		try
		{
			return repoService.getRepository(repoOwner, repoName);
		} 
		catch (IOException e)
		{
			System.err.println("Catch exception in Community getRepository");
			return null;
		}		
	}
	
	public Repository getRepository(int id)
	{
		return repo;	
	}
	
	public String getRepoName()
	{
		return repoName;	
	}

}
