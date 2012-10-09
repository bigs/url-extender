# premiumurlshortener

A URL "shortening" service written in Clojure/compojure.

## Usage

lein run

OR

lein ring server

## Parameters

It relies on environment variables to configure some things...

    REDIS_HOST: ... the redis host
    REDIS_PORT: ... the redis port
    REDIS_PASSWORD: <optional> the redis password
    REDIS_DB: <optional> redis db number
    BASE_URL: the base URL for the service (e.g. http://www.premiumurlshorteningservice.com)

## License

Copyright 2012 Cole Brown

Distributed under the Eclipse Public License, the same as Clojure.
