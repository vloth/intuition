(ns aux.cli 
  (:require ["node:util" :as node.util]
            [clojure.string :as str]))

(defn make-cli-args
  [options raw-args]
  (-> (clj->js
        {:options (into {} (for [[k v] options] [k {:type v}]))
         :args    (if (str/blank? raw-args) [] (str/split raw-args #"\s+"))})
      (node.util/parseArgs)))
