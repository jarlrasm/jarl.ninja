(ns ^:figwheel-always jarl.ninja.keyboard
(:require [jarl.ninja.lookup :as lookup]
          [jarl.ninja.routing :as routing]
          [clojure.string :as string]))


(defn right! [state]
  (let [path (:path state)
        current (lookup/get-page (:pages (:site state))  (:current state) path)
        ]
        (when-not (empty? (:pages current))
          (let [next (first(:pages current))]
          (routing/goto-resource!  (conj (:path state) (:current state))  (:resource next))
          )
        )

    )
 )
(defn left![state]
  (let [path (:path state) ]
      (when-not (empty? path)
        (routing/goto-path!  path)
      )

    )
 )

(defn next-page [state direction]

    (let [allpages (lookup/pages-at-path (:pages (:site state)) (:path state))
          current (lookup/get-page (:pages (:site state))  (:current state) (:path state))]
        (last(sort #(direction (:index %1) (:index %2))(filter #(direction (:index %)(:index current))allpages)))
        )
  )
(defn next! [state direction]

        (let [page (next-page state direction)]
          (when page
            (routing/goto-resource! (:path state)(:resource page) )
            )
          )
  )
(defn down! [state] (next! state >) )

(defn up! [state] (next! state <))

(defn key-pressed! [key state]
  (case key
    37 (left! state)
    39 (right! state)
    38 (up! state)
    40 (down! state)
    nil
  )
)
