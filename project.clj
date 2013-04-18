(defproject receipts "0.1.0-SNAPSHOT"
  :description "HTML5 web-app to manage our receipts."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [domina "1.0.2-SNAPSHOT"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.3"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :ring {:handler receipts.server/handler}
  :cljsbuild { 
    :builds {
      :main {
        :source-paths ["src/cljs"]
        :compiler {:output-to "resources/public/js/cljs.js"
                   :optimizations :simple
                   :pretty-print true}
        :jar true}}}
  :main receipts.server)

