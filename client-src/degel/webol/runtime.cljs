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
        "  STEP (not yet implemented)\n"
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
                           :print-cmd (str "PRINT " (format-list expr-vals) ",")
                           :println-cmd (str "PRINT " (format-list expr-vals))
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
  (screen/line-out (format-line line-num statement) {}))


(defn set-program [program]
  (store/put! [:program] (into (sorted-map) program)))


(defn get-program []
  (store/fetch [:program]))


(defn list-program []
  (doseq [[line-num statement] (get-program)]
    (screen/line-out (format-line line-num statement) {})))


(defn next-line
  ;; [TODO] {FogBugz:143} No way this is efficent. But, may not matter yet. Optimize when needed.
  [program line]
  (first (subseq program > line)))


(declare interpret interpret1 interpret-expr)


(defn- continue-program [program {:keys [trace] :as flags}]
  (let [[line-num statement] (next-line program (store/fetch [:register :pc]))]
    (store/put! [:register :pc] line-num)
    (if (nil? line-num)
      (screen/line-out "** DONE **" {})
      (do
        (when trace
          (screen/line-out (format-line line-num statement) {:color "Red"}))
        (interpret statement)
        ((.-setTimeout js/window) #(continue-program program flags) 0)))))


(defn run-program [{:keys [trace] :as flags}]
  (let [program (store/fetch [:program])]
    (store/put! [:register :pc] 0)
    (continue-program program trace)))


(defn interpret-dim [vars]
  (doseq [[- var] vars]
    (store/update! [:program-vars] assoc var 0)))



(defn- interpret-if [[[- lhs op rhs] [- statement]]]
  (let [left (interpret-expr lhs)
        right (interpret-expr rhs)
        fcn (get {"==" == "!=" not= "<" < "<=" <= ">" > ">=" >=} op)]
    (if (fcn left right)
      (interpret1 statement))))


(defn interpret-goto [[[- line-num]]]
  (store/put! [:register :pc] (dec line-num)))


(defn error ;; [TODO] {FogBugz:144} TEMP
  [& msg-and-params]
  (let [line (store/fetch [:register :pc])
        line-prefix (when (and line (> line 0))
                    (str "[line " (store/fetch [:register :pc]) "] "))
        errmsg (apply str line-prefix msg-and-params)]
    (throw (js/Error. errmsg "(no file yet)" line))))


(defn interpret-let [[[- lhs] rhs]]
  (if (get (store/fetch [:program-vars]) lhs)
    (store/update! [:program-vars] assoc lhs (interpret-expr rhs))
    (error "Undefined assignment '" lhs "'")))


(defn get-var-val [var]
  (or (get (store/fetch [:program-vars]) var)
      (error "Undefined variable '" var "'")))


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


;; STILL TO DO:
;;
;; Graphics mode
;; - GMODE
;; - GSET x y
;; - GCLEAR
;; - GLINE x y
;;
;; INPUT
;;
;; FUNCTIONS
;; - Math functions
;;   ABS, trig, SQRT, FLOOR, CEILING

(defn- interpret1 [[action & rest]]
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
    (clear-program)

    :print-cmd
    (screen/text-out (str/join " " (map interpret-expr rest)) {})

    :println-cmd
    (screen/line-out (str/join " " (map interpret-expr rest)) {})

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


(defn interpret [statement]
  (try (interpret1 statement)
       (catch js/Error e
         (screen/line-out (str "Fatal error: " (.-message e)) {:color "DarkRed"})
         (screen/line-out "Program ended." {}))))
