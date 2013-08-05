(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [domina.events :as events]
            [degel.utils.html :as dhtml]
            [degel.webol.store :as store]
            [degel.webol.line-parser :as parser]
            [degel.webol.screen :as screen]
            [degel.webol.page :as page]))


(def memory-per-row 10)
(def memory-num-rows 10)

(defn debug [x]
   (js* "debugger;")
   x)

(defn clear-all []
  (doseq [n (range (* memory-per-row memory-num-rows))]
    (store/put! [:memory n] 0))
  (store/put! [:register :pc] 0)
  (store/put! [:register :sp] nil))

(defn register-name [index]
  (case index
    0 "Program counter"
    1 "Stack pointer"
    "Unknown"))

(defn register-key [index]
  (case index
    0 [:register :pc]
    1 [:register :sp]
    [:error]))

(defn- or-empty [s]
  (str (or s "(empty)")))

(defn ^:export init []
  (clear-all)
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory")
    (dhtml/table memory-num-rows memory-per-row
                 :cell-fn #(let [loc (+ (* %1 memory-per-row) %2)]
                             (page/location-and-value
                              loc
                              (-> [:memory loc] store/fetch or-empty)))))
  (dom/set-html! (dom/by-id "registers")
    (dhtml/table 1 2
                 :cell-fn #(page/location-and-value
                            (register-name %2)
                            (->  (register-key %2) store/fetch or-empty))))
  (screen/text-mode)
  (store/alert! [:input :line] :input-line
                (fn [_ _ _ line]
                  (let [line-map (parser/parse line)]
                    (screen/text-out line-map)
                    (screen/newline-out))))
  (events/listen! (dom/by-id "input") :keyup
    #(when (= 13 (-> % events/raw-event .-keyCode))
       (let [control (-> % events/target)]
         (store/put! [:input :line] (-> control dom/value))
         (dom/set-value! control "")))))
