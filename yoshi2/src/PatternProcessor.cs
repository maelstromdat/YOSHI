using YOSHI.CommunityData;

namespace YOSHI
{
    /// <summary>
    /// This class is responsible for transforming the numeric values of community characteristics into community 
    /// patterns.
    /// 
    /// The thresholds from the following paper were used:
    /// Authors:    D.A. Tamburri, F. Palomba, A. Serebrenik, and A. Zaidman
    /// Title:      Discovering community patterns in open - source: a systematic approach and its evaluation
    /// Journal:    Empir.Softw.Eng.
    /// Volume:     24
    /// Number:     3
    /// Pages:      1369--1417
    /// Year:       2019
    /// URL:        https://doi.org/10.1007/s10664-018-9659-9
    /// </summary>
    public static class PatternProcessor
    {
        // The thresholds that will be used to compute the patterns for each community.
        private static readonly int th_global_distance = 4926;      // Kilometers
        private static readonly float th_formality_lvl_low = 0.1F;
        private static readonly float th_formality_lvl_high = 20F;
        private static readonly float th_engagement_lvl = 3.5F;
        //private static readonly float th_cohesion_lvl = 11.0F;
        private static readonly int th_longevity = 93;              // Days

        /// <summary>
        /// This method implements the decision tree from the YOSHI paper <see cref="Program"/>.
        /// </summary>
        /// <param name="community">The community whose patterns should be computed.</param>
        public static void ComputePattern(Community community)
        {
            Characteristics chars = community.Characteristics;
            Pattern pattern = community.Pattern;

            if (chars.Structure) // Community exhibits structure
            {
                pattern.SN = true;

                if (chars.Dispersion >= th_global_distance) // Dispersed
                {
                    pattern.NoP = true;
                    if (chars.Formality < th_formality_lvl_low) // Informal
                    {
                        pattern.IN = true;
                    }
                    else if (chars.Formality > th_formality_lvl_high) // Formal
                    {
                        pattern.FN = true;
                    }
                }
                else // Not dispersed
                {
                    pattern.CoP = true;
                    //if (chars.Cohesion > th_cohesion_lvl) // Cohesive
                    //{
                    //    pattern.WorkGroup = true;
                    //}
                    if (chars.Longevity < th_longevity) // Low durability / short-lived
                    {
                        pattern.PT = true;
                    }
                    // Note: The threshold mentions > 0.1 and < 20. Since for formality and informality we have < 0.1
                    // and > 20 respectively, I have decided to include 0.1 and 20 in this threshold
                    if (chars.Formality >= th_formality_lvl_low && chars.Formality <= th_formality_lvl_high) // Not informal but also not formal
                    {
                        pattern.FG = true;
                    }
                }

                if (chars.Engagement > th_engagement_lvl)
                {
                    pattern.IC = true;
                }
            }
        }
    }
}
