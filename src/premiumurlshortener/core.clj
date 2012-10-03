(ns premiumurlshortener.core
  (:use compojure.core
        premiumurlshortener.views)
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [compojure.response :as response]))

(defroutes main-routes
  (GET "/" [] (index-page)))

(def main (handler/site main))

