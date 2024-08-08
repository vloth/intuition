(ns intuition.components.config 
  (:require [clojure.string :as str]
            [intuition.adapter :refer [->plain-js]]))

(defn format-env-keys
  [env]
  (into {}
        (for [[k v] env]
          [(-> k
               name
               str/lower-case
               (str/replace "_" "-")
               keyword) v])))

(defn new-config
  [extra-inputs filter-keys]
  (-> (.-env js/process)
      ->plain-js
      (js->clj :keywordize-keys true)
      (select-keys filter-keys)
      format-env-keys
      (merge extra-inputs)))
