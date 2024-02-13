using System.Collections.Generic;
using System.Globalization;
using System.Linq;
using System.Text;

namespace YOSHI
{
    /// <summary>
    /// Class responsible for the Hofstede Indices. 
    /// </summary>
    public static class HI
    {
        public readonly static Dictionary<string, (int Pdi, int Idv, int Mas, int Uai)> Hofstede
        = new Dictionary<string, (int Pdi, int Idv, int Mas, int Uai)>(new CaseAccentInsensitiveEqualityComparer())
        {
                { "albania", (90, 20, 80, 70) },
                { "algeria", (80, 35, 35, 70) },
                { "angola", (83, 18, 20, 60) },
                { "argentina", (49, 46, 56, 86) },
                { "armenia", (85, 22, 50, 88) },
                { "australia", (38, 90, 61, 51) },
                { "austria", (11, 55, 79, 70) },
                { "azerbaijan", (85, 22, 50, 88) },
                { "bangladesh", (80, 20, 55, 60) },
                { "belarus", (95, 25, 20, 95) },
                { "belgium", (65, 75, 54, 94) },
                { "bhutan", (94, 52, 32, 28) },
                { "bolivia", (78, 10, 42, 87) },
                { "bosnia and herzegovina", (90, 22, 48, 87) },
                { "brazil", (69, 38, 49, 76) },
                { "bulgaria", (70, 30, 40, 85) },
                { "burkina faso", (70, 15, 50, 55) },
                { "canada", (39, 80, 52, 48) },
                { "cape verde", (75, 20, 15, 40) },
                { "chile", (63, 23, 28, 86) },
                { "china", (80, 20, 66, 30) },
                { "colombia", (67, 13, 64, 80) },
                { "costa rica", (35, 15, 21, 86) },
                { "croatia", (73, 33, 40, 80) },
                { "czechia", (57, 58, 57, 74) },
                { "denmark", (18, 74, 16, 23) },
                { "dominican republic", (65, 30, 65, 45) },
                { "ecuador", (78, 8, 63, 67) },
                { "egypt", (70, 25, 45, 80) },
                { "el salvador", (66, 19, 40, 94) },
                { "estonia", (40, 60, 30, 60) },
                { "ethiopia", (70, 20, 65, 55) },
                { "fiji", (78, 14, 46, 48) },
                { "finland", (33, 63, 26, 59) },
                { "france", (68, 71, 43, 86) },
                { "georgia", (65, 41, 55, 85) },
                { "germany", (35, 67, 66, 65) },
                { "ghana", (80, 15, 40, 65) },
                { "greece", (60, 35, 57, 100) },
                { "guatemala", (95, 6, 37, 98) },
                { "honduras", (80, 20, 40, 50) },
                { "hong kong sar", (68, 25, 57, 29) },
                { "hungary", (46, 80, 88, 82) },
                { "iceland", (30, 60, 10, 50) },
                { "india", (77, 48, 56, 40) },
                { "indonesia", (78, 14, 46, 48) },
                { "iran", (58, 41, 43, 59) },
                { "iraq", (95, 30, 70, 85) },
                { "ireland", (28, 70, 68, 35) },
                { "israel", (13, 54, 47, 81) },
                { "italy", (50, 76, 70, 75) },
                { "jamaica", (45, 39, 68, 13) },
                { "japan", (54, 46, 95, 92) },
                { "jordan", (70, 30, 45, 65) },
                { "kazakhstan", (88, 20, 50, 88) },
                { "kenya", (70, 25, 60, 50) },
                { "kuwait", (90, 25, 40, 80) },
                { "latvia", (44, 70, 9, 63) },
                { "lebanon", (75, 40, 65, 50) },
                { "libya", (80, 38, 52, 68) },
                { "lithuania", (42, 60, 19, 65) },
                { "luxembourg", (40, 60, 50, 70) },
                { "malawi", (70, 30, 40, 50) },
                { "malaysia", (100, 26, 50, 36) },
                { "malta", (56, 59, 47, 96) },
                { "mexico", (81, 30, 69, 82) },
                { "moldova", (90, 27, 39, 95) },
                { "montenegro", (88, 24, 48, 90) },
                { "morocco", (70, 46, 53, 68) },
                { "mozambique", (85, 15, 38, 44) },
                { "namibia", (65, 30, 40, 45) },
                { "nepal", (65, 30, 40, 40) },
                { "netherlands", (38, 80, 14, 53) },
                { "new zealand", (22, 79, 58, 49) },
                { "nigeria", (80, 30, 60, 55) },
                { "north macedonia", (90, 22, 45, 87) },
                { "norway", (31, 69, 8, 50) },
                { "pakistan", (55, 14, 50, 70) },
                { "panama", (95, 11, 44, 86) },
                { "paraguay", (70, 12, 40, 85) },
                { "peru", (64, 16, 42, 87) },
                { "philippines", (94, 32, 64, 44) },
                { "poland", (68, 60, 64, 93) },
                { "portugal", (63, 27, 31, 99) },
                { "puerto rico", (68, 27, 56, 38) },
                { "qatar", (93, 25, 55, 80) },
                { "romania", (90, 30, 42, 90) },
                { "russia", (93, 39, 36, 95) },
                { "são tomé and princípe", (75, 37, 24, 70) },
                { "saudi arabia", (95, 25, 60, 80) },
                { "senegal", (70, 25, 45, 55) },
                { "serbia", (86, 25, 43, 92) },
                { "sierra leone", (70, 20, 40, 50) },
                { "singapore", (74, 20, 48, 8) },
                { "slovakia", (100, 52, 100, 51) },
                { "slovenia", (71, 27, 19, 88) },
                { "south africa", (49, 65, 63, 49) },
                { "south korea", (60, 18, 39, 85) },
                { "spain", (57, 51, 42, 86) },
                { "sri lanka", (80, 35, 10, 45) },
                { "suriname", (85, 47, 37, 92) },
                { "sweden", (31, 71, 5, 29) },
                { "switzerland", (34, 68, 70, 58) },
                { "syria", (80, 35, 52, 60) },
                { "taiwan", (58, 17, 45, 69) },
                { "tanzania", (70, 25, 40, 50) },
                { "thailand", (64, 20, 34, 64) },
                { "trinidad and tobago", (47, 16, 58, 55) },
                { "tunisia", (70, 40, 40, 75) },
                { "turkey", (66, 37, 45, 85) },
                { "ukraine", (92, 25, 27, 95) },
                { "united arab emirates", (90, 25, 50, 80) },
                { "united kingdom", (35, 89, 66, 35) },
                { "united states", (40, 91, 62, 46) },
                { "uruguay", (61, 36, 38, 98) },
                { "venezuela", (81, 12, 73, 76) },
                { "vietnam", (70, 20, 40, 30) },
                { "zambia", (60, 35, 40, 50) },
        };

        /// <summary>
        /// Equality comparer of strings that ignores lower/uppercase and accents (diacritics). Note that if the 
        /// Hofstede dictionary was not initialized with this equality comparer, it would likely fail to identify 
        /// "são tomé and princípe" or inconsistencies. This equality comparer has been tested. 
        /// </summary>
        public class CaseAccentInsensitiveEqualityComparer : IEqualityComparer<string>
        {
            public bool Equals(string x, string y)
            {
                return string.Compare(x, y, CultureInfo.InvariantCulture, CompareOptions.IgnoreNonSpace | CompareOptions.IgnoreCase) == 0;
            }

            public int GetHashCode(string obj)
            {
                return obj != null ? this.RemoveDiacritics(obj).ToUpperInvariant().GetHashCode() : 0;
            }

            private string RemoveDiacritics(string text)
            {
                return string.Concat(
                    text.Normalize(NormalizationForm.FormD)
                    .Where(ch => CharUnicodeInfo.GetUnicodeCategory(ch) !=
                                                  UnicodeCategory.NonSpacingMark)
                  ).Normalize(NormalizationForm.FormC);
            }
        }
    }
}