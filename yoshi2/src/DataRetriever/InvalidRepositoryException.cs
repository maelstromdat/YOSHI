using System;

namespace YOSHI.DataRetrieverNS
{
    /// <summary>
    /// Class used to identify invalid repositories
    /// </summary>
    public class InvalidRepositoryException : Exception
    {
        public InvalidRepositoryException()
        {
        }
        public InvalidRepositoryException(string message)
            : base(message)
        {
        }
        public InvalidRepositoryException(string message, Exception inner)
            : base(message, inner)
        {
        }
    }
}
