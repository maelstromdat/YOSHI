namespace YOSHI.CommunityData.MetricData
{
    /// <summary>
    /// This class is used to store values for metrics used to compute a community's formality.
    /// </summary>
    public class Formality
    {
        public float MeanMembershipType { get; set; }
        public float Milestones { get; set; }
        public double Lifetime { get; set; }

        // Mean membership type value implemented per the original Yoshi's buggy implementation
        // Used for comparison between Yoshi and Yoshi 2
        public float MeanMembershipTypeOld { get; set; }
        public double BuggedLifetimeMS { get; set; }
    }
}
