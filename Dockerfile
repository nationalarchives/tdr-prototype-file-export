FROM openjdk:8-slim
WORKDIR export
RUN apt-get update
RUN apt-get install -y gnupg
COPY download/target/scala-2.13/tdr-download.jar tdr-download.jar
COPY export-zip/target/scala-2.13/tdr-export.jar tdr-export.jar
CMD OUTPUT_DIR="$(mktemp -d)" \
  && echo "$TNA_GPG_PUBLIC_KEY" > tna_gpg.pub \
  && gpg --import tna_gpg.pub \
  && AWS_REGION=eu-west-2 \
      AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
      AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
      OUTPUT_DIR=$OUTPUT_DIR \
      java -jar tdr-download.jar \
  && tar -zcf tdr-files.tar.gz $OUTPUT_DIR \
  && gpg --output tdr-files.gpg --encrypt --recipient $TNA_GPG_RECIPIENT --always-trust tdr-files.tar.gz \
  && AWS_REGION=eu-west-2 \
      AWS_ACCESS_KEY_ID=$ACCESS_KEY_ID \
      AWS_SECRET_ACCESS_KEY=$SECRET_ACCESS_KEY \
      ARCHIVE_FILEPATH=tdr-files.gpg \
      java -jar tdr-export.jar
