(ns degel.webol.runtime
  (:require [domina :as dom :refer [log]]
            [clojure.string :as str]
            [degel.webol.screen :as screen]
            [degel.webol.store :as store]))


(defn show-language-help [bad-cmd]
  (screen/line-out
   (str (if bad-cmd (str "Error: \"" bad-cmd "\" is not a legal command to Webol\n") "")
        "Try one of:\n"
        "  EDIT (not yet implemented)\n"
        "  HELP - show this help\n"
        "  LIST (not yet implemented)\n"
        "  MAN or MANUAL - Popup web page about Webol\n"
        "  PRINT - print an expression\n"
        "  RENUMBER (not yet implemented)\n"
        "  RUN (not yet implemented)\n"
        "  TRACE (not yet implemented)")
   {:color (if bad-cmd "DarkRed" "DarkBlue")}))
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

    :manual-cmd
    (.open js/window "/webol-help.html" "Webol Help" "width=700,height=500,resizable=1")

    :bad-cmd
    (show-language-help (first rest))
    :help-cmd
    (show-language-help nil)

    (screen/line-out (str "*** Unknown PARSE: " action ": " rest) {:color "DarkRed"})))
