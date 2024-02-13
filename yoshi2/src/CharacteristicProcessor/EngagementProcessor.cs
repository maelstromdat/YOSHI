using Octokit;
using System;
using System.Collections.Generic;
using System.Linq;
using YOSHI.CommunityData;
using YOSHI.CommunityData.MetricData;
using YOSHI.DataRetrieverNS;

namespace YOSHI.CharacteristicProcessorNS
{
    public static partial class CharacteristicProcessor
    {
        /// <summary>
        /// A method that computes several metrics used to measure community engagement. It modifies the given community.
        /// </summary>
        /// <param name="community">The community for which we need to compute the engagement.</param>
        private static void ComputeEngagement(Community community)
        {
            Data data = community.Data;
            Engagement engagement = community.Metrics.Engagement;
            engagement.MedianNrCommentsPerPullReq =
                MedianNrCommentsPerPullReq(data.MapPullReqsToComments);
            engagement.MedianMonthlyPullCommitCommentsDistribution = MedianMonthlyCommentsDistribution(
                data.CommitComments,
                data.MapPullReqsToComments.Values.SelectMany(x => x).ToList(),
                data.MemberUsernames
            );
            engagement.MedianActiveMember = MedianContains(data.ActiveMembers, data.MemberUsernames);
            engagement.MedianWatcher = MedianContains(data.Watchers, data.MemberUsernames);
            engagement.MedianStargazer = MedianContains(data.Stargazers, data.MemberUsernames);
            engagement.MedianMonthlyCommitDistribution = MedianMonthlyCommitDistribution(data.CommitsWithinTimeWindow, data.MemberUsernames);
            engagement.MedianMonthlyFileCollabDistribution = MedianMonthlyFileCollabDistribution(data.CommitsWithinTimeWindow, data.MemberUsernames);

            community.Characteristics.Engagement =
                (float)(engagement.MedianNrCommentsPerPullReq + engagement.MedianMonthlyPullCommitCommentsDistribution
                + engagement.MedianActiveMember + engagement.MedianWatcher + engagement.MedianStargazer
                + engagement.MedianMonthlyCommitDistribution + engagement.MedianMonthlyFileCollabDistribution);

            // EXTRA COMPUTATIONS FOR COMPARISON METRICS
            engagement.MedianCommitDistribution = MedianCommitDistribution(data.CommitsWithinTimeWindow, data.MemberUsernames);
            engagement.MedianFileCollabDistribution = MedianFileCollabDistribution(data.CommitsWithinTimeWindow, data.MemberUsernames);
        }

        /// <summary>
        /// Given a list pull requests and a list of members within the snapshot period, compute the median number of 
        /// pull request comments per pull request. 
        /// </summary>
        /// <param name="mapPullReqsToComments">A mapping from pull requests to their corresponding comments.</param>
        /// <returns>The median value of pull request review comments per member.</returns>
        private static double MedianNrCommentsPerPullReq(Dictionary<PullRequest, List<IssueComment>> mapPullReqsToComments)
        {
            if (mapPullReqsToComments.Count < 1)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("No pull requests within time window!");
                Console.ResetColor();
                return 0d;
            }

            // Compute the comments per pull request
            // Note: the pull requests and comments not from members and not within the snapshot period have been
            // filtered in the DataRetriever.
            List<int> commentsPerPullReq = mapPullReqsToComments.Values
                                                  .Select(list => list.Count())
                                                  .ToList();

            // From the comments per Pull Request, compute the median
            // Re-architecting Software Forges... "Finally, in average, we observed that the number of discussions,
            // comments or threads spreading from a thread or discussion is comprised between 0 or 1."
            return Statistics.ComputeMedian(commentsPerPullReq);
        }

        /// <summary>
        /// Computes the median of all members' average (commit/pull-request) comments per month in the last 3 months. 
        /// </summary>
        /// <param name="commitComments">A list of commit comments</param>
        /// <param name="pullReqComments">A list of pull request comments</param>
        /// <param name="memberUsernames">A list of member usernames</param>
        /// <returns>The median of all members' average (commit/pull-request) comments per month in the last 3 months.</returns>
        private static double MedianMonthlyCommentsDistribution(IReadOnlyList<CommitComment> commitComments, List<IssueComment> pullReqComments, HashSet<string> memberUsernames)
        {
            if (commitComments.Count < 1 && pullReqComments.Count < 1)
            {
                Console.ForegroundColor = ConsoleColor.Red;
                Console.WriteLine("No commit or pull request comments within time window!");
                Console.ResetColor();
                return 0d;
            }

            // Store the dates of the comments per member, so we can count the number of comments per month for each member
            Dictionary<string, List<DateTimeOffset>> commentDatesPerMember = new Dictionary<string, List<DateTimeOffset>>();

            foreach (string username in memberUsernames)
            {
                commentDatesPerMember.Add(username, new List<DateTimeOffset>());
            }

            foreach (CommitComment comment in commitComments)
            {
                // Use a comment's latest date, which is either UpdatedAt or CreatedAt
                DateTimeOffset date =
                    comment.UpdatedAt != null && comment.UpdatedAt > comment.CreatedAt ? (DateTimeOffset)comment.UpdatedAt : comment.CreatedAt;
                commentDatesPerMember[comment.User.Login].Add(date);
            }
            foreach (IssueComment comment in pullReqComments)
            {
                DateTimeOffset date =
                    comment.UpdatedAt != null && comment.UpdatedAt > comment.CreatedAt ? (DateTimeOffset)comment.UpdatedAt : comment.CreatedAt;
                commentDatesPerMember[comment.User.Login].Add(date);
            }

            List<double> meanCommentsPerMonthPerMember = new List<double>();
            foreach (string username in memberUsernames)
            {
                // Check for each comment in which month it took place and compute the average comments per month for this member
                List<int> nrCommentsPerMonth = new List<int> { 0, 0, 0 };
                foreach (DateTimeOffset date in commentDatesPerMember[username])
                {
                    nrCommentsPerMonth[CheckMonth(date)]++;
                }
                
                meanCommentsPerMonthPerMember.Add(nrCommentsPerMonth.Average());
            }

            return Statistics.ComputeMedian(meanCommentsPerMonthPerMember);
        }

        /// <summary>
        /// Given a date, check in which month it appears over the 3-month window. 0 means it occurs in the first month 
        /// of the snapshot (i.e., the oldest), 1 in the second month, 2 in the last month (i.e., the latest month).
        /// </summary>
        /// <param name="date">The date to check.</param>
        /// <returns>The number of the month in which the date occurs</returns>
        private static int CheckMonth(DateTimeOffset date)
        {
            return date switch
            {
                // Comment within 1-30 days before today (i.e., third month of the snapshot)
                DateTimeOffset n when Filters.CheckWithinTimeWindow(n, 30) => 2,
                // Comment within 31-60 days before today (i.e., second month of the snapshot)
                DateTimeOffset n when Filters.CheckWithinTimeWindow(n, 60) => 1,
                // Comment within 61-90 days before today (i.e., first month of the snapshot)
                _ => 0,
            };
        }

        /// <summary>
        /// Given a set of users and a set of members active in the last 90 days, compute a list containing for each 
        /// member whether they are contained in the set of users (1) or not (0). Then compute the median of that list.
        /// </summary>
        /// <param name="users">A set of users.</param>
        /// <param name="memberUsernames">A set of members.</param>
        /// <returns>The median value whether members occur in the set of users.</returns>
        private static double MedianContains(HashSet<string> users, HashSet<string> memberUsernames)
        {
            List<int> userValues = new List<int>();
            foreach (string username in memberUsernames)
            {
                if (users.Contains(username))
                {
                    userValues.Add(1);
                }
                else
                {
                    userValues.Add(0);
                }
            }
            return Statistics.ComputeMedian(userValues);
        }

        private static double MedianMonthlyCommitDistribution(List<GitHubCommit> commitsWithinWindow, HashSet<string> memberUsernames)
        {
            Dictionary<string, List<DateTimeOffset>> commitDatesPerMember = new Dictionary<string, List<DateTimeOffset>>();
            foreach (string username in memberUsernames)
            {
                commitDatesPerMember.Add(username, new List<DateTimeOffset>());
            }

            foreach (GitHubCommit commit in commitsWithinWindow)
            {
                // Note: all commits within the timewindow have already accessed committer, so we do not need to check
                // that committer is not null.
                if (Filters.ValidCommitterWithinTimeWindow(commit, memberUsernames))
                {
                    commitDatesPerMember[commit.Committer.Login].Add(commit.Commit.Committer.Date);
                }
                // Prevent counting commits twice
                if (Filters.ValidAuthorWithinTimeWindow(commit, memberUsernames) && (!Filters.ValidCommitterWithinTimeWindow(commit, memberUsernames) || (commit.Committer.Login != commit.Author.Login)))
                {
                    commitDatesPerMember[commit.Author.Login].Add(commit.Commit.Author.Date);
                }
            }

            List<double> meanCommitsPerMonthPerMember = new List<double>();
            foreach (string username in memberUsernames)
            {
                // Check for each comment in which month it took place and compute the average comments per month for this member
                List<int> nrCommentsPerMonth = new List<int> { 0, 0, 0 };
                foreach (DateTimeOffset date in commitDatesPerMember[username])
                {
                    nrCommentsPerMonth[CheckMonth(date)]++;
                }

                meanCommitsPerMonthPerMember.Add(nrCommentsPerMonth.Average());
            }

            return Statistics.ComputeMedian(meanCommitsPerMonthPerMember);
        }

        private static double MedianMonthlyFileCollabDistribution(List<GitHubCommit> commitsWithinWindow, HashSet<string> memberUsernames)
        {
            // Extract the changed filenames
            HashSet<(string, string)> changedFileNames = ExtractCommittersPerFile(commitsWithinWindow, memberUsernames).Item1;

            // Extract largest non-overlapping sets of changed filenames
            Graph<string> filenamesGraph = new Graph<string>();
            filenamesGraph.AddEdges(changedFileNames);
            List<HashSet<string>> connectedComponents = filenamesGraph.GetConnectedComponents().ToList();

            // Extract the committers per file per month
            List<Dictionary<string, HashSet<string>>> committersPerFilePerMonth = ExtractCommittersPerFilePerMonth(commitsWithinWindow, memberUsernames);

            Dictionary<string, int[]> countcommittersPerFilePerMonth = new Dictionary<string, int[]>();
            // Loop over 3 months
            for (int i = 0; i < 2; i++)
            {
                // Merge files in the dictionary whose names got changed
                // Note: "ref" is used to indicate that committersPerFile may be modified by the method
                Dictionary<string, HashSet<string>> committersPerFile = committersPerFilePerMonth[i];
                MergeKeysWithUpdatedNames(connectedComponents, ref committersPerFile);
                foreach (KeyValuePair<string, HashSet<string>> fileCommitters in committersPerFile)
                {
                    if (!countcommittersPerFilePerMonth.ContainsKey(fileCommitters.Key))
                    {
                        countcommittersPerFilePerMonth.Add(fileCommitters.Key, new int[3]);
                    }
                    countcommittersPerFilePerMonth[fileCommitters.Key][i] = fileCommitters.Value.Count;
                }
            }

            List<double> meanCommittersPerFilePerMonth = new List<double>();
            foreach (KeyValuePair<string, int[]> nrCommittersPerFilePerMonth in countcommittersPerFilePerMonth)
            {
                meanCommittersPerFilePerMonth.Add(nrCommittersPerFilePerMonth.Value.Average());
            }

            return Statistics.ComputeMedian(meanCommittersPerFilePerMonth);
        }

        /// <summary>
        /// Given a list of commits and a list of members, extract for each file the unique committers that have 
        /// modified that file, while keeping track of name changes.
        /// </summary>
        /// <param name="commits"></param>
        /// <param name="memberUsernames"></param>
        /// <returns></returns>
        private static List<Dictionary<string, HashSet<string>>>
            ExtractCommittersPerFilePerMonth(List<GitHubCommit> commits, HashSet<string> memberUsernames)
        {

            List<GitHubCommit> commitsMonth0 = new List<GitHubCommit>();
            List<GitHubCommit> commitsMonth1 = new List<GitHubCommit>();
            List<GitHubCommit> commitsMonth2 = new List<GitHubCommit>();
            foreach (GitHubCommit commit in commits)
            {
                // Extract commit date
                DateTimeOffset? date = null;
                if (commit.Commit != null && commit.Commit.Committer != null && commit.Commit.Committer.Date != null)
                {
                    date = commit.Commit.Committer.Date;
                }
                else if (commit.Commit != null && commit.Commit.Author != null && commit.Commit.Author.Date != null)
                {
                    date = commit.Commit.Committer.Date;
                }

                // If date is not null, add the commit to the correct month
                if (date != null)
                {
                    switch (CheckMonth((DateTimeOffset)date))
                    {
                        case 0:
                            commitsMonth0.Add(commit);
                            break;
                        case 1:
                            commitsMonth1.Add(commit);
                            break;
                        case 2:
                            commitsMonth2.Add(commit);
                            break;
                    }
                }
            }

            // Compute per month the changed file names and the committers per file
            (
                HashSet<(string, string)> _,
                Dictionary<string, HashSet<string>> committersPerFileMonth0
            ) = ExtractCommittersPerFile(commitsMonth0, memberUsernames);
            (
                HashSet<(string, string)> _,
                Dictionary<string, HashSet<string>> committersPerFileMonth1
            ) = ExtractCommittersPerFile(commitsMonth1, memberUsernames);
            (
                HashSet<(string, string)> _,
                Dictionary<string, HashSet<string>> committersPerFileMonth2
            ) = ExtractCommittersPerFile(commitsMonth2, memberUsernames);

            List<Dictionary<string, HashSet<string>>> committersPerFilePerMonth = new List<Dictionary<string, HashSet<string>>> { committersPerFileMonth0, committersPerFileMonth1, committersPerFileMonth2 };

            return committersPerFilePerMonth;
        }

        /// <summary>
        /// Merge files in the dictionary whose names got changed
        /// </summary>
        /// <param name="updatedFilenames">A list of sets of updated filenames.</param>
        /// <param name="committersPerFile">A dictionary of committers per file.</param>
        private static void MergeKeysWithUpdatedNames(
            List<HashSet<string>> updatedFilenames,
            ref Dictionary<string, HashSet<string>> committersPerFile
        )
        {
            foreach (HashSet<string> set in updatedFilenames)
            {
                // Find the filename used in the dictionary. The first one returned from this set will be kept in the
                // dictionary
                // Note: not all filenames need to occur in the dictionary, sometimes older files (outside the 3-month
                // window) get their names changed (or relocated which causes their name to be changed)
                string nameUsedInDict = "";
                foreach (string name in set)
                {
                    if (committersPerFile.ContainsKey(name))
                    {
                        nameUsedInDict = name;
                        break;
                    }
                }

                // Remove the name from the set so we're left with the filenames that we want to remove from the dictionary
                if (nameUsedInDict != "")
                {
                    set.Remove(nameUsedInDict);
                }

                // Foreach filename that we want to remove from the dictionary, merge their values with the file that we
                // want to keep in the dictionary and remove it
                foreach (string name in set)
                {
                    if (committersPerFile.ContainsKey(name))
                    {
                        committersPerFile[nameUsedInDict].UnionWith(committersPerFile[name]);
                        committersPerFile.Remove(name);
                    }
                }
            }
        }

        /// <summary>
        /// Given a list of commits and a list of members, extract for each file the unique committers that have 
        /// modified that file, while keeping track of name changes.
        /// </summary>
        /// <param name="commits"></param>
        /// <param name="memberUsernames"></param>
        /// <returns></returns>
        private static (HashSet<(string, string)>, Dictionary<string, HashSet<string>>)
            ExtractCommittersPerFile(List<GitHubCommit> commits, HashSet<string> memberUsernames)
        {
            HashSet<(string, string)> changedFileNames = new HashSet<(string, string)>(); // Used to keep track of changed filenames.
            Dictionary<string, HashSet<string>> committersPerFile = new Dictionary<string, HashSet<string>>(); // Used to keep track of the unique committers/authors per file. 

            foreach (GitHubCommit commit in commits)
            {
                // Loop over all files affected by the current commit
                foreach (GitHubCommitFile file in commit.Files)
                {
                    if (file.Filename != null)
                    {
                        // Keep track of changed filenames, will be resolved later
                        if (file.PreviousFileName != null)
                        {
                            changedFileNames.Add((file.Filename, file.PreviousFileName));
                        }

                        // Check if we previously saw this file, add as key to the dictionary, add the committer to its value
                        if (!committersPerFile.ContainsKey(file.Filename))
                        {
                            committersPerFile.Add(file.Filename, new HashSet<string>());
                        }

                        if (Filters.ValidCommitterWithinTimeWindow(commit, memberUsernames))
                        {
                            committersPerFile[file.Filename].Add(commit.Committer.Login);
                        }

                        // Add the commit author to the current file's entry in the dictionary
                        if (Filters.ValidAuthorWithinTimeWindow(commit, memberUsernames))
                        {
                            committersPerFile[file.Filename].Add(commit.Author.Login);
                        }
                    }
                }
            }

            return (changedFileNames, committersPerFile);
        }

        // EXTRA METHODS FOR COMPUTING MORE METRICS FOR COMPARISON
        /// <summary>
        /// 
        /// </summary>
        /// <param name="commitsWithinWindow"></param>
        /// <param name="memberUsernames"></param>
        /// <returns></returns>
        private static double MedianCommitDistribution(List<GitHubCommit> commitsWithinWindow, HashSet<string> memberUsernames)
        {
            Dictionary<string, int> nrCommitsPerUser = new Dictionary<string, int>();
            foreach (string username in memberUsernames)
            {
                nrCommitsPerUser.Add(username, 0);
            }

            foreach (GitHubCommit commit in commitsWithinWindow)
            {
                // Note: all commits within the timewindow have already accessed committer, so we do not need to check
                // that committer is not null.
                if (Filters.ValidCommitterWithinTimeWindow(commit, memberUsernames))
                {
                    nrCommitsPerUser[commit.Committer.Login]++;
                }
                // Prevent double counting a commit
                if (Filters.ValidAuthorWithinTimeWindow(commit, memberUsernames) && (!Filters.ValidCommitterWithinTimeWindow(commit, memberUsernames) || (commit.Committer.Login != commit.Author.Login)))
                {
                    nrCommitsPerUser[commit.Author.Login]++;
                }
            }

            return (double)Statistics.ComputeMedian(nrCommitsPerUser.Values.ToList()) / commitsWithinWindow.Count;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="commits"></param>
        /// <param name="memberUsernames"></param>
        /// <returns></returns>
        private static double MedianFileCollabDistribution(List<GitHubCommit> commits, HashSet<string> memberUsernames)
        {
            // Extract the committers per file and the changed filenames
            (
                HashSet<(string, string)> changedFileNames,
                Dictionary<string, HashSet<string>> committersPerFile
            ) = ExtractCommittersPerFile(commits, memberUsernames);

            // Extract largest non-overlapping sets of changed filenames
            Graph<string> filenamesGraph = new Graph<string>();
            filenamesGraph.AddEdges(changedFileNames);
            List<HashSet<string>> connectedComponents = filenamesGraph.GetConnectedComponents().ToList();

            // Merge files in the dictionary whose names got changed
            // Note: "ref" is used to indicate that committersPerFile may be modified by the method
            MergeKeysWithUpdatedNames(connectedComponents, ref committersPerFile);

            List<int> nrCommittersPerFile = committersPerFile.Values
                                                  .Select(set => set.Count())
                                                  .ToList();

            return Statistics.ComputeMedian(nrCommittersPerFile) / nrCommittersPerFile.Count;
        }
    }
}