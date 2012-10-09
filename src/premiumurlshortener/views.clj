(ns premiumurlshortener.views
  (:require [premiumurlshortener.url :as url]
            [compojure.route :as route]
            [stencil.core :as stencil]))

(defn index-page []
  "Renders the index page."
  (stencil/render-file "static/index" {}))

(defn translate-url [token]
  "Redirects a 'shortened' URL"
  (let [translation (url/translate-url token)]
    (if (nil? translation)
      (route/not-found "Premium Shortened URL Not Found.")
      {:status 302
       :headers {"Location" translation}})))

(defn remove-url [url code]
  "Removes a shortened URL."
  (url/remove-url url code))

(defn generate-url [url]
  "Generates a new 'shortened' URL."
  (url/generate-url url))

