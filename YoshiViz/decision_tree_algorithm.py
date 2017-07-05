
"""
this file is the algorithm part
F1:(DecisionTreeAlgorithm)input the metrics to define(return) the community type
"""
import json
from YoshiViz import *


#from thresholds_functions import Engagement,Informality,Situatedness,NetworkOfPractice

"""
this file is the preparation of decision tree
F1:(Engagement)judge if is high eneagement, return boolean value, True if engagement is high
F2:(Informality)judge if is informal or not, return boolean value, True if it is informal
F3:(Situatedness)judge if is situated, return boolean vaule, True if situatedness is nearby, Flase is for far alway remoteness 
F4:(NetworkOfPractice)judge if is NoP, True if it is NoP
"""
#global constant variable for thresholds
#geographical distance
GEOGRAPHICAL_DISTANCE = 4000
#culture distance(The data is already in percentage)
CULTURAL_DISTANCE = 15.0
#average milestones
AVERAGE_MILESTONES = 0.1
#hierarchical degree
HIERARCHICAL_DEGREE = 0.8
#self similarity
SELF_SIMILARITY = 0.9
#average subscription
AVERAGE_SUBSCRIPTION = 9.794
#average user commits 
AVERAGE_USER_COMMITS = 14.22
#average user collaboration files
AVERAGE_USER_COLLABORATION_FILES = 0.200
#average file contributor
AVERAGE_FILE_CONTRIBUTOR = 1.220


def engagement(
        unique_comments, avg_subscriptions, avg_user_commits,
        avg_user_collaboration_files, avg_file_contributors):
    """
    Define the engagement type of a software community.
    :param unique_comments: Only one person is using comment? (boolean)
    :param avg_subscriptions: Average subscriptions number.
    :param avg_user_commits: Average number of commits per user.
    :param avg_user_collaboration_files: Average number of file
           collaboratively created by users.
    :param avg_file_contributors:
    :return: True if high engagement, false otherwise.
    """
    if (unique_comments and
        avg_subscriptions >= AVERAGE_SUBSCRIPTION and
        avg_user_commits >= AVERAGE_USER_COMMITS and
        avg_user_collaboration_files >= AVERAGE_USER_COLLABORATION_FILES and
            avg_file_contributors >= AVERAGE_FILE_CONTRIBUTOR):
        return True
    else:
        return False


def informality(avg_milestones_period, hierarchy_degree, has_wiki):
    """
    Judge if the community is informal or not.
    :param avg_milestones_period: Average time spent for each milestone.
    :param hierarchy_degree: Hierarchy degree.
    :param has_wiki: True if the community has a wiki, false otherwise.
    :return: True if the community is informal, false otherwise.
    """
    if (avg_milestones_period <= AVERAGE_MILESTONES and
        hierarchy_degree <= HIERARCHICAL_DEGREE and
            has_wiki):
        return True
    else:
        return False


def situatedness(avg_distance, avg_cultural_distance):
    """
    Judge if the community is physically situated,
    :param avg_distance: Average physical distance of the members of this
    community.
    :param avg_cultural_distance: Average cultural distance of the members of this
    community.
    :return: True if the community is physically situated, False otherwise.
    """
    if (avg_distance <= GEOGRAPHICAL_DISTANCE and
            avg_cultural_distance <= CULTURAL_DISTANCE):
        return True
    else:
        return False


def network_of_practice(self_similarity):
    """
    Judge if this community type is network of practice.
    :param self_similarity: Self similarity of the community.
    :return: True if the community's type is network of practice, false otherwise.
    """
    if self_similarity >= SELF_SIMILARITY:
        return True
    else:
        return False
'''
above is the thresholds functions
'''


def community_type(file_directory, repository_name):
    """
    Returns the various properties of one specific community.
    :param file_directory: File directory.
    :param repository_name: Name of the wanted repository.
    :return: Repository properties dict.
    """
    #open file
    file = open(file_directory)
    #temp(string) txt to string
    temp = file.read()
    data = json.loads(temp)
    for i in range(len(data['DataCommunity'])):
        if data['DataCommunity'][i]['RepoName'] == repository_name:
            temp_community_type = decision_tree_algorithm(
                data['DataCommunity'][i]['uniqueCommenterExists'],
                data['DataQuality'][i]['avgSubscriptions'],
                data['DataQuality'][i]['avgeUserCommits'],
                data['DataQuality'][i]['avgUserCollaborationFiles'],
                data['DataQuality'][i]['avgeFileContributors'],
                data['DataCommunity'][i]['avgMilestonesPeriod'],
                data['DataCommunity'][i]['hierarchyDegree'],
                data['DataQuality'][i]['hasWiki'],
                data['DataCommunity'][i]['avgDistance'],
                data['DataCommunity'][i]['avgCulturalDistance'],
                data['DataCommunity'][i]['selfSimilarity'])
            return temp_community_type
        if i == len(data['DataCommunity'])-1:
            raise Exception("Community not found")


def decision_tree_algorithm(
    unique_comments, avg_subscriptions, avg_user_commits,
    avg_user_collaboration_files, avg_file_contributors,
    avg_milestones_period, hierarchy_degree, has_wiki,
        avg_distance, avg_cultural_distance, self_similarity):
    """
    Using several community's properties, this algorithm
    defines the type of the community observed.
    :param unique_comments: Number of unique comments.
    :param avg_subscriptions: Average number of subscriptions.
    :param avg_user_commits: Average number of user commits.
    :param avg_user_collaboration_files: Average number of collaborators per file.
    :param avg_file_contributors: Average file owned by contributors.
    :param avg_milestones_period: Average period used to complete a milestone.
    :param hierarchy_degree: Hierarchy degree.
    :param has_wiki: True if the community has a wiki.
    :param avg_distance: Average physical distance between the members.
    :param avg_cultural_distance: Average cultural distance between the members.
    :param self_similarity: Self similarity.
    :return: String of the community type.
    """

    community_type_list = []
    #Situatedness
    if situatedness(avg_distance, avg_cultural_distance):
        community_type_list.append('Community of Practice')
    #Informality
    if informality(avg_milestones_period, hierarchy_degree, has_wiki):
        community_type_list.append('Informal Network')
        #Engagement
        if engagement(unique_comments, avg_subscriptions, avg_user_commits,
                      avg_user_collaboration_files, avg_file_contributors):
            community_type_list.append('Informal Community')
            return ', '.join(community_type_list)
        #judge if is NoP
        elif network_of_practice(self_similarity) and \
                not situatedness(avg_distance, avg_cultural_distance):
            community_type_list.append('Network of Practice')
            return ', '.join(community_type_list)
        #neither IC and NoP
        else:
            return ', '.join(community_type_list)
            
    else:
        community_type_list.append('Formal Network')
        if engagement(unique_comments, avg_subscriptions, avg_user_commits,
                      avg_user_collaboration_files, avg_file_contributors):
            community_type_list.append('Informal Community')
            return ', '.join(community_type_list)
        elif network_of_practice(self_similarity) and \
            not situatedness(avg_distance, avg_cultural_distance):
            community_type_list.append('Network of Practice')
            return ', '.join(community_type_list)
        else:
            return ', '.join(community_type_list)


'''
#file director        
fileDirectory = r'.\input.txt'
#test decision tree
#repositoryName = input("Please input repository name:\n")
repositoryName = 'bootstrap'
print(CommunityType(fileDirectory,repositoryName))
'''



#test for all repository for one time
#file director        
#fileDirectory = r'.\input.txt'
#read from txt
#temp = TxtToString(fileDirectory)
#string to python object
#data = StringToObj(temp)
'''
for i in range(len(data['DataCommunity'])):

        tempCommunityType = DecisionTreeAlgorithm(
                    data['DataCommunity'][i]['uniqueCommenterExists'],
                    data['DataQuality'][i]['avgSubscriptions'],
                    data['DataQuality'][i]['avgeUserCommits'],
                    data['DataQuality'][i]['avgUserCollaborationFiles'],
                    data['DataQuality'][i]['avgeFileContributors'],
                    data['DataCommunity'][i]['avgMilestonesPeriod'],
                    data['DataCommunity'][i]['hierarchyDegree'],
                    data['DataQuality'][i]['hasWiki'],
                    data['DataCommunity'][i]['avgDistance'],
                    data['DataCommunity'][i]['avgCulturalDistance'],
                    data['DataCommunity'][i]['selfSimilarity'])
        print(data['DataCommunity'][i]['RepoOwner'],data['DataCommunity'][i]['RepoName'],tempCommunityType)
'''



        
#test
#print related object
#print (data['DataQuality'][0]['RepoOwner'],data['DataQuality'][0]['monthlyEventsStdDev'])
#print (data['DataCommunity'][0]['RepoOwner'],data['DataCommunity'][0]['avgMilestonesPeriod'])


#test high engagement(true is high engagement)
'''
for i in range(len(data['DataCommunity'])):
    highEngagement = Engagement(data['DataCommunity'][i]['uniqueCommenterExists'],data['DataQuality'][i]['avgSubscriptions'],data['DataQuality'][i]['avgeUserCommits'],data['DataQuality'][i]['avgUserCollaborationFiles'],data['DataQuality'][i]['avgeFileContributors'])
    print (highEngagement)
'''

#test informality(true is informal) of all
'''
for i in range(len(data['DataCommunity'])):
    informal = Informality(data['DataCommunity'][i]['avgMilestonesPeriod'],data['DataCommunity'][i]['hierarchyDegree'],data['DataQuality'][i]['hasWiki'])
    print (informal)
'''

#test situated distance (true is near situatedness) of all
'''
for i in range(len(data['DataCommunity'])):
    situated = Situatedness(data['DataCommunity'][i]['avgDistance'], data['DataCommunity'][i]['avgCulturalDistance'])
    print (situated)
'''
#test self similarity (trur is high self-similartity) of all
'''
for i in range(len(data['DataCommunity'])):
    selfSimi = NetworkOfPractice(data['DataCommunity'][i]['selfSimilarity'])
    print (selfSimi)

'''

    

