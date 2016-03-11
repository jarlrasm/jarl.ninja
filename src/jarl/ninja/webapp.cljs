(ns ^:figwheel-always jarl.ninja.webapp
(:require
          [jarl.ninja.main-menu :as main-menu]
          [jarl.ninja.content :as content]
          [om.core :as om]
          [om.dom :as dom])
  )

(defn main [state owner]
    (om/component
     (dom/div {}
        (om/build content/animated state {})
        (om/build main-menu/menu  state {})
      ))
)