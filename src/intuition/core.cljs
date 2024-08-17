(ns intuition.core
  (:require [intuition.adapter :as adapter]
            [intuition.components.config :refer [new-config]]
            [intuition.components.db :refer [halt-db new-db]]
            [intuition.components.http :refer [new-http]]
            [intuition.controller :as controller]
            [intuition.db.schema :refer [sync-schema]]
            [promesa.core :as p]))

(def ^:private options
  (->> [:db-path :task-type :task-source :jenkins-job-path :git-repository
        :git-branch :git-remote :git-pull]
       (map (fn [k] [k {:type "string"}]))
       (into {})))

(defonce system-atom (atom nil))

(defn- build-system-map
  [source]
  (p/let [config (adapter/->config (new-config source))
          http   (new-http config)
          db     (new-db config)]
    {:config config :db db :http http}))

(defn start-system!
  ([]
   (start-system!
    {:env/data    (.-env js/process)
     :cli/args    (drop 2 js/process.argv)
     :cli/options options}))
  ([source]
   (p/let [system (build-system-map source)]
     (sync-schema (:db system))
     (reset! system-atom system))))

(defn stop-system! []
  (when @system-atom
    (halt-db (:db @system-atom))
    (reset! system-atom nil)))

(defn run-task
  [system]
  (case (get-in system [:config :task/type])
    "jenkins"   (controller/upsert-jenkins-builds system)
    "bitbucket" (controller/upsert-bitbucket-pullrequests system)
    "jira"      (controller/upsert-jira-issues system)
    "git"       (p/do (controller/upsert-git-commits system)
                      (controller/upsert-git-tags system))
    nil))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (p/let [system (-> (start-system!)
                     (p/catch (fn [e]
                                (js/console.error (ex-message e))
                                (js/process.exit 1))))]
    (p/do (run-task system)
          (stop-system!))))

(comment
  ;; stop system
  (js/await (stop-system!))

  ;; start system
  (js/await
   (p/catch (start-system!)
            js/console.error))

  ;; run task
  (js/await
   (-> @system-atom
       (assoc-in [:config :task/type]   "")
       (assoc-in [:config :task/source] "")
       run-task)))

