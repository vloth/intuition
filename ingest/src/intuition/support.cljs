(ns intuition.support
  (:require [clojure.string :as str]
            [promesa.core :as p]))

(defn allseq
  [promises]
  (p/loop [remaining promises
           results   []]
    (if (empty? remaining)
      results
      (p/let [result (first remaining)]
        (p/recur (rest remaining) (conj results result))))))

(defn ->json [js-class-instance] (.assign js/Object #js {} js-class-instance))

(defn parse-int [s] (.parseInt js/Number s 10))

(defn base-64 [text] (.toString text "base64"))

(defn join
  [& paths]
  (-> (str/join "/" paths)
      (str/replace #"[\/]+" "/")
      (str/replace #"^(.+):\/+" "$1://")
      (str/replace #"\/(\?|&|#[^!])" "$1")))
