(ns degel.webol.runtime
  (:require [domina :as dom :refer [log]]
            [clojure.string :as str]
            [degel.webol.screen :as screen]
            [degel.webol.store :as store]
            [degel.utils.storage :as storage]))


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
        "  RUN - Run the program\n"
        "  STEP - Run just one step of the program\n"
        "  TRACE - Run the program, listing each step as it runs")
   {:color (if bad-cmd "DarkRed" "DarkBlue")}))


(defn stop-program-run [{:keys [msg stay-at-current-line]}]
  "Terminate the running program."
  (store/put! [:register :running] false)
  (store/put! [:register :open-loops] nil)
  (when-not stay-at-current-line
    (store/put! [:register :pc] 0))
  (when msg
    (screen/line-out (str "** " msg " **"){:color "DarkRed"})))


(defn clear-program
  "Clear the whole program or part of it.
   CLEAR     - clear everything
   CLEAR n   - clear everything from line N until the end of the program
   CLEAR n,m - clear everything from line N through line M"
  ([] (clear-program nil nil))
  ([start-line end-line]
     (cond (nil? start-line)
           (store/put! [:program] (sorted-map))
           (nil? end-line)
           (store/put! [:program] (subseq (store/fetch [:program]) < start-line))
           :else
           (store/put! [:program]
                       (into (sorted-map)
                             (filter #(or (< (first %) start-line) (> (first %) end-line))
                                     (store/fetch [:program])))))
     (store/put! [:program-vars] {})
     (stop-program-run {})))


(declare format-expr)
(defn- format-list [exprs]
  (str/join ", " (map format-expr exprs)))


(defn- format-let [[lhs rhs]]
  (str "LET " (format-expr lhs) " = " (format-expr rhs)))

(defn- format-for [[var start end skip]]
  (str "FOR " (format-expr var) " = "
       (format-expr start) " TO " (format-expr end)
       (when skip
         (str " BY " (format-expr skip)))))

(defn- format-if [[[_ lhs op rhs] [- statement]]]
  (str "IF " (format-expr lhs) " " op " " (format-expr rhs) " THEN " (format-expr statement)))

(defn format-expr [expr]
  (cond (number? expr) (str expr)
        (string? expr) (str "\"" expr "\"")
        (vector? expr) (let [[expr-type & expr-vals] expr]
                         (condp = expr-type
                           :print-bare-cmd (str "PRINT")
                           :print-cmd (str "PRINT " (format-list expr-vals) ",")
                           :println-cmd (str "PRINT " (format-list expr-vals))
                           :dim-statement (str "DIM " (format-list expr-vals))
                           :for-statement (format-for expr-vals)
                           :goto-statement (str "GOTO " (-> expr-vals first second))
                           :let-statement (format-let expr-vals)
                           :if-statement (format-if expr-vals)
                           :next-statement (str "NEXT " (-> expr-vals first format-expr))
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
  (or (first (subseq program > line))
      [0 nil]))


(declare interpret-top-level interpret interpret-expr)


(defn- continue-program [program {:keys [trace one-step] :as flags}]
  (let [[line-num statement] (next-line program (store/fetch [:register :pc]))]
    (store/put! [:register :pc] line-num)
    (cond (= 0 line-num)
          (stop-program-run {:msg "Done"})

          (not (store/fetch [:register :running]))
          (stop-program-run {}) ;; No msg, since already halted

          :else
          (do
            (when trace
              (screen/line-out (format-line line-num statement) {:color "Red"}))
            (interpret-top-level statement)
            (if one-step
              (stop-program-run {:stay-at-current-line true})
              ((.-setTimeout js/window) #(continue-program program flags) 0))))))


(defn run-program [{:keys [trace one-step] :as flags}]
  (if (store/fetch [:register :running])
    (screen/line-out "Can't run while a program is already running" {:color "DarkRed"})
    (let [program (store/fetch [:program])]
      (store/put! [:register :running] true)
      (when-not one-step
        (store/put! [:register :pc] 0))
      (continue-program program flags))))


(defn error ;; [TODO] {FogBugz:144} TEMP
  [& msg-and-params]
  (let [line (store/fetch [:register :pc])
        line-prefix (when (and line (> line 0))
                    (str "[line " (store/fetch [:register :pc]) "] "))
        errmsg (apply str line-prefix msg-and-params)]
    (throw (js/Error. errmsg "(no file yet)" line))))


(defn- init-var [var]
  (store/update! [:program-vars] assoc var 0))

(defn- get-var-val [var]
  (or (get (store/fetch [:program-vars]) var)
      (error "Undefined variable '" var "'")))

(defn- assign-var [var val]
  (if (get (store/fetch [:program-vars]) var)
    (store/update! [:program-vars] assoc var (interpret-expr val))
    (error "Undefined assignment '" var "'")))


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

(defmulti interpret first)

(defmethod interpret :default [[action & rest]]
  (screen/line-out (str "*** Unknown PARSE: " action ": " rest) {:color "DarkRed"}))

(defmethod interpret :bad-cmd [[_ bad-cmd & _]]
  (show-language-help bad-cmd))

(defmethod interpret :abort-cmd [_]
  (stop-program-run {:msg "Aborted"}))

(defmethod interpret :dim-statement [[_ & vars]]
  (doseq [[- var] vars]
    (init-var var)))

(defmethod interpret :goto-statement [[_ [_ line-num]]]
  (store/put! [:register :pc] (dec line-num)))

(defmethod interpret :for-statement [[_ [_ var] start end skip]]
  (let [open-loops (store/fetch [:register :open-loops])
        open-loop-vars (map :var open-loops)]
    (when (some #{var} open-loop-vars)
      (error "Can't nest loops of " var))
    (store/update! [:register :open-loops] conj
                   {:line (store/fetch [:register :pc])
                    :var var
                    :start start
                    :end end
                    :skip (or skip 1)}))
  (assign-var var start))

(defmethod interpret :next-statement [[_ [_ var]]]
  (let [open-loop (first (store/fetch [:register :open-loops]))]
    (if (= var (:var open-loop))
      (let [new-val (+ (get-var-val var) (:skip open-loop))]
        (assign-var var new-val)
        (if (> new-val (:end open-loop))
          (store/update! [:register :open-loops] pop)
          (store/put! [:register :pc] (:line open-loop))))
      (error "'NEXT " var
             (if (nil? open-loop)
               (str "', but no open loop.")
               (str "' cannot close loop on " (:var open-loop)
                    " (opened at line " (:line open-loop) ")."))))))


(defmethod interpret :if-statement [[_ [_ lhs op rhs] [_ statement]]]
  (let [left (interpret-expr lhs)
        right (interpret-expr rhs)
        fcn (get {"==" == "!=" not= "<" < "<=" <= ">" > ">=" >=} op)]
    (if (fcn left right)
      (interpret statement))))

(defmethod interpret :let-statement [[_ [_ lhs] rhs]]
  (assign-var lhs rhs))

(defmethod interpret :rem-statement [_]
  (do))

(defmethod interpret :clear-cmd [[_ [_ start-line] [_ end-line]]]
  (clear-program start-line end-line))

(defmethod interpret :print-cmd [[_ & exprs]]
  (screen/text-out (str/join " " (map interpret-expr exprs)) {}))

(defmethod interpret :println-cmd [[_ & exprs]]
  (screen/line-out (str/join " " (map interpret-expr exprs)) {}))

(defmethod interpret :print-bare-cmd [_]
  (screen/line-out " " {}))

(defmethod interpret :list-cmd [_]
  (list-program))

(defmethod interpret :run-cmd [_]
  (run-program {:trace false}))

(defmethod interpret :trace-cmd [_]
  (run-program {:trace true}))

(defmethod interpret :step-cmd [_]
  (run-program {:trace true :one-step true}))

(defmethod interpret :save-cmd [[_ new-name]]
  (let [name (or new-name (store/fetch [:program :name]))]
    (store/put! [:program :name] name)
    (storage/write-local ["program" name] (get-program))
    (let [dir (storage/read "program-directory" nil)]
      (when-not (some #{name} dir)
        (storage/write-local "program-directory" (conj dir name))))))

(defmethod interpret :load-cmd [[_ name]]
  (let [got (storage/read ["program" name] nil)]
    (set-program (storage/read ["program" name] nil))
    (store/put! [:program :name] name)))

(defmethod interpret :dir-cmd [_]
  (screen/line-out "Saved programs:" {})
  (doseq [program  (storage/read "program-directory" nil)]
    (screen/line-out (str "- " program) {})))

(defmethod interpret :destroy-cmd [[_ name]]
  (storage/write-local "program-directory"
                       (remove #{name} (storage/read "program-directory" nil)))
  (storage/delete ["program" name]))


(defmethod interpret :progline [[_ [_ line-num] [_ statement]]]
  (store/update! [:program] assoc line-num statement)
  (screen/line-out (format-line line-num statement) {}))

(defmethod interpret :help-cmd [_]
  (show-language-help nil))


(defn interpret-top-level [statement]
  (try (interpret statement)
       (catch js/Error e
         (stop-program-run {:msg (str "Fatal error: " (.-message e))}))))
