(ns intuition.db.query
  (:require ["util" :refer [format]]
            [intuition.components.db :as db]
            [promesa.core :as p]))

(defn upsert-jenkins
  [db builds]
  (when (seq builds)
    (p/do (db/delete db "jenkins" [:source :id] builds)
          (db/insert db
                     "jenkins"
                     [:source :id :duration :result :start_time :end_time :cause :commit :steps]
                     {:on-conflict :ignore}
                     builds))))

(defn get-latest-build
  [db source]
  (p/->
    (db/exec
      db
      (format
        "SELECT id 
         FROM jenkins 
         WHERE source = '%s'AND result != 'UNKNOWN'
         ORDER BY id DESC LIMIT 1"
        source))
    (first)
    (some-> (.-id))))

(defn get-latest-commit
  [db source]
  (p/->
   (db/exec
    db
    (format
     "SELECT hash 
         FROM git.commit 
         WHERE source = '%s' 
         ORDER BY date DESC
         LIMIT 1"
     source))
   (first)
   (some-> (.-hash))))

(defn upsert-commits
  [db commits]
  (db/insert db
             "git.commit"
             [:hash :author_name :author_email :source :date :email :message
              :body :changed_files]
             {:on-conflict :ignore}
             commits))

(defn upsert-tags
  [db tags]
  (db/insert db "git.tag" [:tag :hash :source] {:on-conflict :ignore} tags))

(defn upsert-issues
  [db builds]
  (p/do (db/delete db "jira" [:key] builds)
        (db/insert db
                   "jira"
                   [:key :summary :created :updated :duedate :labels :type
                    :priority :project :assignee_name :assignee_email
                    :reporter_name :reporter_email :resolution
                    :resolution_datetime :status :status_category
                    :status_category_changed :history]
                   {:on-conflict :ignore}
                   builds)))

(defn upsert-pullrequests
  [db pullrequests]
  (db/insert db
             "bitbucket"
             [:repo_slug :id :title :description :summary :state :author
              :closed_by :commit :comment_count :created :updated :destination
              :merged :opened]
             {:on-conflict :replace}
             pullrequests))
