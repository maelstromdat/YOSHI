using Octokit;
using System.Collections.Generic;
using System.Linq;
using YOSHI.CommunityData;
using YOSHI.CommunityData.MetricData;

namespace YOSHI.CharacteristicProcessorNS
{
    public static partial class CharacteristicProcessor
    {
        /// <summary>
        /// A method that computes several metrics used to measure community structure and then decides whether a
        /// community exhibits a structure or not.
        /// </summary>
        /// <param name="community">The community for which we need to compute the structure.</param>
        public static void ComputeStructure(Community community)
        {
            Data data = community.Data;
            // Note: we compute all connections between members and obtain member graphs that are currently unused 
            // TODO: Use/Export the graph. Currently it is tracked, but not used.
            Graph<string> structureGraph = new Graph<string>();
            structureGraph.AddNodes(data.MemberUsernames);

            Structure s = community.Metrics.Structure;
            s.CommonProjects = AddCommonProjectsConnections(ref structureGraph, data.MapUserRepositories);
            s.Followers = AddFollowConnections(ref structureGraph, data.MapUserFollowers, data.MapUserFollowing);
            s.PullReqInteraction = AddPullReqConnections(ref structureGraph, data.MapPullReqsToComments, data.MemberUsernames);

            community.Characteristics.Structure = s.CommonProjects || s.Followers || s.PullReqInteraction;
        }

        /// <summary>
        /// We compute the common projects connections between all users. 
        /// </summary>
        /// <param name="mapUserRepositories">A mapping from usernames to the repositories that they worked on.</param>
        /// <returns>A mapping for each members to a set of other members who worked on a common repository.</returns>
        private static bool AddCommonProjectsConnections(
            ref Graph<string> structureGraph,
            Dictionary<string, HashSet<string>> mapUserRepositories)
        {
            bool commonProjectsConnection = false;

            // Find common projects by comparing the names of repositories they worked on
            // TODO: At the moment we go over each pair twice.
            foreach (KeyValuePair<string, HashSet<string>> firstUser in mapUserRepositories)
            {
                foreach (KeyValuePair<string, HashSet<string>> secondUser in mapUserRepositories)
                {
                    if (firstUser.Key != secondUser.Key)
                    {
                        // We compute the intersections of the list of repositories and then count the number of items
                        IEnumerable<string> commonProjects = firstUser.Value.Intersect(secondUser.Value);
                        if (commonProjects.Count() > 0)
                        {
                            // Two members have a common repository to which they are contributing, except for the 
                            // currently analyzed repository.
                            structureGraph.AddEdge(firstUser.Key, secondUser.Key);
                            commonProjectsConnection = true;
                        }
                    }
                }
            }
            return commonProjectsConnection;
        }

        /// <summary>
        /// This method computes the follower/following connections between each of the members,
        /// but its result does not distinguish between followers and following. 
        /// </summary>
        /// <param name="mapUserFollowers">A mapping for each username to a list of the users followers.</param>
        /// <param name="mapUserFollowing">A mapping for each username to a list of the users that they themselves 
        /// follow.</param>
        /// <returns>A mapping for each username to a combined set of followers and following from which the names 
        /// have been extracted.</returns>
        private static bool AddFollowConnections(
            ref Graph<string> structureGraph,
            Dictionary<string, HashSet<string>> mapUserFollowers,
            Dictionary<string, HashSet<string>> mapUserFollowing)
        {
            bool followConnection = false;

            // Obtain a mapping from all users (usernames) to the names of the followers and following
            foreach (string user in mapUserFollowers.Keys)
            {
                HashSet<string> followerOrFollowing = new HashSet<string>(mapUserFollowers[user].Union(mapUserFollowing[user]));
                foreach (string follow in followerOrFollowing)
                {
                    // Two members have a follower/following relation.
                    structureGraph.AddEdge(user, follow);
                    followConnection = true;
                }
            }
            return followConnection;
        }

        /// <summary>
        /// Computes the connections between pull request authors and pull request commenters.
        /// </summary>
        /// <param name="mapPullReqsToComments">A mapping from each pull request to their pull request review comments.</param>
        /// <returns>A mapping for each user to all other users that they're connected to through pull requests.</returns>
        private static bool AddPullReqConnections(
            ref Graph<string> structureGraph,
            Dictionary<PullRequest, List<IssueComment>> mapPullReqsToComments,
            HashSet<string> memberUsernames)
        {
            bool pullReqConnection = false;
            // Add the connections for each pull request commenter and author
            foreach (KeyValuePair<PullRequest, List<IssueComment>> mapPullReqToComments in mapPullReqsToComments)
            {
                string pullReqAuthor = mapPullReqToComments.Key.User.Login;
                // Make sure that the pull request author is also a member
                // (i.e., whether they committed to this repository at least once)
                if (pullReqAuthor != null && memberUsernames.Contains(pullReqAuthor))
                {
                    foreach (IssueComment comment in mapPullReqToComments.Value)
                    {
                        string pullReqCommenter = comment.User.Login;
                        // Make sure that the pull request commenter is also a member
                        // (i.e., whether they committed to this repository at least once)
                        if (pullReqCommenter != null && memberUsernames.Contains(pullReqCommenter))
                        {
                            // Two members have had a recent pull request interaction.
                            structureGraph.AddEdge(pullReqAuthor, pullReqCommenter);
                            pullReqConnection = true;
                        }
                    }
                }
            }
            return pullReqConnection;
        }

    }
}