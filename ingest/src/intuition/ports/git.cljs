(ns intuition.ports.git
  (:require ["simple-git" :as git]
            [promesa.core :as p]))

(defn- is-repo?
  [repo & {:keys [root?] :or {root? true}}]
  (.then (.checkIsRepo repo root?) identity))

(defn- init!
  [repo & {:keys [branch] :or {branch "main"}}]
  (.then (.init repo #js {:--initial-branch branch}) identity))

(defn- add-remote!
  [repo origin remote]
  (.then (.addRemote repo origin remote) identity))

(defn- pull!
  [repo origin remote]
  (.then (.pull repo origin remote) identity))

(defn- log
  [repo opts]
  (.then (.log repo (clj->js opts)) identity))

(defn- list-remote
  [repo opts]
  (.then (.listRemote repo (clj->js opts)) identity))

(defn get-commits
  [{:git/keys [branch repository remote pull?]} from-commit]
  (p/let [repo  (.simpleGit git repository)
          repo? (is-repo? repo)]
    (p/do (when-not repo?
            (p/do (init! repo :branch branch)
                  (add-remote! repo "origin" remote)))
          (when pull? (pull! repo "origin" branch))
          (log repo
               (->> [[:--name-only nil] (when from-commit [:from from-commit])]
                    (filter (comp not nil?))
                    (into {}))))))

(defn get-tags
  [repository]
  (list-remote (.simpleGit git repository)
               [:--tags :--refs]))
