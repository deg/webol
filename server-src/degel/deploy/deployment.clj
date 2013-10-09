(ns degel.deploy.deployment
  (:gen-class)
  (:require [cemerick.austin :as austin]
            [cemerick.austin.repls :as austin-repls]
            [degel.muxx.server :as muxx]
            [degel.receipts.server :as receipts]
            [degel.webol.server :as webol]
            [degel.cljutil.devutils :as dev]))


(defn -main [& [port]]
  (let [port (Integer. (or port (System/getenv "PORT") 3000))]
    (muxx/run-servers :port port
                      :apps [(receipts/app-properties)
                             (webol/app-properties)])))


(def repl-env
  (reset! austin-repls/browser-repl-env (austin/repl-env)))


(defn start-cljs-repl []
  (-main)
  (austin-repls/cljs-repl repl-env))
