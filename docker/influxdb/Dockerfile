FROM influxdb:1.1

ADD startup.sh startup.sh
RUN chmod +x /*.sh

ENTRYPOINT ["/startup.sh"]