(ns intuition.components.db
  (:require ["duckdb$default" :as duckdb]
            [promesa.core :as p]))

(defn new-db
  [{:keys [db-path]}]
  (let [db (duckdb/Database. db-path)] 
    {:db db :conn (.connect db)}))

(defn halt! 
  [{:keys [db conn]}]
  (p/do
    (.close conn)
    (.close db)))
