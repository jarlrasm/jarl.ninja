(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require
          [jarl.ninja.main-menu :as main-menu]
          [jarl.ninja.keyboard :as keyboard]
          [jarl.ninja.navigation :as navigation]
          [jarl.ninja.content :as content]
          [jarl.ninja.routing :as routing]
          [om-tools.dom :as d :include-macros true]
          [om.core :as om]
          [om.dom :as dom]
          [secretary.core :as secretary :refer-macros [defroute]]
          [goog.events :as events]
          [goog.history.EventType]
          [goog.events.KeyHandler.EventType]
          [cljs-http.client :as http]
          [clojure.string :as string])
   (:import goog.History))
(defonce history (History.))
(enable-console-print!)
(defonce app-state (atom {:current "" :path [] :document "" :class "current" :site []}));Not happy about the :class


(defn main [state owner]
    (om/component
     (dom/div {}
        (om/build content/animated state {})
        (om/build main-menu/menu  state {})
      ))
)

(om/root main app-state
           {:target (. js/document (getElementById "app"))})





(go
    (let [response (<! (http/get "site/site.json"))]
        (routing/load-site! app-state (:body response) )
        (println "Routes loaded. Dispatching..")

         (goog.events/listen history goog.history.EventType/NAVIGATE #(secretary/dispatch! (.-token %)))
         (goog.events/listen  js/document "keydown" #( keyboard/key-pressed!(.-keyCode (.-event_ %)) (deref app-state)))
         (doto history (.setEnabled true))

      )
 )

