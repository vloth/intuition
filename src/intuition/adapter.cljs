(ns intuition.adapter
  (:require [clojure.set :refer [rename-keys]]
            [clojure.string :as str]
            [intuition.support :refer [parse-date parse-int]]))

(defn expand-config-keys
  [config]
  (mapcat (fn [[k v]] (map (fn [x] (keyword (str (name k) "-" (name x)))) v))
    config))

(defn- convert-to-slash [key]
  (let [key-name (name key)]
    (keyword (str/replace-first key-name "-" "/"))))

(defn- convert-keys
  [raw-config expanded-config-keys]
  (reduce (fn [acc k]
            (if-let [v (get raw-config k)]
              (assoc acc (convert-to-slash k) v)
              acc))
    {}
    expanded-config-keys))

(defn ->config
  [raw-config expanded-config-keys]
  (-> (convert-keys raw-config expanded-config-keys)
      (update :http/delay-429 parse-int)
      (update :jenkins/delay parse-int)
      (update :jira/delay parse-int)
      (update :bitbucket/filter-from parse-date) 
      (update :bitbucket/past-months parse-int)
      (update :bitbucket/delay parse-int)
      (update :bitbucket/activity-delay parse-int)
      (update :git/pull #(or (true? %) (= "true" %)))
      (rename-keys {:git/pull :git/pull?})))

(defn ->build
  [{:task/keys [source]} jenkins-build]
  {:source     source
   :id         (parse-int (:id jenkins-build))
   :duration   (:durationInMillis jenkins-build)
   :result     (:result jenkins-build)
   :start_time (parse-date (:startTime jenkins-build))
   :end_time   (parse-date (:endTime jenkins-build))
   :cause      (str/join " - " (map :shortDescription (:causes jenkins-build)))
   :commit     (:commitId jenkins-build)
   :steps      (map (fn [step]
                      {:id          (parse-int (:id step))
                       :start_time  (parse-date (:startTime step))
                       :name        (:displayName step)
                       :description (:displayDescription step)
                       :duration    (:durationInMillis step)
                       :output      (:output step)
                       :state       (:state step)
                       :result      (:result step)
                       :type        (:type step)})
                 (:steps jenkins-build))})

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
  (if-not (str/blank? simple-git-return)
    (->> simple-git-return
         (str/split-lines)
         (mapv #(str/split % #"\trefs/tags/"))
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

(defn- pullrequest-state-date
  [state activity]
  (some->> activity
           (filter #(= state (get-in % [:update :state])))
           first
           :update
           :date
           (js/Date.)))

(defn ->pull-request
  [repo-slug bitbucket-pr]
  {:repo_slug     repo-slug
   :id            (:id bitbucket-pr)
   :title         (:title bitbucket-pr)
   :description   (:description bitbucket-pr)
   :summary       (get-in bitbucket-pr [:summary :raw])
   :state         (:state bitbucket-pr)
   :author        (get-in bitbucket-pr [:author :display_name])
   :closed_by     (get-in bitbucket-pr [:closed_by :display_name])
   :commit        (get-in bitbucket-pr [:merge_commit :hash])
   :comment_count (:comment_count bitbucket-pr)
   :created       (js/Date. (:created_on bitbucket-pr))
   :updated       (some-> (:updated_on bitbucket-pr)
                          (js/Date.))
   :destination   (get-in bitbucket-pr [:destination :branch :name])
   :merged        (pullrequest-state-date "MERGED" (:activity bitbucket-pr))
   :opened        (pullrequest-state-date "OPEN" (:activity bitbucket-pr))})
