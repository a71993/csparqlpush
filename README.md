# csparqlpush

This is a project for integrating c-sparql engine's restful API with pubsubhubbub. 

Requirements:
- RabbitMQ 
- Rabbithub plugin for RabbitMQ, which needs to be same version as RabbitMQ. Can be obtained here: https://github.com/brc859844/rabbithub. (requires erlang)
- maven

Components:
- rsp_services_csparql - RDF stream Processors(rsp) RESTful Interfaces downloaded from http://streamreasoning.org/download/ and tweaked to interact with the rabbithub.
- csparql_java_client - Simple implemention of rsp-services made on the example of streamreasoning/rsp-services-client-example.
- client-receiver - simple web forms for registering a query and subscribing to rabbithub exchanges plus an endpoint for the query results.

All three components are lightweight maven projects. First two can be run from a jar. They can be generated with mvn package, which creates a target folder with a jar file inside. Client-reciever can be run using mvn jett:run command. All three can be run from eclipse.  

Simple example query:  
```
REGISTER STREAM query1 AS CONSTRUCT { ?s ?p ?o } FROM STREAM <http://ex.org/rabbit> [RANGE 2s STEP 2s] WHERE { ?s ?p ?o }
```
where 'http://ex.org/rabbit' is the stream where the query is run on and 'query1' is the query name.
