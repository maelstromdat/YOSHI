namespace YOSHI.CommunityData.MetricData
{
    /// <summary>
    /// This class is used to store values for metrics used to compute a community's engagement level.
    /// </summary>
    public class Engagement
    {
        public double MedianNrCommentsPerPullReq { get; set; }
        public double MedianMonthlyPullCommitCommentsDistribution { get; set; }
        public double MedianActiveMember { get; set; }
        public double MedianWatcher { get; set; }
        public double MedianStargazer { get; set; }
        public double MedianCommitDistribution { get; set; }
        public double MedianFileCollabDistribution { get; set; }

        // Extra variables for comparison between Yoshi and Yoshi 2
        public double MedianMonthlyCommitDistribution { get; set; }
        public double MedianMonthlyFileCollabDistribution { get; set; }
    }
}
