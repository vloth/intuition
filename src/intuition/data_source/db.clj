(ns intuition.data-source.db
  (:require [datalevin.core :as dl]))

(def schema
  {:db/ident             {:db/unique :db.unique/identity 
                          :db/valueType :db.type/keyword}

   :commit/id            {:db/unique :db.unique/identity
                          :db/valueType :db.type/string}
   :commit/changed-files {:db/cardinality :db.cardinality/many}
   :commit/repository    {:db/valueType :db.type/keyword}
   :commit/type          {:db/valueType :db.type/keyword}
   :commit/time          {:db/valueType :db.type/instant}
   :commit/message       {:db/valueType :db.type/string}
   :author/name          {:db/valueType :db.type/string}
   :author/email         {:db/valueType :db.type/string}})

(def conn (atom nil))

(defn start! []
  (->> (dl/get-conn "resources/db" schema)
       (reset! conn)))

(defn stop! []
  (when @conn
    (dl/close @conn)
    (reset! conn nil)))

(defn destroy! []
  (when @conn
    (dl/clear @conn)
    (reset! conn nil)))
