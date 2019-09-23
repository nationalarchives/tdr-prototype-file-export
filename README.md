# TDR prototype: file export

This project is part of the [Transfer Digital Records][tdr-docs] project. It is a prototype for an application that a
Digital Archivist might use to export files from S3 once a transfer has been finalized.

[tdr-docs]: https://github.com/nationalarchives/tdr-dev-documentation

## Running the project

The full export has several steps:

- Download files from S3
- Zip the files
- Encrypt the zip file
- Upload the encrypted file to a different S3 bucket

You can run the steps separately, or run them together with Docker.

### Step 1: download files

[Configure your AWS credentials][aws-cli-auth] in the `~/.aws/credentials` file. The download step will uses this
configuration to authenticate requests to S3.

Then run `sbt download/run`.

By default, this will download the contents of a specific S3 bucket to a temporary directory.

You can set some optional environment variables to configure the download:

- `INPUT_BUCKET_NAME`: name of the bucket to download files from
- `INPUT_FOLDER_NAME`: name of the folder to download - this is recursive, so files in subfolders will also be
  downloaded
- `OUTPUT_DIR`: the local folder to download files to

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
  sbt exportZip run
```

setting the `ARCHIVE_FILEPATH` variable to the file to be uploaded.

### Run all steps in Docker

- Build the jar files with `sbt clean assembly`
- Build the image with `docker build . --tag exportfiles`
- Run the Docker image, setting environment variables with your AWS key ID and AWS secret key:

  ```
  docker run \
    --env ACCESS_KEY_ID=your_aws_key_id \
    --env SECRET_ACCESS_KEY=your_aws_secret_key \
    exportfiles:latest
  ```

  You can also set `INPUT_BUCKET_NAME` and `INPUT_FOLDER_NAME` to specify the S3 bucket and folder to download.
