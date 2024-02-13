using Geocoding;
using System;
using System.Collections.Generic;
using System.Linq;
using YOSHI.CommunityData;

namespace YOSHI.CharacteristicProcessorNS
{
    public static partial class CharacteristicProcessor
    {
        /// <summary>
        /// A method that computes several metrics used to measure community dispersion. It modifies the given community.
        /// </summary>
        /// <param name="community">The community for which we need to compute the dispersion.</param>
        private static void ComputeDispersion(Community community)
        {
            List<Location> coordinates = community.Data.Coordinates;
            List<string> countries = community.Data.Countries;

            // Compute the variance of all geographical distances
            List<double> distances = ComputeGeographicalDistances(coordinates);
            double varianceGeographicalDistance = Statistics.ComputeVariance(distances);
            community.Metrics.Dispersion.VarianceGeographicalDistance = varianceGeographicalDistance;
            // Note: Geographical distance includes distances to members from who we do not have Hofstede indices for better accuracy.

            // Compute the variance for four Hofstede indices
            (List<double> pdis, List<double> idvs, List<double> mass, List<double> uais) = ComputeHofstedeIndices(countries);
            double variancePdi = Statistics.ComputeVariance(pdis);
            double varianceIdv = Statistics.ComputeVariance(idvs);
            double varianceMas = Statistics.ComputeVariance(mass);
            double varianceUai = Statistics.ComputeVariance(uais);

            // Determine the average of the variances to obtain the variance of cultural distance
            double varianceCulturalDistance = (variancePdi + varianceIdv + varianceMas + varianceUai) / 4;
            community.Metrics.Dispersion.VarianceHofstedeCulturalDistance = varianceCulturalDistance;

            // Determine the global dispersion
            community.Characteristics.Dispersion = Math.Sqrt((varianceGeographicalDistance + varianceCulturalDistance) / 2);

            // EXTRA COMPUTATIONS FOR COMPARISON YOSHI AND YOSHI 2
            community.Metrics.Dispersion.AverageGeographicalDistance = distances.Average();
            double averagePdi = Statistics.ComputeStandardDeviation(pdis);
            double averageIdv = Statistics.ComputeStandardDeviation(idvs);
            double averageMas = Statistics.ComputeStandardDeviation(mass);
            double averageUai = Statistics.ComputeStandardDeviation(uais);
            community.Metrics.Dispersion.AverageCulturalDispersion = (averagePdi + averageIdv + averageMas + averageUai) / 4.0;
        }

        /// <summary>
        /// Given a list of coordinates, this method computes the list of geographical distances between each unique pair 
        /// of coordinates. It computes the distance using the spherical distance.
        /// </summary>
        /// <param name="coordinates">A list of coordinates for which we want to compute the geographical
        /// distance between each pair.</param>
        /// <returns>A list of geographical distances between each unique pair of coordinates.</returns>
        private static List<double> ComputeGeographicalDistances(List<Location> coordinates)
        {
            // TODO: threshold (percentage) for number of addresses should be set in DataRetriever
            List<double> distances = new List<double>();

            // Compute the medium distance for each distinct pair of addresses in the given list of addresses
            for (int i = 0; i < coordinates.Count - 1; i++)
            {
                Location coordinateA = coordinates[i];
                for (int j = i + 1; j < coordinates.Count; j++)
                {
                    Location coordinateB = coordinates[j];
                    // NOTE: the DistanceBetween method computes spherical distance
                    double distance = coordinateA.DistanceBetween(coordinateB, DistanceUnits.Kilometers);
                    distances.Add(distance);
                }
            }

            return distances;
        }

        /// <summary>
        /// Given a list of addresses, this method compiles separate lists for the present countries'
        /// corresponding hofstede indices (PDI, IDV, MAS, UAI)
        /// </summary>
        /// <param name="countries">A list of countries for which we want to retrieve the Hofstede indices.</param>
        /// <returns>Four lists of Hofstede indices representative for the given addresses.</returns>
        private static (List<double> pdis, List<double> idvs, List<double> mass, List<double> uais)
            ComputeHofstedeIndices(List<string> countries)
        {
            List<double> pdis = new List<double>();
            List<double> idvs = new List<double>();
            List<double> mass = new List<double>();
            List<double> uais = new List<double>();

            foreach (string country in countries)
            {
                pdis.Add(HI.Hofstede[country].Pdi);
                idvs.Add(HI.Hofstede[country].Idv);
                mass.Add(HI.Hofstede[country].Mas);
                uais.Add(HI.Hofstede[country].Uai);
            }

            return (pdis, idvs, mass, uais);
        }
    }
}