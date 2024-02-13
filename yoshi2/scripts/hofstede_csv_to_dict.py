import pandas as pd

# Read the Hofstede indices into a pandas dataframe
data = pd.read_csv("..\\data\\Hofstede Insights - Manual 2021-05-13.csv", delimiter=",", index_col="country")

# Transform all data in the dataframe to strings
data["pdi"]     = data["pdi"].astype(str)
data["idv"]     = data["idv"].astype(str)
data["mas"]     = data["mas"].astype(str)
data["uai"]     = data["uai"].astype(str)

result = ""

for country, row in data.iterrows():
    # Generate the C# code to add the Hofstede metrics to a dictionary of the form:
    # Dictionary<string, (int Pdi, int Idv, int Mas, int Uai)>
    result += "{ \"" + country.lower() + "\", (" + row["pdi"] + ", " + row["idv"] + ", " + row["mas"] + ", " + row["uai"] + ") },\n"

# Print the result so we can copy the generated c# code from the console
print(result)