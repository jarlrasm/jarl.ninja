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
          (routing/goto!  (str (string/join "/" (:path state)) "/" (:current state) "/" (:resource next) ) )
          )
        )

    )
 )
(defn left![state]
  (let [path (:path state)        ]
      (when-not (empty? path)
        (routing/goto!  (string/join "/" path)   )
      )

    )
 )
(defn up! [state]
    (let [allpages (lookup/pages-at-path (:pages (:site state)) (:path state))
          current (lookup/get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(> (:index %1) (:index %2))(filter #(< (:index %)(:index current))allpages)))]
          (when next
            (routing/goto! (str (string/join "/" (:path state)) "/" (:resource next) ) )
            )
          )
        )
      )

(defn down! [state]
    (let [allpages (lookup/pages-at-path (:pages (:site state)) (:path state))
          current (lookup/get-page (:pages (:site state))  (:current state) (:path state))]
        (let [next (first(sort #(< (:index %1) (:index %2))(filter #(> (:index %)(:index current))allpages)))]
          (when next
            (routing/goto!  (str (string/join "/" (:path state)) "/" (:resource next) ) )
            )
          )
        )
      )


(defn key-pressed! [key state]
  (case key
    37 (left! state)
    39 (right! state)
    38 (up! state)
    40 (down! state)
    nil
  )
)
