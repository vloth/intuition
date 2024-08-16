(ns intuition.adapter 
  (:require [clojure.string :as s]
            [intuition.support :refer [parse-int]]))

(defn ->config
  [c]
  {:db/path          (:db-path c)
   :task/type        (:task-type c)
   :task/source      (:task-source c)
   :http/delay-429   (parse-int (:delay-429 c))
   :jenkins/url      (:jenkins-url c)
   :jenkins/username (:jenkins-username c)
   :jenkins/password (:jenkins-password c)
   :jenkins/job-path (:jenkins-job-path c)
   :git/repository   (:git-repository c)
   :git/branch       (:git-branch c)
   :git/remote       (:git-remote c)
   ;; fix pull can be a boolean
   :git/pull?        (= "true" (:git-pull c))
   :jira/url         (:jira-url c)
   :jira/username    (:jira-username c)
   :jira/password    (:jira-password c)
   :jira/jql         (:jira-jql c)})
   

(defn ->build
  [{:task/keys [source]} jenkins-build]
  {:source   source
   :id       (:number jenkins-build)
   :duration (:duration jenkins-build)
   :result   (:result jenkins-build)
   :commits  (->> jenkins-build
                  :changeSets
                  (map :items)
                  (flatten)
                  (map :commitId))})

(defn ->commit
  [source simple-git-return]
  (->> (js->clj simple-git-return :keywordize-keys true)
       :all
       (map #(assoc %
               :source        source
               :date          (js/Date. (:date %))
               :changed_files (-> (js/Object.assign #js {} (:diff %))
                                  (js->clj :keywordize-keys true)
                                  (update :files (fn [f] (mapv :file f)))
                                  :files)))))

(defn ->tag
  [source simple-git-return]
  (if-not (s/blank? simple-git-return)
    (->> simple-git-return
         (s/split-lines)
         (mapv #(s/split % #"\trefs/tags/"))
         (mapv (fn [[hash tag]] {:hash hash :tag tag :source source})))
    []))


(defn- status-change? [{:keys [field]}] (= "status" field))

(defn- ->issue-history-transition
  [{:keys [author created items]}]
  (map (fn [item]
         {:name        (:displayName author)
          :email       (:emailAddress author)
          :created     (js/Date. created)
          :from_status (:fromString item)
          :to_status   (:toString item)})
       (filter status-change? items)))

(defn- ->issue-history
  [jticket]
  (->> jticket
       :changelog
       :histories
       (filter #(some status-change? (:items %)))
       (mapcat ->issue-history-transition)
       (sort-by :created)))

(defn ->issue
  [jira-issue]
  {:key                 (:key jira-issue)
   :summary             (get-in jira-issue [:fields :summary])
   :created             (js/Date. (get-in jira-issue [:fields :created]))
   :updated             (some-> (get-in jira-issue [:fields :updated])
                                (js/Date.))
   :duedate             (some-> (get-in jira-issue [:fields :duedate])
                                (js/Date.))
   :labels              (not-empty (get-in jira-issue [:fields :labels]))
   :type                (get-in jira-issue [:fields :issuetype :name])
   :priority            (get-in jira-issue [:fields :priority :name])
   :project             (get-in jira-issue [:fields :project :name])
   :assignee_name       (get-in jira-issue [:fields :assignee :displayName])
   :assignee_email      (get-in jira-issue [:fields :assignee :emailAddress])
   :reporter_name       (get-in jira-issue [:fields :reporter :displayName])
   :reporter_email      (get-in jira-issue [:fields :reporter :emailAddress])
   :resolution          (get-in jira-issue [:fields :resolution :name])
   :resolution_datetime (some-> (get-in jira-issue [:fields :resolutiondate])
                                (js/Date.))
   :status              (get-in jira-issue [:fields :status :name])
   :status_category     (get-in jira-issue
                                [:fields :status :statusCategory :name])
   :status_category_changed (some-> (get-in jira-issue
                                            [:fields :statuscategorychangedate])
                                    (js/Date.))
   :history             (->issue-history jira-issue)})
