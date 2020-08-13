# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

FROM openjdk:14 as builder

RUN mkdir /code
WORKDIR /code

ENV GRADLE_OPTS -Dorg.gradle.daemon=false

COPY ./gradle/wrapper /code/gradle/wrapper
COPY ./gradlew /code/
RUN ./gradlew --version

COPY ./build.gradle ./settings.gradle /code/

RUN ./gradlew downloadApplicationDependencies

COPY ./src/ /code/src

RUN ./gradlew distTar \
    && cd build/distributions \
    && tar xf *.tar \
    && rm *.tar redcap-*/lib/redcap-*.jar

FROM openjdk:14

MAINTAINER @yatharthranjan, @mpgxvii

LABEL description="RADAR-CNS Redcap Integration docker container"

COPY --from=builder /code/build/distributions/redcap-*/bin/* /usr/bin/
COPY --from=builder /code/build/distributions/redcap-*/lib/* /usr/lib/
COPY --from=builder /code/build/libs/redcap-*.jar /usr/lib/

RUN yum install -y wget \
  && rm -rf /var/cache/yum

EXPOSE 8080

CMD ["redcap"]
