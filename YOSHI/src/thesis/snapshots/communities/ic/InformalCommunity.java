package thesis.snapshots.communities.ic;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.in.INInformality;
import thesis.snapshots.communities.in.INOpenness;

public class InformalCommunity
{
	GitHubClient client;
	public ICEngagement engagement;
	Community community;
	
	public InformalCommunity(GitHubClient client, Community community)
	{
		this.client = client;
		this.community = community;
		engagement = new ICEngagement(community, client);		
	}
}
