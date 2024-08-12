(ns intuition.controller 
  (:require [intuition.adapter :as adapter]
            [intuition.db.query :as query]
            [intuition.ports.jenkins :as jenkins]
            [promesa.core :as p]))

(defn upsert-jenkins-builds
  [{:keys [config db http]}]
  (p/->> (jenkins/get-builds http config)
         (map #(adapter/->build config %))
         (#(query/upsert-jenkins db %))))
