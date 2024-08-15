(ns intuition.core
  (:require [intuition.adapter :as adapter]
            [intuition.components.config :refer [new-config]]
            [intuition.components.db :refer [exec halt-db new-db]]
            [intuition.components.http :refer [new-http]]
            [intuition.controller :as controller]
            [intuition.db.schema :refer [schema-def]]
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
  [config-source]
  (p/let [system (p/catch (build-system-map config-source) js/console.error)]
    (exec (:db system) schema-def)
    (reset! system-atom system)))

(defn stop-system! []
  (when @system-atom
    (halt-db (:db @system-atom))
    (reset! system-atom nil)))

(defn run-task
  [system]
  (case (get-in system [:config :task/type])
    "jenkins" (controller/upsert-jenkins-builds system)
    "git"     (p/do (controller/upsert-git-commits system)
                    (controller/upsert-git-tags system))))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (p/let [config-source
          {:env/data    (.-env js/process)
           :cli/args    (drop 2 js/process.argv)
           :cli/options options}
          system        (start-system! config-source)]
    (prn system)
    (stop-system!)))

(comment
  (deref system-atom)
  (stop-system!)

  ;; start system
  (js/await
   (start-system! {:env/data    (.-env js/process)
                   :cli/args    (drop 2 js/process.argv)
                   :cli/options options}))

  (js/await
   (p/do (controller/upsert-git-commits @system-atom)
         (controller/upsert-git-tags @system-atom))))

