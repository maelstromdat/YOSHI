We retrieved Hofstede Indices from multiple sources.

Source: https://geerthofstede.com/research-and-vsm/dimension-data-matrix/
(1) 6D geerthofstede dimension data matrix 2015-12-08-0-100-no-processing
(2) 4D geerthofstede dimension data matrix 2015-12-08-0-100-removed-nulls
(3) 6D geerthofstede dimension data matrix 2015-12-08-0-100-removed-nulls

(1) was downloaded from the aforementioned source. (2) and (3) have been derived from this dataset. For (2) we removed the 'ltowvs' and 'ivr' columns and then removed countries that still reported null values. For (3) we removed the countries that had null values. 

These datasets have not been operationalized in YOSHI.

Source: https://www.hofstede-insights.com/country-comparison/
Hofstede Insights - Manual 2021-05-13

This dataset was manually extracted from the Country Comparison Tool. This dataset was also implemented in YOSHI. We made the following changes to the country names to fit the Bing Maps responses: 
Czech Republic 	--> Czechia
Hong Kong 	--> Hong Kong sar