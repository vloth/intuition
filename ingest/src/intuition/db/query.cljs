(ns intuition.db.query
  (:require [intuition.components.db :as db]
            [promesa.core :as p]))

(defn upsert-jenkins
  [db builds]
  (prn (count builds))
  (p/do (db/delete db "jenkins" [:source :id] builds)
        (db/insert db
                   "jenkins"
                   [:source :id :duration :result :commits]
                   {:on-conflict :ignore}
                   builds)))
