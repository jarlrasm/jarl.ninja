(ns ^:figwheel-always jarl.ninja.navigation
(:require [jarl.ninja.lookup :as lookup]
          [jarl.ninja.routing :as routing]
          [om.core :as om]
          [om.dom :as dom]))


(defn right-page [state]
  (let [path (:path state)
        current (lookup/get-page (:pages (:site state))  (:current state) path)
        ]
        (if (not(empty? (:pages current)))
          (first(:pages current))
          nil
        )

    )
  )

(defn left-page [state];

  (let [path (:path state)
        pages (:pages (:site state))]
      (when-not (empty? path)
        (lookup/get-page pages (last path) (pop path))
      )
    )
  )

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

(defn overlay [state component]
     (dom/div  #js {:className  "overlay" }
        (when-let[page (next-page state >)]
           (dom/div  #js {:className "down-button" }
            (dom/a #js {:href (str "#" (routing/get-route-to-resource (:path state) (:resource page)))} (dom/label nil (:name page)) (dom/div  #js {:className "down-arrow" } ))

            )
          )
        (when-let[page (next-page state <)]
           (dom/div  #js {:className "up-button" }
            (dom/a #js { :href (str "#" (routing/get-route-to-resource (:path state) (:resource page)))} (dom/label nil (:name page)) (dom/div  #js {:className "up-arrow" }))

            )
          )
        (when-let[page (left-page  state)]
           (dom/div  #js {:className "left-button" }
            (dom/a #js {:href (str "#" (routing/get-route-to-path (:path state) ))}  (dom/label nil (:name page)) (dom/div  #js {:className "left-arrow" }))

           )
          )
        (when-let[page (right-page state)]
           (dom/div  #js {:className "right-button" }
            (dom/a #js {:href (str "#" (routing/get-route-to-resource (conj (:path state)(:current state)) (:resource page)))} (dom/label nil (:name page))(dom/div  #js {:className "right-arrow" }))

           )
          )
        (om/build component state {})

      )
  )
