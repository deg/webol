(ns degel.receipts.server
  (:gen-class)
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.util.response :refer [redirect]]
            [shoreleave.middleware.rpc :refer [wrap-rpc]]
            [compojure.handler :refer [site]]))


(defroutes app-routes
  (GET "/" [] (redirect "/new-receipt.html"))
  ; to serve static pages saved in resources/public directory
  (resources "/")
  (not-found "<h1>David moans: 'page not found'.</h1>"))


(def app (-> app-routes wrap-rpc site))


(defn -main [& [port]]
      (let [port (Integer. (or port (System/getenv "PORT") 5000))]
        (run-jetty #'app {:port port :join? false})))
