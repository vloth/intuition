(ns support.db 
  (:require [cljs.test :refer [is]]
            [promesa.core :as p]
            [support.system :as s]))

(defn assert= 
  [query expected]
  (p/let [db-state (s/query-db query)]
    (is (= expected db-state))))
