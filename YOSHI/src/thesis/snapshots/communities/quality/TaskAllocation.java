package thesis.snapshots.communities.quality;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.egit.github.core.Commit;
import org.eclipse.egit.github.core.CommitFile;
import org.eclipse.egit.github.core.CommitUser;
import org.eclipse.egit.github.core.Reference;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.Tree;
import org.eclipse.egit.github.core.TreeEntry;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.PageIterator;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.DataService;
import org.eclipse.egit.github.core.service.RepositoryService;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.data.Main;
import thesis.snapshots.communities.nop.NetworkOfPractice;
import thesis.snapshots.communities.structure.MembersGraph;
import thesis.snapshots.communities.utils.Statistics;

public class TaskAllocation
{
	GitHubClient client;
	RepositoryService repoService;
	
	private Map<String, Integer> userCommits;
	private Map<String, Set<String>> fileChangedUsers;
	private Map<String, Set<String>> userCollaborationFiles;
	private Map<String, Long> commiterLongevity;
	private Set<String> uniqueCommiters;
	
	Community community;
	NetworkOfPractice networkOfPractice;
	
	String user;
	String repository;
	
	private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
	
	public TaskAllocation(CommunitiesData communitiesData, GitHubClient client, String user, String repository)
	{
		this.community = communitiesData.getCommunity();
		this.client = client;
		this.networkOfPractice = communitiesData.getNetworkOfPractice();
		
		this.user = user;
		this.repository = repository;
	}	

	/**
	 * commits / milestone
	 */
	public void commitsEvolution() throws IOException
	{
		collaborationFiles();
		
		Repository repo = community.getRepository();
		CommitService commitService = new CommitService(client);
		PageIterator<RepositoryCommit> pageCommits = commitService.pageCommits(repo);
		Map<String, Date> commiterStartDate = new HashMap<String, Date>();
		Map<String, Date> commiterStopDate = new HashMap<String, Date>();

		userCommits = new HashMap<String, Integer>();
		commiterLongevity = new HashMap<String, Long>();		
		
		int totalCommits = 0;
		
		while(pageCommits.hasNext())
		{
			try
			{
				Collection<RepositoryCommit> collectionCommits = pageCommits.next();
				
				for(RepositoryCommit commit : collectionCommits)
				{
					if(commit.getCommitter().getLogin() != null)
					{
						String commiter = commit.getCommitter().getLogin();
						Date commitDate = commit.getCommit().getAuthor().getDate();		
						
						if(commiter != null)
						{
							int countUserCommits = 1;
							if(userCommits.containsKey(commiter))
							{
								countUserCommits+=userCommits.get(commiter);
								
								Date startDate = commiterStartDate.get(commiter);
								Date stopDate = commiterStopDate.get(commiter);							
								if(startDate.getTime() > commitDate.getTime())
								{
									commiterStartDate.put(commiter, commitDate);
								}							
								if(stopDate.getTime() < commitDate.getTime())
								{
									commiterStopDate.put(commiter, commitDate);
								}
							}
							else
							{
								commiterStartDate.put(commiter, commitDate);
								commiterStopDate.put(commiter, commitDate);
							}
	
							userCommits.put(commiter, countUserCommits);		
						}
					}
				}
				
				totalCommits+=collectionCommits.size();
				// FIXME
				if(totalCommits > Main.MAX_LIMIT)
				{
					break;
				}
			}
			catch(Exception e)
			{				
				//e.printStackTrace();
			}
		}
		
		for(String user : userCommits.keySet())
		{
			long stopDate = commiterStopDate.get(user).getTime();
			long startDate = commiterStartDate.get(user).getTime();
			long difference = stopDate - startDate;
			
			long countDays = difference / MILLIS_IN_DAY;
			//System.out.println(user+" "+commiterStartDate.get(user)+" "+commiterStopDate.get(user)+" "+countDays);
			commiterLongevity.put(user, countDays);			
		}
	}
	
	public double[] userCommits() throws IOException
	{
		if(userCommits == null)
		{
			commitsEvolution();
		}
		
		double[] commits = new double[userCommits.size()];
		int i = 0;
		for(String user : userCommits.keySet())
		{
			commits[i] = userCommits.get(user);
			i++;
		}
		return commits;
	}
	
	public double[] fileChangedUsers() throws IOException
	{
		if(fileChangedUsers == null)
		{
			commitsEvolution();
		}
		
		double[] users = new double[fileChangedUsers.size()];
		int i = 0;
		for(String filename : fileChangedUsers.keySet())
		{
			users[i] = fileChangedUsers.get(filename).size();
			i++;
		}
		return users;		
	}
	
	public double[] userCollaborationFiles() throws IOException
	{
		if(userCollaborationFiles == null)
		{
			commitsEvolution();
		}
		
		double[] collaboration = new double[userCollaborationFiles.size()];
		int i = 0;
		for(String filename : userCollaborationFiles.keySet())
		{
			collaboration[i] = userCollaborationFiles.get(filename).size();
			i++;
		}
		return collaboration;				
	}
	
	public double[] commiterLongivity() throws IOException
	{
		if(commiterLongevity == null)
		{
			commitsEvolution();
		}		
		
		double[] longivity = new double[userCommits.size()];
		int i = 0;
		for(String user : userCommits.keySet())
		{
			longivity[i] = commiterLongevity.get(user);
			i++;
		}
		return longivity;
	}
	
	public double taUserCommits() throws IOException
	{
		double[] values = userCommits();
		double avgUserCommits = Statistics.getMean(values);
		return avgUserCommits;
	}
	
	public double taFileChangedUsers() throws IOException
	{
		double[] values = fileChangedUsers();
		double avgFileContributors = Statistics.getMean(values);
		return avgFileContributors;
	}
	
	public double taUserCollaborationFiles() throws IOException
	{
		double[] values = userCollaborationFiles();
		double avgUserCollaborationFiles = Statistics.getMean(values);
		avgUserCollaborationFiles = avgUserCollaborationFiles / uniqueCommiters.size();
		return avgUserCollaborationFiles;
	}
	
	public double taCommiterLongivity() throws IOException
	{
		double[] values = commiterLongivity();
		double avgCommiterLonvigity = Statistics.getMean(values);
		return avgCommiterLonvigity;
	}
	
	public int activeMembers() throws IOException
	{
		int activeMembers = networkOfPractice.size.nopActiveMembers();
		return activeMembers;
	}
	
	public void collaborationFiles() throws IOException
	{
		Repository repo = community.getRepository();
		CommitService commitService = new CommitService(client);
		ContentsService contentsService = new ContentsService(client);

		fileChangedUsers = new HashMap<String, Set<String>>();
		userCollaborationFiles = new HashMap<String, Set<String>>();
		uniqueCommiters = new HashSet<String>();
		
		List<RepositoryContents> repositoryContents = contentsService.getContents(repo);
		DataService dataService = new DataService(client);
		
		int totalCommits = 0;
		
		for(RepositoryContents content : repositoryContents)
		{
			String sha = content.getSha(); 
			String type = content.getType();
			String directory = content.getPath();
			
			if(type.equals("dir"))
			{
				Tree tree = dataService.getTree(repo, sha, true);
				List<TreeEntry> treeEntries = tree.getTree();
				
				for(TreeEntry treeEntry : treeEntries)
				{
					if(treeEntry.getType().equals("blob"))
					{
						String path = directory+"/"+treeEntry.getPath(); 
						PageIterator<RepositoryCommit> pageCommits = commitService.pageCommits(repo, null, path);
						
						while(pageCommits.hasNext())
						{
							try
							{
								Collection<RepositoryCommit> collectionCommits = pageCommits.next();
								
								for(RepositoryCommit commit : collectionCommits)
								{
									CommitUser commitUser = commit.getCommit().getCommitter();
									String commiter = commitUser.getName();
									
									if(commiter != null)
									{
										Set<String> contributors;
		
										if(fileChangedUsers.containsKey(path))
										{
											contributors = fileChangedUsers.get(path);
										}
										else
										{
											contributors = new HashSet<String>();
										}
												
										contributors.add(commiter);
										fileChangedUsers.put(path, contributors);
										uniqueCommiters.add(commiter);
									}
								}
								
								totalCommits+=collectionCommits.size();
								// FIXME
								if(totalCommits > Main.MAX_LIMIT)
								{
									break;
								}
		
							}				
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}
					}
				}
			}
		}
		
		for(String file : fileChangedUsers.keySet())
		{
			Object[] contributors = fileChangedUsers.get(file).toArray();
			
			for(int i = 0; i < contributors.length - 1; i++)
			{
				String contributorA = contributors[i].toString();
				Set<String> fileCollaborators = new HashSet<String>();
				Set<String> setA;
				
				if(userCollaborationFiles.containsKey(contributorA))
				{
					setA = userCollaborationFiles.get(contributorA);
				}
				else
				{
					setA = new HashSet<String>();
				}
				
				for(int j = i + 1; j < contributors.length; j++)
				{
					String contributorB = contributors[j].toString();
					Set<String> setB;
					
					if(userCollaborationFiles.containsKey(contributorB))
					{
						setB = userCollaborationFiles.get(contributorB);
					}
					else
					{
						setB = new HashSet<String>();
					}
					
					setB.add(contributorA);
					userCollaborationFiles.put(contributorB, setB);
					
					fileCollaborators.add(contributorB);	
				}
				
				setA.addAll(fileCollaborators);
				userCollaborationFiles.put(contributorA, setA);
			}
		}
		
		/*
		for(String file : fileChangedUsers.keySet())
		{
			Set<String> contributors = fileChangedUsers.get(file);
			System.out.println(file);
			for(String user : contributors)
			{
				System.out.print(user+" ");
			}
			System.out.println();
		}
		*/
	}
	
	public void writeData1()
	{
		try {
			File file = new File("help1");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw);			
	        String NEWLINE = System.getProperty("line.separator");

			for(String filename : fileChangedUsers.keySet())
			{
				Set<String> contributors = fileChangedUsers.get(filename);
				String content = contributors.size()+NEWLINE;
				bw.write(content);
			}
	        
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
	
	public void writeData3()
	{
		try {
			File file = new File("help3");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw);			
	        String NEWLINE = System.getProperty("line.separator");

	    	for(String user : commiterLongevity.keySet())
	    	{
				String content = commiterLongevity.get(user)+NEWLINE;
				bw.write(content);
			}
	        
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void writeData4()
	{
		try {
			File file = new File("help4");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw);			
	        String NEWLINE = System.getProperty("line.separator");

	    	for(String user : userCommits.keySet())
	    	{
				String content = userCommits.get(user)+NEWLINE;
				bw.write(content);
			}
	        
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}	
	
	
	public void writeData2()
	{
		try {
			File file = new File("help2");
 
			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw);			
	        String NEWLINE = System.getProperty("line.separator");

			for(String filename : userCollaborationFiles.keySet())
			{
				Set<String> contributors = userCollaborationFiles.get(filename);
				String content = contributors.size()+NEWLINE;
				bw.write(content);
			}
	        
			bw.close();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}		
}
