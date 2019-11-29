FROM cassandra:2.2.8
WORKDIR /etc/cassandra

RUN sed -i.old 's/auto_snapshot: true/auto_snapshot: false/g' cassandra.yaml
RUN sed -i.old 's/write_request_timeout_in_ms: 2000/write_request_timeout_in_ms: 5000/g' cassandra.yaml
RUN sed -i.old 's/#MAX_HEAP_SIZE="4G"/MAX_HEAP_SIZE="2G"/g' cassandra-env.sh
RUN sed -i.old 's/#HEAP_NEWSIZE="800M"/HEAP_NEWSIZE="200M"/g' cassandra-env.sh

ENV CASSANDRA_LISTEN_ADDRESS 127.0.0.1

