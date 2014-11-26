package thesis.snapshots.communities.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import thesis.snapshots.communities.quality.Qualities;
import thesis.snapshots.communities.structure.SocialStructure;
import thesis.snapshots.communities.utils.Pair;

public class Main
{
	List<Pair<String, String>> repositories;
	String filenameIn;
	String filenameOut;
	
	public final static int MAX_LIMIT = 5000;
	
	public Main(String filenameIn, String filenameOut)
	{
		this.filenameIn = filenameIn;
		this.filenameOut = filenameOut;
	}
	
	public void readURLs()
	{
		repositories = new ArrayList<Pair<String,String>>();
		BufferedReader br = null;
		 
		try {
 
			String sCurrentLine;
 
			br = new BufferedReader(new FileReader(filenameIn));
			br.readLine();
 
			while ((sCurrentLine = br.readLine()) != null) 
			{
				String[] words = spiltLine(sCurrentLine);
				repositories.add(new Pair<String, String>(words[0], words[1]));
			}
 
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		finally {
			try {
				if (br != null)br.close();
			} 
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void writeData(String data)
	{
		try {
			File file = new File(filenameOut);
 
			// if file doesn't exist, then create it
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file, true);
			
			BufferedWriter bw = new BufferedWriter(fw);			
	        String NEWLINE = System.getProperty("line.separator");

			String content = data+NEWLINE;
			System.out.println("Writing results and finalising...");
			bw.write(content);
			bw.close();
			fw.close();
			System.out.println("Done.");
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public void compute() throws IOException
	{
		for(Pair<String, String> repo : repositories)
		{
			System.out.println("I'm attempting to compute communities data for " + repo.getFirst() + " and " + repo.getSecond());
			compute(repo.getFirst(), repo.getSecond());
		}
	}
	public void compute(String repoName, String repoOwner) throws IOException
	{
		CommunitiesData commData = new CommunitiesData(repoOwner, repoName);
		Community community = commData.getCommunity();
		System.out.println("I'm checking if the community has data that I can use...");
		boolean cons = community.hasURL() && community.hasMilestones();
		System.out.println("Done... my assessment on this repo is: " + cons);
		if(cons)
			
		{
			System.out.println("I'm currently investigating on "+repoName+" / "+repoOwner);
			//these lines selectively gather intelligence on the communities and then all results are wrapped in line within data, a text file
			String dataCommunity = commData.communitiesData();			
			
			Qualities qualities = new Qualities(repoOwner, repoName);
			String dataQuality = qualities.getQualities();
			
			String data = repoOwner+"/"+repoName+", "+dataCommunity+", "+dataQuality+"\n";
			System.out.println(data);
			writeData(data);
		}
		System.out.println("I'm done investigating on "+repoName+" / "+repoOwner);
	}
	
	private String[] spiltLine(String line)
	{
		String[] words = line.split(",");
		return words;
	}
	
	public static void main(String[] args) throws IOException
	{
		System.out.println("Hello! I'm YOSHI! I was born to study organizational and social structures behind software communities...");
		System.out.println("My GitHub Client Authorization token is: 71df10f4a580add22f3d62d9bcd08c2a3ac8fdf4");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("During my run I will explain what characteristics I'm computing for the repositories that you provided...");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
		Main process = new Main("src/thesis/snapshots/communities/data/repos.csv", 
				               "src/thesis/snapshots/communities/data/YOSHI_data.csv");
		
		System.out.println("I'm attempting to compute on repos.csv and data shall be saved in YOSHI_data.csv");
		process.readURLs();
		process.compute();
	}
}
