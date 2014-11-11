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
        [:button {:id "join-game"} "join-game"]
        [:button {:id "reset-game"} "reset-game"]
        [:button {:id "pick-card"} "pick-card"]
        [:pre {:id "debug-state"} (-> (wonders7.game.state/state-view) hiccup.util/as-str hiccup.util/escape-html)]]
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jquery/1.11.1/jquery.min.js")
      (hiccup.page/include-js "//ajax.googleapis.com/ajax/libs/jqueryui/1.11.2/jquery-ui.min.js")
      (hiccup.page/include-js "js/app.js")]))

(defn msg-to-client [[client-stream uuid] msg]
  (stream/put! client-stream msg))

(defn msg-broadcast [msg]
  (map #(msg-to-client % msg) @clients))

(defn msg-from-client [msg ws]
  (let [data (read-json msg)]
    (info "mesg received" data)
    (when (= (:command data) "join")
      (info "processing join command")
      (wonders7.game.state/join-game :player-name (:name data) :player-id (get @clients ws))
      (msg-broadcast (json-str (wonders7.game.state/state-view))))))

(defn ws-create-handler [req]
  (let [ws @(http/websocket-connection req)]
    (stream/on-closed ws #(swap! clients dissoc ws))
    (stream/consume #(msg-from-client % ws) ws)
    (swap! clients assoc ws (uuid))))

(defroutes app-routes
  (GET "/" [] game-page)
  (GET "/ws" [] ws-create-handler)
  (GET "/join/:pname" [pname] (-> (wonders7.game.state/join-game :player-name pname :player-id "dummy")
                                  write-str))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))

;(http/start-server app {:port 8080})

(clojure.pprint/pprint (wonders7.game.state/state-view))
