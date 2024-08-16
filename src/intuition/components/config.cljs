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

(defn ->cli-args
  [cli-options args]
  (-> (clj->js {:options cli-options :args args})
      node.util/parseArgs
      .-values
      ->json
      (js->clj :keywordize-keys true)))

(defn new-config
  ([]
   (new-config {:env/data {}}))
  ([source]
   (merge (->env (:env/data source))
          (->cli-args (:cli/options source) (:cli/args source)))))
