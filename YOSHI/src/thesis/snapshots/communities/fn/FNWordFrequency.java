package thesis.snapshots.communities.fn;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.eclipse.egit.github.core.Comment;
import org.eclipse.egit.github.core.CommitComment;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.PullRequestService;

import thesis.snapshots.communities.data.Community;

public class FNWordFrequency
{
	Community community;
	GitHubClient client;

	public FNWordFrequency(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
	}
	
	public Map<String, Integer> fnWordFrequency() throws IOException
	{
		Map<String, Integer> wordFrequency = new HashMap<String, Integer>();
		
		Repository repo = community.getRepository();
		IssueService issueService = new IssueService(client);
		PullRequestService pullRequestService = new PullRequestService(client);
		PageIterator<PullRequest> pageIterator = pullRequestService.pagePullRequests(repo, "closed");
		System.out.println("attempting to determine issueComments");
		while(pageIterator.hasNext())
		{
			try
			{
				Collection<PullRequest> pullRequests = pageIterator.next();

				for(PullRequest pullRequest : pullRequests)
				{
					List<Comment> issueComments;
					try
					{
						issueComments = issueService.getComments(repo, pullRequest.getNumber());
					}
					catch(Exception e)
					{
						issueComments = new ArrayList<Comment>();
					}
					
					if(issueComments.size() > 0)
					{
						for(Comment comment : issueComments)
						{
							String commentContent = comment.getBody();
							String[] words = spiltSentence(commentContent);
							for(String word : words)
							{
								if(wordCheck(word))
								{
									int frequency = 1;
									if(wordFrequency.containsKey(word))
									{
										frequency+=wordFrequency.get(word);
									}
									wordFrequency.put(word, frequency);
								}
							}
						}	
					}
				}
			}
			catch(Exception e)
			{	
				System.err.println("Catch exception in FNWordFrequency, fnWordFrequency");
				break;
			}
			System.out.println("Done.");
		}

		CommitService commitService = new CommitService(client);
		System.out.println("attempting to determine pageComments");
		PageIterator<CommitComment> pageComments = commitService.pageComments(repo);
		
		while(pageComments.hasNext())
		{
			try
			{
				
				Collection<CommitComment> commitComments = pageComments.next();
				
				for(CommitComment comment : commitComments)
				{
					String commentContent = comment.getBody();
					String[] words = spiltSentence(commentContent);
					for(String word : words)
					{
						if(wordCheck(word))
						{
							int frequency = 1;
							if(wordFrequency.containsKey(word))
							{
								frequency+=wordFrequency.get(word);
							}
							wordFrequency.put(word, frequency);
						}
					}
				}
			}
			catch(Exception e)
			{		
				System.err.println("Catch exception in FNWordFrequency, fnWordFrequency");
				break;
			}
			System.out.println("Done.");
		}
		
		Map<String, Integer> sortedMapAsc = sortByComparator(wordFrequency, false);

		int wordLimiter = 0;
        for (Entry<String, Integer> entry : sortedMapAsc.entrySet())
        {
            System.out.println(entry.getKey()+", "+entry.getValue());
			wordLimiter++;
			if(wordLimiter > 200)
			{
				break;
			}
		}
		return wordFrequency;
	}
	
	private boolean wordCheck(String word)
	{
		if(word.length() >= 4)
		{
			return true;
		}
		return false;	
	}
	
	private String[] spiltSentence(String sentence)
	{
		String[] words = sentence.split("\\s+");
		for (int i = 0; i < words.length; i++) 
		{
		    words[i] = words[i].replaceAll("[^\\w]", "").toLowerCase();
		}
		return words;
	}
	
	private static Map<String, Integer> sortByComparator(Map<String, Integer> unsortMap, final boolean order)
    {
        List<Entry<String, Integer>> list = new LinkedList<Entry<String, Integer>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String, Integer>>()
        {
            public int compare(Entry<String, Integer> o1,
                    Entry<String, Integer> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Entry<String, Integer> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }	
}
