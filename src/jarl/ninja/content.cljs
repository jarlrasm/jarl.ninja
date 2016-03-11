(ns ^:figwheel-always jarl.ninja.content
(:require-macros [cljs.core.async.macros :refer [go]])
(:require [jarl.ninja.lookup :as lookup]
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

(defn remove-current-page! [app-state direction]
          (case direction
          :right  (swap! app-state assoc :class "out-left")
          :left (swap! app-state assoc :class "out-right")
          :up  (swap! app-state assoc :class "out-down")
          :down (swap! app-state assoc :class "out-up"))
  )
(defn prepare-new-page! [app-state direction]

        (swap! app-state assoc :document "");Neccesary to clean up?
        (case direction
          :right  (swap! app-state assoc :class "new-right")
          :left (swap! app-state assoc :class "new-left")
          :up  (swap! app-state assoc :class "new-up")
          :down (swap! app-state assoc :class "new-down"))
  )
(defn show-new-page![app-state] (swap! app-state assoc :class "current"))


(defn load-page! [app-state page path];;Jesus this is ugly
  (let [state (deref app-state)
        allpages (:pages (:site state))
        direction (lookup/get-direction-to state page path)]

      (remove-current-page! app-state direction)
      (swap! app-state assoc :current page)
      (swap! app-state assoc :path path)
      (js/setTimeout (fn []
        (prepare-new-page! app-state direction)

        (js/setTimeout (fn []; This should be more clever. Problem is if the markdown is loaded to fast,it will enter from the wrong side
        (go
              (println (str "Loading site/markdown/" (:markdown(first (filter #(= page (:resource %)) allpages)))))
              (let [response (<! (http/get (str "site/markdown/" (:markdown(lookup/get-page allpages page path)))))]
                (println  "Loaded")
                (swap! app-state assoc :document (md->html(:body response)))
                (show-new-page! app-state)
              )
         ))
        200))
      1000)
      )

    )


