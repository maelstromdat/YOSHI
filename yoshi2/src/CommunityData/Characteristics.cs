namespace YOSHI.CommunityData
{
    /// <summary>
    /// This class is responsible for storing specific computed values for community characteristics. 
    /// </summary>
    public class Characteristics
    {
        public bool Structure { get; set; }
        public double Dispersion { get; set; }
        public double Formality { get; set; }
        public float Engagement { get; set; }
        public float Longevity { get; set; }
        public float Cohesion { get; set; }
    }
}