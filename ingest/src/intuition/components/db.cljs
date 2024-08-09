(ns intuition.components.db
  (:require ["duckdb$default" :as duckdb]
            ["util" :refer [promisify]]
            [promesa.core :as p]))

(defn new-db
  [{:db/keys [path]}]
  (let [db   (duckdb/Database. path)
        conn (.connect db)]
    {:db   db
     :conn conn
     :exec (-> (.-all conn)
               (promisify)
               (.bind conn))}))

(defn halt-db
  [{:keys [db conn]}]
  (p/do
    (.close conn)
    (.close db)))
