# Ontology Development Skill

This skill encompasses what you should do when developing an ontology.

## Data Analysis

First, analyze correctly the input data and requirements.

There is a PDF file that describes the structure of each XML file, with some semantic meaning, although not much.
There is also the .xsd and .xml files themselves, which you should cross-reference with the PDF that describes them.

Create a list of every term you extract from the PDF.

The output of this step should be a .md file with each entity and its respective fields

## Vocabulary Alignment

The next step is vocabulary aligment, where you will search for pre-existing vocabulary terms that can represent the fields in the input data

Search academic knowledge bases such as Google Scholar or arXiv for literature regarding ontologies that try to model the domain in question or similar. 

The output of this step should be a markdown file with a table. This table should have a row matching each field in the input data and each column should be an existing vocabulary. Each cell value should be the vocabulary term that exists that can semantically represent that field.

If a row has more than one possible term to match, explain each one in the file and choose the one you think is best.

Each ontology should be correctly referenced to its base URI, so they can be manually checked and confirmed.

## Conceptual Model Diagram

With the vocabulary aligntment done, the final step is to build a conceptual model of the information and data hierarchy. In this case, what is most important is how different entities relate to eachother, not necessarily in a tree-like structure like XML, remember this is Web Semantics and Linked Data.

Write the diagram using the mermaid language. Make the diagram compact, it should just contain boxes with the entity's name and arrows to other entities related to it. 

Don't try to cram everything into one diagram, modularize it into logical segments. For example, a diagram can show the conceptual model of the "Members of Parliament" class, while another diagram can describe the conceptual model of the "Events" class.

The mermaid chart should be written as a flowchart, with the elk layout.