(ns ^:figwheel-always jarl.ninja.core
(:require-macros [cljs.core.async.macros :refer [go]])
(:require
          [jarl.ninja.keyboard :as keyboard]
          [jarl.ninja.routing :as routing]
          [jarl.ninja.webapp :as webapp]
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
           {:target (. js/document (getElementById "app"))})



(go
    (let [response (<! (http/get "site/site.json"))]
        (routing/load-site! app-state (:body response) )
        (println "Routes loaded. Dispatching..")

         (goog.events/listen history goog.history.EventType/NAVIGATE #(routing/goto! (.-token %)))
         (goog.events/listen  js/document "keydown" #( keyboard/key-pressed!(.-keyCode (.-event_ %)) (deref app-state)))
         (doto history (.setEnabled true))

      )
 )

