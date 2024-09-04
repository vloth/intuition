(ns aux.db 
  (:require [aux.system :as s]
            [cljs.test :refer [is]]
            [promesa.core :as p]))

(defn assert= 
  [query expected]
  (p/let [db-state (s/query-db query)]
    (is (= expected db-state))))
