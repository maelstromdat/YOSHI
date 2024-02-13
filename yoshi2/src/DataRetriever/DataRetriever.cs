using Octokit;
using System;
using System.Collections.Generic;
using System.Threading.Tasks;
using YOSHI.CommunityData;
using YOSHI.DataRetrieverNS.Geocoding;

namespace YOSHI.DataRetrieverNS
{
    /// <summary>
    /// This class is responsible for retrieving data from GitHub. 
    /// </summary>
    public static class DataRetriever
    {
        public static readonly GitHubClient Client;
        // Default 24-hour operations with a basic Windows App, Non-profit, and Education key.
        // Info about rate limiting: https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-api-best-practices

        private static readonly ApiOptions MaxSizeBatches = new ApiOptions // allows us to fetch with 100 at a time
        {
            PageSize = 100
        };

        static DataRetriever()
        {
            try
            {
                // Read the GitHub Access Token and the Bing Maps Key from Windows Environment Variables
                string githubAccessToken = Environment.GetEnvironmentVariable("YOSHI_GitHubAccessToken");

                // Set the GitHub Client and set the authentication token from GitHub for the GitHub REST API
                Client = new GitHubClient(new ProductHeaderValue("yoshi"));
                Credentials tokenAuth = new Credentials(githubAccessToken);
                Client.Credentials = tokenAuth;
            }
            catch (Exception e)
            {
                throw new Exception("Error during client initialization.", e);
            }
        }

        /// <summary>
        /// Method that retrieves all GitHub data that is needed to compute the validity of this repository. A repository
        /// is valid when it has at least 100 commits (all time), it has at least 10 members active in the last 90 days,
        /// it has at least 1 milestone (all time), and it has enough location data to compute dispersion. 
        /// </summary>
        /// <param name="community">The community for which we need to retrieve GitHub Data.</param>
        /// <returns>A boolean whether the community is valid or not.</returns>
        /// <exception cref="Exception">Thrown when something goes wrong while retrieving GitHub data.</exception>
        public static async Task RetrieveDataAndCheckValidity(Community community)
        {
            string repoName = community.RepoName;
            string repoOwner = community.RepoOwner;
            Data data = community.Data;

            // Inspection of projects, requirements are at least 100 commits, at least 10 members, at least 50,000 LOC, must use milestones and issues
            try
            {
                // TODO: Done. Check whether all GitHub data is limited to the time window (e.g., no remaining data from today, only data from 90 days before today)
                await GitHubRequestsRemaining();
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine("Bing Maps API requests remaining: {0}", GeoService.BingRequestsLeft);
                Console.ResetColor();

                // There must be at least 100 commits
                Console.WriteLine("Retrieving all commits...");
                CommitRequest commitRequest = new CommitRequest { Until = Filters.EndDateTimeWindow };
                data.Commits = await GitHubRateLimitHandler.Delegate(Client.Repository.Commit.GetAll, repoOwner, repoName, commitRequest, MaxSizeBatches);
                if (data.Commits.Count < 100)
                {
                    throw new InvalidRepositoryException("Too few commits (" + data.Commits.Count + ").");
                }

                Console.WriteLine("Filtering commits...");
                List<GitHubCommit> commitsWithinTimeWindow = Filters.ExtractCommitsWithinTimeWindow(data.Commits);

                Console.WriteLine("Extracting usernames from commits...");
                data.MemberUsernames = Filters.ExtractUsernamesFromCommits(commitsWithinTimeWindow);

                // There must be at least 2 members (active in the last 90 days)
                Console.WriteLine("Retrieving user data...");
                (data.Members, data.MemberUsernames) = await RetrieveMembers(data.MemberUsernames);
                if (data.MemberUsernames.Count < 2)
                {
                    throw new InvalidRepositoryException("Too few members (" + data.MemberUsernames.Count + ").");
                }

                // There must be at least one closed milestone
                Console.WriteLine("Retrieving closed milestones...");
                MilestoneRequest stateFilter = new MilestoneRequest { State = ItemStateFilter.Closed };
                IReadOnlyList<Milestone> milestones = await GitHubRateLimitHandler.Delegate(Client.Issue.Milestone.GetAllForRepository, repoOwner, repoName, stateFilter, MaxSizeBatches);
                data.Milestones = Filters.FilterMilestones(milestones); // Remove milestones after the end time
                if (data.Milestones.Count < 1)
                {
                    throw new InvalidRepositoryException("Too few milestones (" + data.Milestones.Count + ").");
                }

                // There must be enough location data available to compute dispersion. TODO: Determine the threshold (maybe as percentage)
                Console.WriteLine("Retrieving addresses...");
                // Retrieve coordinates necessary for geographical distance and countries necessary for Hofstede indices
                (data.Coordinates, data.Countries) = await GeoService.RetrieveMemberAddresses(data.Members, repoName);
                Console.ForegroundColor = ConsoleColor.Cyan;
                Console.WriteLine("Bing Maps API requests remaining: {0}", GeoService.BingRequestsLeft);
                Console.ResetColor();
                if (data.Coordinates.Count < 2)
                {
                    throw new InvalidRepositoryException("Too few coordinates (" + data.Coordinates.Count + ").");
                }
                if (data.Countries.Count < 2)
                {
                    throw new InvalidRepositoryException("Too few addresses in Hofstede indexed countries (" + data.Countries.Count + ").");
                }
            }
            catch (InvalidRepositoryException)
            {
                throw;
            }
            catch
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("Something went wrong while retrieving data from GitHub to check validity of repo: " + repoName);
                Console.ResetColor();
                throw;
            }
        }

        /// <summary>
        /// Method that retrieves all GitHub data that is needed to compute only the structure metrics and modifies the 
        /// community data to store that information. It retrieves a mapping from a member to their followers, a mapping 
        /// from a member to their following, and a mapping from a member to their owned repositories.
        /// </summary>
        /// <param name="community">The community for which we need to retrieve GitHub Data.</param>
        /// <returns>No object or value is returned by this method when it completes.</returns>
        /// <exception cref="Exception">Thrown when something goes wrong while retrieving GitHub data.</exception>
        public static async Task RetrieveStructureData(Community community)
        {
            await GitHubRequestsRemaining();

            string repoName = community.RepoName;
            string repoOwner = community.RepoOwner;
            Data data = community.Data;
            try
            {
                Console.WriteLine("Retrieving data per member...");
                (data.MapUserFollowers, data.MapUserFollowing, data.MapUserRepositories)
                    = await RetrieveDataPerMember(repoName, data.MemberUsernames);

                Console.WriteLine("Retrieving pull requests...");
                List<PullRequest> pullRequests = await RetrievePullRequests(repoOwner, repoName, data.MemberUsernames);

                Console.WriteLine("Retrieve merged pull requests' details...");
                List<PullRequest> mergedPullRequests = new List<PullRequest>();
                
                foreach (PullRequest pullRequest in pullRequests)
                {
                    if (pullRequest.Merged)
                    {
                        PullRequest detailedPullRequest = await GitHubRateLimitHandler.Delegate(Client.PullRequest.Get, repoOwner, repoName, pullRequest.Number);
                        mergedPullRequests.Add(detailedPullRequest);
                    }
                }
                data.MergedPullRequests = mergedPullRequests;

                Console.WriteLine("Retrieving pull request comments...");
                List<IssueComment> pullRequestComments = await RetrievePullRequestComments(repoOwner, repoName, data.MemberUsernames);

                Console.WriteLine("Map pull requests to comments...");
                data.MapPullReqsToComments = MapPullRequestsToComments(pullRequests, pullRequestComments);
            }
            catch
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("Something went wrong while retrieving data from GitHub to compute structure of repo: " + repoName);
                Console.ResetColor();
                throw;
            }
        }

        /// <summary>
        /// Method that retrieves all GitHub data that is needed to compute all but structure metrics and modifies the 
        /// community data to store that information. 
        /// </summary>
        /// <param name="community">The community for which we need to retrieve GitHub Data.</param>
        /// <returns>No object or value is returned by this method when it completes.</returns>
        /// <exception cref="Exception">Thrown when something goes wrong while retrieving GitHub data.</exception>
        public static async Task RetrieveMiscellaneousData(Community community)
        {
            await GitHubRequestsRemaining();

            string repoName = community.RepoName;
            string repoOwner = community.RepoOwner;
            Data data = community.Data;
            try
            {
                Console.WriteLine("Extract commits within time window...");
                List<GitHubCommit> commitsWithinTimeWindow = Filters.ExtractCommitsWithinTimeWindow(data.Commits);
                Console.WriteLine("Retrieve commit details..."); // Necessary to retrieve what files were changed each commit
                List<GitHubCommit> detailedCommitsWithinTimeWindow = new List<GitHubCommit>();
                foreach (GitHubCommit commit in commitsWithinTimeWindow)
                {
                    GitHubCommit detailedCommit = await GitHubRateLimitHandler.Delegate(Client.Repository.Commit.Get, repoOwner, repoName, commit.Sha);
                    detailedCommitsWithinTimeWindow.Add(detailedCommit);
                }
                Console.WriteLine("Filtering detailed commits...");
                data.CommitsWithinTimeWindow = Filters.FilterDetailedCommits(detailedCommitsWithinTimeWindow, data.MemberUsernames);

                // Set the first and last commit from the time window
                (data.FirstCommitHash, data.LastCommitHash, data.FirstCommitDateTime, data.LastCommitDateTime) = Filters.FirstLastCommit(data.CommitsWithinTimeWindow);

                // A member is considered active if they made a commit in the last 30 days
                Console.WriteLine("Extracting active users...");
                data.ActiveMembers = Filters.ExtractMembersFromCommits(data.CommitsWithinTimeWindow, data.MemberUsernames, 30);

                Console.WriteLine("Retrieving commit comments...");
                IReadOnlyList<CommitComment> commitComments = await GitHubRateLimitHandler.Delegate(Client.Repository.Comment.GetAllForRepository, repoOwner, repoName, MaxSizeBatches);
                data.CommitComments = Filters.FilterComments(commitComments, data.MemberUsernames);

                // Snapshot at time of retrieval, there is no way to retrieve watchers from a past time
                Console.WriteLine("Retrieving watchers...");
                IReadOnlyList<User> watchers = await GitHubRateLimitHandler.Delegate(Client.Activity.Watching.GetAllWatchers, repoOwner, repoName, MaxSizeBatches);
                data.Watchers = Filters.ExtractUsernamesFromUsers(watchers, data.MemberUsernames);

                // Snapshot at time of retrieval, there is no way to retrieve stargazers from a past time
                Console.WriteLine("Retrieving stargazers...");
                IReadOnlyList<User> stargazers = await GitHubRateLimitHandler.Delegate(Client.Activity.Starring.GetAllStargazers, repoOwner, repoName, MaxSizeBatches);
                data.Stargazers = Filters.ExtractUsernamesFromUsers(stargazers, data.MemberUsernames);
            }
            catch
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("Something went wrong while retrieving miscellaneous data from GitHub of repo: " + repoName);
                Console.ResetColor();
                throw;
            }

            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.WriteLine("Retrieved all GitHub data for this community.");
            Console.ResetColor();
            await GitHubRequestsRemaining();
        }

        /// <summary>
        /// Retrieves the GitHub User information from a set of usernames. Since parameters cannot be modified in async
        /// methods, we return an extra variable without usernames that cause exceptions.
        /// </summary>
        /// <param name="usernames">A set of usernames to retrieve the GitHub data from.</param>
        /// <returns>A list of GitHub User information and an updated set of usernames, excluding all usernames that
        /// caused exceptions. </returns>
        private static async Task<(List<User>, HashSet<string>)> RetrieveMembers(HashSet<string> usernames)
        {
            List<User> members = new List<User>();
            HashSet<string> updatedUsernames = new HashSet<string>(); // A separate list to exclude usernames that cause exceptions
            HashSet<string> bots = new HashSet<string>();
            HashSet<string> organizations = new HashSet<string>();
            foreach (string username in usernames)
            {
                try
                {
                    // Snapshot at time of retrieval, there is no way to retrieve users information from a past time
                    User user = await GitHubRateLimitHandler.Delegate(Client.User.Get, username);

                    // Exclude organizations and bots
                    // Note: not all bots/organizations have the correct accounttype. We are bound to let through some
                    // bots/organizations this way, but it is better than nothing.
                    if (user.Type == AccountType.User)
                    {
                        members.Add(user);
                        updatedUsernames.Add(username);
                    }
                    else
                    {
                        if (user.Type == AccountType.Bot)
                        {
                            bots.Add(user.Login);
                        }
                        else // organization
                        {
                            organizations.Add(user.Login);
                        }
                    }
                }
                catch
                {
                    // Skip the usernames that cause exceptions
                    continue;
                }
            }

            // Report whether any bots were identified
            Console.ForegroundColor = ConsoleColor.Blue;
            if (bots.Count > 0)
            {
                Console.WriteLine("The following users were classified as a bot: ");
                foreach (string bot in bots)
                {
                    Console.WriteLine(bot);
                }
            }

            // Report whether any organizations were identifed
            if (organizations.Count > 0)
            {
                Console.WriteLine("The following users were classified as an organization: ");
                foreach (string org in organizations)
                {
                    Console.WriteLine(org);
                }
            }
            Console.ResetColor();

            return (members, updatedUsernames);
        }

        /// <summary>
        /// For all repository members we retrieve their followers (i.e., who's following them) and following 
        /// (i.e., who they're following), and we retrieve the repositories they worked on.
        /// </summary>
        /// <param name="data">The data object of the community in which we store all retrieved GitHub data.</param>
        private static async Task<(
            Dictionary<string, HashSet<string>> mapUserFollowers,
            Dictionary<string, HashSet<string>> mapUserFollowing,
            Dictionary<string, HashSet<string>> mapUserRepositories)>
            RetrieveDataPerMember(string repoName, HashSet<string> memberUsernames)
        {
            Dictionary<string, HashSet<string>> mapUserFollowers = new Dictionary<string, HashSet<string>>();
            Dictionary<string, HashSet<string>> mapUserFollowing = new Dictionary<string, HashSet<string>>();
            Dictionary<string, HashSet<string>> mapUserRepositories = new Dictionary<string, HashSet<string>>();

            foreach (string username in memberUsernames)
            {
                // Get the given user's followers, limited to members that are also part of the current repository
                // Snapshot at time of retrieval, there is no way to retrieve followers from a past time
                IReadOnlyList<User> followers = await GitHubRateLimitHandler.Delegate(Client.User.Followers.GetAll, username, MaxSizeBatches);
                HashSet<string> followersNames = Filters.ExtractUsernamesFromUsers(followers, memberUsernames);

                // Get the given user's users that they're following, limited to members that are also part of the current repository
                // Snapshot at time of retrieval, there is no way to retrieve following from a past time
                IReadOnlyList<User> following = await GitHubRateLimitHandler.Delegate(Client.User.Followers.GetAllFollowing, username, MaxSizeBatches);
                HashSet<string> followingNames = Filters.ExtractUsernamesFromUsers(following, memberUsernames);

                // Currently: Assume that they have all contributed to their owned repositories
                // Snapshot at time of retrieval, there is no way to retrieve repositories from a past time
                // Assumption: We assume that the users have a commit to all of their repositories, or if not that they 
                // are working on a commit for that repository.
                IReadOnlyList<Repository> repositories =
                    await GitHubRateLimitHandler.Delegate(Client.Repository.GetAllForUser, username, MaxSizeBatches);
                HashSet<string> repos = Filters.ExtractRepoNamesFromRepos(repositories, repoName);

                // Store all user data
                mapUserFollowers.Add(username, followersNames);
                mapUserFollowing.Add(username, followingNames);
                mapUserRepositories.Add(username, repos);
            }

            return (mapUserFollowers, mapUserFollowing, mapUserRepositories);
        }

        /// <summary>
        /// This method retrieves all pull requests for a repository. Filters all pull requests by non-committers, i.e., 
        /// users that are not considered members.
        /// </summary>
        /// <param name="repoOwner">Repository owner</param>
        /// <param name="repoName">Repository name</param>
        /// <returns>A list of pull requests.</returns>
        private static async Task<List<PullRequest>> RetrievePullRequests(string repoOwner, string repoName, HashSet<string> memberUsernames)
        {
            // We want all pull requests, since they often do not get closed correctly or closed at all, even if they're merged
            PullRequestRequest stateFilter = new PullRequestRequest { State = ItemStateFilter.All };
            IReadOnlyList<PullRequest> pullRequests =
                await GitHubRateLimitHandler.Delegate(Client.PullRequest.GetAllForRepository, repoOwner, repoName, stateFilter, MaxSizeBatches);

            // Filter out all pull requests outside the time window
            Console.WriteLine("Filtering pull requests outside the time window...");
            List<PullRequest> pullRequestsWithinWindow = Filters.FilterPullRequests(pullRequests, memberUsernames);

            return pullRequestsWithinWindow;
        }

        private static async Task<List<IssueComment>> RetrievePullRequestComments(string repoOwner, string repoName, HashSet<string> memberUsernames)
        {
            // Retrieve all pull request comments since the start of the time window and filter the comments
            // NOTE: There are two types of pull request comments, namely comments and review comments.
            // Only review comments are available through the pull request API. The other type of comments are available
            // through the Issues API, as GitHub's REST API v3 considers every pull request as an issue, but not every issue
            // is a pull request. For this reason, "Issues" endpoints may return both issues and pull requests in the
            // response. The number of pull request review comments increases as the number of mistakes in the pull
            // request rises, as pull request review comments are comments on a portion of the unified diff made during
            // a pull request review. 
            IssueCommentRequest issueCommentRequest = new IssueCommentRequest { Since = Filters.StartDateTimeWindow };
            IReadOnlyList<IssueComment> comments =
                await GitHubRateLimitHandler.Delegate(Client.Issue.Comment.GetAllForRepository, repoOwner, repoName, issueCommentRequest, MaxSizeBatches);

            Console.WriteLine("Filtering pull request comments...");
            List<IssueComment> filteredComments = Filters.FilterComments(comments, memberUsernames);

            return filteredComments;
        }

        /// <summary>
        /// Given a list of pull requests for a repository, this method retrieves the pull request review comments
        /// for each pull request and maps them in a dictionary. Filters all pull request comments by 
        /// non-committers, i.e., users that are not considered members.
        /// </summary>
        /// <param name="repoOwner">Repository owner</param>
        /// <param name="repoName">Repository name</param>
        /// <returns>A dictionary mapping pull requests to pull request review comments.</returns>
        private static Dictionary<PullRequest, List<IssueComment>> MapPullRequestsToComments(
            List<PullRequest> pullRequests, List<IssueComment> comments)
        {
            Dictionary<PullRequest, List<IssueComment>> mapPullReqsToComments =
                new Dictionary<PullRequest, List<IssueComment>>();
            // Temporarily store the pull requests by number, to easily link them to the comments
            Dictionary<int, PullRequest> pullRequestByNumber = new Dictionary<int, PullRequest>();

            foreach (PullRequest pullRequest in pullRequests)
            {
                mapPullReqsToComments.Add(pullRequest, new List<IssueComment>());
                pullRequestByNumber.Add(pullRequest.Number, pullRequest);
            }

            // Map the remaining comments to the pull requests
            foreach (IssueComment comment in comments)
            {
                try
                {
                    string[] splitUrl = comment.HtmlUrl.Split(new char[] { '/', '#' });
                    int pullRequestNumber = int.Parse(splitUrl[6]);
                    PullRequest pullRequest = pullRequestByNumber[pullRequestNumber]; 
                    // It is possible that there are non-matching keys, which means the pull request was created and
                    // last updated outside the time window
                    mapPullReqsToComments[pullRequest].Add(comment);
                }
                catch
                {
                    // Skip comments that don't have a pull request number in their pull request url.
                    continue;
                }
            }

            return mapPullReqsToComments;
        }

        /// <summary>
        /// Method used to report on GitHub rate limits.
        /// </summary>
        /// <returns>No object or value is returned by this method when it completes.</returns>
        private static async Task GitHubRequestsRemaining()
        {
            ApiInfo apiInfo = Client.GetLastApiInfo();
            RateLimit rateLimit = apiInfo?.RateLimit;
            if (rateLimit == null)
            {
                // Note: This is a free API call.
                MiscellaneousRateLimit miscellaneousRateLimit = await Client.Miscellaneous.GetRateLimits();
                rateLimit = miscellaneousRateLimit.Rate;
            }

            int? howManyRequestsDoIHaveLeftAfter = rateLimit?.Remaining;
            DateTimeOffset resetTime = rateLimit.Reset;
            Console.ForegroundColor = ConsoleColor.Cyan;
            Console.WriteLine("GitHub API requests remaining: {0}, reset time: {1}", howManyRequestsDoIHaveLeftAfter, resetTime.DateTime.ToLocalTime().ToString());
            Console.ResetColor();
        }
    }
}
