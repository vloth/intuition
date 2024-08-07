(ns intuition.adapter-test
  (:require ["node:util" :as node.util]
            [cljs.test :refer [deftest are]]
            [clojure.string :as str]
            [intuition.adapter :as adapter]))

(defn- make-cli-args
  [options raw-args]
  (-> (clj->js
        {:options (into {} (for [[k v] options] [k {:type v}]))
         :args    (if (str/blank? raw-args) [] (str/split raw-args #"\s+"))})
      (node.util/parseArgs)))

(deftest transform-args->config
  (are [x y]
       (= x (adapter/args->config (make-cli-args {:foo :boolean :bar :string} y)))
    {:foo true :bar "123"} "--foo --bar=123"
    {:bar "123"} "--bar=123"
    {} ""))
