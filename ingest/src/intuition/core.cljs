(ns intuition.core
  (:require [intuition.adapter :as adapter]
            [intuition.components.config :refer [new-config]]
            [intuition.components.db :refer [halt-db new-db]]
            [intuition.components.http :refer [new-http]]
            [intuition.controller :as controller]
            [promesa.core :as p]))

(def ^:private options
  (->> [:db-path :task-type :job-path]
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
  (p/->> (-> (build-system-map config-source)
             (p/catch js/console.error))
         (reset! system-atom)))

(defn stop-system! []
  (some-> @system-atom halt-db))

(defn run-task
  [config]
  (case (:task config)
    "jenkins" (controller/upsert-jenkins-builds config)))

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
  (js/await 
    (start-system! {:env/data    (.-env js/process)
                    :cli/args    (drop 2 js/process.argv)
                    :cli/options options})))
