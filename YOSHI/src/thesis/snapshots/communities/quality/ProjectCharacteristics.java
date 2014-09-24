package thesis.snapshots.communities.quality;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.IssueEvent;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.fn.FormalNetwork;
import thesis.snapshots.communities.structure.MembersGraph;
import thesis.snapshots.communities.utils.Statistics;

public class ProjectCharacteristics
{
	GitHubClient client;
	RepositoryService repoService;
	
	private Map<Date, Integer> monthlyEvents;
	private boolean hasWiki;
	private int totalIssues;
	private int reopenedIssues;
	
	Community community;
	FormalNetwork formalNetwork;
	
	String user;
	String repository;
	
	private static final int MAX_EVENTS = 20000;
	private static final String REOPENED = "reopened";
	
	public ProjectCharacteristics (CommunitiesData communitiesData, GitHubClient client, String user, String repository)
	{
		this.community = communitiesData.getCommunity();
		this.client = client;
		this.formalNetwork = communitiesData.getFormalNetwork();
		
		this.user = user;
		this.repository = repository;
	}	

	/**
	 * Evolution in number of events : events / month
	 * API only allows last 300 events
	 */
	public void eventsEvolution() throws IOException
	{
		//EventService eventService = new EventService(client);		
		//PageIterator<Event> eventIterator = eventService.pageEvents(repo);
		
		IssueService issueService = new IssueService(client);
		PageIterator<IssueEvent> issueEventIterator = issueService.pageEvents(user, repository);
		PageIterator<Issue> issueIterator = issueService.pageIssues(user, repository);
		
		monthlyEvents = new TreeMap<Date, Integer>();
		int countEvents = 0;
		reopenedIssues = totalIssues = 0;
		
		while(issueEventIterator.hasNext())
		{
			try
			{
				Collection<IssueEvent> collectionEvents = issueEventIterator.next();
				
				for(IssueEvent event : collectionEvents)
				{
					Date created = event.getCreatedAt();
					Date monthCreated = new Date(created.getYear(), created.getMonth(), 1);

					int numberEvents = 1;
					if(monthlyEvents.containsKey(monthCreated))
					{
						numberEvents+=monthlyEvents.get(monthCreated);						
					}					
					monthlyEvents.put(monthCreated, numberEvents);
					
					if(event.getEvent().equals(REOPENED))
					{
						reopenedIssues++;
					}
				}
				
				countEvents+=collectionEvents.size();
				if(countEvents > MAX_EVENTS)
				{
					break;
				}
			}
			catch(Exception e)
			{		
				System.err.println("Catch exception in ProjectStructure, eventsEvolution");
			}
		}
		
		for(Date date : monthlyEvents.keySet())	
		{	
			System.out.println(date.getMonth()+"/"+date.getYear()+" "+monthlyEvents.get(date));	
		}
		
		while(issueIterator.hasNext())
		{
			try
			{
				Collection<Issue> collectionIssues = issueIterator.next();
				totalIssues+=collectionIssues.size();
			}
			catch(Exception e)
			{			
				System.err.println("Catch exception in ProjectStructure, eventsEvolution");
			}
		}
	}
	
	//  on 	average, each member posts no less than 30 comments/month
	public double pcMonthlyEventsStdDev() throws IOException
	{
		double[] values = new double[monthlyEvents.size()];

		if(monthlyEvents == null)
		{ 
			eventsEvolution();
		}		
		
		int i = 0;
		for(Date date : monthlyEvents.keySet())
		{
			values[i] = monthlyEvents.get(date);
			i++;			
		}
		
		double stdDev = Statistics.getStdDev(values);
		return stdDev;
	}	
	
	public Map<Date, Integer> pcMonthlyEventsGraph() throws IOException
	{
		if(monthlyEvents == null)
		{ 
			eventsEvolution();
		}
		return monthlyEvents;
	}	
	
	public double pcReopenedIssuesPercentage() throws IOException
	{
		if(monthlyEvents == null)
		{ 
			eventsEvolution();
		}
		
		double percentage = (double) reopenedIssues / totalIssues;
		return percentage;
	}		
	
	public double pcMilestonesPerDay()
	{
		float avgMilestones = formalNetwork.formality.fnGetMilestonesPerDay();
		return avgMilestones;
	}
	
	public boolean pcHasWiki()
	{
		Repository repo = community.getRepository();
		boolean hasWiki = repo.isHasWiki();
		return hasWiki;
	}
}
