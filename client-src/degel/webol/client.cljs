(ns degel.webol.client
  (:require [domina :as dom :refer [log]]
            [redmapel :as rml]
            [degel.utils.html :as dhtml]
            [degel.webol.page :as page]))


(def ^:export webol-tree (rml/make-redmapel-tree))

(defn fetch [key]
  (rml/fetch webol-tree key))

(defn put! [key value]
  (rml/put! webol-tree key value)
  value)

(defn update! [key f & args]
  (apply rml/update! webol-tree key f args)
  (fetch key))

(defn alert! [key id f]
  (rml/alert! webol-tree key id f))

(def memory-per-row 10)
(def memory-num-rows 10)


(defn clear-all []
  (doseq [n (range (* memory-per-row memory-num-rows))]
    (rml/put! webol-tree [:memory n] 0))
  (rml/put! webol-tree [:register :pc] 0)
  (rml/put! webol-tree [:register :sp] nil))

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

(defn or-empty [s]
  (str (or s "(empty)")))

(defn string-into [s n s1]
  (str (subs s 0 n) s1 (subs s (+ n (count s1)))))


(defn on-screen [fcn]
  (let [canvas (dom/by-id "sketchboard")
        context    (.getContext canvas "2d")
        width  (.-width canvas)
        height (.-height canvas)]
    (fcn {:canvas canvas :context context :width width :height height})))

(defn text-mode []
  (on-screen (fn [{:keys [context width height]}]
               (let [char-height 14]
                 (set! (.-textBaseline context) "top")
                 (set! (.-font context) (str char-height "px Monospace"))
                 (let [char-width (-> context (.measureText "M") .-width)
                       width-in-chars (fix (/ width char-width))
                       height-in-lines (fix (/ height char-height))]
                   (put! [:screen :mode] :text)
                   (put! [:screen :line-height] char-height)
                   (put! [:screen :char-width] char-width)
                   (put! [:screen :width-in-chars] width-in-chars)
                   (put! [:screen :height-in-lines] height-in-lines)
                   (put! [:screen :text-x] 0)
                   (put! [:screen :text-y] 0)
                   (alert! [:screen :line] :text-draw-line
                           (fn [_ [_ _ line] _ text]
                             (set! (.-fillStyle context) "BurlyWood")
                             (.fillRect context 0 (* line char-height) width char-height)
                             (set! (.-fillStyle context) "DarkBlue")
                             (.fillText context text 0 (* line char-height))))
                   (doseq [n (range height-in-lines)]
                       (put! [:screen :line n]
                           (clojure.string/join (repeat width-in-chars " ")))))))))


(defn text-pixel-pos
  ([]
     (text-pixel-pos (fetch [:screen :text-x]) (fetch [:screen :text-y])))
  ([text-x text-y]
     [(* text-x (fetch [:screen :char-width]))
      (* text-y (fetch [:screen :line-height]))]))


(defn text-scroll
  ([]
     (text-scroll 1))
  ([num-lines]
     (when (= (fetch [:screen :mode]) :text)
       (let [height-in-lines (fetch [:screen :height-in-lines])]
         (doseq [n (rest (range height-in-lines))]
           (put! [:screen :line (dec n)]
                 (fetch [:screen :line n])))))))


(defn text-out [text]
  (when (= :text (fetch [:screen :mode]))
    (let [start-x (fetch [:screen :text-x])
          start-y (fetch [:screen :text-y])]
      (update! [:screen :text-x] + (count text))
      (update! [:screen :line start-y] string-into start-x text))))


(defn newline-out []
  (when (= (fetch [:screen :mode]) :text)
    (put! [:screen :text-x] 0)
    (let [new-y (update! [:screen :text-y] inc)]
      (when (>= new-y (fetch [:screen :height-in-lines]))
        (text-scroll 1)
        (update! [:screen :text-y] dec)))))


(defn ^:export init []
  (clear-all)
  (dom/set-html! (dom/by-id "page") (page/webol-page))
  (dom/set-html! (dom/by-id "memory")
    (dhtml/table memory-num-rows memory-per-row
                 :cell-fn #(let [loc (+ (* %1 memory-per-row) %2)]
                             (page/location-and-value
                              loc
                              (-> [:memory loc] fetch or-empty)))))
  (dom/set-html! (dom/by-id "registers")
    (dhtml/table 1 2
                 :cell-fn #(page/location-and-value
                            (register-name %2)
                            (->  (register-key %2) fetch or-empty))))
  (text-mode)
  (text-out "abc")
  (doseq [n (range (fix (rand 30)))]
    (text-out (str "line " n))
    (text-out (str " foo " (rand)))
    (newline-out)))
