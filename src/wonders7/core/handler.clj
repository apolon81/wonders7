(ns wonders7.core.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [aleph.http :as http]
            [manifold.stream :as stream]
            [clojure.tools.logging :refer [info]]
            [hiccup.page]
            [hiccup.util]
            [wonders7.game.state]))

(defn uuid [] (str (java.util.UUID/randomUUID)))

(def clients (atom {}))

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
        [:p (-> wonders7.game.state/current-state hiccup.util/as-str hiccup.util/escape-html)]
        [:button {:id "join-game"} "join-game"]
        [:button {:id "reset-game"} "reset-game"]]
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js")
      (hiccup.page/include-js "js/app.js")]))

(defn msg-from-client [msg ws]
  (info (str "client: " (get @clients ws) " message: " msg)))

(defn ws-create-handler [req]
  (let [ws @(http/websocket-connection req)]
    (stream/on-closed ws #(swap! clients dissoc ws))
    (stream/consume #(msg-from-client % ws) ws)
    (swap! clients assoc ws (uuid))))

(defroutes app-routes
  (GET "/" [] game-page)
  (GET "/ws" [] ws-create-handler)
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

(defn msg-to-client [[client-stream uuid]]
  (stream/put! client-stream (str "The server side says hello to " uuid " client!")))

(defn msg-broadcast []
  (map #(msg-to-client %) @clients))

;(http/start-server app {:port 8080})
