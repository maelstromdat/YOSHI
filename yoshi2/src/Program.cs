using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using YOSHI.CharacteristicProcessorNS;
using YOSHI.CommunityData;
using YOSHI.DataRetrieverNS;
using YOSHI.DataRetrieverNS.Geocoding;

namespace YOSHI
{
    /// <summary>
    /// This class is the main file of the revised YOSHI. This tool will use GitHub data to identify community patterns.
    /// It is based on YOSHI from the paper below. To achieve its purposes it will take input from a file, which can 
    /// contain multiple lines of "owner, repository" pairs. Then it uses this input to extract GitHub data using the 
    /// GitHub API: https://docs.github.com/en/rest
    /// Using the extracted data, YOSHI computes several metrics that are used to obtain numerical values for several 
    /// community characteristics. These characteristics are then used to identify a community's pattern.
    /// 
    /// The following paper provides a detailed explanation of community patterns, characteristics, and YOSHI:
    /// Authors:    D.A. Tamburri, F. Palomba, A. Serebrenik, and A. Zaidman
    /// Title:      Discovering community patterns in open - source: a systematic approach and its evaluation
    /// Journal:    Empir.Softw.Eng.
    /// Volume:     24
    /// Number:     3
    /// Pages:      1369--1417
    /// Year:       2019
    /// URL:        https://doi.org/10.1007/s10664-018-9659-9
    /// </summary>
    class Program
    {
        static async Task Main()
        {
            // Retrieve the communities through console input handled by the IOModule.
            List<Community> communities = IOModule.TakeInput();
            Dictionary<string, string> failedCommunities = new Dictionary<string, string>();

            foreach (Community community in communities)
            {
                try
                {
                    Console.WriteLine("------------------------------------------------"); // Line to distinguish between communities
                    Console.ForegroundColor = ConsoleColor.Green;
                    Console.WriteLine("Started processing community {0} from {1}. Time: {2}", community.RepoName, community.RepoOwner, DateTime.Now.ToString());
                    Console.ResetColor();

                    // Retrieving GitHub data needed to compute whether the community is valid (i.e., it has at least
                    // 100 commits (all time), it has at least 10 members active in the last 90 days, it has at least
                    // 1 milestone (all time), and it has enough location data to compute dispersion. 
                    Console.WriteLine("Retrieving GitHub data needed for checking validity...");
                    await DataRetriever.RetrieveDataAndCheckValidity(community);

                    // Retrieving GitHub data needed to compute whether the community exhibits a structure
                    Console.WriteLine("Retrieving GitHub data needed for computing structure...");
                    await DataRetriever.RetrieveStructureData(community);

                    Console.WriteLine("Computing community structure...");
                    CharacteristicProcessor.ComputeStructure(community);

                    // If the community exhibits a structure then:
                    if (community.Characteristics.Structure)
                    {
                        Console.WriteLine("Community exhibits a structure...");

                        // Miscellaneous characteristics are: dispersion, formality, cohesion, engagement, longevity
                        Console.WriteLine("Retrieving GitHub data needed for miscellaneous characteristics...");
                        await DataRetriever.RetrieveMiscellaneousData(community);

                        Console.WriteLine("Computing miscellaneous characteristics...");
                        CharacteristicProcessor.ComputeMiscellaneousCharacteristics(community);

                        Console.WriteLine("Determining community pattern...");
                        PatternProcessor.ComputePattern(community);
                    }
                    else
                    {
                        // The community exhibits no structure, hence we cannot compute a pattern. Thus we skip computing 
                        // all other characteristics.
                        throw new InvalidRepositoryException("This project does not exhibit a community structure.");
                    }

                    Console.WriteLine("Writing community data to file...");
                    IOModule.WriteToFile(community);

                    Console.ForegroundColor = ConsoleColor.Green;
                    Console.WriteLine("Finished processing community {0} from {1}. Time: {2}", community.RepoName, community.RepoOwner, DateTime.Now.ToString());
                    Console.ResetColor();
                }
                catch (GeocoderRateLimitException e)
                {
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine(e.Message);
                    Console.ResetColor();
                    failedCommunities.Add(community.RepoName, e.Message);
                    break;
                }
                catch (InvalidRepositoryException e)
                {
                    // Skip this repository if it is not valid
                    Console.ForegroundColor = ConsoleColor.Yellow;
                    Console.WriteLine("Community {0} from {1} is not valid", community.RepoName, community.RepoOwner);
                    Console.WriteLine("Exception: {0}. {1}", e.GetType(), e.Message);
                    Console.ResetColor();
                    failedCommunities.Add(community.RepoName, e.Message);
                    continue;
                }
                catch (Exception e)
                {
                    // We want to output the number of Bing Maps Requests left, since it can take hours for Bing Maps Requests to update
                    Console.ForegroundColor = ConsoleColor.Cyan;
                    Console.WriteLine("There are still {0} Bing Maps Requests left", GeoService.BingRequestsLeft);
                    Console.ResetColor();
                    Console.ForegroundColor = ConsoleColor.Red;
                    Console.WriteLine("Exception: {0}. {1}", e.GetType(), e.Message);
                    Console.ResetColor();
                    failedCommunities.Add(community.RepoName, e.Message);
                    continue;
                }
            }
            // We want to output the number of Bing Maps Requests left, since it can take hours for Bing Maps Requests to update
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.WriteLine("There are still {0} Bing Maps Requests left", GeoService.BingRequestsLeft);
            Console.ResetColor();

            // Make sure to output the communities that failed at the end to make them easily identifiable
            if (failedCommunities.Count > 0)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("The following communities failed due to exceptions:");
                foreach (KeyValuePair<string, string> failedCommunity in failedCommunities)
                {
                    Console.WriteLine("{0}, {1}", failedCommunity.Key, failedCommunity.Value);
                }
                Console.ResetColor();
            }

            // Prevent the console window from automatically closing after the main process is done running
            // TODO: Write the console log to a file
            Console.BackgroundColor = ConsoleColor.DarkGreen;
            Console.WriteLine("The application has finished processing the inputted communities.");
            Console.WriteLine("Press Enter to close this window . . .");
            Console.ResetColor();
            ConsoleKeyInfo key = Console.ReadKey();
            while (key.Key != ConsoleKey.Enter)
            {
                key = Console.ReadKey();
            };
        }
    }
}
