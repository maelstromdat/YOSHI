namespace YOSHI.CommunityData
{
    /// <summary>
    /// This class is responsible for storing all community related data. 
    /// We will use this class to store the community data in separate objects. 
    /// </summary>
    public class Community
    {
        public string RepoOwner { get; }
        public string RepoName { get; }
        public Data Data { get; }
        public Metrics Metrics { get; }
        public Characteristics Characteristics { get; }
        public Pattern Pattern { get; set; }

        public Community(string owner, string name)
        {
            this.RepoOwner = owner;
            this.RepoName = name;
            this.Data = new Data();
            this.Metrics = new Metrics();
            this.Characteristics = new Characteristics();
            this.Pattern = new Pattern();
        }
    }
}
