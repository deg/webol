(defproject receipts "0.1.0-SNAPSHOT"
  :description "HTML5 web-app to manage our receipts."
  :url "https://bitbucket.org/degeldeg/receipts"
  :license {:name "Proprietary software - this is not open-source"
            :url "http://nonesuch/com/no-license.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ;; Degel's Clojure utility library
                 [degel-clojure-utils "0.1.6"]

                 ;; Degel's Redmapel state tree library
                 [redmapel "0.1.7"]

                 ;; Needed, I think, in order to build a runnable uberjar with compojure
                 [ring/ring-jetty-adapter "1.1.8"]

                 ;; Routing library for Ring web application library
                 [compojure "1.1.5" :exclusions [ring/ring-core org.clojure/tools.macro]]

                 ;; DOM manipulation library for ClojureScript
                 [domina "1.0.2-SNAPSHOT"]

                 ;; HTML generation for Clojurescript (Ported from Clojure Hiccup)
                 [hiccups "0.2.0"]

                 ;; Ring/Compojure RPC
                 ;; [TODO] Look at the other Shoreleave libs too. Support for local storage,
                 ;;        browser history, repl, etc.
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]

                 ;; Clojure/ClojureScript validation
                 [com.cemerick/valip "0.3.2"]

                 ;; Clojure interface to AWS SimpleDB
                 [com.cemerick/rummage "1.0.1" :exclusions [commons-codec]]

                 ;; For CLJS REPL
                 [com.cemerick/piggieback "0.0.4"]
                 ]

  :plugins [[lein-cljsbuild "0.3.2" :exclusions [org.clojure/clojure]]
            [lein-ring "0.8.3" :exclusions [org.clojure/clojure]]

            ;; Not supported in lein 2.2.0, but here as a reminder to get features
            ;; mentioned in https://groups.google.com/forum/#!msg/clojure/9cA5hvFJTkw/fnWwxvALd64J
            #_[lein-pedantic "0.0.5"]
            ]

  :min-lein-version "2.0.0"

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :source-paths ["server-src"]
  :ring {:handler degel.receipts.server/app}
  ;:profiles {:dev {:hooks [leiningen.cljsbuild]}}
  :main degel.receipts.server

  :cljsbuild {
    :crossovers [valip.core
                 valip.predicates
                 degel.cljutil.utils
                 degel.redmapel
                 degel.receipts.db
                 degel.receipts.static-validators]
    :builds {
      :dev {
            :source-paths ["client-src" "client-src-dev"]
            :compiler {:output-to "resources/public/js/receipts-dev.js"
                       :optimizations :whitespace
                       :pretty-print true}
            :jar false}
      :production {
            :source-paths ["client-src" "client-src-dev"] ; TODO brepl not safe here
            :compiler {:output-to "resources/public/js/receipts.js"
                       :optimizations #_ :advanced :simple ; TODO Until dom issue resolved
                       :pretty-print #_ false true}
            :jar true}}})

