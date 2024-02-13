using Octokit;
using System;
using System.Collections.Generic;
using YOSHI.CommunityData;

namespace YOSHI.DataRetrieverNS
{
    /// <summary>
    /// Class responsible for filtering the GitHub data. It checks that everything is within the given time window.
    /// It filters out all data about GitHub users that are not considered members.
    /// </summary>
    public static class Filters
    {
        public static DateTimeOffset EndDateTimeWindow { get; private set; }
        public static DateTimeOffset StartDateTimeWindow { get; private set; }

        public static void SetTimeWindow(DateTimeOffset endDateTimeWindow)
        {
            int days = 90; // snapshot period of 3 months (approximated using 90 days)
            // Note: Currently other length periods are not supported.
            // Engagementprocessor uses hardcoded month thresholds of 30 and 60
            EndDateTimeWindow = endDateTimeWindow.ToUniversalTime();
            StartDateTimeWindow = EndDateTimeWindow.AddDays(-days);
        }

        /// <summary>
        /// Extracts commits committed within the given time window (default 3 months, approximated using 90 days). 
        /// Checks that the commits have a committer.
        /// </summary>
        /// <param name="commits">A list of commits</param>
        /// <returns>A list of commits that all were committed within the time window.</returns>
        public static List<GitHubCommit> ExtractCommitsWithinTimeWindow(IReadOnlyList<GitHubCommit> commits)
        {
            // Get all commits in the last 90 days
            List<GitHubCommit> filteredCommits = new List<GitHubCommit>();
            foreach (GitHubCommit commit in commits)
            {
                if ((commit.Committer != null && commit.Committer.Login != null && CheckWithinTimeWindow(commit.Commit.Committer.Date))
                    || (commit.Author != null && commit.Author.Login != null && CheckWithinTimeWindow(commit.Commit.Author.Date)))
                {
                    filteredCommits.Add(commit);
                }
            }
            return filteredCommits;
        }

        /// <summary>
        /// Extracts commits committed within the given time window (default 3 months, approximated using 90 days). 
        /// Checks that the commits have a committer and that the commit has information on what files were affected.
        /// </summary>
        /// <param name="commits">A list of commits</param>
        /// <returns>A list of commits that all were committed within the time window.</returns>
        public static List<GitHubCommit> FilterDetailedCommits(IReadOnlyList<GitHubCommit> commits, HashSet<string> memberUsernames)
        {
            // Get all commits in the last 90 days
            List<GitHubCommit> filteredCommits = new List<GitHubCommit>();
            foreach (GitHubCommit commit in commits)
            {
                if ((ValidCommitterWithinTimeWindow(commit, memberUsernames)
                    || ValidAuthorWithinTimeWindow(commit, memberUsernames))
                    && commit.Files != null)
                {
                    filteredCommits.Add(commit);
                }
            }
            return filteredCommits;
        }

        /// <summary>
        /// Filter out all commits that do not have a committer, or are not considered current members (i.e., have not
        /// committed in the last 90 days).
        /// </summary>
        /// <param name="commits">A list of commits to filter</param>
        /// <param name="memberUsernames">A set of usernames of those considered members.</param>
        /// <returns>A filtered list of commits</returns>
        public static IReadOnlyList<GitHubCommit> FilterAllCommits(IReadOnlyList<GitHubCommit> commits, HashSet<string> memberUsernames)
        {
            // Get all commits in the last 90 days
            List<GitHubCommit> filteredCommits = new List<GitHubCommit>();
            foreach (GitHubCommit commit in commits)
            {
                if (ValidCommitter(commit, memberUsernames) || ValidAuthor(commit, memberUsernames))
                {
                    filteredCommits.Add(commit);
                }
            }
            return filteredCommits;
        }

        /// <summary>
        /// This method retrieves all User objects and usernames for all committers and commit authors in the last 90
        /// days. Note: It is possible that open pull request authors have commits on their own forks. These are not detected 
        /// as members as they have not yet made a contribution. 
        /// </summary>
        /// <param name="commits">A list of commits</param>
        /// <returns>A tuple containing a list of users and a list of usernames.</returns>
        public static HashSet<string> ExtractUsernamesFromCommits(List<GitHubCommit> commits, int days = 90)
        {
            // Get the user info of all members that have made at least one commit in the last 90 days
            HashSet<string> usernames = new HashSet<string>();
            foreach (GitHubCommit commit in commits)
            {
                // Check that committer date also falls within the time window before adding the author in the list of members
                if (commit.Committer != null && commit.Committer.Login != null && commit.Committer.Login != "web-flow" && CheckWithinTimeWindow(commit.Commit.Committer.Date, days))
                {
                    usernames.Add(commit.Committer.Login);
                }
                // Check that author date also falls within the time window before adding the author in the list of members
                if (commit.Author != null && commit.Author.Login != null && commit.Author.Login != "web-flow" && CheckWithinTimeWindow(commit.Commit.Author.Date, days))
                {
                    usernames.Add(commit.Author.Login);
                }
            }
            // TODO: Apply alias resolution
            return usernames;
        }

        public static IReadOnlyList<Milestone> FilterMilestones(IReadOnlyList<Milestone> milestones)
        {
            List<Milestone> milestonesInTimeWindow = new List<Milestone>();
            foreach (Milestone milestone in milestones)
            {
                if (milestone.ClosedAt != null && milestone.ClosedAt <= EndDateTimeWindow)
                {
                    milestonesInTimeWindow.Add(milestone);
                }
            }
            return milestonesInTimeWindow;
        }

        /// <summary>
        /// This method retrieves all User objects and usernames for all committers and commit authors in the last 90
        /// days. Note: It is possible that open pull request authors have commits on their own forks. These are not detected 
        /// as members as they have not yet made a contribution. 
        /// </summary>
        /// <param name="commits">A list of commits</param>
        /// <returns>A tuple containing a list of users and a list of usernames.</returns>
        public static HashSet<string> ExtractMembersFromCommits(List<GitHubCommit> commits, HashSet<string> memberUsernames, int days = 90)
        {
            // Get the user info of all members that have made at least one commit in the last 90 days
            HashSet<string> usernames = new HashSet<string>();
            foreach (GitHubCommit commit in commits)
            {
                // Check that committer date also falls within the time window before adding the author in the list of members
                if (commit.Committer != null && commit.Committer.Login != null
                    && memberUsernames.Contains(commit.Committer.Login) && CheckWithinTimeWindow(commit.Commit.Committer.Date, days))
                {
                    usernames.Add(commit.Committer.Login);
                }
                // Check that author date also falls within the time window before adding the author in the list of members
                if (commit.Author != null && commit.Author.Login != null
                    && memberUsernames.Contains(commit.Author.Login) && CheckWithinTimeWindow(commit.Commit.Author.Date, days))
                {
                    usernames.Add(commit.Author.Login);
                }
            }
            // TODO: Apply alias resolution
            return usernames;
        }

        /// <summary>
        /// Given a list of users, extracts a set of usernames. Also checks whether users are considered members within 
        /// the time period.
        /// </summary>
        /// <param name="users">The list of users that we want to extract the usernames from.</param>
        /// <param name="memberUsernames">The list of members within the time period.</param>
        /// <returns>A set of usernames</returns>
        public static HashSet<string> ExtractUsernamesFromUsers(IReadOnlyList<User> users, HashSet<string> memberUsernames)
        {
            HashSet<string> names = new HashSet<string>();
            foreach (User user in users)
            {
                if (user.Login != null && memberUsernames.Contains(user.Login))
                {
                    names.Add(user.Login);
                }
            }
            return names;
        }

        /// <summary>
        /// Filter out all pull requests that are not within the time window, do not have an author, or are not considered
        /// current members (i.e., have not committed in the last 90 days).
        /// </summary>
        /// <param name="pullRequests">A list of pull request to filter</param>
        /// <param name="memberUsernames">A set of usernames of those considered members.</param>
        /// <returns>A filtered list of pull requests</returns>
        public static List<PullRequest> FilterPullRequests(IReadOnlyList<PullRequest> pullRequests, HashSet<string> memberUsernames)
        {
            // Extract only the pull requests that fall within the 3-month time window (approximately 90 days)
            // Note: this cannot be added as a parameter in the GitHub API request.
            List<PullRequest> filteredPullRequests = new List<PullRequest>();
            foreach (PullRequest pullRequest in pullRequests)
            {
                if ((CheckWithinTimeWindow(pullRequest.UpdatedAt) || CheckWithinTimeWindow(pullRequest.CreatedAt)
                    || CheckWithinTimeWindow(pullRequest.MergedAt) || CheckWithinTimeWindow(pullRequest.ClosedAt))
                    && pullRequest.User != null
                    && pullRequest.User.Login != null
                    && memberUsernames.Contains(pullRequest.User.Login))
                {
                    filteredPullRequests.Add(pullRequest);
                }
            }
            return filteredPullRequests;
        }

        /// <summary>
        /// Filter out all non-pull-request issue-comments that are not within the time window, do not have an author, 
        /// or are not considered current members (i.e., have not committed in the last 90 days).
        /// </summary>
        /// <param name="comments">A list of issue comments to filter</param>
        /// <param name="memberUsernames">A set of usernames of those considered members.</param>
        /// <returns>A filtered list of pull request comments</returns>
        public static List<IssueComment> FilterComments(IReadOnlyList<IssueComment> comments, HashSet<string> memberUsernames)
        {
            // Filter out all comments that are not within the time window, do not have an author, or are not 
            // considered current members (i.e., have not committed in the last 90 days). 
            // Note: the 3 months period cannot be added as a parameter in the GitHub API request.
            List<IssueComment> filteredComments = new List<IssueComment>();
            foreach (IssueComment comment in comments)
            {
                if (comment.HtmlUrl.Contains("pull")
                    && (CheckWithinTimeWindow(comment.UpdatedAt) || CheckWithinTimeWindow(comment.CreatedAt))
                    && comment.User != null
                    && comment.User.Login != null
                    && memberUsernames.Contains(comment.User.Login))
                {
                    filteredComments.Add(comment);
                }
            }
            return filteredComments;
        }

        /// <summary>
        /// Given a list of commits, this method extracts the first and last commit date and returns them as formatted 
        /// strings.
        /// </summary>
        /// <param name="commits">List of commits to extract the first and last commit dates from</param>
        /// <returns>First and last commit dates as formatted strings.</returns>
        public static (string, string, string, string) FirstLastCommit(List<GitHubCommit> commits)
        {
            string hashFirstCommit = "";
            string hashLastCommit = "";
            DateTimeOffset dateFirstCommit = DateTimeOffset.MaxValue;
            DateTimeOffset dateLastCommit = DateTimeOffset.MinValue;
            foreach (GitHubCommit commit in commits)
            {
                DateTimeOffset dateCurrentCommit = commit.Commit.Committer.Date;
                // If current earliest commit is later than current commit
                if (dateFirstCommit.CompareTo(dateCurrentCommit) > 0)
                {
                    dateFirstCommit = dateCurrentCommit;
                    hashFirstCommit = commit.Sha;
                }
                // If current latest commit is earlier than current commit
                if (dateLastCommit.CompareTo(dateCurrentCommit) < 0)
                {
                    dateLastCommit = dateCurrentCommit;
                    hashLastCommit = commit.Sha;
                }
            }

            dateFirstCommit.ToUniversalTime();
            dateLastCommit.ToUniversalTime();

            return (hashFirstCommit, hashLastCommit, dateFirstCommit.ToString("yyyy-MM-dd HH:mm:ss zzz"), dateLastCommit.ToString("yyyy-MM-dd HH:mm:ss zzz"));
        }

        /// <summary>
        /// Filter out all comments that are not within the time window, do not have an author, or are not considered
        /// current members (i.e., have not committed in the last 90 days).
        /// </summary>
        /// <param name="comments">A list of commit comments to filter</param>
        /// <param name="memberUsernames">A set of usernames of those considered members.</param>
        /// <returns>A filtered list of commit comments</returns>
        public static List<CommitComment> FilterComments(IReadOnlyList<CommitComment> comments, HashSet<string> memberUsernames)
        {
            List<CommitComment> filteredComments = new List<CommitComment>();
            foreach (CommitComment comment in comments)
            {
                if ((CheckWithinTimeWindow(comment.UpdatedAt) || CheckWithinTimeWindow(comment.CreatedAt))
                    && comment.User != null
                    && comment.User.Login != null
                    && memberUsernames.Contains(comment.User.Login))
                {
                    filteredComments.Add(comment);
                }
            }
            return filteredComments;
        }

        /// <summary>
        /// A method that takes a DateTimeOffset object and checks whether it is within the specified time window x number 
        /// of days (Default: 3 months,  i.e., x = 90 days). This window ends at the specified end of the time window and 
        /// starts at midnight x days prior.
        /// </summary>
        /// <param name="dateTime">A DateTimeOffset object</param>
        /// <returns>Whether the DateTimeOffset object falls within the time window.</returns>
        public static bool CheckWithinTimeWindow(DateTimeOffset? dateTime, int days = 90)
        {
            if (dateTime == null)
            {
                return false;
            }

            // We set the date time offset window for the 3 months earlier from now (approximated using 90 days)
            DateTimeOffset startDate = EndDateTimeWindow.AddDays(-days);
            return dateTime >= startDate && dateTime <= EndDateTimeWindow;
        }

        /// <summary>
        /// Given a list of repositories, extract the names of the repositories, exclude the name of the current 
        /// repository.
        /// </summary>
        /// <param name="repositories">A list of repositories.</param>
        /// <param name="currentRepoName">The name of the repository we're currently processing.</param>
        /// <returns>A set of repository names excluding the current repository name.</returns>
        public static HashSet<string> ExtractRepoNamesFromRepos(IReadOnlyList<Repository> repositories, string currentRepoName)
        {
            HashSet<string> repoNames = new HashSet<string>();
            foreach (Repository repo in repositories)
            {
                if (repo.Name != currentRepoName)
                {
                    repoNames.Add(repo.Name);
                }
            }
            return repoNames;
        }

        /// <summary>
        /// Given a commit, check whether the committer is valid (i.e., the committer is not null, the committer's login
        /// is not null, and the committer is considered a member in the last 3 months).
        /// </summary>
        /// <param name="commit">The commit to check</param>
        /// <param name="memberUsernames">A set of members</param>
        /// <returns>Whether the committer of the given commit is valid</returns>
        public static bool ValidCommitter(GitHubCommit commit, HashSet<string> memberUsernames)
        {
            return commit.Committer != null
                && commit.Committer.Login != null
                && memberUsernames.Contains(commit.Committer.Login);
        }

        /// <summary>
        /// Given a commit, check whether the author is valid (i.e., the author is not null, the author's login
        /// is not null, and the author is considered a member in the last 3 months).
        /// </summary>
        /// <param name="commit">The commit to check</param>
        /// <param name="memberUsernames">A set of members</param>
        /// <returns>Whether the author of the given commit is valid</returns>
        public static bool ValidAuthor(GitHubCommit commit, HashSet<string> memberUsernames)
        {
            return commit.Author != null
                && commit.Author.Login != null
                && memberUsernames.Contains(commit.Author.Login);
        }

        /// <summary>
        /// Given a commit, check whether the committer is valid (i.e., the committer is not null, the committer's login
        /// is not null, the committer date is within the 3 month window, and the committer is considered a member in 
        /// the last 3 months).
        /// </summary>
        /// <param name="commit">The commit to check</param>
        /// <param name="memberUsernames">A set of members</param>
        /// <returns>Whether the committer of the given commit is valid</returns>
        public static bool ValidCommitterWithinTimeWindow(GitHubCommit commit, HashSet<string> memberUsernames)
        {
            return ValidCommitter(commit, memberUsernames) && CheckWithinTimeWindow(commit.Commit.Committer.Date);
        }

        /// <summary>
        /// Given a commit, check whether the author is valid (i.e., the author is not null, the author's login
        /// is not null, the author date is within the 3 month window, and the author is considered a member in 
        /// the last 3 months).
        /// </summary>
        /// <param name="commit">The commit to check</param>
        /// <param name="memberUsernames">A set of members</param>
        /// <returns>Whether the committer of the given commit is valid</returns>
        public static bool ValidAuthorWithinTimeWindow(GitHubCommit commit, HashSet<string> memberUsernames)
        {
            return ValidAuthor(commit, memberUsernames) && CheckWithinTimeWindow(commit.Commit.Author.Date);
        }
    }
}
