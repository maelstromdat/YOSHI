package thesis.snapshots.communities.fn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.MilestoneService;

import thesis.snapshots.communities.data.Community;
/**
 * Formality can be determined by calculating the number of milestones assigned to the project 
 * and relating this to the lifetime of the project itself. 
 * By computing the number of user groups, and different set of permissions, we can estimate
 * the hierarchization degree.
 * The hierarchy degree is high when the number of permission groups exceeds 2, or 
 * an average of 80% of the users of the same organization are concentrated within a single group.
 */

/**
 * Issues
 * 	assignee
 *  comments 
 *  milestones
 */
public class FNFormality 
{
	Community community;
	GitHubClient client;
	private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

	public FNFormality(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
	}
	
	// Used : OK
	public float fnGetMilestonesPerDay()
	{
		List<Milestone> milestones = getMilestones("closed");
		float milestonesPerDay;
		int closedMilestones = milestones.size();
		Date firstMilestone = new Date();	firstMilestone.setYear(9999);
		Date lastMilestone = new Date();	lastMilestone.setYear(0); 
		
		for(Milestone milestone : milestones)
		{
			Date milestoneStartDate = milestone.getCreatedAt();
			if(milestoneStartDate.getTime() > lastMilestone.getTime())
			{
				lastMilestone = milestoneStartDate;
			}
			
			if(milestoneStartDate.getTime() < firstMilestone.getTime())
			{
				firstMilestone = milestoneStartDate;
			}
		}

		milestonesPerDay=(float)closedMilestones / ((lastMilestone.getTime() - firstMilestone.getTime()) / MILLIS_IN_DAY);
		return milestonesPerDay;
	}
	
	// Not used
	/**
	public Map<String, Integer> teamHierarchDegree() throws IOException 
	{
		TeamService teamService = new TeamService(client);
		List<Team> teams = teamService.getTeams(community.getRepository());
		Map<String, Integer> permissions = new HashMap<String, Integer>();		
		
		for(Team team : teams)
		{
			String permission = team.getPermission();
			Integer membersCount = team.getMembersCount();
			
			if(permissions.containsKey(permission))
			{
				int prevMembersCount = permissions.get(permission);
				permissions.put(permission, prevMembersCount + membersCount); 
			}
			else
			{
				permissions.put(team.getPermission(), membersCount);	
			}			
		}
		
		for(String key : permissions.keySet())
		{
			System.out.println("Key : "+permissions.get(key));
		}
		
		return permissions;
	}
	*/
	
	public List<Milestone> getMilestones(String state)
	{		
		Repository repository = community.getRepository();
		MilestoneService milestoneService = new MilestoneService(client);
		List<Milestone> milestones = new ArrayList<Milestone>();

		try
		{
			milestones = milestoneService.getMilestones(repository, state);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return milestones;
	}
	
}
