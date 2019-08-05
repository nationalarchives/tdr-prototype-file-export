# TDR prototype: file export

This project is part of the [Transfer Digital Records][tdr-docs] project. It is a prototype for an application that a
Digital Archivist might use to export files from S3 once a transfer has been finalized.

[tdr-docs]: https://github.com/nationalarchives/tdr-dev-documentation

## Running the project

The full export has several steps:

- Download files from S3
- Zip the files
- Encrypt the zip file
- Upload the encrypted file to an FTP server

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

### Step 3: encrypt the archive

Encrypt the tar.gz file using [gpg2]. How exactly you do this depends on whether you are using your own GPG key for
testing purposes, or the TNA GPG public key.

[gpg2]: https://linux.die.net/man/1/gpg2

### Step 4: upload the encrypted file

Run:

```
ARCHIVE_FILEPATH=/path/of/file/to/upload \
  FTP_ENDPOINT=some-ftp.example.com \
  FTP_USERNAME=someUsername \
  sbt exportZip run
```

setting the `ARCHIVE_FILEPATH` variable to the file to be uploaded, and filling in `FTP_ENDPOINT` and `FTP_USERNAME`.

By default, this step will read your SSH key from `~/.ssh/id_rsa`. Set the `SSH_KEY_FILE` environment variable to use a
different key.

### Run all steps in Docker

- Build the image with `docker build . --tag exportfiles`
- Run the Docker image, setting environment variables with your AWS key ID, AWS secret key, SSH key contents, and GPG
  public key contents and recipient ID:

  ```
  docker run \
    --env FTP_SSH_KEY="`cat ~/.ssh/id_rsa`" \
    --env TNA_GPG_PUBLIC_KEY="`cat ~/tna-gpg.pub`" \
    --env TNA_GPG_RECIPIENT="gpg-key-owner@example.com" \
    --env ACCESS_KEY_ID=your_aws_key_id \
    --env SECRET_ACCESS_KEY=your_aws_secret_key \
    --env FTP_ENDPOINT=some-ftp.example.com \
    --env FTP_USERNAME=someUsername \
    exportfiles:latest
  ```

  You can also set `INPUT_BUCKET_NAME` and `INPUT_FOLDER_NAME` to specify the S3 bucket and folder to download.
