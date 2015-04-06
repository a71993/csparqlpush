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

All three components are lightweight maven projects, which run on jetty servlet engine. All can be run from eclipse or from a jar file. Jar can be generated with mvn package, which creates a target folder with a jar file inside.
rabbithub and rsp_services_csparql needs to be started first. 
csparql_java_client will register a new stream named 'http://ex.org/rabbit' and feeds some rdf-s on it from a file or a folder, that it takes as a parameter. If none is given, it will auto-generate some random rdf triples.
client-receiver can be used to register a query on a stream. Csparql rsp_services will then automatically create an rabbitMQ exchange which can be subscribed to using client-reciever. Subscriptions callback url, where results are sent, is hard-coded at the moment and can be seen on client-receiver's results page.

Simple example query: REGISTER STREAM query1 AS CONSTRUCT { ?s ?p ?o } FROM STREAM <http://ex.org/rabbit> [RANGE 2s STEP 2s] WHERE { ?s ?p ?o }
where 'http://ex.org/rabbit' is the stream where the query is run and 'query1' is the query name.
