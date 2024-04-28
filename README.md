# Decoding Us
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

[Decoding-Us.com](https://decoding-us.com) is a platform to empower genetic genealogists, citizen scientists, and academics to explore the common origins of humankind.

The codebase reuses learnings from prior projects [YDNA-Warehouse](https://ydna-warehouse.org) as a foundation.

## Tech Stack
* [Play Framework](https://www.playframework.com) - The High Velocity Web Framework For Java and Scala
* [Scala](https://scala-lang.org) - A strong statically typed high-level language that supports OO and functional programming
* [PostgreSQL](https://www.postgresql.org) - An Open Source Relational Database
* [AWS S3](https://aws.amazon.com/pm/serv-s3) - Amazon Cloud Object Storage
* [HTSJDK](https://samtools.github.io/htsjdk) - A Java API for high-throughput sequencing data (HTS) formats

### Local Development settings
```
docker run --name postgres \
-e POSTGRES_DB=decodingus_database \
-e POSTGRES_USER=decodingus_username \
-e POSTGRES_PASSWORD=decodingus_password \
-p 5432:5432 -d my-postgis-16