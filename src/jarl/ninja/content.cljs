(ns ^:figwheel-always jarl.ninja.content
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [jarl.ninja.navigation :as navigation]
          [om.core :as om]
          [om.dom :as dom]
          [cljs.core.async :as async :refer [chan <!]]
          [markdown.core :refer [md->html]]
          [cljs-http.client :as http]
          [clojure.string :as string]))

(defn  html [document owner]
  (reify
    om/IRender
    (render [this]
      (om.dom/div #js {:dangerouslySetInnerHTML #js {:__html document}} nil)))
  )

(defn animated  [state owner]

  (reify
    om/IRender
    (render [this]
    (om.dom/div #js {:className (:class state)}
    (om/build html (:document state) {})
    )
    )
  ))

(defn load-page! [app-state page path];;Jesus this is ugly
  (let [state (deref app-state)]
    (let [allpages (:pages (:site state))]
      (let [direction (navigation/get-direction-to state page path)]
      (println  (str "Load page " (string/join "/" path ) "/" page))
        (case direction
          :right  (swap! app-state assoc :class "out-left")
          :left (swap! app-state assoc :class "out-right")
          :up  (swap! app-state assoc :class "out-down")
          :down (swap! app-state assoc :class "out-up"))
      (swap! app-state assoc :current page)
      (swap! app-state assoc :path path)
      (js/setTimeout (fn []
        (swap! app-state assoc :document "")
        (case direction
          :right  (swap! app-state assoc :class "new-right")
          :left (swap! app-state assoc :class "new-left")
          :up  (swap! app-state assoc :class "new-up")
          :down (swap! app-state assoc :class "new-down"))

        (js/setTimeout (fn []
        (go
              (println (str "Loading site/markdown/" (:markdown(first (filter #(= page (:resource %)) allpages)))))
              (let [response (<! (http/get (str "site/markdown/" (:markdown(navigation/get-page allpages page path)))))]
                (println  "Loaded")
                (swap! app-state assoc :document (md->html(:body response)))
                (swap! app-state assoc :class "current")
              )
         ))
        200))
      1000)
      )

    )
  )
  )
