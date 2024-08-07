(ns intuition.core
  (:require ["node:util" :as node.util]
            [intuition.adapter :as adapter]))

(def ^:private options 
  {:db-path {:type "string"} 
   :task    {:type "string"}})

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main []
  (-> (clj->js {:options options :args (drop 2 js/process.argv)})
      (node.util/parseArgs)
      (adapter/args->config)
      (prn)))
