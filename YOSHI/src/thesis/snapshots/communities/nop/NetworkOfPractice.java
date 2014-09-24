package thesis.snapshots.communities.nop;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.in.INInformality;
import thesis.snapshots.communities.in.INOpenness;

public class NetworkOfPractice
{
	GitHubClient client;
	public NOPDispersion dispersion;
	public NOPSimilarity similarity;
	public NOPSize size;
	Community community;
	
	public NetworkOfPractice(GitHubClient client, Community community)
	{
		this.client = client;
		this.community = community;
		dispersion = new NOPDispersion(community, client);
		similarity = new NOPSimilarity(community, client);
		size = new NOPSize(community, client);
	}
}
