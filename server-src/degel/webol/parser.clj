(ns degel.webol.parser
  (:require [clojure.edn]
            [instaparse.core :as insta]))

(defn read-grammar
  ([]
     (read-grammar "server-src/degel/webol/grammar.txt"))
  ([filename]
     (slurp filename)))

(def ^:private line-parser (insta/parser (read-grammar)))

(defn parse-line [line]
  (let [rslt (->> (line-parser line)
                  (insta/transform
                   {:integer clojure.edn/read-string
                    :float #(clojure.edn/read-string
                             ;; Work around Clojure's failure to parse, e.g., ".7" as a float.
                             (if (= (first %) \.) (str "0" %) %))
                    :string-with-embedded-dquotes str}))]
    (cond
     (insta/failure? rslt)
     {:status :error :error (insta/get-failure rslt)}

     (and (= (count rslt) 2) (= (first rslt) :input-line))
     {:status :success :parse (second rslt)}

     ;; Should never happen, unless grammar is broken
     :else {:status :error :error rslt})))

