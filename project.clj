(defproject receipts "0.1.0-SNAPSHOT"
  :description "HTML5 web-app to manage our receipts."
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.5"]
                 [domina "1.0.2-SNAPSHOT"]
                 [hiccups "0.2.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]]
  :plugins [[lein-cljsbuild "0.3.0"]
            [lein-ring "0.8.3"]]
  :hooks [leiningen.cljsbuild]
  :source-paths ["src/clj"]
  :ring {:handler degel.receipts.server.remotes/app}
  :cljsbuild { 
    :builds {
      :dev {
            :source-paths ["src/cljs" "src/brepl"]
            :compiler {:output-to "resources/public/js/receipts-dev.js"
                       :optimizations :simple
                       :pretty-print true}
            :jar true}
      :prod {
            :source-paths ["src/cljs" "src/brepl"] ; TODO brepl not safe here
            :compiler {:output-to "resources/public/js/receipts.js"
                       :optimizations :advanced
                       :pretty-print true}
            :jar true}}}
  :main receipts.server)

