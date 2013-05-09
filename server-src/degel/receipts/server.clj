(ns degel.receipts.server
  (:require [compojure.core :refer [defroutes GET POST]]
            [compojure.route :refer [resources not-found]]
            [ring.util.response :refer [redirect]]
            [compojure.handler :refer [site]]))

(defroutes app-routes
  (GET "/" [] (redirect "/new-receipt.html"))
  ; to serve static pages saved in resources/public directory
  (resources "/")
  (not-found "<h1>David moans: 'page not found'.</h1>"))


