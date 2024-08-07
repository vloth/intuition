(ns core)

(defn sum [a b] (+ a b))

#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn main [] (prn :hi))
