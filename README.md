# RADAR-CNS REDCap Integration

[REDCap](https://projectredcap.org/) is a secure web application for building and managing online
surveys and databases. While REDCap can be used to collect virtually any type of data
(including 21 CFR Part 11, FISMA, and HIPAA-compliant environments), it is specifically geared to
support online or offline data capture for research studies and operations. The REDCap Consortium,
a vast support network of collaborators, is composed of thousands of active institutional
partners in over one hundred countries who utilize and support REDCap in various ways.

In REDCap, it is possible to set a `Data Entry Trigger`. is an advanced feature. It provides a way
for REDCap to trigger a call to a remote web address (URL), in which it will send a HTTP Pos
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

This project exposed a REST endpoint located at `<host>:<port>/redcap/trigger`.

It is highly recommended to use an encrypted connection (i.e. SSL/HTTPS) for accessing set
`Data Entry Trigger`.

## How does it works
Upon receiving a request, the service verifies whether the trigger is related to the REDCap
enrolment event. If so, it triggers a Subject creation in the
[RADAR-CNS Management Portal](https://github.com/RADAR-CNS/ManagementPortal). After creating a new
subject, the serivce update the RADAR REDCap `RADAR Enrolment` form adding:
- `RADAR-CNS Subject Identifier`: unique identifier within the a RADAR-CNS Platform instance
- `Human Readable Identifier`: unique identifier used for visualising data
At the end of the process, the `RADAR Enrolment` will become `COMPLETED`.

The `RADAR Enrolment` establish a relation between data stored in REDCap and data stored in the
RADAR-CNS Platform.

## Configuration
This service requires a configuration file named `radar.yml` that can be stored at:
- `/usr/share/tomcat8/conf/`
- `/usr/local/tomcat/conf/radar/`
- at the path provided by the environment variable `CONFIG_FOLDER`

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
  project_id: #Management Portal project identifier
``` 

The service validates the configuration file during the deploy phase. If the file is invalid, the
deploy is stopped.

## Log files
The project has been created to be deployed on [Apache Tomcat](http://tomcat.apache.org).
Application logs are redirected to `standard output`. In case of invalid deploy check
`$CATALINA_HOME/logs/catalina.*` and `$CATALINA_HOME/logs/localhost.*`

## Docker deploy example
 1. Start a tomcat container 
 `sudo docker run --name tomcat -it --rm -d -p 8888:8080 tomcat:8.0.44-jre8`
 2. Create the `radar` folder under `conf`
 `sudo docker exec -i -t tomcat mkdir /usr/local/tomcat/conf/radar`
 3. Copy a valid config file
 `sudo docker cp /path/to/radar.yml tomcat:/usr/local/tomcat/conf/radar/radar.yml`
 4. Deploy the web app
 `sudo docker cp redcap-1.0-SNAPSHOT.war tomcat:/usr/local/tomcat/webapps/redcap.war`
 5. The web app will now running at `http:localhost:8080/redcap/trigger`
 

## Credits
Part of this document has been extracted from the [REDCap](https://projectredcap.org/) documentation.