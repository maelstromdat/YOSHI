using System;
using System.Collections.Generic;
using System.Linq;

namespace YOSHI
{
    /// <summary>
    /// Class that implements statistics computations. Cannot implement a generic method that takes numerics in c#:
    /// See https://stackoverflow.com/questions/22261510/creating-a-method-in-c-sharp-that-can-take-double-decimal-and-floats-without-r
    /// Therefore we repeat code.
    /// </summary>
    public static class Statistics
    {
        /// <summary>
        /// Given a list of integers, this method sorts the list in place and then computes the average median. 
        /// I.e., the median whenever the list has an odd number of elements, the average of the middle 2 elements if 
        /// the list has an even number of elements.
        /// </summary>
        /// <param name="list">The list to obtain the median from. Note: Will be modified in place.</param>
        /// <returns>The median from the given list.</returns>
        /// <exception cref="InvalidOperationException">Thrown when list is empty.</exception>
        public static double ComputeMedian(List<int> list)
        {
            if (list.Count > 0)
            {
                list.Sort();
                return list.Count % 2 == 0 ? (list[(list.Count / 2) - 1] + list[list.Count / 2]) / 2.0 : list[list.Count / 2];
            }
            else
            {
                throw new InvalidOperationException("List contains no elements");
            }
        }

        /// <summary>
        /// Given a list of doubles, this method sorts the list in place and then computes the average median. 
        /// I.e., the median whenever the list has an odd number of elements, the average of the middle 2 elements if 
        /// the list has an even number of elements.
        /// </summary>
        /// <param name="list">The list to obtain the median from. Note: Will be modified in place.</param>
        /// <returns>The median from the given list.</returns>
        /// <exception cref="InvalidOperationException">Thrown when list is empty.</exception>
        public static double ComputeMedian(List<double> list)
        {
            if (list.Count > 0)
            {
                list.Sort();
                return list.Count % 2 == 0 ? (list[(list.Count / 2) - 1] + list[list.Count / 2]) / 2.0 : list[list.Count / 2];
            }
            else
            {
                throw new InvalidOperationException("List contains no elements");
            }
        }

        /// <summary>
        /// Easy access method to compute the variance of a list.
        /// </summary>
        /// <param name="list">List to compute the variance of. May not be empty.</param>
        /// <returns>The variance of the list.</returns>
        /// <exception cref="InvalidOperationException">Thrown when list is empty.</exception>
        public static double ComputeVariance(List<double> list)
        {
            if (list.Count > 0)
            {
                double mean = list.Average();
                double temp = 0;
                foreach (double value in list)
                {
                    temp += (value - mean) * (value - mean);
                }
                return temp / list.Count;
            }
            else
            {
                throw new InvalidOperationException("List contains no elements");
            }
        }

        /// <summary>
        /// Easy access metthod to compute the standard deviation of a list of doubles.
        /// </summary>
        /// <param name="list">List to compute the standard deviation of. May not be empty.</param>
        /// <returns>The standard deviation of the list.</returns>
        /// <exception cref="InvalidOperationException">Thrown when list is empty.</exception>
        public static double ComputeStandardDeviation(List<double> list)
        {
            return list.Count > 0 ? Math.Sqrt(ComputeVariance(list)) : throw new InvalidOperationException("List contains no elements");
        }
    }
}