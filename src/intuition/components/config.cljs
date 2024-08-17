(ns intuition.components.config
  (:require ["node:util" :as node.util]
            [clojure.string :as str]
            [intuition.support :refer [->json]]))

(defn- format-env-name
  [env-name]
  (-> (name env-name)
      str/lower-case
      (str/replace "_" "-")
      keyword))

(defn- ->env
  [env]
  (->> (js->clj (->json env) :keywordize-keys true)
       (#(for [[k v] %] [(format-env-name k) v]))
       (into {})))

(defn- parse-args
  [{:cli/keys [options args]}]
  (node.util/parseArgs (clj->js {:options options :args args})))

(defn ->cli-args
  [parsed-args]
  (-> parsed-args
      .-values
      ->json
      (js->clj :keywordize-keys true)))

(defn new-config
  ([] (new-config {:env/data {}}))
  ([source]
   (let [env-data (->env (:env/data source))
         cli-data (->cli-args (parse-args source))]
     (merge env-data cli-data))))
