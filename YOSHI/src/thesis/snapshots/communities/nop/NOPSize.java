package thesis.snapshots.communities.nop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.data.Main;

/**
 * To calculate the number of active members, we con-
 * sidered developers with the largest contribution, namely
 * those whose total contribution is at least equal to 50%
 * of the entire contributions to the project. The values
 */
public class NOPSize
{
	Community community;
	GitHubClient client;
	
	public NOPSize(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
	}
	
	// TODO : check if members have login for commiters list
	public int nopActiveMembers() throws IOException
	{
		CommitService commitService = new CommitService(client);
		Repository repo = community.getRepository();
		// or use DataService for getting commits
		Map<String, Integer> contributions = new HashMap<String, Integer>();
		int activeMembers = 0;
		int commitsSize = 0;

		PageIterator<RepositoryCommit> pageCommits = commitService.pageCommits(repo);
		while(pageCommits.hasNext())
		{
			try
			{
				Collection<RepositoryCommit> commits = pageCommits.next();
				
				for(RepositoryCommit commit : commits)
				{
					User user = commit.getCommitter();
					if(user != null)
					{
						if(user.getLogin() != null)
						{
							String committer = user.getLogin();
							int countCommits = 1;
							if(contributions.containsKey(committer))
							{
								countCommits+= contributions.get(committer);
							}
							contributions.put(committer, countCommits);
						}
					}
				}

				commitsSize+=commits.size();				
				//FIXME
				if(commitsSize > Main.MAX_LIMIT)
				{
					break;
				}
			}
			catch(Exception e)
			{				
				e.printStackTrace();
			}
		}
		
		List<Integer> descContributions = new ArrayList<Integer>(contributions.values());
		Collections.sort(descContributions);
		Collections.reverse(descContributions);
		
		int countCommits = 0;
		for(Integer commit : descContributions)
		{
			countCommits+=commit;
			activeMembers++;
			if(countCommits > (commitsSize / 2))
			{
				break;
			}
		}
		
		return activeMembers;
	}
}
