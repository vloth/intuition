(ns test-support
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

(defmacro with-redef-async
  "Temporarily redefines vars to new values for the duration of the asynchronous body.
  Restores the original values after the body completes.

  Usage:
  (with-redef-async [var1 new-val1
                     var2 new-val2]
    ~@body)

  bindings - A vector of var and new-val pairs.
  body - The asynchronous body to execute with the redefined vars."
  [bindings & body]
  (let [pairs (partition 2 bindings)
        old-vals (map (fn [[var _]] (symbol (str (name var) "-old"))) pairs)
        set-news (map (fn [[var new-val]] `(set! ~var ~new-val)) pairs)
        restores (map (fn [[var _ old-val]] `(set! ~var ~old-val)) (map vector (map first pairs) old-vals))]
    `(let [~@(mapcat (fn [[var old-val]] [old-val var]) (map vector (map first pairs) old-vals))]
       ~@set-news
       (p/let [result# (do ~@body)]
         ~@restores
         result#))))

(defn new-id
  "Generates a random base62 ID string.
  
  The ID is composed of characters from the set [0-9a-zA-Z], and is generated
  by converting a random integer to a base62 representation.
  
  Returns:
  A string representing the base62 ID. If the generated ID is empty, returns \"0\"."
  []
  (let [base62-chars
          "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        base 62
        random-number (rand-int (Math/pow 62 8))
        digits (take-while #(> % 0) (iterate #(quot % base) random-number))
        result (apply str (map #(nth base62-chars (mod % base)) digits))]
    (if (empty? result) "0" result)))
