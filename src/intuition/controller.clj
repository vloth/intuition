(ns intuition.controller
  (:require
   [intuition.adapter :as adapter]
   [intuition.data-source.git :as data-source.git]))

(defn get-commits
  [{:keys [repository folder force-pull git-remote since]}]
  (let [repo (or (data-source.git/clone-repo folder git-remote)
                 (data-source.git/load-repo folder :pull force-pull))]
    (mapv (partial adapter/->commit repository)
          (data-source.git/git-log repo since))))
