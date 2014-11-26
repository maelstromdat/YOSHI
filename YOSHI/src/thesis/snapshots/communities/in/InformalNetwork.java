package thesis.snapshots.communities.in;

import java.io.IOException;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.Community;

public class InformalNetwork
{
	GitHubClient client;
	public INInformality informality;
	public INOpenness openness;
	Community community;
	
	public InformalNetwork(GitHubClient client, Community community) throws IOException
	{
		this.client = client;
		this.community = community;
		informality = new INInformality(community, client);
		openness = new INOpenness(community, client);
	}
}
 