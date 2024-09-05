(ns integration.intuition.controller.git-test
  (:require [cljs.test :refer [use-fixtures]]
            [intuition.controller :as controller]
            [intuition.ports.git :as git]
            [intuition.support :refer [mkdir]]
            [promesa.core :as p]
            [support.db :as aux.db]
            [support.system :as s]
            [support.test :refer [deftest-async with-redef-async]]))

(defn generate-git-tags
  [& versions]
  (apply str (map #(str "hash" % "\trefs/tags/v" % "\n") versions)))

(def config
  {:db/path        ":memory:"
   :task/source    "example"
   :git/pull?      true
   :git/branch     "main"
   :git/remote     "git+ssh://example.com"
   :git/repository "example"})

(use-fixtures :once {:before (s/start-system config) :after s/halt-system})
(use-fixtures :each {:after s/reset-system})

(deftest-async test-upsert-empty-list
  (with-redef-async
    [git/get-tags (constantly "") mkdir (constantly nil)
     git/get-commits (constantly (constantly [{:all []}]))]
    (p/do (controller/upsert-git-tags @s/system-atom)
          (controller/upsert-git-commits @s/system-atom)
          (aux.db/assert= "select * from git.tag" [])
          (aux.db/assert= "select * from git.commit" []))))

(deftest-async test-upsert-git-tags
  (with-redef-async
    [git/get-tags (constantly (generate-git-tags "1.0" "1.1" "2.0"))]
    (p/do (controller/upsert-git-tags @s/system-atom)
          (aux.db/assert= "select * from git.tag"
                          [{:tag "v1.0" :hash "hash1.0" :source "example"}
                           {:tag "v1.1" :hash "hash1.1" :source "example"}
                           {:tag "v2.0" :hash "hash2.0" :source "example"}]))))
