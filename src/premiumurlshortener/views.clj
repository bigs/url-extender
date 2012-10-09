(ns premiumurlshortener.views
  (:require [premiumurlshortener.url :as url]
            [compojure.route :as route]))

(defn index-page []
  "Hello, world.")

(defn translate-url [token]
  (let [translation (url/translate-url token)]
    (if (nil? translation)
      (route/not-found "Premium Shortened URL Not Found.")
      {:status 302
       :headers {"Location" translation}})))

(defn remove-url [url code]
  (url/remove-url url code))

(defn generate-url [url]
  (url/generate-url url))

