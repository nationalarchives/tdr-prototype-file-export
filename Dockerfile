FROM openjdk:8-slim
WORKDIR export
COPY target/scala-2.13/tdr-prototype-export-files-assembly-0.1.jar tdr-export.jar
CMD java -jar tdr-export.jar
