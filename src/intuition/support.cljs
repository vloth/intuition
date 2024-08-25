(ns intuition.support
  (:require ["node:fs/promises" :as nfs]
            [clojure.string :as str]
            [promesa.core :as p]))

(defn allseq
  "Takes a collection of promises and returns a promise that resolves to a 
  collection of results, maintaining the order of the original promises."
  [promises]
  (p/loop [remaining promises
           results   []]
    (if (empty? remaining)
      results
      (p/let [result (first remaining)]
        (p/recur (rest remaining) (conj results result))))))

(defn ->json
  "Converts a JavaScript class instance into a plain JavaScript object."
  [js-class-instance]
  (.assign js/Object #js {} js-class-instance))

(defn parse-date
  "Parses the given string into a date."
  [date]
  (when (not-empty date) (js/Date. date)))

(defn parse-int
  "Parses the given string into an integer."
  [number]
  (when (not-empty number)
    (.parseInt js/Number number 10)))

(defn base-64
  "Encodes a buffer into the Base64 string representation."
  [buffer]
  (.toString buffer "base64"))

(defn join
  "Joins path segments into a single normalized path string."
  [& paths]
  (-> (str/join "/" paths)
      (str/replace #"[\/]+" "/")
      (str/replace #"^(.+):\/+" "$1://")
      (str/replace #"\/(\?|&|#[^!])" "$1")))

(defn mkdir
  "Creates a directory at the specified folder path. If the directory already exists, 
  it does nothing. The operation is recursive, meaning it will create any necessary 
  parent directories as well."
  [folder-path]
  (nfs/mkdir folder-path #js {:recursive true}))

(defn add-time
  "Adds a specified amount of time to a given date. The time unit is specified by a keyword (:date, :month, :year)."
  [unit initial-date amount]
  (let [date (js/Date. initial-date)]
    (case unit
      :date  (.setDate date (+ (.getDate date) amount))
      :month (.setMonth date (+ (.getMonth date) amount))
      :year  (.setFullYear date (+ (.getFullYear date) amount))
      (throw (ex-info "Invalid time unit" {:unit unit})))
    date))
