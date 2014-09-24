package thesis.snapshots.communities.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.client.GitHubClient;

import thesis.snapshots.communities.data.CommunitiesData;
import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.ic.ICEngagement;
import thesis.snapshots.communities.nop.NOPSimilarity;
import thesis.snapshots.communities.quality.Qualities;

import com.google.gson.Gson;
import com.sandeep.visual.data.Student;

@WebServlet("/NOPServlet")
public class NOPServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public NOPServlet() {
		super();			
	}

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		//List<SimilarityLanguage> listOfStudent = getSimilarityLanguage();
		//List<SimilarityLanguage> listOfStudent = getMonthlyComments();
		List<SimilarityLanguage> listOfStudent = getMonthlyEvents();
		Gson gson = new Gson();
		String jsonString = gson.toJson(listOfStudent);
		response.setContentType("application/json");
		response.getWriter().write(jsonString);
	}

	private List<SimilarityLanguage> getSimilarityLanguage() throws IOException {
		//OAuthentication
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token("4e8c429a9ac38957f6d061bdb1ed4ba1e595209b");
		Community community = new Community("liferay", "liferay-portal", client);		
		NOPSimilarity similarity = new NOPSimilarity(community, client);
		Map<String, Integer> langFrequency = similarity.membersSimilarity();		
		List<SimilarityLanguage> listOfLangs = new ArrayList<SimilarityLanguage>();
		
		for(String language : langFrequency.keySet())
		{
			SimilarityLanguage simLang = new SimilarityLanguage();
			simLang.setName(language);
			simLang.setCount(langFrequency.get(language));
			listOfLangs.add(simLang);
		}

		return listOfLangs;
	}
	
	private List<SimilarityLanguage> getMonthlyComments() throws IOException {
		//OAuthentication
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token("4e8c429a9ac38957f6d061bdb1ed4ba1e595209b");
		Community community = new Community("griddynamics", "yhadoop-common", client);
		
		ICEngagement engagement = new ICEngagement(community, client);
		Map<Date, Integer> monthlyComments = engagement.icGraphHighEngagement();		
		List<SimilarityLanguage> listOfMonthlyComm = new ArrayList<SimilarityLanguage>();
		
		for(Date date : monthlyComments.keySet())
		{
			SimilarityLanguage data = new SimilarityLanguage();
			String name = date.getMonth()+"/"+date.getYear();
			data.setName(name);
			data.setCount(monthlyComments.get(date));
			listOfMonthlyComm.add(data);
		}
		return listOfMonthlyComm;
	}	
	
	private List<SimilarityLanguage> getMonthlyEvents() throws IOException {
		Qualities qualities = new Qualities("twbs", "bootstrap");

		Map<Date, Integer> monthlyEvents = qualities.prjCharacteristics.pcMonthlyEventsGraph();
		List<SimilarityLanguage> listOfMonthlyEvents = new ArrayList<SimilarityLanguage>();
		
		for(Date date : monthlyEvents.keySet())
		{
			SimilarityLanguage data = new SimilarityLanguage();
			String name = date.getMonth()+"/"+date.getYear();
			data.setName(name);
			data.setCount(monthlyEvents.get(date));
			listOfMonthlyEvents.add(data);
		}
		return listOfMonthlyEvents;
	}		
}
