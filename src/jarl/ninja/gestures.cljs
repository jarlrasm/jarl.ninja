(ns ^:figwheel-always jarl.ninja.gestures
(:require [cljsjs.hammer]
          [jarl.ninja.navigation :as navigation]
 )
)


(defn swipe [event state]
  (case (.-direction event)
   2 (navigation/right! state)
   4 (navigation/left! state)
   8 (navigation/down! state)
   16 (navigation/up! state)
  (println "Wut?"))
)
(defn pan [event state]
  ;(println "pan")
)
(defn set-up [element app-state]
  (let [mc (js/Hammer. element)]
    (. mc (on "swipe" #(swipe % (deref app-state))))
    (. mc (on "pan" #(pan % (deref app-state))))
    (. (. mc (get "swipe")) set #js{ :direction Hammer.DIRECTION_ALL })
    (. (. mc (get "pan")) set #js{ :direction Hammer.DIRECTION_ALL })
    )
  )
