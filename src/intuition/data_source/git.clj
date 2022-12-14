(ns intuition.data-source.git
  (:require [clj-jgit.internal :as gi]
            [clj-jgit.porcelain :as g]
            [clj-jgit.querying :as gq]
            [clojure.java.io :as io])
  (:import [org.eclipse.jgit.revwalk.filter RevFilter]))

(defn load-repo
  [location & {:keys [pull]}]
  (when (.exists (io/file location))
    (let [repo (g/load-repo location)]
      (when pull (g/git-pull repo))
      repo)))

(defn clone-repo
  [path & {:keys [address branch]}]
  (when-not (.exists (io/file path))
    (g/git-clone address
                 :dir path
                 :branch (or branch "master"))))

(defn git-log
  [repo since]
  (let [walker (gi/new-rev-walk repo)]
    (map #(gq/commit-info-without-branches repo walker (:id %))
         (g/git-log repo {:rev-filter RevFilter/NO_MERGES :since since}))))
