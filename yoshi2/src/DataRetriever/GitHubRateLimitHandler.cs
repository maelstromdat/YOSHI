using Octokit;
using System;
using System.Threading;
using System.Threading.Tasks;

namespace YOSHI.DataRetrieverNS
{
    public static class GitHubRateLimitHandler
    {

        // AUXILIARY: Methods used to delegate GitHub API calls and handling of rate limits. 

        /// <summary>
        /// This method is used to delegate the GitHub API requests. It handles the rate limit. 
        /// </summary>
        /// <typeparam name="T">The type that func will return.</typeparam>
        /// <param name="func">The function that we want to call.</param>
        /// <param name="repoOwner">The name of the repository owner, whose repository we want data from.</param>
        /// <param name="repoName">The name of the repository we want to get data from.</param>
        /// <returns>No object or value is returned by this method when it completes.</returns>
        /// <exception cref="Exception">Throws an exception if after 3 times of trying to retrieve data, 
        /// the data RateLimitExceededException still occurs, or if another exception is thrown.</exception>
        public async static Task<T> Delegate<T>(
            Func<string, string, Task<T>> func,
            string repoOwner,
            string repoName)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="sha">Commit sha of the commit to retrieve.</param>
        public async static Task<T> Delegate<T>(
            Func<string, string, string, Task<T>> func,
            string repoOwner,
            string repoName,
            string sha)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, sha);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="number">Number of the pull request to retrieve.</param>
        public async static Task<T> Delegate<T>(
            Func<string, string, int, Task<T>> func,
            string repoOwner,
            string repoName,
            int number)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, number);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="maxBatchSize">Setting API options to retrieve max batch sizes, reducing the number of requests.</param>
        public async static Task<T> Delegate<T>(
            Func<string, string, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="maxBatchSize">Setting API options to retrieve max batch sizes, reducing the number of requests.</param>
        public async static Task<T> Delegate<T>(
            Func<string, string, CommitRequest, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            CommitRequest commitRequest,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, commitRequest, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="state">The milestone request applying a state filter. Can be "open", "closed", or "all".
        /// https://docs.github.com/en/rest/reference/issues#list-milestones
        /// </param>
        public async static Task<T> Delegate<T>(
            Func<string, string, MilestoneRequest, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            MilestoneRequest state,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, state, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="state">The pull request request applying a state filter. Can be "open", "closed", or "all".
        /// https://docs.github.com/en/rest/reference/pulls#list-pull-requests
        /// </param>
        public async static Task<T> Delegate<T>(
            Func<string, string, PullRequestRequest, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            PullRequestRequest state,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, state, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="since">Only comments updated at or after this time are returned.
        /// https://docs.github.com/en/rest/reference/pulls#list-review-comments-in-a-repository
        /// </param>
        public async static Task<T> Delegate<T>(
            Func<string, string, IssueCommentRequest, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            IssueCommentRequest since,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, since, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="id">An extra paramater to specify an ID to get a specific item from a repository.</param>
        public async static Task<T> Delegate<T>(
            Func<string, string, int, ApiOptions, Task<T>> func,
            string repoOwner,
            string repoName,
            int id,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(repoOwner, repoName, id, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <summary>
        /// This method is used to delegate the GitHub API requests. It handles the rate limit. 
        /// </summary>
        /// <typeparam name="T">The type that func will return.</typeparam>
        /// <param name="func">The function that we want to call.</param>
        /// <param name="username">The username, whose data we want to retrieve.</param>
        /// <returns>No object or value is returned by this method when it completes.</returns>
        /// <exception cref="Exception">Throws an exception if after 3 times of trying to retrieve data, 
        /// the data RateLimitExceededException still occurs, or if another exception is thrown.</exception>
        public async static Task<T> Delegate<T>(
            Func<string, Task<T>> func,
            string username)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(username);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <param name="maxBatchSize">The username, whose data we want to retrieve.</param>
        public async static Task<T> Delegate<T>(
            Func<string, ApiOptions, Task<T>> func,
            string username,
            ApiOptions maxBatchSize)
        {
            for (int i = 0; i < 3; i++)
            {
                try
                {
                    Task<T> task = func(username, maxBatchSize);
                    return await task;
                }
                catch (RateLimitExceededException)
                {
                    // When we exceed the rate limit we check when the limit resets and wait until that time before we try 2 more times.
                    WaitUntilReset();
                }
            }
            throw new Exception("Failed too many times to retrieve GitHub data.");
        }

        /// <summary>
        /// A method to take care of the waiting until the GitHub rate reset. 
        /// </summary>
        private static void WaitUntilReset()
        {
            Console.ForegroundColor = ConsoleColor.Magenta;
            // Set the default wait time to one hour
            TimeSpan timespan = TimeSpan.FromHours(1);

            ApiInfo apiInfo = DataRetriever.Client.GetLastApiInfo();
            RateLimit rateLimit = apiInfo?.RateLimit;
            DateTimeOffset? whenDoesTheLimitReset = rateLimit?.Reset;
            if (whenDoesTheLimitReset != null)
            {
                DateTimeOffset limitReset = (DateTimeOffset)whenDoesTheLimitReset;
                timespan = (DateTimeOffset)whenDoesTheLimitReset - DateTimeOffset.Now;
                timespan = timespan.Add(TimeSpan.FromSeconds(30)); // Add 30 seconds to the timespan

                Console.WriteLine("GitHub Rate Limit reached. Time: " + DateTime.Now.ToString());
                Console.WriteLine("Waiting until: " + limitReset.AddSeconds(30).DateTime.ToLocalTime().ToString());
            }
            else
            {
                // If we don't know the reset time, we wait the default time of 1 hour
                Console.WriteLine("Waiting until: " + DateTimeOffset.Now.DateTime.ToLocalTime().AddHours(1));
            }
            Console.ResetColor(); // Reset before sleep, otherwise color remains even when application is closed during the sleep.
            Thread.Sleep(timespan); // Wait until the rate limit resets
            Console.ForegroundColor = ConsoleColor.Magenta;
            Console.WriteLine("Done waiting for the rate limit reset, continuing now: " + DateTime.Now.ToString());
            Console.ResetColor();
        }
    }
}