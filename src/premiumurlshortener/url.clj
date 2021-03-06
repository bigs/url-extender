(ns premiumurlshortener.url
  (:require [taoensso.carmine :as car]
            [crypto.random :as random]
            [stencil.core :as stencil]))

;; System constants
(def redis-host (get (System/getenv) "REDIS_HOST" "127.0.0.1"))
(def redis-port (Integer/parseInt (get (System/getenv) "REDIS_PORT" "6379")))
(def redis-db (Integer/parseInt (get (System/getenv) "REDIS_DB" "0")))
(def redis-password (get (System/getenv) "REDIS_PASSWORD" nil))
(def base-url (get (System/getenv) "BASE_URL" "http://localhost:8080"))

;; Define redis connection
(def redis-pool (car/make-conn-pool))
(def redis-server (car/make-conn-spec
                    :host redis-host
                    :port redis-port
                    :password redis-password
                    :timeout 4000))

;; Create a simple macro
(defmacro redis [& body] `(car/with-conn redis-pool redis-server ~@body))

;; Helpers
(def min-length-token (max 1 (- 141 (.length base-url))))
(def min-length-remove-code 10)
(def url-ttl 2592000)

(defn generate-token [url]
  "Generates a random token guaranteed to be longer than `url`."
  (clojure.string/replace
    (random/base64 (max (+ (.length url) 1) min-length-token))
    #"(\+|=|/)" "_"))

(defn generate-remove-code []
  "Generates a random removal code."
  (clojure.string/replace (random/base64 min-length-remove-code)
    #"(\+|=|/)" "_"))

(defn decorate-url [url]
  "Prepend 'http://' to URLs that lack it."
  (if (= (.indexOf url "http://") 0)
    url
    (str "http://" url)))

;; Actions
(defn translate-url [token]
  "Fetches the URL corresponding to a token."
  (redis (car/hget token "url")))

(defn url-response [url remove-code token]
  "Generates the response page for URLs."
  (stencil/render-file "templates/url"
                       {"url" url
                        "remove-code" remove-code
                        "token" token
                        "base" base-url}))

(defn generate-unique-url [url]
  "Generates a unique translated URL given a URL."
  ;; We only make the removal code once
  (let [remove-code (generate-remove-code)
        url         (decorate-url url)]
    ;; Then loop over randomly generated URLs until
    ;; we've found one that isn't taken. Efficient. Totally.
    (loop [token (generate-token url)]
      (if (= (redis (car/exists token)) 1)
        (recur (generate-token url))
        ;; Add all the relevant information to redis
        (do 
          (redis (car/hset token "url" url))
          (redis (car/hset token "remove" remove-code))
          (redis (car/set url token))
          (redis (car/expire url url-ttl))
          (redis (car/expire token url-ttl))
          (url-response url remove-code token))))))

(defn generate-url [url]
  "Generates a lengthened URL"
  (let [url (decorate-url url)]
    (if (= (redis (car/exists url)) 1)
      ;; If it exsits, return existing url
      (let [token (redis (car/get url))
            remove-code (redis (car/hget token "remove"))]
        (url-response url remove-code token))
      ;; Otherwise, generate a new one
      (generate-unique-url url))))

(defn remove-url-from-redis [url token]
  "Deletes a URL from redis."
  (redis (car/del token))
  (redis (car/del url))
  (str "Deleted URL " url))

(defn remove-url-helper [token code]
  "Validates removal code. If valid, deletes."
  (let [remove-code (redis (car/hget token "remove"))
        url         (redis (car/hget token "url"))]
    (if (= remove-code code)
      ;; DELETE
      (remove-url-from-redis url token)
      "Invalid removal code.")))

(defn remove-url [token code]
  "Attempts to remove a URL from the database."
  (if (= (redis (car/exists token)) 1)
    (remove-url-helper token code)
    (str "URL /" token "does not exist.")))

