(ns intuition.log
  (:require ["util" :refer [format]]
            [clojure.string :as str]))

(def bold-style "\u001b[1m")
(def blue-color "\u001b[34m")
(def red-color "\u001b[31m")

(defn rand-color
  []
  (let [r (rand-int 256)
        g (rand-int 256)
        b (rand-int 256)]
    (format "\u001b[38;2;%d;%d;%dm" r g b)))

(defn id-color
  [text]
  (let [hash (hash text)
        r    (mod (+ hash 31) 256)
        g    (mod (+ hash 63) 256)
        b    (mod (+ hash 127) 256)]
    (format "\u001b[38;2;%d;%d;%dm" r g b)))

(defn- add-color
  [m [k v]]
  (str/replace m (str k) (str (if (fn? v) (v k) v) k "\u001b[0m")))

(defn format-message
  [msg args]
  (reduce (fn [m [i arg]] (str/replace m (str "%" (inc i)) arg))
    msg
    (map-indexed vector args)))

(defn log
  [{:keys [msg args color]}]
  (-> (format-message msg (clj->js args))
      (#(reduce add-color % color))
      js/console.log))
