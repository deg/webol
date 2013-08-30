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
  (store/put! [:program] (sorted-map))
  (store/put! [:program-vars] {}))


(declare format-expr)
(defn- format-list [exprs]
  (str/join ", " (map format-expr exprs)))


(defn- format-let [[lhs rhs]]
  (str "LET " (format-expr lhs) " = " (format-expr rhs)))

(defn- format-if [[[- lhs op rhs] [- statement]]]
  (str "IF " (format-expr lhs) " " op " " (format-expr rhs) " THEN " (format-expr statement)))

(defn format-expr [expr]
  (cond (number? expr) (str expr)
        (string? expr) (str "\"" expr "\"")
        (vector? expr) (let [[expr-type & expr-vals] expr]
                         (condp = expr-type
                           :print-cmd (str "PRINT " (format-list expr-vals))
                           :dim-statement (str "DIM " (format-list expr-vals))
                           :goto-statement (str "GOTO " (-> expr-vals first second))
                           :let-statement (format-let expr-vals)
                           :if-statement (format-if expr-vals)
                           :rem-statement (str "REM" (-> expr-vals first second))
                           :add (str/join "+" (map format-expr expr-vals))
                           :sub (str/join "-" (map format-expr expr-vals))
                           :mul (str/join "*" (map format-expr expr-vals))
                           :div (str/join "/" (map format-expr expr-vals))
                           :parens (str "(" (format-expr (first expr-vals)) ")")
                           :var (first expr-vals)
                           (str "<*** UNKNOWN VEXPR: " expr-type " " expr-vals ">")))))


(defn format-line [line-num statement-tree]
  (str line-num " " (format-expr statement-tree)))


(defn record-progline [[[- line-num] [- statement]]]
  (store/update! [:program] assoc line-num statement)
  (screen/line-out (format-line line-num statement)))


(defn set-program [program]
  (store/put! [:program] (into (sorted-map) program)))


(defn get-program []
  (store/fetch [:program]))


(defn list-program []
  (doseq [[line-num statement] (get-program)]
    (screen/line-out (format-line line-num statement))))


(defn next-line
  "[TODO] No way this can be efficent. But, may not matter yet. Optimize when needed."
  [program line]
  (first (subseq program > line)))


(declare interpret interpret-expr)

(defn run-program [{:keys [trace]}]
  (let [program (store/fetch [:program])
        max-lines 100] ;; [TODO] TEMP.. needed until we have vars and/or if and/or end and/or interrupt
    (loop [[line-num statement] (next-line program 0)
           ttl max-lines]
      (store/put! [:register :pc] line-num)
      (when (and line-num (> ttl 0))
        (when trace
          (screen/line-out (format-line line-num statement) {:color "Red"}))
        (interpret statement)
        (recur (next-line program (store/fetch [:register :pc]))
               (dec ttl))))
    (screen/line-out "** DONE **")))


(defn interpret-dim [vars]
  (doseq [[- var] vars]
    (store/update! [:program-vars] assoc var 0)))



(defn- interpret-if [[[- lhs op rhs] [- statement]]]
  (let [left (interpret-expr lhs)
        right (interpret-expr rhs)
        fcn (get {"==" == "!=" not= "<" < "<=" <= ">" > ">=" >=} op)]
    (if (fcn left right)
      (interpret statement))))


(defn interpret-goto [[[- line-num]]]
  (store/put! [:register :pc] (dec line-num)))


(defn error ;; [TODO] TEMP
  [& msg]
  (let [line (store/fetch [:register :pc])]
  (screen/line-out
   (apply str (if line (str "[line " (store/fetch [:register :pc]) "] ") "")
          msg))))


(defn interpret-let [[[- lhs] rhs]]
  (if (get (store/fetch [:program-vars]) lhs)
    (store/update! [:program-vars] assoc lhs (interpret-expr rhs))
    (error "Undefined lhs variable: " lhs)))


(defn get-var-val [var]
  (or (get (store/fetch [:program-vars]) var)
      (error "Undefined variable: " var)))


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
                           :var (get-var-val (first expr-vals))
                           (str "<*** UNKNOWN VEXPR: " expr-type " " expr-vals ">")))
        :else "<*** UNKNOWN EXPR: " expr ">"))


(defn interpret [[action & rest]]
  (condp = action
    :dim-statement
    (interpret-dim rest)

    :goto-statement
    (interpret-goto rest)

    :if-statement
    (interpret-if rest)

    :let-statement
    (interpret-let rest)

    :rem-statement
    (do)

    :clear-cmd
    (clear-program rest)

    :print-cmd
    (->> (map interpret-expr rest) (str/join " ") screen/line-out)

    :list-cmd
    (list-program)

    :run-cmd
    (run-program {:trace false})

    :trace-cmd
    (run-program {:trace true})

    :progline
    (record-progline rest)

    :manual-cmd
    (.open js/window "/webol-help.html" "Webol Help" "width=700,height=500,resizable=1")

    :bad-cmd
    (show-language-help (first rest))
    :help-cmd
    (show-language-help nil)

    (screen/line-out (str "*** Unknown PARSE: " action ": " rest) {:color "DarkRed"})))
