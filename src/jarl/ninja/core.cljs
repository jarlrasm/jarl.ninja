(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [om-bootstrap.button :as b]
          [om-bootstrap.nav :as n]
          [om-bootstrap.panel :as p]
          [om-tools.dom :as d :include-macros true]
          [om.core :as om]
          [om.dom :as dom]
          [secretary.core :as secretary :refer-macros [defroute]]
          [goog.events :as events]
          [goog.history.EventType :as EventType]
          [markdown.core :refer [md->html]]
          [cljs-http.client :as http]
          [cljs.core.async :as async :refer [<!]])
   (:import goog.History))

(defonce app-state (atom {:current "root" :about "" :links ""}))

(go
    (let [response (<! (http/get "markdown/about.md"))]
        (swap! app-state assoc :about (md->html(:body response)))
      )
 )
(go
    (let [response (<! (http/get "markdown/links.md"))]
        (swap! app-state assoc :links (md->html(:body response)))
      )
 )
(defn main-menu [active] (n/navbar
 {:brand (d/a {:href "#/"}
              "Jarl")}
 (n/nav
  {:collapsible? true
   :active-key active}
  (n/nav-item {:key "about" :href "#/about"} "About me")
  (n/nav-item {:key "links" :href "#/links"} "Links"))
  )
)

(defn links[state] (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html (:links state)}} nil))
(defn root[state] (d/img {:src "images/me.jpg" :class "img-responsive .img-rounded"}))
(defn about[state] (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html (:about state)}} nil))

(defn main [state owner]
  (reify
    om/IRender
    (render [this]
     (dom/div {}
      (main-menu  (:current state))
      (p/panel {}
      (case (:current state)
        "root" (root state)
        "about" (about state)
        "links" (links state)))))))


(defn load-om [component page]
  (swap! app-state assoc :current page)
  (om/root component app-state
           {:target (. js/document (getElementById "app"))}))


(defroute "/" []
  (load-om main "root"))

(defroute "/about" []
  (load-om main "about"))

(defroute "/links" []
  (load-om main  "links"))

(secretary/dispatch! "/")

(let [h (History.)]
  (goog.events/listen h EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
  (doto h
    (.setEnabled true)))

