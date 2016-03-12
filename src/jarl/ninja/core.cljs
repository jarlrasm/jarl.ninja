(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require
          [jarl.ninja.navigation :as navigation]
          [jarl.ninja.routing :as routing]
          [jarl.ninja.webapp :as webapp]
          [jarl.ninja.gestures :as gestures]
          [om.core :as om]
          [goog.events :as events]
          [goog.history.EventType]
          [goog.events.KeyHandler.EventType]
          [cljs-http.client :as http])
   (:import goog.History))
(defonce history (History.))
(enable-console-print!)

(defonce app-state (atom {:current "" :path [] :document "" :class "current" :site []}));Not happy about the :class


(om/root webapp/main app-state
           {:target (. js/document (getElementById "app"))}
         )




(go
    (let [response (<! (http/get "site/site.json"))]
        (routing/load-site! app-state (:body response) )
        (println "Routes loaded. Dispatching..")
         (goog.events/listen history goog.history.EventType/NAVIGATE #(routing/goto-route! (.-token %)))
         (goog.events/listen  js/document "keydown" #( navigation/key-pressed!(.-keyCode (.-event_ %)) (deref app-state)))
         (gestures/set-up  (.-body  js/document)  app-state)
         (doto history (.setEnabled true))
      )
 )

