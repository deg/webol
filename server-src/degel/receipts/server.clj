(ns degel.receipts.server
  (:require [compojure.core :refer [defroutes GET]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [compojure.handler :refer [site]]))

(defroutes app-routes
  ; to serve document root address
  (GET "/" [] (redirect "/new-receipt.html"))
  ; to serve static pages saved in resources/public directory
  (resources "/")
  ; if page is not found
  #_(not-found "<h1>Page not found.</h1>"))

(def handler
  (site app-routes))


