(ns intuition.controller
  (:require [intuition.adapter :as adapter]
            [intuition.db.query :as query]
            [intuition.ports.bitbucket :as bitbucket]
            [intuition.ports.git :as git]
            [intuition.ports.github :as github]
            [intuition.ports.jenkins :as jenkins]
            [intuition.ports.jira :as jira]
            [intuition.support :refer [add-time mkdir]]
            [promesa.core :as p]))

(defn upsert-github-issues
  [{:keys [config db http]}]
  (p/->> (github/get-issues http config)
         (map #(adapter/->gh-issue config %))
         (#(query/upsert-github-issues db %))))

(defn upsert-jenkins-builds
  [{:keys [config db http]}]
  (p/->> (query/get-latest-build db (:task/source config))
         (jenkins/get-builds http config)
         (map #(adapter/->build config %))
         (#(query/upsert-jenkins db %))))

(defn upsert-git-tags
  [{:keys [db config]}]
  (p/->> (git/get-tags (:git/repository config))
         (adapter/->tag (:task/source config))
         (#(query/upsert-tags db %))))

(defn upsert-git-commits
  [{:keys [db config]}]
  (p/do (mkdir (:git/repository config))
        (p/->> (query/get-latest-commit db (:task/source config))
               (git/get-commits config)
               (adapter/->commit (:task/source config))
               (#(query/upsert-commits db %)))))

(defn upsert-jira-issues
  [{:keys [db http config]}]
  (p/->> (jira/search http config)
         (map adapter/->issue)
         (#(query/upsert-issues db %))))

(defn upsert-bitbucket-pullrequests
  [{:keys [db http config]}]
  (p/->> (or (:bitbucket/filter-from config)
             (some->> (:bitbucket/past-months config)
                      (add-time :month (js/Date.))))
         (bitbucket/get-pull-requests http config)
         (map #(adapter/->pull-request (:bitbucket/repo-slug config) %))
         (query/upsert-pullrequests db)))
