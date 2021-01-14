YOSHI
=====

a tool to study online software development communities

Open-source communities are powerful assets for software development. However, many such communities receive little or no steering support, since they may not refer to any one leader, let alone an organisation or sponsor. Also, deeper knowledge about the organisational and social workings behind a community might foster external contribution, critical for communities’ longevity. This paper outlines YOSHI, i.e., “Yielding Open-Source Health Information”, as an attempt to offer organisational and social transparency as well as computer-assisted steering to open-source communities, by inheriting and implementing insights from organisations and social networks research. YOSHI typifies open-source communities by eliciting and processing their key characteristics. This improves open-source communities’ transparency and efficiency, e.g., by allowing informed governance, more in line with communities’ own desired way of working. We evaluate YOSHI studying cases from GitHub.

YOSHI Architecture

First, an information retrieval component is responsible for retrieving data with which YOSHI can perform its functions. The retrieval component automates the retrieval of data from public repositories of projects hosted on Github, using GET requests form GitHub Archive to obtain publicly available data. This component retrieves data from two data sources: source code management systems and issue trackers. GitHub Archive is a project to record the public GitHub timeline, archive it, and make it easily accessible for further analysis and this archive dataset can be accessed via Google BigQuery. This data is used to compute attributes values related to the software development process and study targeted open-source software development communities. GitHub Java API is the library used for communicating with the GitHub API, supporting the GitHub v3 API. The client package contains classes that communicate with the GitHub API over HTTPS and the client is responsible for converting JSON responses to appropriate Java model classes. The package contains the classes that invoke API calls and return model classes representing resources that were created, read, updated, or deleted. The Service classes (see middle part of Fig. 1) provided by this API are used for retrieving information about the file structure of the repository, the members contributing to the project, issues associated to a repositories and their current status.

Second, the processing component is responsible for evaluating metrics using data available from the retrieval component and compute community typification. This component uses: (a) Gephi - a Java library which provides useful and efficient network visualisation and exploration techniques; (b) Google Geocoding API - used for converting the addresses of reposi- tories members into geographic coordinates, which is used to calculate distances. This API provides a direct way to access services via an HTTP request. this component implements an algorithm reproducing the decision-tree from [8], also repre- sented as Algorithm 1. Also, this component is responsible to evaluate community metrics against related thresholds from [11] to establish the presence of key community characteristics necessary for Algorithm 1 to perform its function. 

Third, the visualisation component uses data computed by the processing component to create graphical representations of it such as the geographical distribution of members, project members longevity, etc. This component is able to export as pictures and Comma-Separated Values (CSV) the produced representations, if needed.
