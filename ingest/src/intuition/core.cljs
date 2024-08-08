(ns intuition.core
  (:require ["node:util" :as node.util]
            [intuition.adapter :as adapter]
            [intuition.components.config :refer [new-config]]
            [intuition.components.db :refer [halt-db new-db]]
            [promesa.core :as p]))

(def ^:private options
  {:db-path {:type "string"}
   :task    {:type "string"}})

(def system-atom (atom nil))

(defn- build-system-map
  [extra-config]
  (p/let [env-vars [:JENKINS_URL :DB_PATH]
          config   (new-config extra-config env-vars)
          db       (new-db config)]
    {:config config :db db}))

(defn start-system!
  [extra-config]
  (p/->> (-> (build-system-map extra-config)
             (p/catch js/console.error))
         (reset! system-atom)))

(defn stop-system! []
  (some-> @system-atom
          halt-db))

(defn get-args-config []
  (-> (clj->js {:options options :args (drop 2 js/process.argv)})
      node.util/parseArgs
      adapter/args->config))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (p/let [args-config (get-args-config)
          system      (start-system! args-config)]
    (prn system)
    (stop-system!)))

(comment 
  (deref system-atom)
  (start-system! {}))
