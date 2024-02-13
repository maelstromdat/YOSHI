using CsvHelper;
using CsvHelper.Configuration;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.IO;
using YOSHI.CommunityData;
using YOSHI.DataRetrieverNS;
using YOSHI.DataRetrieverNS.Geocoding;

namespace YOSHI
{
    /// <summary>
    /// This class is responsible for the IO-operations of YOSHI. 
    /// </summary>
    public static class IOModule
    {
        private static string OutDirFile;      // The output directory including filename

        /// <summary>
        /// This method is used to guide the user in inputting the input directory, input filename, outfput directory 
        /// and the output filename.
        /// </summary>
        /// <exception cref="IOException">Thrown when something goes wrong while reading the input or when 
        /// writing to the output file.</exception>
        public static List<Community> TakeInput()
        {
            try
            {
                // Take and validate the input file
                string inFile;
                do
                {
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    Console.WriteLine("Please enter the absolute directory of the input file, including filename and " +
                        "its extension.");
                    Console.ResetColor();
                    inFile = Console.ReadLine();
                }
                while (!File.Exists(inFile));

                string outDir;
                do
                {
                    // Take the output directory
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    Console.WriteLine("Please enter an existing absolute directory for the output file.");
                    Console.ResetColor();
                    outDir = @"" + Console.ReadLine();
                }
                while (!Directory.Exists(outDir));

                // Take and validate the input specifying the output file 
                do
                {
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    Console.WriteLine("Please enter the filename of the output file. Do not include an extension, " +
                        "as its extension will be \".csv\"");
                    Console.ResetColor();
                    string outFilename = Console.ReadLine();

                    OutDirFile = outDir + '\\' + outFilename + ".csv";
                }
                while (File.Exists(OutDirFile));

                // Create the output file and write the headers
                using FileStream stream = File.Open(OutDirFile, FileMode.CreateNew);
                using StreamWriter writer = new StreamWriter(stream);
                using CsvWriter csv = new CsvWriter(writer, CultureInfo.InvariantCulture);
                csv.Context.RegisterClassMap<CommunityMap>();
                csv.WriteHeader<Community>();
                csv.NextRecord();

                // https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-dev-center-help/understanding-bing-maps-transactions
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine("Windows App, Non-profit, and Education keys can make 50,000 requests per 24 hour period.");
                Console.WriteLine("Please enter the number of Bing Maps requests left.");
                Console.ResetColor();
                int bingRequestsLeft = Convert.ToInt32(Console.ReadLine());
                GeoService.BingRequestsLeft = bingRequestsLeft;

                // Set the enddate of the time window, it defaults to use midnight UTC time.
                // It is possible to enter a specific time, but this has not been tested.
                DateTimeOffset endDate;
                Console.ForegroundColor = ConsoleColor.DarkGray;
                Console.WriteLine("Enter end date of time window (YYYY-MM-DD) in UTC");
                Console.ResetColor();
                while (!DateTimeOffset.TryParseExact(Console.ReadLine(),"yyyy-MM-dd", CultureInfo.InvariantCulture, DateTimeStyles.AssumeUniversal, out endDate))
                {
                    Console.ForegroundColor = ConsoleColor.DarkGray;
                    Console.WriteLine("Invalid date");
                    Console.WriteLine("Enter end date of time window (YYYY-MM-DD) in UTC");
                    Console.ResetColor();
                }
                // Make sure that it is counted as UTC datetime and not as a local time
                Filters.SetTimeWindow(endDate);

                return ReadFile(inFile);
            }
            catch (IOException e)
            {
                throw new IOException("Failed to read input or to write headers to output file", e);
            }
        }

        /// <summary>
        /// A method used to read the file named after the value stored with the input filename (InFilename) at the 
        /// specified input directory (InDir).
        /// </summary>
        /// <returns>A list of communities storing just the repo owner and repo name.</returns>
        /// <exception cref="IOException">Thrown when something goes wrong while reading the input file.</exception>
        private static List<Community> ReadFile(string inFile)
        {
            List<Community> communities = new List<Community>();
            try
            {
                using StreamReader reader = new StreamReader(inFile);
                using CsvReader csv = new CsvReader(reader, CultureInfo.InvariantCulture);
                csv.Read();
                csv.ReadHeader();
                while (csv.Read())
                {
                    // The CSV file needs to have "RepoName" and "RepoOwner" as headers
                    Community community = new Community(csv.GetField("RepoOwner"), csv.GetField("RepoName"));
                    communities.Add(community);
                }
            }
            catch (IOException e)
            {
                throw new IOException("Something went wrong while reading the input file.", e);
            }

            return communities;
        }

        /// <summary>
        /// A method used to write community data to a file named after the value stored with the output filename 
        /// (OutFilename) at the specified output directory (OutDir).
        /// </summary>
        public static void WriteToFile(Community community)
        {
            // Append to the file.
            CsvConfiguration config = new CsvConfiguration(CultureInfo.InvariantCulture);
            using FileStream stream = File.Open(OutDirFile, FileMode.Append);
            using StreamWriter writer = new StreamWriter(stream);
            using CsvWriter csv = new CsvWriter(writer, config);
            csv.Context.RegisterClassMap<CommunityMap>();
            csv.WriteRecord(community);
            csv.NextRecord();
        }

        /// <summary>
        /// This class maps the structure of the output, i.e., all community data that will be written to a CSV format.
        /// Each Map function represents a field in the CSV-file.
        /// </summary>
        public sealed class CommunityMap : ClassMap<Community>
        {
            public CommunityMap()
            {
                this.Map(m => m.RepoOwner).Index(0);
                this.Map(m => m.RepoName).Index(1);
                this.Map(m => m.Data.FirstCommitHash).Index(2);
                this.Map(m => m.Data.LastCommitHash).Index(3);
                this.Map(m => m.Data.FirstCommitDateTime).Name("StartTime").Index(4);
                this.Map(m => m.Data.LastCommitDateTime).Name("EndTime").Index(5);

                // Report number of members and the number of locations known, as well as the number of hofstede locations known.
                // Then we can decide afterward whether we exclude certain communities, if we have too little information.
                this.Map(m => m.Data.Members.Count).Name("NrMembers").Index(12);
                this.Map(m => m.Data.Coordinates.Count).Name("NrLocations").Index(15);
                this.Map(m => m.Data.Countries.Count).Name("NrHiCountries").Index(17);
                this.Map(m => m.Data.Contributors).Name("NrContributors").Index(18);
                this.Map(m => m.Data.Collaborators).Name("NrCollaborators").Index(19);

                this.Map(m => m.Metrics.Structure.CommonProjects).Index(20);
                this.Map(m => m.Metrics.Structure.Followers).Index(30);
                this.Map(m => m.Metrics.Structure.PullReqInteraction).Index(40);

                this.Map(m => m.Metrics.Dispersion.VarianceGeographicalDistance).Index(50);
                this.Map(m => m.Metrics.Dispersion.VarianceHofstedeCulturalDistance).Index(60);

                this.Map(m => m.Metrics.Formality.MeanMembershipType).Index(70);
                this.Map(m => m.Metrics.Formality.Milestones).Index(80);
                this.Map(m => m.Metrics.Formality.Lifetime).Index(90);

                this.Map(m => m.Metrics.Engagement.MedianNrCommentsPerPullReq).Index(100);
                this.Map(m => m.Metrics.Engagement.MedianMonthlyPullCommitCommentsDistribution).Index(110);
                this.Map(m => m.Metrics.Engagement.MedianActiveMember).Index(120);
                this.Map(m => m.Metrics.Engagement.MedianWatcher).Index(130);
                this.Map(m => m.Metrics.Engagement.MedianStargazer).Index(140);
                this.Map(m => m.Metrics.Engagement.MedianMonthlyCommitDistribution).Index(150);
                this.Map(m => m.Metrics.Engagement.MedianMonthlyFileCollabDistribution).Index(160);

                this.Map(m => m.Metrics.Longevity.MeanCommitterLongevity).Index(170);

                //this.Map(m => m.Metrics.Cohesion.Followers).Index(180);

                this.Map(m => m.Characteristics.Structure).Index(190);
                this.Map(m => m.Characteristics.Dispersion).Index(200);
                this.Map(m => m.Characteristics.Formality).Index(210);
                this.Map(m => m.Characteristics.Engagement).Index(220);
                this.Map(m => m.Characteristics.Longevity).Index(230);
                //this.Map(m => m.Characteristics.Cohesion).Index(240);

                this.Map(m => m.Pattern.SN).Index(250);
                this.Map(m => m.Pattern.FG).Index(260);
                this.Map(m => m.Pattern.PT).Index(270);
                //this.Map(m => m.Pattern.WorkGroup).Index(280);
                this.Map(m => m.Pattern.NoP).Index(290);
                this.Map(m => m.Pattern.IC).Index(300);
                this.Map(m => m.Pattern.FN).Index(310);
                this.Map(m => m.Pattern.IN).Index(320);
                this.Map(m => m.Pattern.CoP).Index(330);

                // EXTRA VARIABLES FOR COMPARIONS BETWEEN YOSHI AND YOSHI 2
                this.Map(m => m.Metrics.Dispersion.AverageGeographicalDistance).Index(340);
                this.Map(m => m.Metrics.Dispersion.AverageCulturalDispersion).Index(350);

                this.Map(m => m.Metrics.Formality.MeanMembershipTypeOld).Index(360);
                this.Map(m => m.Metrics.Formality.BuggedLifetimeMS).Index(365);

                this.Map(m => m.Metrics.Engagement.MedianCommitDistribution).Index(370);
                this.Map(m => m.Metrics.Engagement.MedianFileCollabDistribution).Index(380);
            }
        }
    }
}
