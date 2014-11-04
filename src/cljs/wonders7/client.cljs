(ns wonders7client
  (:require [ajax.core :refer [GET POST]]))

(defn handle-join-game []
  (do
    (js/alert "Hello!")
    (GET "/join")))

(defn handle-reset-game []
  (do
    (js/alert "Hello!")
    (GET "/reset")))

(def join-game (.getElementById js/document "join-game"))
(def reset-game (.getElementById js/document "reset-game"))

(.addEventListener join-game "click" handle-join-game)
(.addEventListener reset-game "click" handle-reset-game)
