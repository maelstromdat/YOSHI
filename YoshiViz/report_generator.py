import os
import json
from jinja2 import Template


def generate_pdf_report(yoshi_output_file, project_name, community_type):
    """
    Generates a PDF report of a given output of Yoshi.
    :param yoshi_output_file: Path to the Yoshi output file.
    :param project_name: Name of the project.
    :param community_type
    """
    yoshi_file = open(yoshi_output_file, "r")
    json_data = json.load(yoshi_file)
    community_index = _find_community_index(json_data, project_name)
    yoshi_file.close()
    template_file = open(os.path.join(os.path.abspath('.'),
                                      'YoshiViz', 'output', 'template.jinja'), 'r')
    template = Template(template_file.read())
    template_file.close()
    img_link, description_text = _find_img_link(community_type)
    output = template.render(DataQuality=json_data['DataQuality'][community_index],
                             DataCommunity=json_data['DataCommunity'][community_index],
                             community_type=community_type,
                             img_links=img_link,
                             description_text=description_text)
    output_file = open(os.path.join(os.path.abspath('.'), 'YoshiViz', 'output',
                                    project_name + '_report.html'), 'w')
    output_file.write(output)
    output_file.close()


def _find_community_index(json_data, community_name):
    """
    DANGER

    Extracts the various community metrics from a json string.
    :param json_data: json string.
    :param community_name: Name of the community to extract.
    :return: Community object of the wanted community.
    """

    #First we retrieve the good community from the mess up json…
    index = 0
    found = False
    while not found:
        try:
            if json_data['DataQuality'][index]['RepoName'] == community_name:
                return index
            else:
                index += 1
        except:
            raise Exception("The community " +
                            community_name + " cannot be found in the specified file.")


def _find_img_link(community_type):
    """
    DANGER
    Return the img link of a community type.
    :param community_type: String containing the community type.
    :return: Community type subtree image link.
    """

    community_list = community_type.split(', ')
    link_list = []
    description_text = ''
    if 'Community of Practice' in community_list:
        description_text += "<h4>Community of Practice:</h4>"
        with open(os.path.join(os.path.abspath('.'),
                    'YoshiViz', 'Community description', 'CoP.txt'), 'r') as f:
            description_text += "<p>" + f.read() + "</p>"
        link_list.append('Cop.jpg')
    if 'Informal Network' in community_list:
        description_text += "<h4>Informal Network:</h4>"
        with open(os.path.join(os.path.abspath('.'),
                    'YoshiViz', 'Community description', 'IN.txt'), 'r') as f:
            description_text += "<p>" + f.read() + "</p>"
        link_list.append('Informal_Network.jpg')
    if 'Informal Community' in community_list:
        description_text += "<h4>Informal community:</h4>"
        with open(os.path.join(os.path.abspath('.'),
                    'YoshiViz', 'Community description', 'IC.txt'), 'r') as f:
            description_text += "<p>" + f.read() + "</p>"
        link_list.append('Informal_community.jpg')
    if 'Network of Practice' in community_list:
        description_text += "<h4>Network of Practice:</h4>"
        with open(os.path.join(os.path.abspath('.'),
                    'YoshiViz', 'Community description', 'NoP.txt'), 'r') as f:
            description_text += "<p>" + f.read() + "</p>"
        link_list.append('Nop.jpg')
    if 'Formal Network' in community_list:
        description_text += "<h4>Formal Network:</h4>"
        with open(os.path.join(os.path.abspath('.'),
                    'YoshiViz', 'Community description', 'FN.txt'), 'r') as f:
            description_text += "<p>" + f.read() + "</p>"
        link_list.append('Formal.jpg')
    return link_list, description_text