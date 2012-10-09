(defproject premiumurlshortener "0.1.0-SNAPSHOT"
  :description "Premium URL Shortening Service"
  :url "http://premiumurlshorteningservice.herokuapp.com"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.3"]
                 [ring "1.2.0-SNAPSHOT"]
                 [com.taoensso/carmine "0.11.1"]
                 [crypto-random "1.1.0"]
                 [stencil "0.3.1"]]
  :plugins [[lein-ring "0.7.3"]]
  :ring {:handler premiumurlshortener.core/app}
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}}
  :main premiumurlshortener.core)

