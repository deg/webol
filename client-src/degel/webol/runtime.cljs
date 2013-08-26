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


(defn clear-program []
  (store/put! [:program (sorted-map)]))


(defn format-expr [expr]
  (cond (number? expr) (str expr)
        (string? expr) (str "\"" expr "\" ")
        (vector? expr) (let [[expr-type & expr-vals] expr]
                         (condp = expr-type
                           :print-cmd (str "PRINT " (str/join " " (map format-expr expr-vals)))
                           :add (str/join "+" (map format-expr expr-vals))
                           :sub (str/join "-" (map format-expr expr-vals))
                           :mul (str/join "*" (map format-expr expr-vals))
                           :div (str/join "/" (map format-expr expr-vals))
                           :parens (str "(" (format-expr (first expr-vals)) ")")
                           (str "<*** UNKNOWN VEXPR: " expr-type " " expr-vals ">")))))


(defn format-line [line-num statement-tree]
  (str line-num " " (format-expr statement-tree)))


(defn record-progline [[[- line-num] [- statement]]]
  (store/update! [:program] assoc line-num statement)
  (screen/line-out (format-line line-num statement)))


(defn list-program []
  (doseq [[line-num statement] (store/fetch [:program])]
    (screen/line-out (format-line line-num statement))))


(defn interpret-expr [expr]
  (cond (number? expr) expr
        (string? expr) expr
        (vector? expr) (let [[expr-type & expr-vals] expr]
                         (condp = expr-type
                           :add (reduce + (map interpret-expr expr-vals))
                           :sub (reduce - (map interpret-expr expr-vals))
                           :mul (reduce * (map interpret-expr expr-vals))
                           :div (reduce / (map interpret-expr expr-vals))
                           :parens (interpret-expr (first expr-vals))
                           (str "<*** UNKNOWN VEXPR: " expr-type " " expr-vals ">")))
        :else "<*** UNKNOWN EXPR: " expr ">"))


(defn interpret [[action & rest]]
  (condp = action
    :print-cmd
    (->> (map interpret-expr rest) (str/join " ") screen/line-out)

    :list-cmd
    (list-program)

    :progline
    (record-progline rest)

    :manual-cmd
    (.open js/window "/webol-help.html" "Webol Help" "width=700,height=500,resizable=1")

    :bad-cmd
    (show-language-help (first rest))
    :help-cmd
    (show-language-help nil)

    (screen/line-out (str "*** Unknown PARSE: " action ": " rest) {:color "DarkRed"})))
