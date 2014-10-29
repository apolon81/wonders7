(ns wonders7.routes.home
  (:require [compojure.core :refer :all]
            [wonders7.layout :as layout]
            [wonders7.util :as util]))

(defn home-page []
  (layout/render
    "home.html" {:content (util/md->html "/md/docs.md")}))

(defn about-page []
  (layout/render "about.html"))

(defroutes home-routes
  (GET "/" [] (home-page))
  (GET "/about" [] (about-page)))
