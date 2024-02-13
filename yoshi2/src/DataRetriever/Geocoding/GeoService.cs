using Geocoding;
using Geocoding.Microsoft;
using Octokit;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;

namespace YOSHI.DataRetrieverNS.Geocoding
{
    public static class GeoService
    {
        public static int BingRequestsLeft { get; set; } = 50000;
        private static readonly BingMapsGeocoder Geocoder =
            new BingMapsGeocoder(Environment.GetEnvironmentVariable("YOSHI_BingMapsKey"));

        /// <summary>
        /// A method that takes a list of users and computes the addresses for all members. Users that have not 
        /// specified their locations or cause exceptions are skipped.
        /// </summary>
        /// <param name="members">A list of members to retrieve the addresses from</param>
        /// <param name="repoName">The repository name, used in exception handling</param>
        /// <returns>A list of addresses for the passed list of members</returns>
        /// <exception cref="GeocoderRateLimitException">Thrown when the Bing Rate Limit is exceeded.</exception>
        /// <exception cref="BingGeocodingException">Thrown when Bing Geocoding could not successfully retrieve a location.</exception>
        public static async Task<(List<Location>, List<string>)> RetrieveMemberAddresses(List<User> members, string repoName)
        {
            List<Location> coordinates = new List<Location>();
            List<string> countries = new List<string>();

            // NOTE: We loop over all user objects instead of usernames to access location data
            foreach (User member in members)
            {
                // Retrieve the member's addresses
                try
                {
                    if (member.Location != null)
                    {
                        BingAddress address = await GetBingAddress(member.Location);
                        // EXTRA LOGGING FOR RETROACTIVE ANALYSIS
                        Console.WriteLine("GitHub Address: {0}, Coordinates: {1}, CountryRegion: {2}", member.Location, address.Coordinates.ToString(), address.CountryRegion);
                        coordinates.Add(address.Coordinates);
                        // Note: The ContainsKey method of the Hofstede dictionary has been adjusted to be case insensitive and 
                        // diacritic insensitive
                        if (HI.Hofstede.ContainsKey(address.CountryRegion))
                        {
                            countries.Add(address.CountryRegion);
                        }
                    }
                    // Note: We do not filter out all users that we do not have complete information from,
                    // it could filter out information too aggressively.
                }
                catch (BingGeocodingException e)
                {
                    // Continue with the next user if this user was causing an exception
                    Console.ForegroundColor = ConsoleColor.DarkYellow;
                    Console.WriteLine("Could not retrieve the location from {0} in repo {1}", member.Login, repoName);
                    Console.WriteLine(e.InnerException.Message);
                    Console.ResetColor();
                    continue;
                }
                catch (GeocoderRateLimitException)
                {
                    throw;
                }
            }
            return (coordinates, countries);
        }

        /// <summary>
        /// This method uses a Geocoding API to perform forward geocoding, i.e., enter an address and obtain Bing Address.
        /// 
        /// Bing Maps TOU: https://www.microsoft.com/en-us/maps/product/terms-april-2011
        /// </summary>
        /// <param name="githubLocation">The location of which we want the Bing Maps Address.</param>
        /// <returns>A BingAddress containing the longitude and latitude found from the given address.</returns>
        /// <exception cref="BingGeocodingException">Thrown when the returned status in MapLocationFinderResult is 
        /// anything but "Success".</exception>
        /// <exception cref="GeocoderRateLimitException">Thrown when the rate limit has been reached.</exception>
        private static async Task<BingAddress> GetBingAddress(string githubLocation)
        {
            if (BingRequestsLeft > 50) // Give ourselves a small buffer to not go over the limit.
            {
                BingRequestsLeft--;
                // Note: MapLocationFinder does not throw exceptions, instead it returns a status. 
                try
                {
                    IEnumerable<BingAddress> resultAddresses = await Geocoder.GeocodeAsync(githubLocation);
                    BingAddress result = resultAddresses.FirstOrDefault();
                    return result != null && result.CountryRegion != null
                        ? result
                        : throw new BingGeocodingException(new Exception("Result for address \"" + githubLocation + "\" is null"));
                }
                catch (BingGeocodingException)
                {
                    throw;
                }
            }
            else
            {
                throw new GeocoderRateLimitException("Too few Bing Requests left.");
            }
        }
    }
}