FROM ubuntu:14.04.4

MAINTAINER Walter fan (walter.fan@gmail.com)

RUN rm /bin/sh && ln -s /bin/bash /bin/sh
RUN apt-get -qq update && apt-get -qqy install curl apt-transport-https wget adduser libfontconfig
RUN curl -sL https://repos.influxdata.com/influxdb.key | apt-key add - 
RUN source /etc/lsb-release
RUN echo deb https://repos.influxdata.com/ubuntu trusty stable | tee /etc/apt/sources.list.d/influxdb.list
RUN wget https://grafanarel.s3.amazonaws.com/builds/grafana_3.1.1-1470047149_amd64.deb
RUN dpkg -i grafana_3.1.1-1470047149_amd64.deb
RUN echo "deb https://packagecloud.io/grafana/stable/debian/ jessie main" >> /etc/apt/sources.list
RUN curl https://packagecloud.io/gpg.key | apt-key add -
RUN apt-get -qq update && apt-get -qqy install influxdb grafana

ADD startup.sh .
ENTRYPOINT ["/bin/sh", "startup.sh"]

