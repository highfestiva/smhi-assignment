# SMHI Assignment

In my words, this assignment is about checking that I can gather, restructure and present data from some source in a sensible fasion. The code should be clear and extensible C# or Java (I pick the latter), and should adhere to industry standards.

## What to do

Design a backend solution using the SMHI open API

* Fetch from [Meteorological Observations -
API | SMHI Open Data Portal](https://opendata.smhi.se/metobs/api).
* Get data for "Byvind" and "Lufttemperatur" from API and return as one dataset
* Restructure the data for easy consumption from a front-end application
* Publish restructured data in a REST API
* The rest API should be able to filter on:
  - station Id and last hour
  - station Id and last day
  - defaulting to all stations latest hour when no parameters set
* Testable components
* At least two tests
* Validate requests using an API key
* Optional: include discoverability

## Early design choices

I pick Java Spring Boot, as that's what I'm most comfortable with. If left to my own devices, I'd use Python as this task is going to be mostly data manipulation - and that's a lot more efficient in Python.

Data should be structured, but relatively unclear how exactly just yet, and also allow for ease of change in the future (in the spirit of the assignment), so I pick a document database. I like MongoDB, so I go with that.

The API should return the latest data, so I'll perform downloads from SMHI regularly using Spring Boot's @Scheduled.

The API keys might as well reside in the database, I'll use Spring Security to verify.

I had to look up "discoverability", sounds neat with the HATEOAS principles, so I'll add that.

Since there are at least two processes identified (DB+app), I'll use Docker for virtualization, so it's easy to test run for the assessors. docker-compose to be precise.

### Initial plan

I'll start by generating a Spring Boot project, creating the docker-compose files and making sure everything starts as it should. I'll test it with a simple curl.

Then I'll go have a look at the SMHI API, create some downloader and transformer and write the appropriate tests afterwards. In this scenario I'll write the tests after, as I want to get a handle on the input and output data to know what to test for.

Now it's time to connect the app to the DB and add the transformed documents to it. Run the updates regularly.

When that's done I'll design an initial version of the REST API, which I'll come back to later.

Finally I'll do the API keys, discoverability, clean things up, add some additional tests and go over the code, data, documentation to make sure I feel it's good enough to hand over.

#### Improved by AI

ChatGPT told me there's a package called MapStruct, that can do automatic bean mappings with annotations and compile-time checking. Great! I'll use that to avoid some boilerplate.

## Deliberately skipping

HTTPS/certs. Deploy. CI/CD. Redundancy, resilience, reverse proxy, load balancing. Optimization. Caching.

## Steps taken

1. Generated a basic spring boot project.
1. Found schema files on the [SMHI site](https://opendata.smhi.se/metobs/schemas), which I downloaded and put in the resources/xsd directory.
1. Incorporated JAXB to generate the schema code.
1. Added a downloader service.
1. Added a startup component which does the download. That way it's always obvious if everything works, and there's always recent data in the DB.
1. Added a data pipeline service, "Metrology Ingest", to download, transform and store the data.

## Instructions for manual start+test

1. `docker-compose up --build`
1. `curl http://127.0.0.1/v1/status`
1. `curl http://127.0.0.1/v1/something`
