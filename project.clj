(defproject receipts "0.1.0-SNAPSHOT"
  :description "HTML5 web-app to manage our receipts."
  :url "http://example.com/FIXME"
  :license {:name "Proprietary software - this is not open-source"
            :url "http://nonesuch/com/no-license.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]

                 ;; Degel's Clojure utility library
                 [degel-clojure-utils "0.1.2"  :exclusions [org.clojure/tools.macro]]

                 [compojure "1.1.5" #_:exclusions #_[ring/ring-core]]
                 [domina "1.0.2-SNAPSHOT"]
                 [hiccups "0.2.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [com.cemerick/valip "0.3.2"]
                 [com.cemerick/rummage "1.0.1" :exclusions [commons-codec]]]
  :plugins [[lein-cljsbuild "0.3.0" :exclusions [org.clojure/clojure]]
            [lein-ring "0.8.3" :exclusions [org.clojure/clojure]]
            ;; [TODO] Fix. For now, no pedantic, because of conflicts in compojure dependency
            ;; on ring/ring.core in local vs. Heroku deployment.
            #_[lein-pedantic "0.0.5"]]
  :min-lein-version "2.0.0"
  ;; [TODO] Figure out what this breaks 'lein trampoline ring server'
  ;;:hooks [leiningen.cljsbuild]
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
            :jar false}
      :production {
            :source-paths ["client-src" "client-src-dev"] ; TODO brepl not safe here
            :compiler {:output-to "resources/public/js/receipts.js"
                       :optimizations :advanced
                       :pretty-print true}
            :jar true}}})

