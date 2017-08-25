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
To set a valid token for accessing REDCap API, in the current implementation it is needed to update
the class `org.radarcns.redcap.util.RedCapUser` 

## Credits
Part of this document has been extracted from the [REDCap](https://projectredcap.org/) documentation.