(ns premiumurlshortener.url
  (:require [taoensso.carmine :as car]
            [crypto.random :as random]
            [stencil.core :as stencil]))

;; Define redis connection
(def redis-host (get (System/getenv) "REDIS_HOST" "127.0.0.1"))
(def redis-port (Integer/parseInt (get (System/getenv) "REDIS_PORT" "6379")))
(def redis-db (Integer/parseInt (get (System/getenv) "REDIS_DB" "0")))
(def redis-password (get (System/getenv) "REDIS_PASSWORD" nil))

(def redis-pool (car/make-conn-pool))
(def redis-server (car/make-conn-spec
                    :host redis-host
                    :port redis-port
                    :password redis-password
                    :timeout 4000))

;; Create a simple macro
(defmacro redis [& body] `(car/with-conn redis-pool redis-server ~@body))

;; Helpers
(def min-length-token 109)
(def min-length-remove-code 10)
(def url-ttl 2592000)

(defn generate-token [url]
  (clojure.string/replace (random/base64 (max (+ (.length url) 1) min-length-token))
    #"(\+|=|/)" "_"))

(defn generate-remove-code []
  (clojure.string/replace (random/base64 min-length-remove-code)
    #"(\+|=|/)" "_"))

;; Actions
(defn translate-url [token]
  (redis (car/hget token "url")))

(defn generate-unique-url [url]
  "Generates a unique translated URL given a URL."
  ;; We only make the removal code once
  (let [remove-code (generate-remove-code)]
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
          (stencil/render-file "templates/url"
                               {"url" url
                                "remove-code" remove-code
                                "token" token}))))))

(defn generate-url [url]
  "Generates a lengthened URL"
  (if (= (redis (car/exists url)) 1)
    ;; If it exsits, return existing url
    (translate-url (redis (car/get url)))
    ;; Otherwise, generate a new one
    (generate-unique-url url)))

(defn remove-url-from-redis [url token]
  (redis (car/del token))
  (redis (car/del url))
  "OK.")

(defn remove-url-helper [token code]
  (let [remove-code (redis (car/hget token "remove"))
        url         (redis (car/hget token "url"))]
    (if (= remove-code code)
      ;; DELETE
      (remove-url-from-redis url token)
      "Invalid removal code.")))

(defn remove-url [token code]
  (if (= (redis (car/exists token)) 1)
    (remove-url-helper token code)
    (str "URL /" token "does not exist.")))

