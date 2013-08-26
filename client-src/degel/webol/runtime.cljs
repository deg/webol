(ns degel.webol.runtime
  (:require [domina :as dom :refer [log]]
            [clojure.string :as str]
            [degel.webol.screen :as screen]
            [degel.webol.store :as store]))


(defn interpret-expr [expr]
  (cond (number? expr) expr
        (string? expr) expr
        (vector? expr) (let [[expr-type & expr-vals] expr]
                         (condp = expr-type
                           :arith (interpret-expr (first expr-vals))
                           :add (reduce + (map interpret-expr expr-vals))
                           :sub (reduce - (map interpret-expr expr-vals))
                           :mul (reduce * (map interpret-expr expr-vals))
                           :div (reduce / (map interpret-expr expr-vals))
                           (str "<*** UNKNOWN VEXPR: " expr-type " " expr-vals ">")))
        :else "<*** UNKNOWN EXPR: " expr ">"))


(defn interpret [[action & rest]]
  (condp = action
    :print-cmd
    (->> (map interpret-expr rest) (str/join " ") screen/line-out)

    (screen/line-out (str "*** Unknown PARSE: " action ": " rest) {:color "DarkRed"})))
