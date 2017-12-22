FROM solr:7.1.0-alpine

USER root
# deploy solr service configuration and libraries
COPY jetty.xml /opt/solr/server/etc/jetty.xml
COPY webdefault.xml /opt/solr/server/etc/webdefault.xml
COPY realm.properties /opt/solr/server/etc/realm.properties
COPY collection /opt/solr/server/solr/bits
RUN chown -R $SOLR_USER:$SOLR_USER /opt/solr

USER $SOLR_USER

