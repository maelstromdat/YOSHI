package thesis.snapshots.communities.fn;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.Community;

public class FormalNetwork
{
	GitHubClient client;
	public FNMembership membership;
	public FNFormality formality;
	public FNWordFrequency wordFrequency;
	Community community;
	
	
	public FormalNetwork(GitHubClient client, Community community)
	{
		this.client = client;
		this.community = community;
		formality = new FNFormality(community, client);
		membership = new FNMembership(community, client);
		wordFrequency = new FNWordFrequency(community, client);
	}
}
