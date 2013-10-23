(ns degel.webol.parser
  (:require [clojure.edn]
            [instaparse.core :as insta]))

;;; (defn read-grammar
;;;  ([]
;;;     (read-grammar "../receipts/server-src/degel/webol/grammar.txt"))
;;;  ([filename]
;;;     (slurp filename)))

;; [TODO] 10Oct13 - Need to move grammar into some kind of resource file.
;;        Loading from grammar.txt file was problematic because I couldn't
;;        find a path that would work from other projects and Heroku.
(def ^:private grammar
  "
(* A full line to be parsed *)
input-line = <ows> (action | (!action bad-cmd) | progline) <ows>

(* Immediate mode commands *)
<action> = abort-cmd | clear-cmd | edit-cmd | help-cmd | list-cmd |
           println-cmd | print-cmd | renumber-cmd | run-cmd | save-cmd |
           step-cmd | trace-cmd

abort-cmd     = <#\"(?i)abort\">
clear-cmd     = <#\"(?i)clear\">
edit-cmd     = <#\"(?i)edit\"> <ows> line-num
help-cmd     = <#\"(?i)help\">
list-cmd     = <#\"(?i)list\">
println-cmd    = <#\"(?i)print\"> <ows> expr-list
print-cmd    = <#\"(?i)print\"> <ows> expr-list <comma-delim>
renumber-cmd = <#\"(?i)renumber\"> (<ows> line-num)?
run-cmd      = <#\"(?i)run\"> (<ows> line-num)?
save-cmd    = <#\"(?i)save\">
step-cmd    = <#\"(?i)step\">
trace-cmd    = <#\"(?i)trace\">

bad-cmd      = #\"[a-zA-Z].*\"

progline  = line-num <ows> statement

statement = dim-statement | goto-statement | if-statement | let-statement|
            print-statement | rem-statement

dim-statement = <#\"(?i)dim\"> <ows> var-list
goto-statement = <#\"(?i)goto\"> <ows> line-num
if-statement = <#\"(?i)if\"> <ows> condition <ows> <#\"(?i)then\"> <ows> statement
let-statement = <#\"(?i)let\">  <ows> var <ows> <\"=\"> <ows> expr
<print-statement> = print-cmd | println-cmd
rem-statement = <#\"(?i)rem\"> comment

condition = expr <ows> cond-op <ows> expr
<cond-op> = \"==\" | \"!=\" | \"<\" | \"<=\" | \">\" | \">=\"

<var-list> = var | var-list <comma-delim> var

comment = #\".*\"

<expr-list> = expr | expr-list <comma-delim> expr
<expr> = quoted-string | arith

<quoted-string> = <dquote> string-with-embedded-dquotes <dquote>
<simple-string> = #\"[^\\\"]*\"
string-with-embedded-dquotes = simple-string | simple-string '\"\"' string-with-embedded-dquotes

<comma-delim> = <ows> <\",\"> <ows>

line-num = integer

(* Arithmetic expression parser *)
<arith> = add-sub
<add-sub> = mul-div | add | sub
add = add-sub <'+'> mul-div
sub = add-sub <'-'> mul-div
<mul-div> = term | mul | div
mul = mul-div <'*'> term
div = mul-div <'/'> term
<term> = atom | parens
parens = <'('> add-sub <')'>
<atom> = number | var
var = #'[A-Za-z][A-Za-z0-9]*'
<number> = integer | float
float = #'[0-9]*\\.[0-9]+'
integer = #'[0-9]+'

(* Lexical items *)
dquote = '\"'
squote = \"'\"
ows = #'\\s*'
"
  )

(def ^:private line-parser (insta/parser grammar #_(read-grammar)))

(defn- err-to-str [parse-result]
  (-> parse-result insta/get-failure
      (#(with-out-str (instaparse.failure/pprint-failure %)))))

(defn parse-line
  ([line]
     (parse-line line false))
  ([line errs-to-strs?]
     (let [rslt (->> (line-parser line)
                     (insta/transform
                      {:integer clojure.edn/read-string
                       :float #(clojure.edn/read-string
                                ;; Work around Clojure's failure to parse, e.g., ".7" as a float.
                                (if (= (first %) \.) (str "0" %) %))
                       :string-with-embedded-dquotes str}))]
       (cond
        (insta/failure? rslt)
        {:status :error :error (if errs-to-strs? (err-to-str rslt) rslt)}

        (and (= (count rslt) 2) (= (first rslt) :input-line))
        {:status :success :parse (second rslt)}

        ;; Should never happen, unless grammar is broken
        :else {:status :error :error (if errs-to-strs? (err-to-str rslt) rslt)}))))

