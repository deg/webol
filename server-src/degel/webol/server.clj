(ns degel.webol.server
  (:gen-class)
  (:require [shoreleave.middleware.rpc :as rpc]
            [degel.webol.parser :refer [parse-line]]
            [degel.muxx.server :as muxx]
            [degel.cljutil.introspect] ;; Needed just include code for rpc from client
            [degel.cljutil.devutils :as dev]))


(rpc/defremote get-parse-tree [line]
  (parse-line line true))


(defn app-properties
  "Descriptor of this web app, primarily for the sake of muxx."
  []
  {:name          "webol"
   :base-page     "/webol.html"
   :production-js "js/webol.js"
   :dev-js        "js/webol-dev.js"})



;; Standalone top-level. Either call this or wrap this app into a
;; larger muxx deployment.
(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT") 3000))]
    (muxx/run-servers :port port
                      :apps [(app-properties)])))
