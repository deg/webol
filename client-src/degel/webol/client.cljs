(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [domina.events :as events]
            [degel.utils.html :as dhtml]
            [shoreleave.remotes.http-rpc :refer [remote-callback]]
            [clojure.browser.repl]
            [degel.utils.storage :refer [read write-local]]
            [degel.webol.runtime :as rt]
            [degel.webol.screen :as screen]
            [degel.webol.store :as store]
            [degel.webol.page :as page]))


(def memory-per-row 10)
(def memory-num-rows 10)

(defn debug [x]
   (js* "debugger;")
   x)

(defn clear-all []
  (rt/clear-program)
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

(defn- draw-registers []
  (dom/set-html! (dom/by-id "registers")
    (dhtml/table 1 2
                 :cell-fn #(page/location-and-value
                            (register-name %2)
                            (->  (register-key %2) store/fetch or-empty)))))

(defn ^:export init []
  (clear-all)
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory")
    (dhtml/table memory-num-rows memory-per-row
                 :cell-fn #(let [loc (+ (* %1 memory-per-row) %2)]
                             (page/location-and-value
                              loc
                              (-> [:memory loc] store/fetch or-empty)))))
  (draw-registers)
  (screen/text-mode)
  (store/alert! [:register] :draw-pc (fn [_ _ _ _] (draw-registers)))
  (store/guard! [:input :line] :input-line
                ;; This somewhat abuses the guard metaphor. I'm using it here as a
                ;; one-shot: We don't need or want to store anything in [:input :line];
                ;; we just want to play with triggering. (To be honest: this has
                ;; zero advantage over simply skipping this whole trampoline, but
                ;; I hope the ideas will lead to enhancing redmapel).
                (fn [_ _ _ line]
                  (screen/line-out (str "=> " line) {:color "DarkGreen"})
                  (remote-callback :get-parse-tree [line]
                    (fn [{:keys [status parse error]}]
                      (condp = status
                        :error (screen/line-out (str "ERROR: " error) {:color "DarkRed"})
                        :success (rt/interpret parse))))
                  false))
  (rt/set-program (read "program"))
  (events/listen! (dom/by-id :save-program) :click
    #(write-local "program" (rt/get-program)))
  (events/listen! (dom/by-id "input") :keyup
    #(when (= 13 (-> % events/raw-event .-keyCode))
       (let [control (-> % events/target)]
         (store/put! [:input :line] (-> control dom/value))
         (dom/set-value! control "")))))
