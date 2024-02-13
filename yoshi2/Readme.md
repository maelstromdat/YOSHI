# YOSHI Revision #

## About ##

[//]: # (TODO: give an explanation of YOSHI)

The original YOSHI is not usable anymore due to deprecated APIs. Due to the lack of documentation for the code, we decided to implement a revised YOSHI according to the paper by D.A. Tamburri et al.: 
```
@article{DBLP:journals/ese/TamburriPSZ19,
  author    = {Damian A. Tamburri and
               Fabio Palomba and
               Alexander Serebrenik and
               Andy Zaidman},
  title     = {Discovering community patterns in open-source: a systematic approach
               and its evaluation},
  journal   = {Empir. Softw. Eng.},
  volume    = {24},
  number    = {3},
  pages     = {1369--1417},
  year      = {2019},
  url       = {https://doi.org/10.1007/s10664-018-9659-9},
  doi       = {10.1007/s10664-018-9659-9},
  timestamp = {Tue, 25 Aug 2020 16:58:55 +0200},
  biburl    = {https://dblp.org/rec/journals/ese/TamburriPSZ19.bib},
  bibsource = {dblp computer science bibliography, https://dblp.org}
}
```

Our specific implementation is described in the Master's Thesis by Jari van Meijel. 

[//]: # (TODO: Describe where to find the thesis.)

---
## Installation ##

### Windows x64 ###
To use this application you need your own GitHub Access Token and Bing Maps Key, which will be described in the steps below.
1. Download the latest Windows x64 release from this GitHub repository. 

[//]: # (TODO: upload a release to the GitHub repository.)

2. Get your own GitHub Access Token: https://docs.github.com/en/github/authenticating-to-github/keeping-your-account-and-data-secure/creating-a-personal-access-token 
3. Get your own Bing Maps Key: https://docs.microsoft.com/en-us/bingmaps/getting-started/bing-maps-dev-center-help/getting-a-bing-maps-key
4. Set the keys as environment variables:

| Variable                | Value                            |
| -----------------       | -------------------------------- |
| YOSHI_BingMapsKey       | ```<Your-Bing-Maps-Key>```       |
| YOSHI_GitHubAccessToken | ```<Your-GitHub-Access-Token>``` |

### Other Platforms ### 

1. Download the GitHub repository.
2. Open the project in Visual Studio 2019 (Download here: https://visualstudio.microsoft.com/downloads/). 
3. Open a terminal in Visual Studio 2019.
4. Find the command to obtain an executable for the specified platform on: https://docs.microsoft.com/en-us/dotnet/core/deploying/
5. Run the command.<br />

---
## How To Use ##

1. Prepare an input file (.csv). The file must have the following header: ```RepoOwner,RepoName```<br />
On each row you can specify a repository as follows: ```<repository-owner>,<repository-name>```

2. When running the application you will be prompted to enter the absolute directory of the input file, including the filename and its extension. <br /> 
E.g., if it is stored in your downloads folder and the file is called input.csv:<br />
    ```C:\Users\<username>\Downloads\input.csv```<br />
Note: Even if the file path has spaces, do not use quotation marks.
3. Next you will be prompted to enter the output path. Do not include the filename.<br />
E.g., if you want to store the output in a file in the downloads folder: <br />
    ```C:\Users\<username>\Downloads\```<br />
Note: Even if the path has spaces, do not use quotation marks.
4. Next enter the output filename (do not include an extension, its extension will be .csv)<br />
E.g., if you want to name the output file "output"<br />
    ```output```<br />
Note: If the file already exists, you will be asked to input a different filename.
5. Next you need to specify how many bing requests you have left. Bing Maps limits the amount of free requests per different type of key. Since there is no way to retrieve the amount of requests left through an API call, we ask you to specify it yourself. You can find the usage report for your key in the Bing Maps Portal: https://www.bingmapsportal.com/<br /> 
E.g., if you have 400 requests left:<br />
    ```400```

After following these steps the application will process the GitHub repositories specified in the input file. After finishing processing a community, the data is written to the output file. This action fails if the output file is opened in another program, therefore, do not open the output file while the application is running. Note: the output file will not include communities that the application failed to process.

### IMPORTANT: Disable Quick Edit Mode ###

Note that it is possible to halt a console application in Windows due to the command line's Quick Edit mode. This is very difficult to notice. To get rid of this behavior, right click the top border of the command window and select ```Properties```. Then in ```Options > Edit Options``` make sure to disable ```Quick Edit Mode```. 

### Console Logging Colors ###

The console will log its progress (not written to an output file). To make some messages easily recognizable we used the following color coding of the log messages:

| Color       | Description                                                                                |
| ----------- | ------------------------------------------------------------------------------------------ |
| White       | User input and general progress reports.                                                   |
| Dark Gray   | Messages that ask for user input.                                                          |
| Green       | The start and end of processing a community.                                               |
| Dark Green  | The program has finished and is ready to be closed.                                        |
| Cyan        | Updates on GitHub and Bing Maps Rate Limits.                                               |
| Magenta     | Pauses caused by the GitHub Rate Limit.                                                    |
| Blue        | Filtered bot and organization accounts.                                                    |
| Yellow      | A community does not fulfill the minimum requirements.                                     |
| Dark Yellow | Bing Maps geocoding exceptions (caught and skipped).                                       |
| Red         | Caught exceptions that caused the retrieval of community patterns for a community to fail. |

---