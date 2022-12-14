(ns intuition.core
  (:require
   [nextjournal.clerk :as clerk]
   [intuition.data-source.db :as db]))

;; Start database and notebook
(do (clerk/serve! {:browse? true})
    (db/start!))

;; Start clerk
(clerk/serve! {:browse? true})

;; Halt clerk
(clerk/halt!)

;; Init database
(db/start!)

;; Destroy database
(db/destroy!)
