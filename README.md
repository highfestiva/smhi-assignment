# SMHI Assignment

In my words, this assignment is about checking that I can gather, restructure and present data from some source in a sensible fasion. The code should be clear and extensible C# or Java (I pick the latter), and should adhere to industry standards.

This readme is intentionally disorganized, so to speak. I've tried to reflect my thinking by expanding on it from top to bottom, but had to "refactor"
small things a couple of times. This will make it harder to read, but hopefully easier to see what I've been up to.

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

HTTPS/certs. Deploy. CI/CD. Redundancy, resilience, reverse proxy, load balancing. Optimization. Caching. Database indices.

## Data investigation

So I looked at the data from SMHI, I'll use XML & the schema to load data using discoverability. The contents is fairly straight-forward and looks something liket his:

```
                                                       +-- latest-day  --- data
                                       +-- Station A --+-- latest-hour --- data
                                       |               +-- ...
                                       |
                                       |               +-- latest-day  --- data
              +-- parameter "Byvind" --+-- Station B --+-- latest-hour --- data
              |                        |               +-- ...
              |                        +-- ...
              |                                                        +-- latest-day  --- data
              |                                        +-- Station A --+-- latest-hour --- data
              |                                        |               +-- ...
              |                                        |
version 1.0 --+-- parameter "Luftemperatur" -----------+-- Station C -- ...
              |                                        +-- ...
              +-- ...

```

There are many different air temperature parameters, such as 1 month average and the minimum value over 24 hours. I'll ingest all air temperature values with a period
1d or shorter, as the assignment doesn't say exactly what's desired.

As to the output data, the assignment specifically says it's either asking for the last hour or the last day, so I'll return all the relevant values for 1h or 1d. I'm
going to keep all station data within a single document. It would be easy to split into 1h and 1d documents for scalability, but the data set is small, and there are
so few documents that even if it expanded several orders of magnitude, we'd be fine.

The pros and cons of this method: post-load filtering and scalability are drawbacks. An advantage is the same download and store routine for all the data, instead of
splitting it (1h/1d) either on the download or the storage. The code complexity of both are similar, it really makes no difference either way in _this_ scenario.

I'll keep some of the original data, which might be helpful. This is what I'm thinking currently:

```json
[
  {
    "station": "Arvidsjaur A",
    "stationId": "159880",
    "observations": [
      {
        "type": "airTemp",
        "interval": "1h",
        "value": "4.3",
        "unit": "°C",
        "quality": "G",
        "updatedAt": "2025-10-18T19:00:00Z",
        "originalDescription": ["Lufttemperatur", "momentanvärde, 1 gång/tim"]
      },
      {
        "type": "gustWind",
        "interval": "1h",
        "value": "7.2",
        "unit": "m/s",
        "quality": "G",
        "updatedAt": "2025-10-18T18:00:00Z",
        "originalDescription": ["Byvind", "max, 1 gång/tim"]
      }
    ]
  },
  ...
]
```

Here the advantage of using a NOSQL db starts to show, because the fields don't have to be fixed, and if new ones are added in the future version, it can be returned as
stored.

The "type", which should be international in a good API, has to be "translated" somehow. I'm adding a tiny class for it, with a lookup table. Even if the number of
parameters would be in the 100s in the future, it's not a big deal. Using some i18n package is not a good idea, as you want it to be machine readable in the future too.
The same goes for "unit."

## Design decisions along the way

I decided to use the same DTO for the database, for returning to the customer and in calls between the layers. It's a pragmatic approach, which saves conversion
boilerplate. It doesn't really do any harm, as what I'm saving is what I want to present to the HTTP caller.

## Steps taken

1. Generated a basic spring boot project.
1. Found schema files on the [SMHI site](https://opendata.smhi.se/metobs/schemas), which I downloaded and put in the resources/xsd directory.
1. Incorporated JAXB to generate the schema code.
1. Added a downloader service.
1. Added a startup component which does the download. That way it's always obvious if everything works, and there's always recent data in the DB.
1. Added a data pipeline service, "Metrology Ingest", to download, transform and store the data.
1. Thought some about the data (see "Data investigation" above).
1. Implemented a bulk save repository to efficiently store the "station observations."
1. Implemented a test for the transform service (a single sample).
1. Implemented part of the transform service. Decided to scrap the MapStruct idea, as it became clunkier than just straightforward java.
1. Implemented a test for the full transform (a station containing multiple samples).
1. Implemented the complete transform service.
1. Got the first download+save going.
1. Renamed "measurement" "observation," which is more in line with meterology.
1. Added the REST API endpoint for observations.
1. Added the observation interval filtering.
1. Added the API key, had some trouble (IDE probably wasn't compiling right, worked after undoing and redoing all :).
1. Added an endpoint for just fetching stations.

## REST API

Time to think about what the REST API should look like. After utilizing the discoverability from SMHI, I've decided against using it, since this is a much more
condensed data set, and that's also more in line with the assignment ("easy consumption"). I'm thinking something like this:

```
GET /api/v1/observations?station=345&interval=1h
```

For pagination, it might be good to also have `&page=0&size=25`. This allows for asking for a single station, or all - if no stations are set. The interval
would support 1h and 1d, and also be optional and default to 1h. I'm skipping auxillary `/api/v1/observations/stations/{station_id}/interval/{}` and so forth.

For meta-data we could do:

```
GET /api/v1/stations?page=0&size=10
```

It's also easy to think of listing time intervals, what types of observations there are, what units they use, and so forth. But for simple consumption, less is
more, and I'll go with these two for starters.

The output JSON for observations might look something like this:

```json
{
  "totalStations": 123,
  "page": 0,
  "pageSize": 25,
  "stations": [
    {
      "stationId": "34567",
      "stationName": "Arvidsjaur",
      "observations": [
        {
          "type": "airTemp",
          "interval": "1h",
          "value": "4.3",
          "unit": "°C",
          "quality": "G",
          "updatedAt": "2025-10-18T19:00:00Z",
          "originalDescription": ["Lufttemperatur", "momentanvärde, 1 gång/tim"]
        },
        {
          "type": "gustWind",
          "interval": "1h",
          "value": "7.2",
          "unit": "m/s",
          "quality": "G",
          "updatedAt": "2025-10-18T18:00:00Z",
          "originalDescription": ["Byvind", "max, 1 gång/tim"]
        }
      ]
    }
  ]
}
```

And the response JSON body for stations might look this way:

```json
{
  "totalStations": 123,
  "stations": [
    {
      "stationId": "34567",
      "stationName": "Arvidsjaur",
    },
    {
      "stationId": "34568",
      "stationName": "Färgelanda",
    }
  ]
}
```

If we like it's super-easy to add more data to either stations or observations in the future. Geolocation or altitude, for instance.

## Things to improve

### Functionality

* Distinguish different observations with types. Currently 1d airTemp can mean both bi-daily max and a last 24h average.
 - airTempMin12h
 - airTempInstant
 - ...
* Nicer pagination, some frontenders want a list of pages to highlight and "buttonify."
* More meta-data for stations: geolocation, altitude, etc.
* Get closest stations, given a geocoord (supported in MongoDB).
* Sorting, stations by name/proximity/altitude/latitude, etc.
* Endpoint query filter on observation types.
* Meta-data endpoint for intervals.
* Discoverability done right, and a root endpoint for it.
* Issuing API keys.
* Cleaning up this readme. :)
...

### Code

* Code vulnerability scanning.
* CI/CD.
* Refactor, or at least comment, the streams in SmhiDownloadService. Some people love streams, others hate it.
* Add some autoformatting, a script or part of the build step.
* Add some linter.
* Logging unauthorized requests.
* Adding performance instrumentation, for instance micrometer.io.
* 
...

## Instructions for manual start+test

1. `docker-compose up --build`
1. `curl -H "Authorization: Bearer ABCDH" http://127.0.0.1:8080/api/v1/status`
1. `curl -H "Authorization: Bearer ABCDH" 'http://127.0.0.1:8080/api/v1/observations?interval=1d&page=2&size=3' | python -m json.tool`
1. `curl -H "Authorization: Bearer ABCDH" http://127.0.0.1/api/v1/stations | python -m json.tool`
