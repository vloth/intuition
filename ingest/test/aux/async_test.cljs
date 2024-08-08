(ns aux.async-test
  (:require [cljs.test :refer-macros [async is]]
            [promesa.core :as p]))

(defmacro deftest-async
  "Define an asyncronous (promise based) test."
  [name & body]
  `(cljs.test/deftest ~name
     (async done
            (-> (do ~@body)
                (p/catch 
                  (fn [error#]
                    (is false
                        (str "Promise rejected unexpectedly: " error#))))
                (p/finally done)))))
