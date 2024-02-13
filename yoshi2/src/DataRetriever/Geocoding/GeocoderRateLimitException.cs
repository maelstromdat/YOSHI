using System;

namespace YOSHI.DataRetrieverNS.Geocoding
{
    /// <summary>
    /// Class used to identify the rate limit exception from Bing Maps API
    /// </summary>
    public class GeocoderRateLimitException : Exception
    {
        public GeocoderRateLimitException()
        {
        }
        public GeocoderRateLimitException(string message)
            : base(message)
        {
        }
        public GeocoderRateLimitException(string message, Exception inner)
            : base(message, inner)
        {
        }
    }
}
