(ns premiumurlshortener.core
  (:use compojure.core
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [premiumurlshortener.views :as views]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/" [] (views/index-page))
  (POST "/" [url] (views/generate-url url))
  (GET "/:url" [url] (views/translate-url url))
  (GET "/:url/remove/:code" [url code] (views/remove-url url code))
  (route/not-found "Page not found."))

(def app (handler/site main-routes))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))
        host (get (System/getenv) "HOST" "0.0.0.0")]
  (run-jetty app {:port port :host host})))

