FROM openjdk:8-slim
WORKDIR export
RUN apt-get update
COPY download/target/scala-2.13/tdr-download.jar tdr-download.jar
COPY export-zip/target/scala-2.13/tdr-export.jar tdr-export.jar
CMD OUTPUT_DIR="$(mktemp -d)" \
  && AWS_REGION=eu-west-2 \
      AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
      AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
      OUTPUT_DIR=$OUTPUT_DIR \
      java -jar tdr-download.jar \
  && tar -zcf tdr-files.tar.gz $OUTPUT_DIR \
  && AWS_REGION=eu-west-2 \
      AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
      AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
      ARCHIVE_FILEPATH=tdr-files.tar.gz \
      java -jar tdr-export.jar
