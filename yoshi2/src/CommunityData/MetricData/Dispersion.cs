namespace YOSHI.CommunityData.MetricData
{
    /// <summary>
    /// This class is used to store values for metrics used to compute a community's dispersion.
    /// </summary>
    public class Dispersion
    {
        // Note: population variance
        public double VarianceGeographicalDistance { get; set; }
        public double VarianceHofstedeCulturalDistance { get; set; }
        
        // Extra variables for comparison between Yoshi and Yoshi 2
        public double AverageGeographicalDistance { get; set; }
        public double AverageCulturalDispersion { get; set; }
    }
}
