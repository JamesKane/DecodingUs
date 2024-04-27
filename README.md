# Decoding Us
[![License](https://img.shields.io/badge/License-BSD%203--Clause-blue.svg)](https://opensource.org/licenses/BSD-3-Clause)

[Decoding-Us.com](https://decoding-us.com) is a platform to empower genetic genealogists, citizen scientists, and academics to explore the common origins of human-kind.

### Local Development settings
```
docker run --name postgres \
-e POSTGRES_DB=decodingus_database \
-e POSTGRES_USER=decodingus_username \
-e POSTGRES_PASSWORD=decodingus_password \
-p 5432:5432 -d my-postgis-16