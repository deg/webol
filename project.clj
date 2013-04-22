(defproject receipts "0.1.0-SNAPSHOT"
  :description "HTML5 web-app to manage our receipts."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [domina "1.0.2-SNAPSHOT"]
                 [hiccups "0.2.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [com.cemerick/rummage "1.0.1"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.3"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["server-src"]
  :ring {:handler degel.receipts.remotes/app}
  :cljsbuild {
    :crossovers [valip.core valip.predicates degel.receipts.static-validators]
    :builds {
      :dev {
            :source-paths ["client-src" "client-src-dev"]
            :compiler {:output-to "resources/public/js/receipts-dev.js"
                       :optimizations :whitespace
                       :pretty-print true}
            :jar true}
      :prod {
            :source-paths ["client-src" "client-src-dev"] ; TODO brepl not safe here
            :compiler {:output-to "resources/public/js/receipts.js"
                       :optimizations :advanced
                       :pretty-print true}
            :jar true}}}
  :main receipts.server)

