(ns premiumurlshortener.core
  (:use compojure.core
        premiumurlshortener.views
        ring.adapter.jetty)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/" [] (index-page))
  (route/not-found "Page not found"))

(def app (handler/site main-routes))

(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
  (run-jetty app {:port port})))

