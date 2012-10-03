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

(def -main (run-jetty (handler/site main-routes) {:port 3000}))

