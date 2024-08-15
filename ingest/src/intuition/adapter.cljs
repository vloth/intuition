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
   :git/pull?        (= "true" (:git-pull c))})

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
