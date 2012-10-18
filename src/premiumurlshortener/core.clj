(ns premiumurlshortener.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [premiumurlshortener.views :as views]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  ;; Main route. Form to create URL's.
  (GET "/" [] (views/index-page))
  ;; POST route to generate URL's.
  (POST "/" [url] (views/generate-url url))
  ;; Retrieve a URL.
  (GET "/:url" [url] (views/translate-url url))
  ;; Delete a URL.
  (GET "/:url/remove/:code" [url code] (views/remove-url url code))
  (route/resources "/static")
  (route/not-found "Page not found."))

;; Ring plugin compliance
(def app (handler/site main-routes))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))
        host (get (System/getenv) "HOST" "0.0.0.0")]
  (run-jetty app {:port port :host host})))

