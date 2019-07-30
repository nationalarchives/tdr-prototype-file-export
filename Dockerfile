FROM openjdk:8-slim
WORKDIR export
RUN mkdir /tmp/tdr-export
COPY download/target/scala-2.13/tdr-download.jar tdr-download.jar
COPY export-zip/target/scala-2.13/tdr-export.jar tdr-export.jar
CMD OUTPUT_DIR="$(mktemp -d)" \
  && AWS_REGION=eu-west-2 \
  AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
  AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
  OUTPUT_DIR=$OUTPUT_DIR \
  java -jar tdr-export.jar \
  && tar -zcvf tdr-files.tar.gz $OUTPUT_DIR \
  && tar -tvf tdr-files.tar.gz
