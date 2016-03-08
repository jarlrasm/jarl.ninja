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
          [goog.events :as events]
          [goog.string ]
          [goog.history.EventType :as EventType]
          [markdown.core :refer [md->html]]
          [cljs-http.client :as http]
          [cljs.core.async :as async :refer [<!]])
   (:import goog.History))
(def format goog.string.format)
(defonce history (History.))
(enable-console-print!)
(defonce app-state (atom {:current "" :document "" :class "current" :site []}))

(defn nav-item [page]
  (n/nav-item {:key (:resource page) :href (format "#%s" (:resource page))} (:name page)))

(defn main-menu [active state]
  (n/navbar
 {:brand (d/a {:href "#"}
              (:name (first (filter #( = "" (:resource %)) (:pages (:site state))))))}
 (apply n/nav
  {:collapsible? true
   :active-key active}
    (map nav-item (filter #(not (= "" (:resource %))) (:pages (:site state))))
    )
  )
)

(defn  content [document owner]
  (reify
    om/IRender
    (render [this]
      (p/panel {}
      (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html document}} nil))))
  )

(defn content-wrapper  [state owner]

  (reify
    om/IRender
    (render [this]
    (om.dom/div #js {:className (:class state)}
    (om/build content (:document state) {})
    )
    )
  ))
(defn main [state owner]
    (om/component
     (dom/div {}
        (om/build content-wrapper state {})
        (main-menu  (:current state) state)
      ))
)

(om/root main app-state
           {:target (. js/document (getElementById "app"))})


(def load "<div>loading..<div>")

(defn load-page [ page]
  (println  (str "Load page "  page))
  (swap! app-state assoc :class "out")
  (swap! app-state assoc :current page)
  (js/setTimeout (fn []
  (swap! app-state assoc :class "next")
  ;(swap! app-state assoc :document load)
  (go
    (let [allpages (:pages (:site (deref app-state)))]
        (println (str "Loading site/markdown/" (:markdown(first (filter #(= page (:resource %)) allpages)))))
        (let [response (<! (http/get (str "site/markdown/" (:markdown(first (filter #(= page (:resource %)) allpages))))))]
          (println  "Loaded")
          (swap! app-state assoc :document (md->html(:body response)))
          (swap! app-state assoc :class "current")
        )
      )
   ))
  1000)
  )



(defn addpage [page]

  (println  (str "Creating route /" (:resource page)))
  (defroute (str "/" (:resource page))[]
    (load-page (:resource page)))

  )
(defn load-routes [pages]
  (doseq [page (:pages pages)]
    (addpage page)
  )
  )
(go
    (let [response (<! (http/get "site/site.json"))]
        (swap! app-state assoc :site (:body response))
        (load-routes (:body response))
        (println "Routes loaded. Dispatching..")

         (goog.events/listen history EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
         (doto history (.setEnabled true))

      )
 )

