(ns intuition.controller 
  (:require [intuition.adapter :as adapter]
            [intuition.db.query :as query]
            [intuition.ports.git :as git]
            [intuition.ports.jenkins :as jenkins]
            [intuition.ports.jira :as jira]
            [intuition.support :refer [mkdir]]
            [promesa.core :as p]))

(defn upsert-jenkins-builds
  [{:keys [config db http]}]
  (p/->> (jenkins/get-builds http config)
         (map #(adapter/->build config %))
         (#(query/upsert-jenkins db %))))

(defn upsert-git-commits
  [{:keys [db config]}]
  (p/do (mkdir (:git/repository config))
        (p/->> (query/get-latest-commit db (:task/source config))
               (git/get-commits config)
               (adapter/->commit (:task/source config))
               (#(query/upsert-commits db %)))))

(defn upsert-git-tags
  [{:keys [db config]}]
  (p/->> (git/get-tags (:git/repository config))
         (adapter/->tag (:task/source config))
         (#(query/upsert-tags db %))))


(defn upsert-jira-issues
  [{:keys [db http config]}]
  (p/->> (jira/search http config)
         (map adapter/->issue)
         (#(query/upsert-issues db %))))
