FROM openjdk:8-slim
WORKDIR export
RUN mkdir /tmp/tdr-export
COPY target/scala-2.13/tdr-prototype-export-files-assembly-0.1.jar tdr-export.jar
CMD AWS_REGION=eu-west-2 \
  AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
  AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
  java -jar tdr-export.jar
