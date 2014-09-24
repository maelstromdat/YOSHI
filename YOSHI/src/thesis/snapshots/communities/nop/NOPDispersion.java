package thesis.snapshots.communities.nop;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.egit.github.core.Contributor;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import thesis.snapshots.communities.data.Community;
import thesis.snapshots.communities.utils.GeoCoordinate;
import thesis.snapshots.communities.utils.GeoService;
import thesis.snapshots.communities.utils.Statistics;

public class NOPDispersion
{
	private final String[] countries = {"Argentina", "Australia", "Austria", "Belgium", "Brazil", "Chile", "China", "Colombia", "Costa Rica", "Czech Republic", "Denmark", "Ecuador", "Egypt", "El Salvador", "Ethiopia", "Finland", "France", "Germany", "Ghana", "Greece", "Guatemala", "Hong Kong", "Hungary", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel", "Italy", "Jamaica", "Japan", "Kenya", "Kuwait", "Lebanon", "Libya", "Malaysia", "Mexico", "Netherlands", "New Zealand", "Nigeria", "Norway", "Pakistan", "Panama", "Peru", "Philippines", "Poland", "Portugal", "Saudi Arabia", "Sierra Leone", "Singapore", "South Africa", "South Korea", "Spain", "Sweden", "Switzerland", "Taiwan", "Tanzania", "Thailand", "Turkey", "United Arab Emirates", "United Kingdom", "United States", "Uruguay", "Venezuela", "Zambia", "San Francisco", "USA", "California", "Boston", "Texas", "Atlanta", "Vancouver", "Mountain View", "Chicago", "Seattle", "Menlo Park"};
	private final int[] PDI = {49, 36, 11, 65, 69, 63, 80, 67, 35, 57, 18, 78, 80, 66, 64, 33, 68, 35, 77, 60, 95, 68, 46, 77, 78, 58, 80, 28, 13, 50, 45, 54, 64, 80, 80, 80, 104, 81, 38, 22, 77, 31, 55, 95, 64, 94, 68, 63, 80, 77, 74, 49, 60, 57, 31, 34, 58, 64, 64, 66, 80, 35, 40, 61, 81, 64, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40};
	private final int[] IDV = {46, 90, 55, 75, 38, 23, 20, 13, 15, 58, 74, 8, 38, 19, 27, 63, 71, 67, 20, 35, 6, 25, 55, 48, 14, 41, 38, 70, 54, 76, 39, 46, 27, 38, 38, 38, 26, 30, 80, 79, 20, 69, 14, 11, 16, 32, 60, 27, 38, 20, 20, 65, 18, 51, 71, 68, 17, 27, 20, 37, 38, 89, 91, 36, 12, 27, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91, 91};
	private final int[] MAS = {56, 61, 79, 54, 49, 28, 66, 64, 21, 57, 16, 63, 52, 40, 41, 26, 43, 66, 46, 57, 37, 57, 88, 56, 46, 43, 52, 68, 47, 70, 68, 95, 41, 52, 52, 52, 50, 69, 14, 58, 46, 8, 50, 44, 42, 64, 64, 31, 52, 46, 48, 63, 39, 42, 5, 70, 45, 41, 34, 45, 52, 66, 62, 38, 73, 41, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62, 62};
	private final int[] UAI = {86, 51, 70, 94, 76, 86, 40, 80, 86, 74, 23, 67, 68, 94, 52, 59, 86, 65, 54, 112, 101, 29, 82, 40, 48, 59, 68, 35, 81, 75, 13, 92, 52, 68, 68, 68, 36, 82, 53, 49, 54, 50, 70, 86, 87, 44, 93, 104, 68, 54, 8, 49, 85, 86, 29, 58, 69, 52, 64, 85, 68, 35, 46, 100, 76, 52, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46, 46};
	private final int[] LTO = {0, 31, 0, 0, 65, 0, 118, 0, 0, 0, 0, 0, 0, 0, 25, 0, 0, 31, 16, 0, 0, 96, 0, 61, 0, 0, 0, 0, 0, 0, 0, 80, 25, 0, 0, 0, 0, 0, 44, 30, 16, 20, 0, 0, 0, 19, 0, 0, 0, 16, 48, 0, 75, 0, 33, 0, 87, 25, 56, 0, 0, 25, 29, 0, 0, 25, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29, 29};

	Community community;
	GitHubClient client;
	
	private UserService userService;
	private RepositoryService repoService;	
	private GeoService geoService;

	public NOPDispersion(Community community, GitHubClient client)
	{
		this.community = community;		
		this.client = client;
		
		userService = new UserService(client);
		repoService = new RepositoryService(client);
		geoService = new GeoService();
	}
	
	private List<User> getCollabContri() throws IOException
	{
		CollaboratorService collaboratorService = new CollaboratorService(client);
		List<User> collaborators = collaboratorService.getCollaborators(community.getRepository()); 
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
		
		for(Contributor contributor : contributors)
		{
			if(contributor.getLogin() != null)
			{
				User user = userService.getUser(contributor.getLogin());
				collaborators.add(user);
			}
		}
		return collaborators;
	}
	
	public Map<String,String[]> dispersionMap() throws IOException
	{
		Map<String,String[]> map = new HashMap<String,String[]>();
		RepositoryService repoService = new RepositoryService(client);
		UserService userService = new UserService(client);
		List<Contributor> contributors = repoService.getContributors(community.getRepository(), false);
	
		/*Catch exception in Community getRepository
Exception in thread "main" java.lang.IllegalArgumentException: Repository provider cannot be null
	at org.eclipse.egit.github.core.service.GitHubService.getId(GitHubService.java:184)
	at org.eclipse.egit.github.core.service.RepositoryService.getContributors(RepositoryService.java:845)
	at thesis.snapshots.communities.nop.NOPDispersion.dispersionMap(NOPDispersion.java:71)
	at thesis.snapshots.communities.data.CommunitiesData.networkOfPracticeDispersionMap(CommunitiesData.java:54)
	at thesis.snapshots.communities.data.CommunitiesData.main(CommunitiesData.java:207)*/
		
		for(Contributor contributor : contributors)
		{
			User user = userService.getUser(contributor.getLogin());
			if(user.getLocation() != null)
			{
				GeoCoordinate geoCoordinate = geoService.getLongitudeLatitude(user.getLocation());
				if(geoCoordinate != null)
				{
					double value = geoCoordinate.getLongitude();
					double fractionalPart = value % 1;
					double integralPart = value - fractionalPart;
					
					int intIntegralPart = (int)integralPart;
					int intFractionalPart = (int)(fractionalPart*100);
					
					// Longitude : west(-) east(+) 
					String longitude = Math.abs(intIntegralPart)+" "+Math.abs(intFractionalPart)+" ";
					if(intIntegralPart < 0)
					{
						longitude+="W";
					}
					else
					{
						longitude+="E";
					}
					
					value = geoCoordinate.getLatitude();
					fractionalPart = value % 1;
					integralPart = value - fractionalPart;
					
					intIntegralPart = (int)integralPart;
					intFractionalPart = (int)(fractionalPart*100);
					
					// Longitude : south(-) north(+) 
					String latitude = Math.abs(intIntegralPart)+" "+Math.abs(intFractionalPart)+" ";
					if(intIntegralPart < 0)
					{
						latitude+="S";
					}
					else
					{
						latitude+="N";
					}					
						
					map.put(user.getLocation(), new String[] {latitude, longitude});
					//map.put("", new String[] {latitude, longitude});
				}
			}	
		}
		return map;		
	}
	
	public double[] nopMembersDistance() throws IOException
	{
		List<GeoCoordinate> locations = new ArrayList<GeoCoordinate>();		
		List<User> collaborators = getCollabContri(); 

		for(User user : collaborators)
		{
			if(user.getLocation() != null)
			{
				GeoCoordinate geoCoordinate = geoService.getLongitudeLatitude(user.getLocation());
				if(geoCoordinate != null)
				{
					locations.add(geoCoordinate);
				}
			}	
		}
		
		double[] membersDispersion = membersDispersion(locations);
		return membersDispersion;
	}
	
	private double[] membersDispersion(List<GeoCoordinate> locations)
	{		
		// distance in km as a double
		double[] dispersion = new double[locations.size()];
		
		for(int i=0; i<locations.size(); i++)
		{
			GeoCoordinate locationA = locations.get(i);
			double mediumDistance = 0;
			for(int j=0; j<locations.size(); j++)
			{
				if(i!=j)
				{
					GeoCoordinate locationB = locations.get(j);
					mediumDistance+=locationA.sphericalDistance(locationB);
				}
			}
			mediumDistance/=(locations.size()-1);
			// converted to km
			dispersion[i] = mediumDistance / 1000;
		}
		
		
		for(GeoCoordinate location : locations)	{ System.out.println(location.getLatitude()+" "+location.getLongitude()); }
		System.out.println(dispersion);
		
		return dispersion;
	}
	
	public double nopMembersCulturalDistance() throws IOException
	{
		List<String> locations = new ArrayList<String>();		
		List<User> collaborators = getCollabContri();	
		List<Integer> devPDI = new ArrayList<Integer>();
		List<Integer> devIDV = new ArrayList<Integer>();
		List<Integer> devMAS = new ArrayList<Integer>();
		List<Integer> devUAI = new ArrayList<Integer>();
				
		for(User user : collaborators)
		{
			if(user.getLocation() != null)
			{
				String location = user.getLocation();
				for(int i = 0; i < countries.length; i++)
				{
					if(location.toLowerCase().contains(countries[i].toLowerCase()))
					{
						locations.add(countries[i]);
						devPDI.add(PDI[i]);
						devIDV.add(IDV[i]);
						devMAS.add(MAS[i]);
						devUAI.add(UAI[i]);
						break;
					}
				}				
			}	
		}
		
		double[] valuesPDI = new double[locations.size()]; 
		double[] valuesIDV = new double[locations.size()];
		double[] valuesMAS = new double[locations.size()];
		double[] valuesUAI = new double[locations.size()];
		for(int i = 0; i < locations.size(); i++)
		{
			valuesPDI[i] = devPDI.get(i);
			valuesIDV[i] = devIDV.get(i);
			valuesMAS[i] = devMAS.get(i);
			valuesUAI[i] = devUAI.get(i);
		}
		
		double pdi = Statistics.getStdDev(valuesPDI);
		double idv = Statistics.getStdDev(valuesIDV);
		double mas = Statistics.getStdDev(valuesMAS);
		double uai = Statistics.getStdDev(valuesUAI);
		double stddev = (pdi + idv + mas + uai) / 4.0;
		return stddev;
	}		
}
