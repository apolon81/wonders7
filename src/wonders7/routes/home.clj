(ns wonders7.routes.home
  (:require [compojure.core :refer :all]
            [wonders7.layout :as layout]
            [wonders7.util :as util]
            [hiccup.page]
            [hiccup.util]
            [wonders7.game-state]))

(defn game-page []
  (hiccup.page/html5
   [:head
     [:title "7wonders"]
     (hiccup.page/include-css "css/7wonders.css")]
   [:body
     [:div
       [:p (-> wonders7.game-state/current-state hiccup.util/as-str hiccup.util/escape-html)]
       [:p {:id "join-game"} "join-game"]
       [:p {:id "reset-game"} "reset-game"]]
     (hiccup.page/include-js "js/cljs.js")]))

(defroutes home-routes
  (GET "/" [] (game-page))
  (GET "/join" [] (wonders7.game-state/join-game "apo"))
  (GET "/reset" [] (do (wonders7.game-state/reset-game) "uhu")))
