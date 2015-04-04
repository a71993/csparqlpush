# csparqlpush

This is a project for integrating c-sparql engine's restful API with pubsubhubbub. 

Requirements:
- RabbitMQ 
- Rabbithub plugin for RabbitMQ, which needs to be same version as RabbitMQ. Can be optained here: https://github.com/brc859844/rabbithub. 

Components:
- rsp_services_csparql - RDF stream Processors(rsp) RESTful Interfaces downloaded from http://streamreasoning.org/download/ and tweaked to interact with the rabbithub.
- csparql_java_client - Simple implemention of rsp-services made on the example of streamreasoning/rsp-services-client-example.
- client-receiver - simple web forms for registering a query and subscribing to rabbithub exchanges plus an endpoint for the query results.
