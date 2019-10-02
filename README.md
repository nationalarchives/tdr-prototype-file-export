# TDR prototype: file export

This project is part of the [Transfer Digital Records][tdr-docs] project. It is a prototype for an application that a
Digital Archivist might use to export files from S3 once a transfer has been finalized.

[tdr-docs]: https://github.com/nationalarchives/tdr-dev-documentation

## Running the project

The full export has several steps:

- Download files from S3, and create a directory of files to export
- Zip the files
- Upload the encrypted file to a different S3 bucket

You can run the steps separately, or run them together with Docker.

### Step 1: download and package files

[Configure your AWS credentials][aws-cli-auth] in the `~/.aws/credentials` file. The download step will use this
configuration to authenticate requests to S3.

Set the mandatory environment variables in the command line or in IntelliJ:

- `GRAPHQL_SERVER`: The hostname of the API, e.g. `http://localhost:8080` in development
- `GRAPHQL_PATH`: The path of the GraphQL API endpoint, e.g. `graphql` in development
- `CONSIGNMENT_ID`: the database ID of the consignment to export

Then run `sbt download/run`.

By default, this will download the contents of a specific S3 bucket to a temporary directory, and create a BagIt bag in
another temporary directory.

You can also set some optional environment variables to configure the download:

- `INPUT_BUCKET_NAME`: name of the S3 bucket to download files from
- `INPUT_FOLDER_NAME`: name of the parent S3 folder (defaults to the consignment ID)
- `FILE_DOWNLOAD_DIR`: the local folder to download files to
- `BAG_DIR`: the local folder to save the BagIt bag to

[aws-cli-auth]: https://docs.aws.amazon.com/cli/latest/userguide/cli-configure-files.html

### Step 2: zip the files

Use `tar` to create a .tar.gz file:

```
tar -zcvf name-of-output-file.tar.gz /path/of/directory/to/zip
```

### Step 3: upload the encrypted file

Run:

```
ARCHIVE_FILEPATH=/path/of/file/to/upload \
  sbt exportZip/run
```

setting the `ARCHIVE_FILEPATH` variable to the file to be uploaded.

### Run all steps in Docker

- Build the jar files with `sbt clean assembly`
- Build the image with `docker build . --tag exportfiles`
- Run the Docker image, setting environment variables:

  ```
  docker run \
    --env ACCESS_KEY_ID=your_aws_key_id \
    --env SECRET_ACCESS_KEY=your_aws_secret_key \
    --env GRAPHQL_SERVER=https://graphql-api-hostname.amazonaws.com \
    --env GRAPHQL_PATH=some/api/path \
    --env CONSIGNMENT_ID=1234 \
    exportfiles:latest
  ```

  You can also set `INPUT_BUCKET_NAME` and `INPUT_FOLDER_NAME` to specify the S3 bucket and folder to download.
