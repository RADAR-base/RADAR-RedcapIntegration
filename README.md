# RADAR-CNS REDCap Integration

[![Build Status](https://api.travis-ci.org/RADAR-base/RADAR-RedcapIntegration.svg?branch=master)](https://travis-ci.org/RADAR-base/RADAR-RedcapIntegration/)

[REDCap](https://projectredcap.org/) is a secure web application for building and managing online
surveys and databases. While REDCap can be used to collect virtually any type of data
(including 21 CFR Part 11, FISMA, and HIPAA-compliant environments), it is specifically geared to
support online or offline data capture for research studies and operations. The REDCap Consortium,
a vast support network of collaborators, is composed of thousands of active institutional
partners in over one hundred countries who utilize and support REDCap in various ways.

The REDcap integration web app integrates a REDcap Project with a Project on [Management Portal](https://github.com/RADAR-base/ManagementPortal) linking corresponding subjects in both systems. 
This ensures that personal identifiable data is kept securely in REDcap (out of [RADAR-base](https://github.com/RADAR-base/RADAR-Docker) platform) and linked via Pseudonymised Data (Human Readable Ids and UUIDs). 
The non-identifiable sensor data is stored in the RADAR-base platform. 

In REDCap, it is possible to set a `Data Entry Trigger` as an advanced feature. It provides a way
for REDCap to trigger a call to a remote web address (URL), in which it will send a HTTP POST
request to the specified URL whenever *any* record or survey response has been created or
modified on *any* data collection instrument or survey in this project (it is *not* triggered by
data imports but only by normal data entry on surveys and data entry forms). Its main purpose is
for notifying other remote systems outside REDCap at the very moment a record/response is created
or modified, whose purpose may be to trigger some kind of action by the remote website, such as
making a call to the REDCap API.

For setting a `Data Entry Trigger`
1. login in a REDCap project using a project admin account
2. go to `Project Setup` view
3. go to `Enable optional modules and customisations` and click to `Additional customisations`
5. add a valid URL
6. verify if the URL is accessible from REDCap
7. enable `Data Entry Trigger`
8. click on `Save`

This project exposed a REST endpoint located at `<host>:<port>/redcap/trigger` or `<host>/redcap/trigger` is mapped to 8080 port.

It is highly recommended to use an encrypted connection (i.e. SSL/HTTPS) for accessing set
`Data Entry Trigger`.

## How does it works
Upon receiving a request, the service verifies whether the trigger is related to the REDCap
enrolment event. If so, it triggers a Subject creation in the
[RADAR-CNS Management Portal](https://github.com/RADAR-base/ManagementPortal). After creating a new
subject, the serivce update the RADAR REDCap `RADAR Enrolment` form adding:
- `RADAR-CNS Subject Identifier`: unique identifier within the a RADAR-CNS Platform instance
- `Human Readable Identifier`: unique identifier used for visualising data
At the end of the process, the `RADAR Enrolment` will become `COMPLETED`.

The `RADAR Enrolment` establish a relation between data stored in REDCap and data stored in the
RADAR-CNS Platform.

## Configuration
This service requires a configuration file named `radar.yml` that can be stored at:
- `/usr/local/etc/radar-redcap-int/`
- at the path provided by the environment variable `REDCAP_INTEGRATION_CONFIG_FOLDER`

The configuration should follow this [template](radar.yml).
For each supported project, the `projects` variable should contains a item like
```yaml
redcap_info:
  url: #URL pointing REDCap instance
  project_id: #REDCap project identifier
  enrolment_event: #Unique identifier for the enrolment event
  integration_form: #Name of integration REDCap form
  token: #REDCap API Token used to identify the REDCap user against the REDCap instance
mp_info:
  project_name: #Management Portal project identifier
``` 

The service validates the configuration file during the deploy phase. If the file is invalid, the
deploy is stopped.

## Log files
The project has been created to be deployed on [Grizzly Server](https://javaee.github.io/grizzly/).
Application logs are redirected to `standard output`.

## Docker deploy example
 1. Update the radar.yml in root for correct configuration.
 2. Then build the docker image naming it `redcapintegration` using the Dockerfile located in root directory
 `$ docker build -t redcapintegration .`
 3. Then run the app in a container using using the image created above mapping port 8080 (host) to 8080 (container)
 `$ docker run --name redcapintegration -v "/your/absolute/path/radar.yml:/usr/local/conf/radar/redcap-int/" -it --rm -d -p 8080:8080 redcapintegration`
 4. Access the  entry point like this
 `$ curl -X POST “<Host IP or URL>:<Port>/redcap/trigger”`
 5. Or if accessing on the same machine as the container do
 `$ curl -X POST “http://localhost:8080/redcap/trigger` 
 6. Please note that the radar.yml config file should be valid or else the deploy will fail.
 
## Docker-compose example

If running this along with other components on docker using docker-compose, you can add the following to your docker-compose.yml file under the `services` tag - 

```yaml
  redcap-integration:
    build: .
    image: redcapintegration
    networks:
      - default
    restart: always
    volumes:
      - "./radar.yml:/usr/local/conf/radar/redcap-int/"
    healthcheck:
      test: ["CMD", "curl", "-IX", "POST", "http://localhost:8080/redcap/trigger"]
      interval: 1m
      timeout: 5s
      retries: 3
```

Please check the RADAR-base platform [docker-compose.yml file](https://github.com/RADAR-base/RADAR-Docker/blob/master/dcompose-stack/radar-cp-hadoop-stack/docker-compose.yml) for more information.

## Scripts
Under the scripts folder there is a python script which will download non-identifiable data from a RedCAP project and upload it to a specified FTP server. It is run as a cron job for RADAR-CNS projects to provide RedCAP data to data analysts. FTP authentication details can be provided through a .netrc file or as arguments to the script. It requires the [requests library]('https://pypi.org/project/requests/').

Usage:
```
python3 scripts/redcap-extract.py --help
python3 scripts/redcap-extract.py PROJECT_NAME REDCAP_TOKEN --redcap-url URL --ftp-ip IP 
```

## Credits
Part of this document has been extracted from the [REDCap](https://projectredcap.org/) documentation.
