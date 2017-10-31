
FROM tomcat:8.5.20-jre8
ENV JAVA_OPTS=-Djava.security.egd=file:/dev/urandom

# Create a radar configuration directory
RUN mkdir $CATALINA_HOME/conf/radar

# Remove any existing war files to avoid conflicts
RUN rm -rf /usr/local/tomcat/webapps/*

# Copy the WAR file to tomcat webapps for deployment
ADD https://github.com/RADAR-CNS/RADAR-RedcapIntegration/releases/download/v0.1-beta.2/redcap-2.0-SNAPSHOT.war $CATALINA_HOME/webapps/redcap.war

EXPOSE 8080
CMD ["catalina.sh", "run"]
