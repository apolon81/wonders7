(ns wonders7.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [aleph.http :as http]
            [manifold.stream :as stream]
            [clojure.tools.logging :refer [info]]
            [clojure.data.json :refer [json-str read-json write-str]]
            [hiccup.page]
            [hiccup.util]
            [wonders7.game.state]
            [wonders7.core.rest-api :as api]
            [wonders7.core.ws-api :as ws-api]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(defn game-page [req]
  (hiccup.page/html5
    [:head
      [:title "7wonders"]
      (hiccup.page/include-css "//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/themes/smoothness/jquery-ui.css")
      (hiccup.page/include-css "css/7wonders.css")]
    [:body
      [:div {:id "join-form"}
        [:form
          [:label {:for "name"} "Name"]
          [:input {:type "text" :name "name" :id "player-name"}]]]
      [:div
        [:button {:id "join-game"} "join-game"]
        [:button {:id "reset-game"} "reset-game"]
        [:button {:id "start-game"} "start-game"]
        [:button {:id "pick-card"} "pick-card"]
        [:pre {:id "debug-state"}]]
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js")
      (hiccup.page/include-js "js/app.js")]))

(defn ws-create-handler [req]
  (let [ws @(http/websocket-connection req)]
    (stream/on-closed ws #(swap! ws-api/clients dissoc ws))
    (stream/consume #(ws-api/msg-from-client % ws) ws)
    (swap! ws-api/clients assoc ws (uuid))))

(defroutes app-routes
  (GET "/" [] game-page)
  (GET "/ws" [] ws-create-handler)
  (POST "/state" [id] (api/state id))
  (POST "/join" [nick id] (api/join nick id))
  (POST "/reset" [id] (api/reset id))
  (POST "/start" [id] (api/start id))
  (route/not-found "Not Found"))

; turned off anti-forgery for now, will adopt it later on
(def app
  (wrap-defaults app-routes (update-in site-defaults [:security :anti-forgery] (fn [x] false))))

;(http/start-server app {:port 8080})
;(add-watch (get wonders7.game.state/current-state :players) :players-watch (fn [k r old-state new-state] (ws-api/msg-broadcast "ping")))
;(ws-api/msg-broadcast "ping")
