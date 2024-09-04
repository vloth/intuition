(ns integration.aux.system
  (:require [cljs.test :refer [async]]
            [intuition.components.db :refer [exec halt-db new-db]]
            [intuition.components.http :refer [new-mock-http]]
            [intuition.db.schema :refer [sync-schema]]
            [intuition.support :refer [allseq]]
            [promesa.core :as p]))

(def system-atom (atom {}))

(defn start-system
  [config]
  (fn []
    (async done
           (let [http-mocks (atom {})]
             (p/do
               (reset! system-atom
                       {:config     config
                        :db         (new-db config)
                        :http       (new-mock-http http-mocks)
                        :http-mocks http-mocks})
               (sync-schema (:db @system-atom)))
             (done)))))

(defn mock-http
  [mocks]
  (swap! (:http-mocks @system-atom) merge mocks))

(defn query-db
  [query]
  (p/let [result (exec (:db @system-atom) query)]
    (js->clj result :keywordize-keys true)))

(defn call [f & args]
  (apply f (conj args @system-atom)))

(defn halt-system []
  (async done
         (p/do (halt-db (:db @system-atom))
               (reset! system-atom {})
               (done))))

(defn reset-system []
  (async done
         (p/do (reset! (:http-mocks @system-atom) {})
               (p/->> ["bitbucket" "jenkins" "jira" "git.commit" "git.tag"]
                      (map #(str "TRUNCATE " %))
                      (map query-db)
                      allseq)
               (done))))
